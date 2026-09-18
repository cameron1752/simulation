package org.example.ai;

import com.jme3.math.Vector3f;

import java.util.Random;
import java.util.Vector;

public class Brain {

    public static final int INPUTS = 5;
    public static final int HIDDEN = 6;
    public static final int OUTPUTS = 2;

    private float[] weightsInputHidden;  // size INPUTS * HIDDEN
    private float[] biasHidden;          // size HIDDEN
    private float[] weightsHiddenOutput; // size HIDDEN * OUTPUTS
    private float[] biasOutput;          // size OUTPUTS

    public Brain(float[] flatWeights){
        int idx = 0;

        weightsInputHidden = new float[INPUTS * HIDDEN];
        for (int i = 0; i < weightsInputHidden.length; i++){
            weightsInputHidden[i] = flatWeights[idx++];
        }

        biasHidden = new float[HIDDEN];
        for (int i = 0; i < biasHidden.length; i++){
            biasHidden[i] = flatWeights[idx++];
        }

        weightsHiddenOutput = new float[HIDDEN * OUTPUTS];
        for (int i = 0; i < weightsHiddenOutput.length; i++) {
            weightsHiddenOutput[i] = flatWeights[idx++];
        }

        biasOutput = new float[OUTPUTS];
        for (int i = 0; i < biasOutput.length; i++) {
            biasOutput[i] = flatWeights[idx++];
        }
    }

    public static int totalWeights(){
        return (INPUTS * HIDDEN) + HIDDEN + (HIDDEN * OUTPUTS) + OUTPUTS;
    }

    public static float[] randomWeights() {
        Random random = new Random();
        float[] weights = new float[totalWeights()];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = (random.nextFloat() * 2f) - 1f; // range -1 to 1
        }
        return weights;
    }

    public Vector3f decide(float[] inputs){
        float[] hidden = new float[HIDDEN];

        for (int h = 0; h < HIDDEN; h++){
            float sum = biasHidden[h];
            for (int i =0 ; i < INPUTS; i++){
                sum += inputs[i] * weightsInputHidden[h * INPUTS + i];
            }
            hidden[h] = (float) Math.tanh(sum);
        }

        float[] output = new float[OUTPUTS];
        for (int o = 0; o < OUTPUTS; o++) {
            float sum = biasOutput[o];
            for (int h = 0; h < HIDDEN; h++) {
                sum += hidden[h] * weightsHiddenOutput[o * HIDDEN + h];
            }
            output[o] = (float) Math.tanh(sum);
        }

        return new Vector3f(output[0], output[1], 0);
    }

    public void mutate(float[] weights) {
        Random random = new Random();
        float mutationRate = 0.02f; // reuse whatever rate you're already using elsewhere
        float mutationStrength = 0.5f; // how big a nudge — tune this

        for (int i = 0; i < weights.length; i++) {
            if (random.nextFloat() < mutationRate) {
                float delta = (random.nextFloat() * 2f - 1f) * mutationStrength;
                weights[i] += delta;
            }
        }
    }

    public float[] crossOver(float[] parentA, float[] parentB) {
        int inputHiddenSize = INPUTS * HIDDEN;
        int hiddenBiasSize = HIDDEN;
        int hiddenOutputSize = HIDDEN * OUTPUTS;
        int outputBiasSize = OUTPUTS;

        int[] boundaries = {
                0,
                inputHiddenSize,
                inputHiddenSize + hiddenBiasSize,
                inputHiddenSize + hiddenBiasSize + hiddenOutputSize,
                inputHiddenSize + hiddenBiasSize + hiddenOutputSize + outputBiasSize
        };

        Random random = new Random();
        int splitLayer = random.nextInt(boundaries.length - 1); // pick which layer boundary to split at

        float[] childWeights = new float[parentA.length];

        for (int i = 0; i < childWeights.length; i++) {
            childWeights[i] = (i < boundaries[splitLayer + 1]) ? parentA[i] : parentB[i];
        }

        return childWeights;
    }

}
