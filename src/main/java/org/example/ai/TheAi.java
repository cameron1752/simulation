package org.example.ai;

import org.example.elevator.ElevatorSim;
import org.example.elevator.People;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TheAi {
    private static List<DNA> population;
    private static List<DNA> matingPool;
    private static final String target = "meats and cheeses always pleases";
//    private static final String target = "42 the answer to life the universe and everything";
//    private static final String target = "Down with disease" +
//        " Three weeks in my bed" +
//        " Trying to stop these demons that keep" +
//        " Dancing in my head" +
//        " Down with disease" +
//        " And I'm up before the dawn" +
//        " A thousand barefoot children outside" +
//        " Dancing on my lawn" +
//        " Dancing on my lawn and I keep" +
//        " Waiting for the time when I can finally say" +
//        " This has all been wonderful, but now I'm on my way" +
//        " But when I think it's time to leave it all behind" +
//        " I try to find a way to, but there's nothing I can say to make it stop" +
//        " Down with disease" +
//        " And the jungles in my mind" +
//        " They're climbing up my waterfalls and" +
//        " Swingin' on my vines" +
//        " So I try to hear the music" +
//        " But I'm always losing time" +
//        " 'Cause they're stepping on my rhythm" +
//        " And they're stealin' all my lines" +
//        " Stealin' all my lines and I keep" +
//        " Waiting for the time when I can finally say" +
//        " That this has all been wonderful, but now I'm on my way" +
//        " When I think it's time to leave it all behind" +
//        " I try to find a way to, but there's nothing I can say to make it stop" +
//        " Waiting for the time when I can finally say" +
//        " That this has all been wonderful, but now I'm on my way" +
//        " But when I think it's time to leave it all behind" +
//        " I try to find a way to, but there's nothing I can say to make it stop" +
//        " This has all been wonderful, but now I'm on my way" +
//        " Waiting for the time when I can finally say" +
//        " That this has all been wonderful, but now I'm on my way" +
//        " Waiting for the time when I can finally say" +
//        " This has all been wonderful, but now I'm on my way";
//private static final String target = "To be or not to be.";
    private static float averageFitness;
    private static float sum;
    private static float count;
    private static float correct;
    private static float wrong;
    private static final int populationSize = 1000;
    private static final int trainingSize = 10000;
    private static final int inputs = 3;
    private static boolean found = false;
    private static int generations = 0;
    private static long start;
    private static float maxFitness = 0.0f;
    private static String bestGene = "";

    public static void main(String[] args) {



//        doTraining();
        doAi();
    }

    private static void doTraining(){
        Perceptron perceptron = new Perceptron(inputs, .001f);

        // create training points
        float[][] training = new float[trainingSize][inputs];
        Random random = new Random();
        int max = trainingSize;
        int min = -trainingSize;

        // fill training points array
        for (int i = 0; i < trainingSize; i++){
            int x = random.nextInt(max - min + 1) + min;
            int y = random.nextInt(max - min + 1) + min;
            training[i][0] = x;
            training[i][1] = y;
            training[i][2] = 1;
        }

        for (float[] set : training){
            float x = set[0];
            float y = set[1];

            float desired = -1;

            if(y > f((int) x)){
                desired = 1;
            }

            perceptron.train(set, desired);
        }

        for (float[] set : training){
            float guess = perceptron.feedForward(set);

            float x = set[0];
            float y = set[1];

            float desired = -1;

            if(y > f((int) x)){
                desired = 1;
            }

            if (guess == desired){
                correct++;
            } else {
                wrong++;
            }
        }

        System.out.println("Correct: " + correct + " Wrong: " + wrong);

    }

    private static float f(int x){
        return .5f * x + 1;
    }

    private static void doAi(){
        start = System.currentTimeMillis();

        population = new ArrayList<>();
        matingPool = new ArrayList<>();

        for (int i = 0; i < populationSize; i++){
            population.add(new DNA(target.length()));
        }

        while (!found) {
            think();
        }

        System.out.println();
        System.out.println("Population: " + populationSize);
        System.out.println("Took: " + generations + " generations");
        System.out.println("Average fitness: " + averageFitness);
        System.out.println("Elapsed Time: " + formatTime(System.currentTimeMillis() - start) + " s");
    }
    private static void think(){
        generations++;
        for (DNA dna : population){
            dna.calculateFitness(target);

            System.out.println("Current Genes: " + dna.getGenesAsString()
                    + " | max fitness: " + maxFitness
                    + " | best gene: " + bestGene
                    + " | elapsed time: " + formatTime(System.currentTimeMillis() - start));

            maxFitness = Math.max(maxFitness, dna.getFitness());

            if (maxFitness == dna.getFitness()){
                bestGene = dna.getGenesAsString();
            }
            addFitnessValue(dna.getFitness());

            if (dna.getGenesAsString().equals(target)){
                found = true;
                return;
            }

        }

        matingPool.clear();

        for (DNA dna : population){
            int n = (int) Math.floor(dna.getFitness() * 100);

            if (dna.getFitness() > .75){
                n = n * 2;
            }

            for (int m = 0; m <= n; m++){
                matingPool.add(dna);
            }
        }

        for (int i = 0; i < population.size(); i++){
            DNA parentA = randomParent();
            DNA parentB = randomParent();

            DNA child = parentA.crossOver(parentB);
            child.mutate();

            population.set(i, child);
        }


    }

    private static DNA randomParent(){
        Random random = new Random();
        return matingPool.get(random.nextInt(matingPool.size()));
    }

    public static void addFitnessValue(float newValue) {
        sum += newValue;
        count++;
        averageFitness = sum / count;
    }

    public static String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long centis = (milliseconds % 1000) / 10;  // reduce ms to 2 digits

        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }
}
