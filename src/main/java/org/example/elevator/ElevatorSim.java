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

public class ElevatorSim extends SimpleApplication {
    static AppSettings settings = new AppSettings(true);
    private final float wallLength = 5f;
    private final float wallHeight = 10f;
    private final int floors = 10;
    private final int offset = 20;
    private final int elevators = 2;
    private ElevatorManager elevatorManager;
    private BitmapText debugText;
    private PeopleManager peopleManager;
    public static void main(String[] args) {

        ElevatorSim app = new ElevatorSim();

        settings.setTitle("My Awesome Game");
        settings.setResolution(1920, 1080);
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


        for (int x = 0; x < elevators; x++){
            initShaft(x * offset);
            Elevator elevator = new Elevator(assetManager, wallLength, wallHeight, floors, x * offset);
            elevatorManager.addElevator(elevator);
            rootNode.attachChild(elevator.getElevator());
        }

    }

    public void initCamera(){
        cam.setLocation(new Vector3f(offset * ((float) elevators / 2), 10f, -200f));
        cam.lookAt(new Vector3f(offset * ((float) elevators / 2), 0f, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(50f);
        flyCam.setEnabled(true);
    }

    private void initShaft(int offset){
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.LightGray);

        Material materialFloor = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialFloor.setColor("Color", ColorRGBA.DarkGray);

        // left wall
        Box lWallBox = new Box(.1f, wallHeight * floors, wallLength);
        Geometry lWall = new Geometry("Left Wall", lWallBox);
        lWall.setLocalTranslation(-wallLength - .1f + offset, 0, 0);
        lWall.setMaterial(material);
//
        // right wall
        Box rWallBox = new Box(.1f, wallHeight * floors, wallLength);
        Geometry rWall = new Geometry("Right Wall", rWallBox);
        rWall.setLocalTranslation(wallLength + .1f + offset, 0, 0);
        rWall.setMaterial(material);
//
        // back wall
        Box backWallBox = new Box(wallLength, wallHeight * floors, .1f);
        Geometry bWall = new Geometry("Back Wall", backWallBox);
        bWall.setLocalTranslation(offset, 0, wallLength + .1f);
        bWall.setMaterial(material);
        

        for (int x = 1; x <= (floors); x = x + 2){
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

        // floor
        Box floorBox = new Box(wallLength, .1f, wallLength);
        Geometry floor = new Geometry("floor", floorBox);
        floor.setLocalTranslation(offset, 0, 0);
        floor.setMaterial(materialFloor);


        rootNode.attachChild(floor);
        rootNode.attachChild(lWall);
        rootNode.attachChild(rWall);
        rootNode.attachChild(bWall);
    }

    @Override
    public void simpleUpdate(float tpf) {
        elevatorManager.update(1);
        peopleManager.update();
        debugText.setText(elevatorManager.getInfo() + peopleManager.toString());
    }

    private void initDebugText(){
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        debugText = new BitmapText(guiFont);
        debugText.setSize(guiFont.getCharSet().getRenderedSize());


        debugText.setLocalTranslation(10, settings.getHeight() - 10, 0);

        guiNode.attachChild(debugText);
    }

    private void initCallKeys(){
        inputManager.addMapping("Call floor 0", new KeyTrigger(KeyInput.KEY_0));
        inputManager.addMapping("Call floor 1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("Call floor 2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("Call floor 3", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping("Call floor 4", new KeyTrigger(KeyInput.KEY_4));
        inputManager.addMapping("Call floor 5", new KeyTrigger(KeyInput.KEY_5));
        inputManager.addMapping("Call floor 6", new KeyTrigger(KeyInput.KEY_6));
        inputManager.addMapping("Call floor 7", new KeyTrigger(KeyInput.KEY_7));
        inputManager.addMapping("Call floor 8", new KeyTrigger(KeyInput.KEY_8));
        inputManager.addMapping("Call floor 9", new KeyTrigger(KeyInput.KEY_9));


        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 0") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(9);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 0");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 1") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(0);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 1");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 2") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(1);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 2");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 3") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(2);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 3");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 4") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(3);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 4");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 5") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(4);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 5");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 6") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(5);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 6");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 7") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(6);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 7");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 8") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(7);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 8");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Call floor 9") && isPressed) {
                    System.out.println(name);
                    elevatorManager.sendClosest(8);
                    //elevatorManager.sendClosest(0);
                }
            }
        }, "Call floor 9");

    }
}
