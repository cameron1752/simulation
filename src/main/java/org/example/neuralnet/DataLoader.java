package org.example.neuralnet;

import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateUnit;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.*;

public class DataLoader {
    private static final double epsilon = 0.000001d;
    private static final int IN = 48, OUT = 24;
    static List<TrainingExample> testData = new ArrayList<>();
    static List<TrainingExample> trainingData = new ArrayList<>();
    static final int SPATIAL_STRIDE = 4;   // keep every 4th lat and lon
    static final int TIME_STRIDE = 3;      // make a window every 3rd hour
    static final int NX = 81;              // number of longitudes in the grid
    static int locationIndex = 0;
    static long rowcount = 0;
    static int threshold = 800000;
    static long lastSeen = 0L;
    static int count = 0;
    static DBManager dbManager;
    private static List<Row> pending = new ArrayList<>();
    private static final int INPUT_SIZE = 3 * IN + 6;
    private static final String URL = "jdbc:postgresql://db:5432/cam";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";
    private record Series(float[] temp, float[] precip, float[] pressure,
                          float[] hourSin, float[] hourCos, float[] daySin, float[] dayCos,
                          float lat, float lon) {}

    public static List<TrainingExample> load(String imagePath, String labelPath) throws IOException {
        List<TrainingExample> examples = new ArrayList<>();

        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        byte[] labelBytes = Files.readAllBytes(Paths.get(labelPath));

        ByteBuffer imageBuffer = ByteBuffer.wrap(imageBytes);
        ByteBuffer labelBuffer = ByteBuffer.wrap(labelBytes);

        // read image file header
        int imageMagic = imageBuffer.getInt();   // should be 2051
        int numImages  = imageBuffer.getInt();
        int numRows    = imageBuffer.getInt();   // 28
        int numCols    = imageBuffer.getInt();   // 28

        // read label file header
        int labelMagic = labelBuffer.getInt();   // should be 2049
        int numLabels  = labelBuffer.getInt();

        System.out.println("Loading " + numImages + " images (" + numRows + "x" + numCols + ")");

        int pixelsPerImage = numRows * numCols; // 784

        for (int i = 0; i < numImages; i++){
            // read pixels and normalize to [0.0, 1.0]
            float[] input = new float[pixelsPerImage];
            for (int p = 0; p < pixelsPerImage; p++){
                input[p] = (imageBuffer.get() & 0xFF) / 255.0f;
            }

            // EMNIST-specific: transpose the 28x28 image
            float[] transposed = new float[pixelsPerImage];
            for (int r = 0; r < numRows; r++){
                for (int c = 0; c < numCols; c++){
                    transposed[c * numRows + r] = input[r * numCols + c];
                }
            }

            // read label and one-hot encode
            int digit = labelBuffer.get() & 0xFF;
            float[] label = new float[10];
            label[digit] = 1.0f;

            examples.add(new TrainingExample(transposed, label));
        }

        return examples;
    }

    static Object[] readFromDB() throws SQLException {
        trainingData = new ArrayList<>();
        testData = new ArrayList<>();
        Object[] result = new Object[2];
        List<Row> freshRows = DBManager.findAfter(lastSeen, threshold);

        if (freshRows.isEmpty()) {               // end of table: flush the last location
            if (!pending.isEmpty()) {
                rows(pending);
                pending = new ArrayList<>();
            }
            count++;
            result[0] = trainingData;
            result[1] = testData;

            return result;
        }

        for (Row row : freshRows) {
            if (!pending.isEmpty() && !sameSpot(row, pending.get(pending.size() - 1))) {
                rows(pending);
                pending = new ArrayList<>();
            }
            pending.add(row);                    // fixes the dropped-first-row bug
        }

        rowcount += freshRows.size();
        lastSeen = freshRows.get(freshRows.size() - 1).getId();

        System.out.printf("page #%d: read %d rows, %d total, train=%d, test=%d, lastSeen=%d%n",
                count, freshRows.size(), rowcount, trainingData.size(), testData.size(), lastSeen);


        count++;

        result[0] = trainingData;
        result[1] = testData;

        return result;
    }

    private static boolean sameSpot(Row a, Row b) {
        return isEqual(a.getLat(), b.getLat()) && isEqual(a.getLon(), b.getLon());
    }

    static void reset() {
        lastSeen = 0L;
        locationIndex = 0;
        rowcount = 0;
        count = 1;
        pending = new ArrayList<>();
        trainingData.clear();
        testData.clear();
    }

    private static void rows(List<Row> rows) {
        int y = locationIndex / NX;
        int x = locationIndex % NX;
        locationIndex++;
        // if (y % SPATIAL_STRIDE != 0 || x % SPATIAL_STRIDE != 0) return;

        int n = rows.size();
        if (n < IN + OUT) return;                // not enough data for a single window

        Series s = toSeries(rows);
        int trainEnd = (int) (n * 0.85);

        for (int start = IN; start + OUT <= trainEnd; start++) {
            trainingData.add(makeWindow(s, start));
        }
        for (int start = Math.max(IN, trainEnd); start + OUT <= n; start++) {
            testData.add(makeWindow(s, start));
        }
    }

    private static Series toSeries(List<Row> rows) {
        int n = rows.size();
        float[] temp = new float[n], precip = new float[n], pressure = new float[n];
        float[] hs = new float[n], hc = new float[n], ds = new float[n], dc = new float[n];

        for (int i = 0; i < n; i++) {
            Row r = rows.get(i);
            temp[i] = r.getTempF();
            float p = r.getPrecip();
            precip[i] = Float.isNaN(p) ? 0f : p;
            pressure[i] = r.getPressure();

            double hourAngle = 2 * Math.PI * getHour(r.getTime()) / 24.0;
            double dayAngle  = 2 * Math.PI * getDay(r.getTime()) / 365.25;
            hs[i] = (float) Math.sin(hourAngle);
            hc[i] = (float) Math.cos(hourAngle);
            ds[i] = (float) Math.sin(dayAngle);
            dc[i] = (float) Math.cos(dayAngle);
        }

        Row first = rows.get(0);
        return new Series(temp, precip, pressure, hs, hc, ds, dc,
                (float) first.getLat(), (float) first.getLon());
    }

    private static TrainingExample makeWindow(Series s, int start) {
        float[] input = new float[INPUT_SIZE];
        int from = start - IN;

        System.arraycopy(s.temp(),     from, input, 0,      IN);
        System.arraycopy(s.precip(),   from, input, IN,     IN);
        System.arraycopy(s.pressure(), from, input, 2 * IN, IN);

        int k = 3 * IN;
        input[k++] = s.lat();
        input[k++] = s.lon();
        input[k++] = s.hourSin()[start];
        input[k++] = s.hourCos()[start];
        input[k++] = s.daySin()[start];
        input[k]   = s.dayCos()[start];

        float[] output = Arrays.copyOfRange(s.temp(), start, start + OUT);
        return new TrainingExample(input, output);
    }

    private static int getHour(String time){
        ZonedDateTime dateTime = ZonedDateTime.parse(time);
        return dateTime.get(ChronoField.HOUR_OF_DAY);
    }

    private static int getDay(String time){
        ZonedDateTime dateTime = ZonedDateTime.parse(time);
        return dateTime.getDayOfYear();
    }

    private static boolean isEqual(double a, double b){
        return Math.abs(a - b) < epsilon;
    }

    private static final String COPY_SQL =
            "COPY curvy_dolphin.weather_staging (latitude, longitude, observed_at, temperature_f, pressure_hpa, precipitation_mm) "
                    + "FROM STDIN WITH (FORMAT csv)";

    public static void loadGrib(String path) throws IOException, SQLException {
        try (NetcdfFile nc = NetcdfFiles.open(path);
             Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            Variable skt = nc.findVariable("Skin_temperature_surface");
            Variable sp  = nc.findVariable("Surface_pressure_surface");
            Variable tp  = nc.findVariable("Total_precipitation_surface_1_Hour_Accumulation");
            Variable timeVar  = nc.findVariable("time");
            Variable time1Var = nc.findVariable("time1");

            double[] lats = (double[]) nc.findVariable("lat").read().get1DJavaArray(DataType.DOUBLE);
            double[] lons = (double[]) nc.findVariable("lon").read().get1DJavaArray(DataType.DOUBLE);
            int ny = lats.length, nx = lons.length;
            int nt = skt.getShape()[0];
            int plane = ny * nx;

            // coordinate strings, built once instead of once per row
            String[] latStr = new String[ny], lonStr = new String[nx];
            for (int y = 0; y < ny; y++) latStr[y] = Double.toString(lats[y]);
            for (int x = 0; x < nx; x++) lonStr[x] = Double.toString(lons[x]);

            // timestamps, built once per time step
            Array times = timeVar.read();
            CalendarDateUnit unit = CalendarDateUnit.of(null, timeVar.getUnitsString());
            String[] timeStr = new String[nt];
            long[] timeMillis = new long[nt];
            for (int t = 0; t < nt; t++) {
                CalendarDate when = unit.makeCalendarDate(times.getDouble(t));
                timeStr[t] = when.toString();          // ISO 8601, which COPY accepts directly
                timeMillis[t] = when.getMillis();
            }

            // matching precipitation index for each time step (-1 if none)
            Array times1 = time1Var.read();
            CalendarDateUnit unit1 = CalendarDateUnit.of(null, time1Var.getUnitsString());
            Map<Long, Integer> precipIndex = new HashMap<>();
            for (int i = 0; i < times1.getSize(); i++) {
                precipIndex.put(unit1.makeCalendarDate(times1.getDouble(i)).getMillis(), i);
            }
            int[] precipT = new int[nt];
            for (int t = 0; t < nt; t++) precipT[t] = precipIndex.getOrDefault(timeMillis[t], -1);

            // each variable as one flat array, laid out [time][lat][lon]
            float[] temp = (float[]) skt.read().get1DJavaArray(DataType.FLOAT);
            float[] pres = (float[]) sp.read().get1DJavaArray(DataType.FLOAT);
            float[] prec = (float[]) tp.read().get1DJavaArray(DataType.FLOAT);

            long written = 0;
            PGConnection pg = conn.unwrap(PGConnection.class);
            try (Writer w = new BufferedWriter(new OutputStreamWriter(
                    new PGCopyOutputStream(pg, COPY_SQL, 1 << 20), StandardCharsets.UTF_8), 1 << 20)) {

                for (int y = 0; y < ny; y++) {
                    for (int x = 0; x < nx; x++) {
                        int cell = y * nx + x;
                        for (int t = 0; t < nt; t++) {
                            int i = t * plane + cell;

                            w.write(latStr[y]);  w.write(',');
                            w.write(lonStr[x]);  w.write(',');
                            w.write(timeStr[t]); w.write(',');
                            writeNum(w, ktoF(temp[i]));      w.write(',');
                            writeNum(w, pres[i] / 100f);     w.write(',');
                            if (precipT[t] >= 0) {
                                writeNum(w, Math.max(0f, prec[precipT[t] * plane + cell] * 1000f));
                            }                                 // empty field = NULL when there's no match
                            w.write('\n');
                        }
                    }
                    written += (long) nx * nt;
                    System.out.printf("lat row %d/%d, %,d rows streamed%n", y + 1, ny, written);
                }
            }   // closing the stream finishes the COPY; with auto-commit on, it commits here

            System.out.printf("Copied %,d rows from %s into staging%n", written, path);
        }
    }

    // writes a float, or an empty field (NULL) if the source value is missing
    private static void writeNum(Writer w, float v) throws IOException {
        if (!Float.isNaN(v) && !Float.isInfinite(v)) w.write(Float.toString(v));
    }

    static int nearest(Array coords, double target) {
        int best = 0;
        for (int i = 1; i < coords.getSize(); i++)
            if (Math.abs(coords.getDouble(i) - target) < Math.abs(coords.getDouble(best) - target)) best = i;
        return best;
    }

    private static float ktoF(float kelvin){
        return (kelvin - 273.15f) * (9f/5) + 32;
    }
}