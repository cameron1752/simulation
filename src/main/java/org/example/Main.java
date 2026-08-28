package org.example;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

import java.util.concurrent.BrokenBarrierException;

public class Main extends SimpleApplication {
    public static void main(String[] args) {

        Main app = new Main();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("My Awesome Game");
        app.setSettings(settings);

        app.start();

    }

    private Geometry player;
    private Geometry second_player;
    double x_vel = 1.0;
    float y_vel = (float) 1.0;
    float g = (float) -9.8;

    long startTime;

    @Override
    public void simpleInitApp() {

        Box b = new Box(1, 1, 1);
        player = new Geometry("Box", b);

        Box b_2 = new Box(1, 1, 1);
        second_player = new Geometry("Box 2", b_2);


        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);

        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Red);

        player.setMaterial(mat);
        second_player.setMaterial(mat2);

//        rootNode.attachChild(player);
        rootNode.attachChild(second_player);

        startTime = System.nanoTime();
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
        player.rotate(0, 2*tpf, 0);
//        second_player.rotate(0, 2*tpf, 0);
//        double x_vel = 0.0;
        Vector3f pos = second_player.getLocalTranslation();

        // if we hit five turn it around, if we hit 0 turn it around
        if (pos.x >= 5){
            x_vel = -2;
        }

        if (pos.x <= -5){
            x_vel = 2;
        }

        if (pos.y <= -10){
            y_vel = 0;
        }


//        second_player.move((float) (x_vel*tpf), 0, 0);
//        second_player.rotate( 0, 0, (float)x_vel*tpf);

        second_player.move( 0, getVelocity(), 0);

        System.out.println(second_player.getLocalTranslation());
    }

    private float getVelocity(){
        long elapsedTime = (long) ((System.nanoTime() - startTime) / 1_000_000_000.0);
        return g * elapsedTime * y_vel;
    }
}