package org.example.ai;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

public class Rocket {
    Vector3f position;
    Vector3f velocity;
    Vector3f acceleration;
    float fitness = 0.0f;
    RocketDNA dna;
    Brain brain;
    private Geometry g;
    private final static float threshold = 5.0f;
    private final static float maxThrust = .1f;
    private final static float maxDistance = 500f;
    private final static float maxVelocity = 3f;
    int geneCounter;

    public Rocket(float x, float y, RocketDNA dna, Material material){
        this.dna = dna;
        this.brain = new Brain(dna.getWeights());
        this.geneCounter = 0;
        this.position = new Vector3f(x, y, 0);
        this.velocity = new Vector3f(0, 0, 0);
        this.acceleration = new Vector3f(0, 0, 0);

        Box s = new Box(1, 1, 1);
        g = new Geometry("Object", s);
        // set color to geometry
        g.setMaterial(material);
        g.setLocalTranslation(this.position);
    }

    private void applyForce(Vector3f force){
        acceleration = acceleration.add(force);
    }

    public void update(Vector3f target){
        float[] inputs = computeInputs(target);
        Vector3f decision = brain.decide(inputs); // x,y in [-1, 1]
        Vector3f force = decision.mult(maxThrust);

        applyForce(force);

        velocity = velocity.add(acceleration);
        if (velocity.length() > maxVelocity){
            velocity = velocity.normalize().mult(maxVelocity);
        }
        position = position.add(velocity);

        g.setLocalTranslation(position);

        acceleration = acceleration.mult(0);
    }

    private float[] computeInputs(Vector3f target){
        Vector3f toTarget = target.subtract(position);
        float distance = toTarget.length();
        Vector3f direction = (distance > 0.0001f) ? toTarget.normalize() : new Vector3f(0, 0, 0);

        float normDistance = Math.min(distance / maxDistance, 1f);
        float normVelX = velocity.x / maxVelocity;
        float normVelY = velocity.y / maxVelocity;

        return new float[]{ direction.x, direction.y, normVelX, normVelY, normDistance };
    }

    public void calculateFitness(Vector3f target){
        float distance = position.distance(target);
        this.fitness = 1 / (distance * distance);
    }

    public boolean hasHitTarget(Vector3f targetPosition) {
        float distance = this.position.distance(targetPosition);
        return distance <= (1 + threshold);
    }

    public Geometry getGeometry(){return this.g;}

}
