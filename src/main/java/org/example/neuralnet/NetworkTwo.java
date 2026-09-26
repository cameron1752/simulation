package org.example.neuralnet;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;

public class NetworkTwo {
    // sizes is an array of our neuron sizes, e.g. [150, 128, 64, 24]
    int[] sizes;
    List<float[]> biases = new ArrayList<>();
    List<float[][]> weights = new ArrayList<>();
    private final List<float[]> zs = new ArrayList<>();
    private final List<float[]> activations = new ArrayList<>();
    private final Random random = new Random();
    private static final String COORDINATOR = "coordinator";

    // slope for negative inputs in leaky ReLU
    private static final float LEAK = 0.01f;

    public NetworkTwo(int[] sizes) {
        this.sizes = sizes;

        // biases start at zero
        for (int x = 1; x < sizes.length; x++) {
            biases.add(new float[sizes[x]]);
        }

        // He initialization: random weights scaled by sqrt(2 / inputs), so the
        // signal neither explodes nor vanishes as it passes through ReLU layers
        for (int x = 0; x < sizes.length - 1; x++) {
            int inputSize = sizes[x];
            int outputSize = sizes[x + 1];
            double scale = Math.sqrt(2.0 / inputSize);

            float[][] weight = new float[outputSize][inputSize];
            for (int y = 0; y < outputSize; y++) {
                for (int z = 0; z < inputSize; z++) {
                    weight[y][z] = (float) (random.nextGaussian() * scale);
                }
            }
            weights.add(weight);
        }
    }

    // hidden layers use leaky ReLU; the output layer is linear (no activation),
    // so it can produce any value, including negatives and values above 1
    public float[] feedForward(float[] input) {
        activations.clear();
        zs.clear();

        float[] activation = input;
        activations.add(activation);

        int last = weights.size() - 1;
        for (int i = 0; i < weights.size(); i++) {
            float[] weighed = VectorHelper.dot(weights.get(i), activation);
            float[] biased = VectorHelper.add(weighed, biases.get(i));
            zs.add(biased);
            activation = (i == last) ? biased : leakyRelu(biased);
            activations.add(activation);
        }

        return activation;
    }

    public void SDG(List<TrainingExample> trainingData, long epochs, long pass, int miniBatchSize,
                    float learningRate, List<TrainingExample> testData, Scaler scaler) {

        long startTime = System.currentTimeMillis();
        // shuffle each epoch: the data arrives grouped by location, so without
        // this every mini-batch would contain a single location's weather
        Collections.shuffle(trainingData, random);

        for (int start = 0; start < trainingData.size(); start += miniBatchSize) {
            int end = Math.min(start + miniBatchSize, trainingData.size());
            updateMiniBatch(trainingData.subList(start, end), learningRate);
        }

        System.out.printf("%n=== Epoch %d of pass %d ===%n", epochs + 1, pass);
        if (!testData.isEmpty()) {
            double mae = evaluate(testData, scaler);
            if (Double.isNaN(mae)) {
                System.out.println("MAE is NaN: training diverged. Try a smaller learning rate.");
                return;
            }
        }
        System.out.printf("Total elapsed time %dms%n", System.currentTimeMillis() - startTime);

    }

    public double evaluate(List<TrainingExample> testData, Scaler scaler) {
        final int H = 24;
        double[] absErr = new double[H], sqErr = new double[H], baseErr = new double[H];
        long within3 = 0, total = 0;

        for (TrainingExample ex : testData) {
            float[] pred = feedForward(ex.input);

            for (int h = 0; h < H; h++) {
                double actual    = scaler.unscaleTemp(ex.label[h]);
                double predicted = scaler.unscaleTemp(pred[h]);
                double yesterday = scaler.unscaleTemp(ex.input[24 + h]);   // same hour, 24h earlier

                double e = predicted - actual;
                absErr[h]  += Math.abs(e);
                sqErr[h]   += e * e;
                baseErr[h] += Math.abs(yesterday - actual);
                if (Math.abs(e) <= 3.0) within3++;
                total++;
            }
        }

        int n = testData.size();
        double mae = 0, baseMae = 0;
        System.out.println("hour ahead | model MAE | RMSE  | persistence MAE");
        for (int h = 0; h < H; h++) {
            mae += absErr[h];
            baseMae += baseErr[h];
            if (h == 0 || h == 5 || h == 11 || h == 23) {
                System.out.printf("   +%-7d | %6.2f°F  | %5.2f | %6.2f°F%n",
                        h + 1, absErr[h] / n, Math.sqrt(sqErr[h] / n), baseErr[h] / n);
            }
        }
        mae /= (double) n * H;
        baseMae /= (double) n * H;

        System.out.printf("Overall MAE: %.2f°F (persistence: %.2f°F)%n", mae, baseMae);
        System.out.printf("Within ±3°F: %.1f%%%n", 100.0 * within3 / total);
        return mae;
    }

    private void updateMiniBatch(List<TrainingExample> batch, float learningRate) {
        List<float[]> nablaB = new ArrayList<>();
        List<float[][]> nablaW = new ArrayList<>();

        for (int x = 1; x < sizes.length; x++) {
            nablaB.add(new float[sizes[x]]);
        }
        for (int x = 0; x < sizes.length - 1; x++) {
            nablaW.add(new float[sizes[x + 1]][sizes[x]]);
        }

        for (TrainingExample example : batch) {
            Object[] results = backprop(example);

            @SuppressWarnings("unchecked")
            List<float[]> deltaNablaB = (List<float[]>) results[0];
            @SuppressWarnings("unchecked")
            List<float[][]> deltaNablaW = (List<float[][]>) results[1];

            for (int x = 0; x < deltaNablaB.size(); x++) {
                float[] acc = nablaB.get(x), d = deltaNablaB.get(x);
                for (int z = 0; z < acc.length; z++) acc[z] += d[z];
            }
            for (int x = 0; x < deltaNablaW.size(); x++) {
                float[][] acc = nablaW.get(x), d = deltaNablaW.get(x);
                for (int y = 0; y < acc.length; y++)
                    for (int z = 0; z < acc[y].length; z++) acc[y][z] += d[y][z];
            }
        }

        float step = learningRate / batch.size();
        for (int z = 0; z < weights.size(); z++) {
            float[][] w = weights.get(z), gw = nablaW.get(z);
            for (int x = 0; x < w.length; x++)
                for (int y = 0; y < w[x].length; y++) w[x][y] -= step * gw[x][y];

            float[] b = biases.get(z), gb = nablaB.get(z);
            for (int x = 0; x < b.length; x++) b[x] -= step * gb[x];
        }
    }

    private Object[] backprop(TrainingExample trainingExample) {
        List<float[]> nablaB = new ArrayList<>();
        List<float[][]> nablaW = new ArrayList<>();

        // forward pass fills zs and activations
        feedForward(trainingExample.input);

        // output layer: linear activation + mean squared error means the
        // delta is simply (output - target), with no activation derivative
        float[] delta = costDerivative(activations.get(activations.size() - 1), trainingExample.label);

        nablaB.add(0, delta);
        nablaW.add(0, VectorHelper.outerMult(delta, activations.get(activations.size() - 2)));

        // propagate backward through the hidden (leaky ReLU) layers
        for (int i = 2; i < sizes.length; i++) {
            float[] z = zs.get(zs.size() - i);
            float[] rp = leakyReluPrime(z);

            delta = VectorHelper.mult(
                    VectorHelper.dot(VectorHelper.transpose(weights.get(weights.size() - i + 1)), delta),
                    rp
            );

            nablaB.add(0, delta);
            nablaW.add(0, VectorHelper.outerMult(delta, activations.get(activations.size() - i - 1)));
        }

        return new Object[]{nablaB, nablaW};
    }

    private float[] costDerivative(float[] output, float[] expected) {
        return VectorHelper.subtract(output, expected);
    }

    private float[] leakyRelu(float[] input) {
        float[] out = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = input[i] > 0 ? input[i] : LEAK * input[i];
        }
        return out;
    }

    private float[] leakyReluPrime(float[] input) {
        float[] out = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = input[i] > 0 ? 1f : LEAK;
        }
        return out;
    }

    public void save(String path) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            for (float[] bias : biases) {
                StringBuilder sb = new StringBuilder();
                for (float b : bias) sb.append(b).append(",");
                writer.println(sb);
            }
            writer.println("---");
            for (float[][] layerWeights : weights) {
                for (float[] row : layerWeights) {
                    StringBuilder sb = new StringBuilder();
                    for (float w : row) sb.append(w).append(",");
                    writer.println(sb);
                }
                writer.println("--");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Network saved to " + path);
    }

    public void load(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            biases.clear();
            weights.clear();

            String line;
            while (!(line = reader.readLine()).equals("---")) {
                String[] parts = line.split(",");
                float[] bias = new float[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) bias[i] = Float.parseFloat(parts[i]);
                }
                biases.add(bias);
            }

            List<float[]> currentLayer = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.equals("--")) {
                    weights.add(currentLayer.toArray(new float[0][]));
                    currentLayer = new ArrayList<>();
                } else {
                    String[] parts = line.split(",");
                    float[] row = new float[parts.length - 1]; // trailing comma
                    for (int i = 0; i < row.length; i++) row[i] = Float.parseFloat(parts[i]);
                    currentLayer.add(row);
                }
            }
        }
        System.out.println("Network loaded from " + path);
    }

    public void write(String instance, int round){
        if (COORDINATOR.equals(instance)) {
            System.out.println("Resetting weights from coordinator!");
            // is coordinator instance, reset weights
            DBManager.resetWeights();
        }
        System.out.println("Writing weights for : " + instance + " in round " + round);
        DBManager.writeWeights(instance, round, toBytes(this));
    }
    public void read(int round) throws InterruptedException {
        System.out.println("reading weights for : coordinator in round " + round);
        // read weights from DB
        fromBytes(this, DBManager.readWeights("coordinator", round));
    }

    static byte[] toBytes(NetworkTwo net) {
        int count = 0;
        for (int l = 0; l < net.weights.size(); l++) {
            float[][] w = net.weights.get(l);
            count += w.length * w[0].length + net.biases.get(l).length;
        }
        ByteBuffer buf = ByteBuffer.allocate(count * Float.BYTES);
        FloatBuffer fb = buf.asFloatBuffer();
        for (int l = 0; l < net.weights.size(); l++) {
            for (float[] row : net.weights.get(l)) fb.put(row);
            fb.put(net.biases.get(l));
        }
        return buf.array();
    }

    static void fromBytes(NetworkTwo net, byte[] data) {
        FloatBuffer fb = ByteBuffer.wrap(data).asFloatBuffer();
        for (int l = 0; l < net.weights.size(); l++) {
            for (float[] row : net.weights.get(l)) fb.get(row);
            fb.get(net.biases.get(l));
        }
        if (fb.hasRemaining()) {
            throw new IllegalStateException("Weight data doesn't match this network's shape");
        }
    }

    public void refresh(){
        // re-read weights from db, waiting until the new average is present
    }

    public void printBiases() {
        for (int layer = 0; layer < biases.size(); layer++) {
            System.out.println("Layer " + layer + " biases: " + Arrays.toString(biases.get(layer)));
        }
    }

    public void printWeights() {
        for (int layer = 0; layer < weights.size(); layer++) {
            float[][] lw = weights.get(layer);
            System.out.println("Layer " + layer + " weights (" + lw.length + "x" + lw[0].length + "):");
            for (float[] row : lw) System.out.println("  " + Arrays.toString(row));
        }
    }
}