package org.example.ai;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Population {
    Rocket[] population;
    int generations;
    Vector3f target;
    float totalFitness;
    int length;
    private Material material;
    static List<Rocket> matingPool = new ArrayList<>();
    int hitList = 0;
    int maxHit = 0;
    private final static String filePath = "output_file.txt";
    private final static int preserveChildren = 100;
    private final static boolean loadWeights = false;
    private final static boolean saveWeights = true;


    public Population(int length, Vector3f target, Material material){
        this.length = length;
        this.population = new Rocket[length];
        this.generations = 1;
        this.target = target;
        this.material = material;

        List<float[]> weights = loadAllWeights(filePath);
        int i = 0;

        if (loadWeights){
            for (; i < length; i++){
                this.population[i] = new Rocket(0, 0, new RocketDNA(weights.get(i)), material);
            }
        }

        System.out.println("pre-loaded " + (i - 1) + " weights");

        for (;i < length; i++){
            this.population[i] = new Rocket(0, 0, new RocketDNA(), material);
        }
    }

    public boolean update(){
        hitList = 0;
        for (Rocket rocket : population){
            if (rocket.hasHitTarget(target)){
                hitList++;
            } else {
                rocket.update(target);
            }
        }
        maxHit = Math.max(maxHit, hitList);
        return hitList > (length / 2);
    }

    public static void saveWeights(float[] weights, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            StringBuilder sb = new StringBuilder();
            for (float w : weights) {
                sb.append(w).append(",");
            }
            sb.append("\n");
            writer.print(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<float[]> loadAllWeights(String filename) {
        List<float[]> allWeights = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (String line : lines) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",");
                float[] weights = new float[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        weights[i] = Float.parseFloat(parts[i]);
                    }
                }
                allWeights.add(weights);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allWeights;
    }

    public void fitness(){
        for (Rocket rocket : population){
            rocket.calculateFitness(target);
        }
    }

    public void selection(){
        totalFitness = 0.0f;

        for (Rocket rocket : population){
            totalFitness += rocket.fitness;
        }

        for (Rocket rocket : population){
            rocket.fitness /= totalFitness;
        }
    }

    public void reproduction(){
        matingPool.clear();
        hitList = 0;
        generations++;
        List<Rocket> topXPerformers = new ArrayList<>();

        if (saveWeights){
            saveBestWeights();
        }


        for (Rocket rocket : population){
            // save to array list for sorting
            topXPerformers.add(rocket);

            // determine how many to add based on fitness
            int n = (int) Math.floor(rocket.fitness * 100) + 1;

            // increase based on if they've hit the target
            if (rocket.hasHitTarget(target)){
                n = n * 3;
            }

            // add to mating pool
            for (int m = 0; m < n; m++){
                matingPool.add(rocket);
            }
        }

        Rocket[] newPopulation = new Rocket[length];

        // preserve the top x performers
        topXPerformers.sort(new RocketComparator());

        for (int i = 0; i < preserveChildren; i++){
            newPopulation[i] = new Rocket(0, 0, topXPerformers.get(i).dna, material);
        }

        for (int i = preserveChildren; i < length; i++){
            RocketDNA parentA = randomParent();
            RocketDNA parentB = randomParent();

            RocketDNA child = parentA.crossOver(parentB);
            child.mutate();

            newPopulation[i] = new Rocket(0, 0, child, material);
        }

        this.population = newPopulation;
    }

    public void saveBestWeights(){
        float maxFitness = 0.0f;

        for (Rocket rocket : population){
            maxFitness = Math.max(rocket.fitness, maxFitness);
        }

        for (Rocket rocket : population) {
            if (rocket.fitness == maxFitness) {
                saveWeights(rocket.dna.getWeights(), filePath);
            }
        }

    }

    private static RocketDNA randomParent(){
        Random random = new Random();
        return matingPool.get(random.nextInt(matingPool.size())).dna;
    }

    public void detachChildren(Node rootNode){
        for (Rocket rocket : population){
            rootNode.detachChild(rocket.getGeometry());
        }
    }

    public void attachChildren(Node rootNode){
        for (Rocket rocket : population){
            rootNode.attachChild(rocket.getGeometry());
        }
    }

    public RocketDNA weightedSelection(){
        Random random = new Random();
        int randSelection = random.nextInt(length);
        return this.population[randSelection].dna;
    }

    public void setMaterial(Material material){
        this.material = material;
    }

    public int getGenerations(){return generations;}
}
