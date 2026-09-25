package org.example.neuralnet;

public abstract class VectorHelper {

    public static float[] dot(float[][] vector1, float[] vector2){
        float[] output = new float[vector1.length];

        for (int x = 0; x < vector1.length; x++){
            output[x] = realDot(vector1[x], vector2);
        }

        return output;
    }

    private static float realDot(float[] vector1, float[] vector2){
        float sum = 0f;

        for (int x = 0; x < vector1.length; x++){
            sum+= vector1[x] * vector2[x];
        }

        return sum;
    }

    public static float[] add(float[] vector1, float[] vector2){
        float[] output = new float[vector1.length];

        for (int x = 0; x < vector1.length; x++){
            output[x] = vector1[x] + vector2[x];
        }

        return output;
    }

    public static float[] subtract(float[] vector1, float[] vector2){
        float[] output = new float[vector1.length];

        for (int x = 0; x < vector1.length; x++){
            output[x] = vector1[x] - vector2[x];
        }

        return output;
    }

    public static float[] mult(float[] vector1, float[] vector2){
        float[] output = new float[vector1.length];

        for (int x = 0; x < vector1.length; x++){
            output[x] = vector1[x] * vector2[x];
        }

        return output;
    }

    public static float[][] outerMult(float[] vector1, float[] vector2){
        float[][] output = new float[vector1.length][vector2.length];

        for (int x = 0; x < vector1.length; x++){
            for (int y = 0; y < vector2.length; y++){
                output[x][y] = vector1[x] * vector2[y];
            }
        }

        return output;
    }

    public static float[][] transpose(float[][] vector){
        float[][] output = new float[vector[0].length][vector.length];

        for (int x = 0; x < vector[0].length; x++){
            for (int y = 0; y < vector.length; y++){
                output[x][y] = vector[y][x];
            }
        }

        return output;
    }

}
