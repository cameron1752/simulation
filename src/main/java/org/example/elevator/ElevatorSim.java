package org.example.elevator;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class ElevatorSim extends SimpleApplication {
    static AppSettings settings = new AppSettings(true);
    private final float wallLength = 5f;
    private final float wallHeight = 10f;
    private final int floors = 20;
    private final int offset = 20;
    private final int elevators = 6;
    private ElevatorManager elevatorManager;
    private BitmapText debugText;
    private PeopleManager peopleManager;
    private List<String> metaAnalysis = new ArrayList<>();
    private int count = 0;
    private final static int threshold = 3;

    public static void main(String[] args) {

        ElevatorSim app = new ElevatorSim();

        settings.setTitle("My Awesome Game");
        settings.setResolution(1920, 1080);
//        settings.setResolution(1200, 800);
//        settings.setFullscreen(true);
        app.setSettings(settings);

        app.start();

    }

    @Override
    public void simpleInitApp() {
        initCamera();
        initCallKeys();
        initDebugText();

        elevatorManager = new ElevatorManager(floors);
        peopleManager = new PeopleManager(elevatorManager);


        for (int x = 0; x < elevators; x++) {
            initShaft(x * offset);
            Elevator elevator = new Elevator(assetManager, wallLength, wallHeight, floors, x * offset, x);
            elevatorManager.addElevator(elevator);
            elevator.attachTo(rootNode);
        }

    }

    private void resetPeople() {
        System.out.println("Resetting people");
        peopleManager.shutdown();
        peopleManager = new PeopleManager(elevatorManager);
    }

    @Override
    public void destroy(){
        peopleManager.shutdown();
        elevatorManager.shutdown();
        System.out.println(metaAnalysis);
        super.destroy();
    }

    public void initCamera() {
        cam.setLocation(new Vector3f(offset * ((float) elevators / 2), 10f, -500f));
        cam.lookAt(new Vector3f(offset * ((float) elevators / 2), 0f, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(50f);
        flyCam.setEnabled(true);
    }

    private void initShaft(int offset) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.LightGray);

        Material materialFloor = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialFloor.setColor("Color", ColorRGBA.DarkGray);

        // left wall
        Box lWallBox = new Box(.1f, (wallHeight * floors) + wallHeight, wallLength);
        Geometry lWall = new Geometry("Left Wall", lWallBox);
        lWall.setLocalTranslation(-wallLength - .1f + offset, 0, 0);
        lWall.setMaterial(material);
//
        // right wall
        Box rWallBox = new Box(.1f, (wallHeight * floors) + wallHeight, wallLength);
        Geometry rWall = new Geometry("Right Wall", rWallBox);
        rWall.setLocalTranslation(wallLength + .1f + offset, 0, 0);
        rWall.setMaterial(material);
//
        // back wall
        Box backWallBox = new Box(wallLength, (wallHeight * floors) + wallHeight, .1f);
        Geometry bWall = new Geometry("Back Wall", backWallBox);
        bWall.setLocalTranslation(offset, 0, wallLength + .1f);
        bWall.setMaterial(material);


        for (int x = 0; x <= (floors); x = x + 2) {
            // floor
            Box floorBox = new Box(wallLength, .1f, wallLength);
            Geometry floor = new Geometry("floor: " + -x, floorBox);
            floor.setLocalTranslation(offset, -(wallHeight * x) - (wallHeight), 0);
            floor.setMaterial(materialFloor);
            rootNode.attachChild(floor);

            floorBox = new Box(wallLength, .1f, wallLength);
            floor = new Geometry("floor: " + x, floorBox);
            floor.setLocalTranslation(offset, (wallHeight * x) + (wallHeight), 0);
            floor.setMaterial(materialFloor);
            rootNode.attachChild(floor);
        }

        rootNode.attachChild(lWall);
        rootNode.attachChild(rWall);
        rootNode.attachChild(bWall);
    }

    @Override
    public void simpleUpdate(float tpf) {
        elevatorManager.update(1);
        peopleManager.update();

        if (peopleManager.getPeople().size() < 2 && count < threshold){
            metaAnalysis.add(peopleManager.toString());
            resetPeople();
            count++;
        }

        if (count > threshold){
            destroy();
        }

        debugText.setText(elevatorManager.getInfo());
    }

    private void initDebugText() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        debugText = new BitmapText(guiFont);
        debugText.setSize(guiFont.getCharSet().getRenderedSize());

        debugText.setLocalTranslation(10, settings.getHeight() - 10, 0);

        guiNode.attachChild(debugText);
    }

    private void initCallKeys() {
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R));

        inputManager.addMapping("Call_0", new KeyTrigger(KeyInput.KEY_0));
        inputManager.addMapping("Call_1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("Call_2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("Call_3", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping("Call_4", new KeyTrigger(KeyInput.KEY_4));
        inputManager.addMapping("Call_5", new KeyTrigger(KeyInput.KEY_5));
        inputManager.addMapping("Call_6", new KeyTrigger(KeyInput.KEY_6));
        inputManager.addMapping("Call_7", new KeyTrigger(KeyInput.KEY_7));
        inputManager.addMapping("Call_8", new KeyTrigger(KeyInput.KEY_8));
        inputManager.addMapping("Call_9", new KeyTrigger(KeyInput.KEY_9));

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Reset") && isPressed) {
                    resetPeople();
                }
            }
        }, "Reset");

        inputManager.addListener(new ActionListener() {
             @Override
             public void onAction(String name, boolean isPressed, float tpf) {
                 if (isPressed && name.startsWith("Call_")) {
                     int value = Integer.parseInt(name.substring("Call_".length()));
                     elevatorManager.sendClosest(1, value);
                 }
             }
                 }, "Call_0", "Call_1", "Call_2", "Call_3", "Call_4",
                "Call_5", "Call_6", "Call_7", "Call_8", "Call_9");
    }
}
