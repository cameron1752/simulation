package org.example.ai;

import java.util.Arrays;
import java.util.Random;

public class DNA {
    private char genes[];
    private float fitness = 0.0f;
    private static float mutationRate = .02f;

    public DNA(int length){
        genes = new char[length];

        for (int i = 0; i < length; i++){
            this.genes[i] = randomCharacter();
        }
    }

    private char randomCharacter(){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 /*-+.,'!@#$%^&()<>?/|][{}";
        Random random = new Random();
        return chars.charAt(random.nextInt(chars.length()));
    }

    // linear fitness
//    public void calculateFitness(String target){
//        float score = 0.0f;
//
//        for (int i = 0; i < genes.length; i++){
//            if (genes[i] == target.charAt(i)){
//                score++;
//            }
//        }
//
//        this.fitness = score / target.length();
//    }

//     quadratic fitness
    public void calculateFitness(String target){
        float score = 0.0f;

        for (int i = 0; i < genes.length; i++){
            if (genes[i] == target.charAt(i)){
                score++;
            }
        }

        this.fitness = (score * score) / (target.length() * target.length());
    }

    // exponential fitness
//    public void calculateFitness(String target){
//        float score = 0.0f;
//
//        for (int i = 0; i < genes.length; i++){
//            if (genes[i] == target.charAt(i)){
//                score++;
//            }
//        }
//
//        this.fitness = (float) (Math.pow(2, score) / Math.pow(2, target.length()));
//    }

    public DNA crossOver(DNA parentB){
        DNA child = new DNA(this.genes.length);
        Random random = new Random();

        int midpoint = (int) Math.floor(random.nextInt(this.genes.length));

        char childGenes[] = new char[this.genes.length];

        for (int i = 0; i < this.genes.length; i++){
            if (i < midpoint){
                childGenes[i] = this.genes[i];
            } else {
                childGenes[i] = parentB.genes[i];
            }
        }

        child.setGenes(childGenes);

        return child;
    }

    public void mutate(){
        Random random = new Random();

        for (int i = 0; i < genes.length; i++){
            float value = random.nextFloat();
            if (value < mutationRate){
                genes[i] = randomCharacter();
            }

        }
    }

    private float getVariableMutation(){
        // more fit a gene is less it mutates
        if (fitness >= .75f){
            mutationRate = .01f;
        }

        if (fitness >= .50f && fitness < .75f){
            mutationRate = .02f;
        }

        if (fitness < .50f){
            mutationRate = .05f;
        }

        return mutationRate;
    }

    public void setGenes(char[] genes){this.genes = genes;}

    public String getGenesAsString(){
        return new String(genes);
    }

    public char[] getGenes(){return this.genes;}

    public float getFitness(){return this.fitness;}

}
