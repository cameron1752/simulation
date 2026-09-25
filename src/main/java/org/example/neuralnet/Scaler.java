package org.example.neuralnet;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class Scaler {
    // groups: {startIndex, endIndex (exclusive)}
    static final int[][] GROUPS = { {0, 48}, {48, 96}, {96, 144}, {144, 145}, {145, 146} };
    final double[] mean = new double[GROUPS.length];
    final double[] std  = new double[GROUPS.length];

    // running accumulators (Welford), live between partialFit calls and finish()
    private final long[]   runCount = new long[GROUPS.length];
    private final double[] runMean  = new double[GROUPS.length];
    private final double[] runM2    = new double[GROUPS.length];

    /** Clears the running statistics so a new fitting pass can start. */
    void reset() {
        for (int g = 0; g < GROUPS.length; g++) {
            runCount[g] = 0;
            runMean[g] = 0;
            runM2[g] = 0;
        }
    }

    /** Accumulates statistics from one chunk of raw (unscaled) training data. */
    void partialFit(List<TrainingExample> train) {
        for (TrainingExample ex : train) {
            float[] in = ex.input;
            for (int g = 0; g < GROUPS.length; g++) {
                long n = runCount[g];
                double m = runMean[g];
                double m2 = runM2[g];
                for (int i = GROUPS[g][0]; i < GROUPS[g][1]; i++) {
                    double v = in[i];
                    n++;
                    double delta = v - m;
                    m += delta / n;
                    m2 += delta * (v - m);
                }
                runCount[g] = n;
                runMean[g] = m;
                runM2[g] = m2;
            }
        }
    }

    /** Turns the accumulated statistics into the mean/std used by transform. */
    void finish() {
        for (int g = 0; g < GROUPS.length; g++) {
            if (runCount[g] == 0) {
                throw new IllegalStateException("No data accumulated for group " + g);
            }
            mean[g] = runMean[g];
            std[g]  = Math.sqrt(runM2[g] / runCount[g]);
            if (std[g] < 1e-8) std[g] = 1;   // avoid dividing by zero for a constant feature
        }
    }

    /** One-shot fit on a single list, same behavior as before. */
    void fit(List<TrainingExample> train) {
        reset();
        partialFit(train);
        finish();
    }

    void transform(List<TrainingExample> data) {
        for (TrainingExample ex : data) {
            float[] in = ex.input, out = ex.label;
            for (int g = 0; g < GROUPS.length; g++)
                for (int i = GROUPS[g][0]; i < GROUPS[g][1]; i++)
                    in[i] = (float) ((in[i] - mean[g]) / std[g]);
            for (int i = 0; i < out.length; i++)             // targets are temperature (group 0)
                out[i] = (float) ((out[i] - mean[0]) / std[0]);
        }
    }

    float unscaleTemp(float scaled) {
        return (float) (scaled * std[0] + mean[0]);
    }

    void save(String path) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            for (int g = 0; g < GROUPS.length; g++) w.println(mean[g] + "," + std[g]);
        }
    }

    void write(){
        // write to database table
        DBManager.saveScaler("Scaler", GROUPS, mean, std);
    }

    void read() throws SQLException, InterruptedException {
        // read from database table
        DBManager.loadScaler(this, "Scaler");
    }

    void load(String path) throws IOException {
        try (BufferedReader r = new BufferedReader(new FileReader(path))) {
            for (int g = 0; g < GROUPS.length; g++) {
                String[] parts = r.readLine().split(",");
                mean[g] = Double.parseDouble(parts[0]);
                std[g]  = Double.parseDouble(parts[1]);
            }
        }
    }
}