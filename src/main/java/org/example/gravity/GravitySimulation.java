package org.example.gravity;

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
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

public class GravitySimulation extends SimpleApplication {

    static AppSettings settings = new AppSettings(true);
    public static void main(String[] args) {

        GravitySimulation app = new GravitySimulation();

        settings.setTitle("My Awesome Game");
        settings.setResolution(1280, 720);
        app.setSettings(settings);

        app.start();

    }

    boolean fallOutOfBox = false;
    float wallHeight = 5f;
    float wallLength = 10f;
    private Container[][] objects;
    int count = 1;
    int spacing = 2;
    int rows = 0;
    int cols = 0;
    private Boolean isRunning = true;
    private BitmapText debugText;
    private boolean useDebug = true;
    float sphereRadius = 1.0f;
    @Override
    public void simpleInitApp() {
        getGrid();
        computeSphereRadius();
        initCamera();
        initObjects();
        initDebugText();
        initKeys();
//        initFloor();

        initCustomFloor();
        for (int x = 0; x < rows; x++){
            for (int y = 0; y < cols; y++){
                rootNode.attachChild(objects[x][y].object);
            }
        }
    }

    public void initCamera(){
        cam.setLocation(new Vector3f(0, 10f, -30f));
        cam.lookAt(new Vector3f(0, 5f, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(50f);
//        float spanX = (rows - 1) * spacing;
//        float spanZ = (cols - 1) * spacing;
//        float centerX = spanX / 2f;
//        float centerZ = spanZ / 2f;
//
//        float topY = 40f; // height above the arena — tune based on spanX/spanZ
//
//        cam.setLocation(new Vector3f(centerX, topY, centerZ));
//        cam.lookAt(new Vector3f(centerX, 0f, centerZ), Vector3f.UNIT_Z);
    }

    public void reset(){
        initCamera();

        for (int x = 0; x < rows; x++){
            for (int y = 0; y < cols; y++){
                rootNode.detachChild(objects[x][y].object);
            }
        }

        objects = new Container[rows][cols];
        initObjects();

        for (int x = 0; x < rows; x++){
            for (int y = 0; y < cols; y++){
                rootNode.attachChild(objects[x][y].object);
            }
        }
    }

    private void initDebugText(){
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        debugText = new BitmapText(guiFont);
        debugText.setSize(guiFont.getCharSet().getRenderedSize());

        debugText.setText("HELLO FROM DEBUG TEXT");


        debugText.setLocalTranslation(10, settings.getHeight() - 10, 0);

        guiNode.attachChild(debugText);
    }

    private void initKeys() {
        inputManager.addMapping(
                "Reset",
                new KeyTrigger(KeyInput.KEY_R)
        );
        inputManager.addMapping(
                "Debug",
                new KeyTrigger(KeyInput.KEY_T)
        );

        inputManager.addListener(actionListener, "Debug");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {

            if (name.equals("Reset") && isPressed) {
                reset();
            }
            if (name.equals("Debug") && isPressed) {
                debugText.setText("");
                useDebug = !useDebug;
            }
        }
    };

    private void computeSphereRadius(){
        float maxSpan = wallLength * 2f;

        // spacing between adjacent centers along the more constrained axis
        float spacingX = maxSpan / Math.max(rows - 1, 1);
        float spacingZ = maxSpan / Math.max(cols - 1, 1);
        float tightestSpacing = Math.min(spacingX, spacingZ);

        // leave a little breathing room so spheres don't spawn touching
        float packingFactor = 0.9f; // <1 = gap between spheres, 1 = touching exactly
        sphereRadius = Math.min((tightestSpacing / 2f) * packingFactor, sphereRadius);
    }

    private void initCustomFloor(){
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.LightGray);

        Material material1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material1.setColor("Color", ColorRGBA.Red);

        Material material2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material2.setColor("Color", ColorRGBA.Blue);


        Material materialFloor = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialFloor.setColor("Color", ColorRGBA.DarkGray);

        Material materialTest = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialTest.setColor("Color", ColorRGBA.Black);

        // floor
        Box floorBox = new Box(wallLength, .1f, wallLength);
        Geometry floor = new Geometry("floor", floorBox);
        floor.setLocalTranslation(0, -wallHeight - 0.1f, 0);
        floor.setMaterial(materialFloor);

        // left wall
        Box lWallBox = new Box(.1f, wallHeight, wallLength);
        Geometry lWall = new Geometry("Left Wall", lWallBox);
        lWall.setLocalTranslation(-wallLength, 0, 0);
        lWall.setMaterial(material);
//
        // right wall
        Box rWallBox = new Box(.1f, wallHeight, wallLength);
        Geometry rWall = new Geometry("Right Wall", rWallBox);
        rWall.setLocalTranslation(wallLength, 0, 0);
        rWall.setMaterial(material1);
//
        // back wall
        Box backWallBox = new Box(wallLength, wallHeight, .1f);
        Geometry bWall = new Geometry("Back Wall", backWallBox);
        bWall.setLocalTranslation(0, 0, wallLength);
        bWall.setMaterial(material2);

        // test object
        Box centerBox = new Box(.1f, wallHeight, .1f);
        Geometry center = new Geometry("center", centerBox);
        center.setLocalTranslation(0, 0, 0);
        center.setMaterial(materialTest);

        // attach
        rootNode.attachChild(center);
        rootNode.attachChild(bWall);
        rootNode.attachChild(rWall);
        rootNode.attachChild(lWall);
        rootNode.attachChild(floor);
    }

    private void initObjects(){
        float spanX = (rows - 1) * spacing;
        float spanZ = (cols - 1) * spacing;

        for (int x = 0; x < rows; x++){
            for (int y = 0; y < cols; y++){
                Sphere s = new Sphere(25, 25, sphereRadius);
                Geometry g = new Geometry("Object " + x + "_" + y, s);

//                int randomHeight = ThreadLocalRandom.current().nextInt(15, 30 + 1);
                int randomHeight = 20;
                float posX = (x * spacing) - (spanX / 2f);
                float posZ = (y * spacing) - (spanZ / 2f);

                Container c = new Container(g, new Vector3f(posX, randomHeight, posZ));

                c.y_pos = randomHeight;

                g.setLocalTranslation(c.position);
                c.grid_x = x;
                c.grid_y = y;

                c.leftThreshold = - (wallLength) + sphereRadius;
                c.rightThreshold =  (wallLength) - sphereRadius;
                c.frontThreshold = -(wallLength) + sphereRadius;
                c.backThreshold =   (wallLength) - sphereRadius;
                c.sphere_radius = sphereRadius;
                c.fallOutOfBox = fallOutOfBox;
                c.ground = -(wallHeight) + sphereRadius;
                c.sphere_radius = sphereRadius;
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", ColorRGBA.randomColor());
                g.setMaterial(mat);
                objects[x][y] = c;
            }
        }
        System.out.println(count + " objects created");
    }

    private void getGrid(){
        rows = (int) Math.sqrt(count);
        cols = (int) Math.ceil((double) count / rows);
        System.out.println(rows + " x " + cols);
        objects = new Container[rows][cols];
    }


    @Override
    public void simpleUpdate(float tpf) {
        if (isRunning){
            for (int x = 0; x < rows; x++){
                for (int y = 0; y < cols; y++){
                    objects[x][y].update(tpf, objects);
                }
            }

            Vector3f camDir = cam.getDirection();
            Vector3f camLoc = cam.getLocation();

            if (useDebug){
                debugText.setText(
                        String.format(
                                "Position: %.2f\nVelocity: %.2f\nTpf: %.4f\nObjects: %d\n" +
                                        "Cam Loc: (%.2f, %.2f, %.2f)\nCam Dir: (%.2f, %.2f, %.2f)\n" +
                                        "Wall Height: %.2f\nWall Length: %.2f\nSpacing: %d\nRows: %d\nCols: %d",
                                objects[0][0].position.y,
                                objects[0][0].cur_y_vel,
                                tpf,
                                count,
                                camLoc.x, camLoc.y, camLoc.z,
                                camDir.x, camDir.y, camDir.z,
                                wallHeight, wallLength, spacing, rows, cols
                        )
                );
            }
        }
    }


}
