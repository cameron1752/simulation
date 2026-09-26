package org.example.neuralnet;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    // Define connection parameters
    private static final String URL = "jdbc:postgresql://db:5432/cam";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";
    private static final String INSERT_RECORD_SQL = "INSERT INTO curvy_dolphin.weather (latitude, longitude, observed_at, temperature_f, pressure_hpa, precipitation_mm)\n" +
            "VALUES (?, ?, ?, ?, ?, ?);";

    private static final String SELECT_COLUMNS = "SELECT id, latitude, longitude, observed_at, temperature_f, pressure_hpa, precipitation_mm\n" +
            "FROM curvy_dolphin.weather ";
    private static final String SCHEMA  = "curvy_dolphin";
    private static final String WEATHER = SCHEMA + ".weather";
    private static final String STAGING = SCHEMA + ".weather_staging";
    private static final String COLUMNS =
            "latitude, longitude, observed_at, temperature_f, pressure_hpa, precipitation_mm";

    private static final String INSERT_WEIGHTS = "INSERT INTO curvy_dolphin.model_weights (instance, round, weights) VALUES (?, ?, ?) "
            + "ON CONFLICT (instance, round) DO UPDATE SET weights = EXCLUDED.weights, updated_at = now()";
    private static final String RESET_WEIGHTS = "DELETE FROM curvy_dolphin.model_weights;";
    private static final String RESET_SCALER = "DELETE FROM curvy_dolphin.model_scaler;";

    // append: skip rows that already exist (needs the unique constraint)
    private static final String INSERT_FROM_STAGING_SQL =
            "INSERT INTO " + WEATHER + " (" + COLUMNS + ") "
                    + "SELECT " + COLUMNS + " FROM " + STAGING + " "
                    + "ORDER BY latitude, longitude, observed_at "
                    + "ON CONFLICT (latitude, longitude, observed_at) DO NOTHING";

    // full reload: no constraint needed during the load; DISTINCT ON drops overlap between files
    private static final String REPLACE_FROM_STAGING_SQL =
            "INSERT INTO " + WEATHER + " (" + COLUMNS + ") "
                    + "SELECT DISTINCT ON (latitude, longitude, observed_at) " + COLUMNS + " "
                    + "FROM " + STAGING + " "
                    + "ORDER BY latitude, longitude, observed_at";
    static List<Row> toBeInserted = new ArrayList<>();

    public DBManager() {
        System.out.println("Connecting to PostgreSQL database...");

        // Try-with-resources statement automatically closes connections
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {

            if (connection != null) {
                System.out.println("Connected to the server successfully!");

                // Example query execution
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery("SELECT version();")) {

                    if (resultSet.next()) {
                        System.out.println("PostgreSQL Version: " + resultSet.getString(1));
                    }
                }
            } else {
                System.out.println("Failed to make connection.");
            }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public static long cutoverFromStaging(boolean replaceExisting) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            long staged;
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + STAGING)) {
                rs.next();
                staged = rs.getLong(1);
            }
            if (staged == 0) {
                System.out.println("Staging table is empty, nothing to cut over.");
                return 0;
            }
            System.out.printf("Cutting over %,d staged rows (replaceExisting=%b)%n", staged, replaceExisting);

            conn.setAutoCommit(false);
            try {
                // settings for this transaction only: no timeout, more memory for the big sort
                stmt.execute("SET LOCAL statement_timeout = 0");
                stmt.execute("SET LOCAL work_mem = '512MB'");

                long start = System.currentTimeMillis();
                long inserted;

                if (replaceExisting) {
                    // load without the unique index, then build it once at the end (much faster)
                    stmt.execute("ALTER TABLE " + WEATHER + " DROP CONSTRAINT IF EXISTS uq_weather_location_time");
                    stmt.execute("TRUNCATE " + WEATHER + " RESTART IDENTITY");

                    inserted = stmt.executeLargeUpdate(REPLACE_FROM_STAGING_SQL);

                    stmt.execute("ALTER TABLE " + WEATHER + " ADD CONSTRAINT uq_weather_location_time "
                            + "UNIQUE (latitude, longitude, observed_at)");
                } else {
                    inserted = stmt.executeLargeUpdate(INSERT_FROM_STAGING_SQL);
                }

                stmt.execute("TRUNCATE " + STAGING);
                conn.commit();

                System.out.printf("Inserted %,d rows (%,d skipped as duplicates) in %,d s%n",
                        inserted, staged - inserted, (System.currentTimeMillis() - start) / 1000);

                // refresh planner statistics after a large load (outside the transaction)
                conn.setAutoCommit(true);
                stmt.execute("ANALYZE " + WEATHER);

                return inserted;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Cutover failed, rolled back. weather and weather_staging are unchanged.");
                throw e;
            }
        }
    }

    public static void processRecords(){
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_RECORD_SQL)) {

            // 1. Turn off auto-commit to handle this as a single transaction
            connection.setAutoCommit(false);

            int batchSize = 10000; // Optimal batch window for memory management
            int count = 0;

            for (Row row : toBeInserted) {
                OffsetDateTime observedAt = OffsetDateTime.parse(row.getTime());
                preparedStatement.setDouble(1, row.getLat());
                preparedStatement.setDouble(2, (row.getLon()));
                preparedStatement.setObject(3, observedAt);
                preparedStatement.setFloat(4, (row.getTempF()));
                preparedStatement.setFloat(5, (row.getPressure()));
                preparedStatement.setFloat(6, (row.getPrecip()));

                // 2. Add the parameters to the batch queue
                preparedStatement.addBatch();
                count++;

                // 3. Execute the batch when it reaches the batchSize limit
                if (count % batchSize == 0) {
                    preparedStatement.executeBatch();
                    System.out.println("Executed batch up to index: " + count);
                }
            }

            // 4. Execute any remaining records left in the queue
            if (count % batchSize != 0) {
                preparedStatement.executeBatch();
            }

            // 5. Commit the entire transaction
            connection.commit();
            System.out.println("All " + count + " records committed successfully!");

        } catch (SQLException e) {
            System.err.println("Batch insert failed. Rolling back changes.");
            e.printStackTrace();
        }

    }

    public static List<Row> findAfter(long lastSeenId, int pageSize) throws SQLException {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }

        String sql = SELECT_COLUMNS + "WHERE id > ? ORDER BY id LIMIT ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, lastSeenId);
            ps.setInt(2, pageSize);
            return readRows(ps);
        }
    }

    private static List<Row> readRows(PreparedStatement ps) throws SQLException {
        List<Row> rows = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Row(
                        rs.getLong("id"),
                        String.valueOf(rs.getObject("observed_at", OffsetDateTime.class)),
                        rs.getFloat("temperature_f"),
                        rs.getFloat("pressure_hpa"),
                        rs.getFloat("precipitation_mm"),
                        rs.getFloat("latitude"),
                        rs.getFloat("longitude")
                ));
            }
        }
        return rows;
    }

    // method to write a networks weights into the database table for distribution
    public static void writeWeights(String instance, int round, byte[] weights){
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement ps = conn.prepareStatement(INSERT_WEIGHTS)) {
            ps.setString(1, instance);
            ps.setInt(2, round);
            ps.setBytes(3, weights);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method to reset the weights between launches
    public static void resetWeights(){
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(RESET_WEIGHTS)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // method to reset the scaler values for each round / reset
    public static void resetScaler(){
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(RESET_SCALER)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void average(int round) {
        // take all weights from table
        // average and re-insert
        String select = "SELECT weights FROM curvy_dolphin.model_weights "
                + "WHERE round = ?;";
        String upsert = "INSERT INTO curvy_dolphin.model_weights (instance, round, weights) VALUES ('coordinator', ?, ?) "
                + "ON CONFLICT (instance, round) DO UPDATE SET weights = EXCLUDED.weights, updated_at = now()";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            double[] sum = null;
            int workers = 0;

            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, round);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        float[] w = bytesToFloats(rs.getBytes(1));
                        if (sum == null) sum = new double[w.length];
                        for (int i = 0; i < w.length; i++) sum[i] += w[i];
                        workers++;
                    }
                }
            }

            float[] avg = new float[sum.length];
            for (int i = 0; i < sum.length; i++) avg[i] = (float) (sum[i] / workers);

            try (PreparedStatement ps = conn.prepareStatement(upsert)) {
                ps.setInt(1, round);
                ps.setBytes(2, floatsToBytes(avg));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static float[] bytesToFloats(byte[] data) {
        float[] out = new float[data.length / Float.BYTES];
        ByteBuffer.wrap(data).asFloatBuffer().get(out);
        return out;
    }

    private static byte[] floatsToBytes(float[] values) {
        ByteBuffer buf = ByteBuffer.allocate(values.length * Float.BYTES);
        buf.asFloatBuffer().put(values);
        return buf.array();
    }

    public static boolean readyForAverage(int instanceCount, int round) {
        // if all instances have uploaded weights -> true
        // SELECT COUNT(instance) GROUP BY round
        // if answer == instances
        // return true;
        int result;
        // else -> false
        String sql = "SELECT COUNT(instance) FROM curvy_dolphin.model_weights WHERE round = ? GROUP BY round";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, round);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()){
                    result = rs.getInt(1);
                    System.out.println("Found : " + result + " weights for round " + round);
                    return result == instanceCount;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readWeights(String instance, int round) throws InterruptedException {
        byte[] result = null;
        while (true){

            if (result == null){
                String sql = "SELECT weights FROM curvy_dolphin.model_weights WHERE instance = ? AND round = ?";
                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, instance);
                    ps.setInt(2, round);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()){
                            result = rs.getBytes(1);
                        } else {
                            System.out.println("Instance " + instance + " weights for round : " + round + " not present");
                            Thread.sleep(10000);
                            result = null;
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return result;
            }

        }
    }

    public static void saveScaler(String name, int[][] GROUPS, double[] mean, double[] std){
        String sql = "INSERT INTO curvy_dolphin.model_scaler (scaler_name, group_index, mean, std) "
                + "VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (scaler_name, group_index) "
                + "DO UPDATE SET mean = EXCLUDED.mean, std = EXCLUDED.std, updated_at = now()";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (int g = 0; g < GROUPS.length; g++) {
                ps.setString(1, name);
                ps.setInt(2, g);
                ps.setDouble(3, mean[g]);
                ps.setDouble(4, std[g]);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadScaler(Scaler scaler, String name) throws SQLException, InterruptedException {
        String sql = "SELECT group_index, mean, std FROM curvy_dolphin.model_scaler "
                + "WHERE scaler_name = ? ORDER BY group_index";

        while (true){
            int found = 0;
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int g = rs.getInt(1);
                        if (g < 0 || g >= scaler.GROUPS.length) {
                            throw new IllegalStateException("Scaler '" + name + "' has unexpected group " + g);
                        }
                        scaler.mean[g] = rs.getDouble(2);
                        scaler.std[g]  = rs.getDouble(3);
                        found++;
                    }
                    if (found > 0){
                        return;
                    }
                }
            }
            if (found != scaler.GROUPS.length && found != 0) {
                throw new IllegalStateException("Scaler '" + name + "' has " + found
                        + " groups, expected " + scaler.GROUPS.length);
            }
            if (found == 0){
                System.out.println("Scaling values not found yet, waiting");
                Thread.sleep(10000);
            }
        }

    }
}
