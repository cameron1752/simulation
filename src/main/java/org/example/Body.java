package org.example;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Body {
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
//    final float g_constant = 0.000000000066740f;
    float g_constant = 1f;
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

    Vector3f currentVelocity;

    public Body( float mass, float radius, Vector3f initialPosition, AssetManager assetManager, float g_constant){
        this.mass = mass;
        this.radius = radius;
        this.assetManager = assetManager;
        this.g_constant = g_constant;

        material = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.randomColor());

        Sphere s = new Sphere(25, 25, radius);
        g = new Geometry("Object", s);
        g.setMaterial(material);

        g.setLocalTranslation(initialPosition);


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
        updateOrbitLine(g.getLocalTranslation());
    }
    private float getMagnitude(Vector3f vector){
        return (float) Math.sqrt((vector.x * vector.x) + (vector.y * vector.y) + (vector.z * vector.z));
    }


    public Vector3f subtract(Vector3f v1, Vector3f v2){
        return new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
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
