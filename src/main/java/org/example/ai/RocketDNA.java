package org.example.ai;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

import java.util.Arrays;
import java.util.Random;

public class RocketDNA {
    private Vector3f[] genes;
    private float fitness = 0.0f;
    private static float mutationRate = .02f;
    private static float mutationStrength = .5f;
    private float[] weights;
    private static float maxThrust = .1f;

    public RocketDNA(){
        this.weights = Brain.randomWeights();
    }

    public RocketDNA(float[] weights){
        this.weights = weights;
    }

    public RocketDNA crossOver(RocketDNA parentB){

        int inputHiddenSize = Brain.INPUTS * Brain.HIDDEN;
        int hiddenBiasSize = Brain.HIDDEN;
        int hiddenOutputSize = Brain.HIDDEN * Brain.OUTPUTS;
        int outputBiasSize = Brain.OUTPUTS;

        int[] boundaries = {
                0,
                inputHiddenSize,
                inputHiddenSize * hiddenBiasSize,
                inputHiddenSize + hiddenBiasSize + hiddenOutputSize,
                inputHiddenSize + hiddenBiasSize + hiddenOutputSize + outputBiasSize
        };

        Random random = new Random();
        int splitLayer = random.nextInt(boundaries.length - 1);

        float[] childWeights = new float[this.weights.length];

        for (int i = 0; i < childWeights.length; i++){
            childWeights[i] = (i < boundaries[splitLayer + 1]) ? this.weights[i] : parentB.weights[i];
        }

        return new RocketDNA(childWeights);
    }

    public void mutate(){
        Random random = new Random();

        for (int i = 0; i < weights.length; i++){
            if (random.nextFloat() < mutationRate){
                float delta = (random.nextFloat() * 2f - 1f) * mutationStrength;
                weights[i] = Math.max(-2f, Math.min(2f, weights[i] + delta));
            }
        }
    }

    public float[] getWeights(){return weights;}

}
