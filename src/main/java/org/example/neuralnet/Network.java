package org.example.neuralnet;

import java.io.*;
import java.util.*;

public class Network {
    // sizes is an array of our neuron sizes
    // for 3 tiered approach would look like
    // 2 inputs, 3 hidden, 1 output
    // [2, 3, 1]
    int[] sizes;
    // bias array for each neuron randomly assigned at birth
    List<float[]> biases = new ArrayList<>();
    // weights array for each neuron randomly assigned at birth
    List<float[][]> weights = new ArrayList<>();
    private List<float[]> zs = new ArrayList<>();
    private List<float[]> activations = new ArrayList<>();
    private Scaler scaler = new Scaler() ;


    public Network(int[] sizes){
        this.sizes = sizes;

        Random random = new Random();

        // generate a list of random biases for each neuron, skipping first layer
        // end array should be like this for above example:
        // biases[0][0] = [.34, .23, .12]
        // biases[1][0] = [.23]
        for (int x = 1; x < sizes.length; x++){

            int outputSize = sizes[x];

            float[] bias = new float[outputSize];
            for (int y = 0; y < outputSize; y++){
                bias[y] = (float) random.nextGaussian();
            }

            biases.add(bias);

        }

        // generate a list of random weights for each neuron, skipping last layer
        // end array should be like this for above example:
        // weights layer 0 (3 x 2) (hidden -> input)
        // [.12, .03]
        // [.23, .98]
        // [-.23, .43]
        // weights layer 1 (1 x 3) (output -> hidden)
        // [.32, .12, .59]

        for (int x = 0; x < sizes.length - 1; x++){

            int inputSize = sizes[x];
            int outputSize = sizes[x + 1];

            float[][] weight = new float[outputSize][inputSize];

            for (int y = 0; y < outputSize; y++){
                for (int z = 0; z < inputSize; z++){
                    weight[y][z] = (float) random.nextGaussian();
                }
            }

            weights.add(weight);
        }

//        printBiases();
//        printWeights();
    }



    public float[] feedForward(float[] input){
        activations.clear();
        zs.clear();

        float[] activation = input;
        activations.add(activation);

        for (int i = 0; i < weights.size(); i++){
            float[] weighed = VectorHelper.dot(weights.get(i), activation);
            float[] biased = VectorHelper.add(weighed, biases.get(i));
            zs.add(biased);
            activation = sigmoid(biased);
            activations.add(activation);
        }

        return activation;
    }

    public void SDG(List<TrainingExample> trainingData, long epochs, long miniBatchSize, float learningRate, List<TrainingExample> testData){

        int nTest = 0;

        if (!testData.isEmpty()){
            nTest = testData.size();
        }

        int n = trainingData.size();

        for (int i = 0; i < epochs; i++){
//            Collections.shuffle(trainingData);
            // create mini batches of size miniBatchSize between 0 and n
            List<List<TrainingExample>> miniBatches = new ArrayList<>();
            List<TrainingExample> miniBatch = new ArrayList<>();

            // loop over training data
            for (int x = 0; x < trainingData.size(); x++){
                // add value to mini batch
                miniBatch.add(trainingData.get(x));

                // if we're at our mini batch size
                if (miniBatch.size() == miniBatchSize){
                    // add mini batch to list of mini batches
                    miniBatches.add(miniBatch);
                    miniBatch = new ArrayList<>();
                }
            }
            // add remainder
            if (!miniBatch.isEmpty()){
                miniBatches.add(miniBatch);
            }

            // for each of the mini batches we created
            for (List<TrainingExample> batch : miniBatches){
                updateMiniBatch(batch, learningRate);
            }

            if (!testData.isEmpty()){
                int correct = (int) evaluate(testData,scaler);
                float percent = ((float) correct / nTest) * 100;
                System.out.printf("Epoch %d: %d / %d (%.2f%%)%n", i, correct, nTest, percent);
            } else {
                System.out.printf("Epoch %d complete%n", i);
            }

        }



    }

    private double evaluate(List<TrainingExample> testData, Scaler scaler) {
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


    private void updateMiniBatch(List<TrainingExample> batch, float learningRate){
        List<float[]> nablaB = new ArrayList<>();
        List<float[][]> nablaW = new ArrayList<>();

        for (int x = 1; x < sizes.length; x++){
            int outputSize = sizes[x];

            nablaB.add(new float[outputSize]);
        }

        for (int x = 0; x < sizes.length - 1; x++){
            int inputSize = sizes[x];
            int outputSize = sizes[x + 1];

            nablaW.add(new float[outputSize][inputSize]);
        }

        for (int i = 0; i < batch.size(); i++){

            Object[] results = backprop(batch.get(i));

            List<float[]> deltaNablaB = (List<float[]>) results[0];
            List<float[][]> deltaNablaW = (List<float[][]>) results[1];


            for (int x = 0; x < deltaNablaB.size(); x++){
                float[] temp = nablaB.get(x);
                for (int z = 0; z < temp.length; z++){
                    temp[z] = deltaNablaB.get(x)[z] + temp[z];
                }
                nablaB.set(x, temp);
            }


            for (int x = 0; x < deltaNablaW.size(); x++){
                float[][] tempW = nablaW.get(x);

                for (int y = 0; y < deltaNablaW.get(x).length; y++){
                    for (int z =0; z < deltaNablaW.get(x)[y].length; z++){
                        tempW[y][z] = deltaNablaW.get(x)[y][z] + tempW[y][z];
                    }
                }
                nablaW.set(x, tempW);
            }


        }

        // layer by layer
        for (int z = 0; z < weights.size(); z++){
            // update weights
            for (int x = 0; x < weights.get(z).length; x++){
                for (int y = 0; y < weights.get(z)[x].length; y++){
                    weights.get(z)[x][y] = weights.get(z)[x][y] - (learningRate / batch.size()) * nablaW.get(z)[x][y];
                }
            }

            // update bias
            for (int x = 0; x < biases.get(z).length; x++){
                biases.get(z)[x] = biases.get(z)[x] - (learningRate / batch.size()) * nablaB.get(z)[x];
            }
        }

    }

    private Object[] backprop(TrainingExample trainingExample){
        List<float[]> nablaB = new ArrayList<>();
        List<float[][]> nablaW = new ArrayList<>();

        activations.clear();
        zs.clear();

        float[] activation = trainingExample.input;
        activations.add(trainingExample.input);


        // for each layer we're weightign the input
        // adding biase to it and storing it in zs
        // performing the sigmoid on it and putting it in activations
        for (int i = 0; i < weights.size(); i++){
            float[] weighed = VectorHelper.dot(weights.get(i), activation);
            float[] biased = VectorHelper.add(weighed, biases.get(i));
            zs.add(biased);

            activation = sigmoid(biased);
            activations.add(activation);
        }

        // Step 1: output layer delta
        float[] delta = VectorHelper.mult(
                costDerivative(activations.get(activations.size() - 1), trainingExample.label),
                sigmoidPrime(zs.get(zs.size() - 1))
        );

        // output layer gradients — insert at front so layer order is correct
        nablaB.add(0, delta);
        nablaW.add(0, VectorHelper.outerMult(delta, activations.get(activations.size() - 2)));

        // Steps 2 & 3: propagate backward through remaining layers
        for (int i = 2; i < sizes.length; i++){
            float[] z = zs.get(zs.size() - i);
            float[] sp = sigmoidPrime(z);

            delta = VectorHelper.mult(
                    VectorHelper.dot(VectorHelper.transpose(weights.get(weights.size() - i + 1)), delta),
                    sp
            );

            nablaB.add(0, delta);
            nablaW.add(0, VectorHelper.outerMult(delta, activations.get(activations.size() - i - 1)));
        }

        return new Object[]{nablaB, nablaW};
    }

    private float[] costDerivative(float[] activations, float[] expected){
        return VectorHelper.subtract(activations, expected);
    }

    // find index of largest value in input array
    public int argMax(float[] input){
        float max = input[0];
        int index = 0;

        for (int i = 0; i < input.length; i++){
            max = Math.max(input[i], max);

            if (max == input[i]){
                index = i;
            }
        }

        return index;
    }

    private float[] sigmoid(float[] input){
        float[] output = new float[input.length];

        for (int x = 0; x < input.length; x++){
            output[x] = (float) (1 / (1 + Math.exp(-input[x])));
        }

        return output;
    }

    private float[] sigmoidPrime(float[] input){
        float[] output = new float[input.length];

        for (int i = 0; i < input.length; i++){
            float[] s = sigmoid(new float[]{input[i]});
            output[i] = s[0] * (1 - s[0]);
        }

        return output;
    }

    public void save(String path){
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            // save biases
            for (float[] bias : biases) {
                StringBuilder sb = new StringBuilder();
                for (float b : bias) {
                    sb.append(b).append(",");
                }
                writer.println(sb);
            }

            // separator between biases and weights
            writer.println("---");

            // save weights
            for (float[][] layerWeights : weights) {
                for (float[] row : layerWeights) {
                    StringBuilder sb = new StringBuilder();
                    for (float w : row) {
                        sb.append(w).append(",");
                    }
                    writer.println(sb);
                }
                // separator between layers
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

            // load biases
            String line;
            while (!(line = reader.readLine()).equals("---")) {
                String[] parts = line.split(",");
                float[] bias = new float[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) bias[i] = Float.parseFloat(parts[i]);
                }
                biases.add(bias);
            }

            // load weights layer by layer
            List<float[]> currentLayer = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.equals("--")) {
                    // end of a layer — convert row list to float[][]
                    float[][] layerWeights = currentLayer.toArray(new float[0][]);
                    weights.add(layerWeights);
                    currentLayer = new ArrayList<>();
                } else {
                    String[] parts = line.split(",");
                    float[] row = new float[parts.length - 1]; // trailing comma leaves empty last element
                    for (int i = 0; i < row.length; i++) {
                        row[i] = Float.parseFloat(parts[i]);
                    }
                    currentLayer.add(row);
                }
            }
        }
        System.out.println("Network loaded from " + path);
    }

/*
* helper functions to validate what we're doing and when
* */
    public void printBiases() {
        for (int layer = 0; layer < biases.size(); layer++) {
            System.out.println("Layer " + layer + " biases: " + Arrays.toString(biases.get(layer)));
        }
    }

    public void printWeights() {
        for (int layer = 0; layer < weights.size(); layer++) {
            float[][] layerWeights = weights.get(layer);
            System.out.println("Layer " + layer + " weights (" + layerWeights.length + "x" + layerWeights[0].length + "):");

            for (float[] row : layerWeights) {
                System.out.println("  " + Arrays.toString(row));
            }
        }
    }
}
