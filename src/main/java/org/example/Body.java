package org.example;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Body {
    String name;
    float mass;
    float radius;
    AssetManager assetManager;
    ColorRGBA color;
    Material material;
    Geometry g;
    List<Body> moons;
    float g_constant;
    private Geometry orbitLine;
    private Mesh orbitMesh;
    private List<Vector3f> orbitPoints = new ArrayList<>();
    boolean isStar = false;
    float original_position;
    Vector3f currentVelocity;
    PlanetTextureGenerator generator;
    PlanetType[] types = PlanetType.values();
    PlanetType randomType;

    public Body(String name, float mass, float radius, Vector3f initialPosition, AssetManager assetManager, float g_constant, boolean isStar){
        this.name = name;
        this.mass = mass;
        this.radius = radius;
        this.assetManager = assetManager;
        this.g_constant = g_constant;
        this.isStar = isStar;

        // initialize moon list
        moons = new ArrayList<>();

        // initialize generator
        generator = new PlanetTextureGenerator(assetManager);

        long seed = (long) (Math.random() * Long.MAX_VALUE);

        if ("org.example.Sun".equals(this.getClass().getName())){
            randomType = PlanetType.STAR;
        } else if ("org.example.Moon".equals(this.getClass().getName())){
            randomType = PlanetType.MOON;
        }else {
            randomType = types[(int) (Math.random() * (types.length - 2))];
        }

        material = generator.generatePlanetMaterial(randomType, seed);

        // initialize geometry object
        Sphere s = new Sphere(25, 25, radius);
        g = new Geometry("Object", s);

        // set color to geometry
        g.setMaterial(material);

        g.setLocalTranslation(initialPosition);
        original_position = initialPosition.x;
    }

    public void addMoon(Body moon){
        moons.add(moon);
    }

    public Geometry getGeometry(){
        return g;
    }


    public void updateVelocity(List<Body> bodies, float tpf){

        for (Body b : bodies){
            if (b != this){
                Vector3f direction = b.g.getLocalTranslation()
                        .subtract(this.g.getLocalTranslation());

                float distance = direction.length();

                direction.normalizeLocal();

                float acceleration = g_constant * b.mass
                        / (distance * distance);

                currentVelocity.addLocal(
                        direction.mult(acceleration * tpf)
                );

            }
        }


    }

    public void updatePosition(float tpf){
        Vector3f position = g.getLocalTranslation();
        position = new Vector3f(
                position.x + (currentVelocity.x * tpf),
                position.y + (currentVelocity.y * tpf),
                position.z + (currentVelocity.z * tpf)
        );
        g.setLocalTranslation(position);

        if (original_position == position.x){
            System.out.println("one orbit");
        }

        updateOrbitLine(g.getLocalTranslation());
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

        if (orbitPoints.size() > 3000){
            orbitPoints.remove(0);
        }

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

    @Override
    public String toString(){
        // object name
        // moons?
        StringBuilder sb = new StringBuilder();

        sb.append("---------------------------------").append("\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Planet Type: ").append(randomType).append("\n");
        sb.append("Mass: ").append(mass).append("\n");
        sb.append("Radius: ").append(radius).append("\n");
        sb.append("Color: ").append(color).append("\n");
        sb.append("Current Position: ").append(g.getLocalTranslation()).append("\n");
        sb.append("Current Velocity: ").append(currentVelocity).append("\n");

        if (!moons.isEmpty()){
            sb.append("Moons: ").append("\n");
            for (Body m : moons){
                sb.append(m.toString()).append("\n");
            }
        } else {
            sb.append("---------------------------------").append("\n");
        }
        return sb.toString();
    }

}
