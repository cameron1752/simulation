package org.example.ai;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

public class RocketSimulation extends SimpleApplication {
    static AppSettings settings = new AppSettings(true);

    private static final int lifeSpan = 200;
    private static final int children = 10000;
    private static final Vector3f initialPosition = new Vector3f(0, 250, 0);
    private Population population;
    private int lifeCount = 0;
    BitmapText debugText;
    Material material;
    private boolean hasHit = false;
    public static void main(String[] args) {

        RocketSimulation app = new RocketSimulation();

        settings.setTitle("My Awesome Game");
        settings.setResolution(1920, 1080);
//        settings.setResolution(1200, 800);
//        settings.setFullscreen(true);
        app.setSettings(settings);

        app.start();

    }

    @Override
    public void simpleInitApp() {
        material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.randomColor());

        initCamera();
        setTarget();
        initDebugText();


        population = new Population(children, initialPosition, material);
        population.attachChildren(rootNode);

    }

    private void setTarget(){
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.LightGray);
        Sphere s = new Sphere(25, 25, 3);
        Geometry g = new Geometry("Object", s);
        // set color to geometry
        g.setMaterial(material);
        g.setLocalTranslation(initialPosition);
        rootNode.attachChild(g);
    }

    public void initCamera() {
        Vector3f origin = new Vector3f(0, 0, 0);

        float midY = (origin.y + initialPosition.y) / 2f; // center between the two points
        float distance = 500f; // pull back far enough to fit both in view — tune this

        cam.setLocation(new Vector3f(0, midY, distance));
        cam.lookAt(new Vector3f(0, midY, 0), Vector3f.UNIT_Y);

        flyCam.setEnabled(false);
    }

    private void initDebugText() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        debugText = new BitmapText(guiFont);
        debugText.setSize(guiFont.getCharSet().getRenderedSize());

        debugText.setLocalTranslation(10, settings.getHeight() - 10, 0);

        guiNode.attachChild(debugText);
    }

    @Override
    public void simpleUpdate(float tpf){

//        if (!hasHit){
            if (lifeCount < lifeSpan){
                hasHit = population.update();
                lifeCount++;
            } else {
                System.out.println("Resetting population");
                lifeCount = 0;
                population.fitness();
                population.selection();
                material.setColor("Color", ColorRGBA.randomColor());
                population.setMaterial(material);
                population.detachChildren(rootNode);
                population.reproduction();
                population.attachChildren(rootNode);
            }
//        }


        debugText.setText("Generation #: " + population.getGenerations()
                + "\n" + "Remaining Cycles: " + (lifeSpan - lifeCount)
                + "\n" + "Direct Hits: " + population.hitList
                + "\n" + "Max Hits: " + population.maxHit
                + "\n" + "Children: " + children);
    }
}
