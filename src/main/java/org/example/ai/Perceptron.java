package org.example.ai;

import java.util.Random;

public class Perceptron {
    float[] weights;
    float learningConstant = .01f;

    public Perceptron(int n, float learningConstant){
        this.weights = new float[n];
        this.learningConstant = learningConstant;

        Random random = new Random();
        for (int i = 0; i < n; i++){
            this.weights[i] = random.nextFloat();

            int negIndicator = random.nextInt(100);

            if (negIndicator % 2 == 0){
                this.weights[i] = -this.weights[i];
            }
        }
    }

    public void train(float[] input, float desired){
        float guess = this.feedForward(input);

        float error = desired - guess;

        for (int i = 0; i < this.weights.length; i++){
            this.weights[i] = this.weights[i] + error * input[i] * this.learningConstant;
        }
    }

    public float feedForward(float[] input){
        float sum = 0.0f;

        for (int i = 0; i < this.weights.length; i++){
            sum += input[i] * this.weights[i];
        }

        return activate(sum);
    }

    private int activate(float input){
        if (input > 0){
            return 1;
        } else {
            return -1;
        }
    }
}
