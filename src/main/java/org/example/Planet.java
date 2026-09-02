package org.example;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Float.NaN;

public class Planet {
    float gravity;
    float mass;
    float radius;
    AssetManager assetManager;
    ColorRGBA color;
    Material material;
    Geometry g;
    private float a;
    private float b;
    float angle = 0;
    float T = 0;
    Star sun;
    final float g_constant = 0.000000000066740f;
    private Geometry orbitLine;
    private Mesh orbitMesh;
    private List<Vector3f> orbitPoints = new ArrayList<>();
// new calc
    private Vector3f r;
    private float r_mag;
    private Vector3f v;
    private float v_mag;
    private Vector3f h;
    private float h_mag;
    private float mu;
    private float e;
    private float E;
    private float p;
    float e_given;

    public Planet(float e_given, float mass, float radius, AssetManager assetManager, ColorRGBA color){
        this.e_given = e_given;
        this.mass = mass;
        this.radius = radius;
        this.assetManager = assetManager;
        this.color = color;

        material = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", this.color);

        v = new Vector3f(0, 0, 0);
        initGeometry();
    }

    private void initGeometry(){
        Sphere s = new Sphere(25, 25, radius);
        g = new Geometry("Object", s);
        g.setMaterial(material);

    }
    public Geometry getGeometry(){
        return g;
    }

    public void setPosition(float x){
        Vector3f position = g.getLocalTranslation();
        position.x = x;
        position.z = 1;
        g.setLocalTranslation(position);

    }

    public void setSun(Star sun){
        this.sun = sun;
        mu = g_constant * sun.mass;
        r = g.getLocalTranslation().clone();

        r_mag = getMagnitude(r);
        v_mag = getMagnitude(v);

        stepZero();

        r_mag = getMagnitude(r);
        v_mag = getMagnitude(v);

        stepOne();
        stepTwo();
        stepThree();
        getPeriod();
    }

    private void stepZero(){
        float v_cir = (float) Math.sqrt(mu / r_mag);
        v.z = (float) (v_cir * Math.sqrt(1 + e_given));
    }

    private void stepOne(){
        // get the angular momentum vector
        h = r.cross(v);
    }

    private void stepTwo(){
        // get eccentricity vector

        float left = (v_mag * v_mag) - (mu / r_mag);
        float right = r.dot(v);

        Vector3f right_vector = new Vector3f(v.x * right, v.y * right, v.z * right);
        Vector3f left_vector = new Vector3f(r.x * left, r.y * left, r.z * left);
        Vector3f inner_vector = new Vector3f(left_vector.x - right_vector.x, left_vector.y - right_vector.y, left_vector.z - right_vector.z);

        Vector3f eccentricity_vector = new Vector3f(inner_vector.x / mu, inner_vector.y / mu, inner_vector.z / mu);

        e = getMagnitude(eccentricity_vector);
    }

    private void stepThree(){
        // specific orbital energy calculation
        float left = (v_mag * v_mag) / 2;
        float right = mu / r_mag;
        E = left - right;

        // semi major axis calculation (should be positive)
        a = -(mu) / (2 * E);

        // semi minor axis calculation
        b = (float) (a * Math.sqrt(1 - (e * e)));

        // semi latus rectum calculation
        h_mag = getMagnitude(h);
        p = (h_mag * h_mag) / mu;
    }

    private float getMagnitude(Vector3f vector){
        return (float) Math.sqrt((vector.x * vector.x) + (vector.y * vector.y) + (vector.z * vector.z));
    }



    private void getPeriod(){
        float left = (float) (2 * Math.PI);
        float right = (float) Math.sqrt((a * a * a) / (g_constant * sun.mass));

        T = left * right;
    }

    public void update(float tpf){
        g.setLocalTranslation(getDistance(tpf));
        updateOrbitLine(g.getLocalTranslation());
        g.rotate(0f, .10f, 0f);
    }
    private Vector3f getDistance(float tpf){
        Vector3f position = g.getLocalTranslation();

//        angle += (360 / T); // iterate the angle based on period * time per frame
        angle += 1;
        float radians = (float) Math.toRadians(angle);

        float x = (float) (r(radians) * Math.cos(radians));
        float z = (float) (r(radians) * Math.sin(radians));

        position.x = x;
        position.z = z;
        System.out.println(position);
        return position;
    }

    private float r(float radians){
        return (float) (p / (1 + (e * Math.cos(radians))));
    }



    public Geometry createOrbitLine() {
        orbitMesh = new Mesh();
        orbitMesh.setMode(Mesh.Mode.LineStrip);
        orbitMesh.setDynamic(); // hint to JME this buffer will be updated frequently

        orbitLine = new Geometry("orbitLine", orbitMesh);
        orbitLine.setMaterial(material);

        return orbitLine;
    }

    public void updateOrbitLine(Vector3f currentPosition) {
        orbitPoints.add(currentPosition.clone());

        float[] positions = new float[orbitPoints.size() * 3];
        for (int i = 0; i < orbitPoints.size(); i++) {
            Vector3f p = orbitPoints.get(i);
            positions[i * 3] = p.x;
            positions[i * 3 + 1] = p.y;
            positions[i * 3 + 2] = p.z;
        }

        orbitMesh.setBuffer(VertexBuffer.Type.Position, 3, positions);
        orbitMesh.updateBound();
        orbitMesh.updateCounts();
    }

}
