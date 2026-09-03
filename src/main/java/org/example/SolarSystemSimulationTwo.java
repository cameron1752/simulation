package org.example;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class SolarSystemSimulationTwo extends SimpleApplication {

    static AppSettings settings = new AppSettings(true);


    float g = 1f;
    float timeScale = 10f;
    private boolean paused = false;
    Body star;
    float cameraHeight = 1000f;
    BitmapText debugText;

    private List<Body> bodies = new ArrayList<>();
    public static void main(String[] args) {

        SolarSystemSimulationTwo app = new SolarSystemSimulationTwo();

        settings.setTitle("My Awesome Game");
        settings.setResolution(1920, 1080);
        app.setSettings(settings);

        app.start();

    }
    @Override
    public void simpleInitApp() {
        initCamera();
        setTopDownView();
        initPlanets();
        setupPauseKey();
        initDebugText();
        initPlanetPreview();
    }

    private Node planetPreviewNode;
    private Camera previewCamera;
    private ViewPort previewViewPort;

    private Body scenario1(){
        Body star = new Body("star", 1000, 6f, new Vector3f(0, 0, 0), assetManager, g, ColorRGBA.randomColor());

        Body planet_1 = new Body("planet_1", 10f, 3f, new Vector3f(80, 0, 0), assetManager, g, ColorRGBA.randomColor());
        Body moon_1 = new Body("moon_1", .0002f, 2f, new Vector3f(86, 0, 0), assetManager, g, ColorRGBA.randomColor());
        Body moon_2 = new Body("moon_2", .0001f, 1f, new Vector3f(74, 0, 0), assetManager, g, ColorRGBA.randomColor());

        Body planet_4 = new Body("planet_4", 18f, 3f, new Vector3f(-60, 0, 0), assetManager, g, ColorRGBA.randomColor());

        planet_1.addMoon(moon_1);
        planet_1.addMoon(moon_2);

        bodies.add(planet_1);
        bodies.add(moon_1);
        bodies.add(moon_2);
        bodies.add(planet_4);
        bodies.add(star);

        return star;
    }

    private Body scenario2(){
        Body star = new Body("star", 1000, 6f, new Vector3f(0, 0, 0), assetManager, g, ColorRGBA.randomColor());

        Body planet_1 = new Body("planet_1", 10f, 3f, new Vector3f(80, 0, 0), assetManager, g, ColorRGBA.randomColor());
        Body moon_1 = new Body("moon_1", .0002f, 2f, new Vector3f(84, 0, 0), assetManager, g, ColorRGBA.randomColor());
        Body moon_2 = new Body("moon_2", .0001f, 1f, new Vector3f(76, 0, 0), assetManager, g, ColorRGBA.randomColor());

        Body planet_4 = new Body("planet_4", 12f, 3f, new Vector3f(-60, 0, 0), assetManager, g, ColorRGBA.randomColor());
        Body moon_3 = new Body("moon_3", .0001f, 1f, new Vector3f(-62, 0, 0), assetManager, g, ColorRGBA.randomColor());

        planet_1.addMoon(moon_1);
        planet_1.addMoon(moon_2);
        planet_4.addMoon(moon_3);

        bodies.add(planet_1);
        bodies.add(moon_1);
        bodies.add(moon_2);
        bodies.add(planet_4);
        bodies.add(moon_3);
        bodies.add(star);

        return star;
    }

    private Body scenario3(){
        Body star = new Body("star", 1000000, 6f, new Vector3f(0, 0, 0), assetManager, g, ColorRGBA.Yellow);

        Body mercury = new Body("mercury", .055f, 2f, new Vector3f(19.35f, 0, 33.51f), assetManager, g, ColorRGBA.Gray);
        Body venus = new Body("venus", .815f, 2.5f, new Vector3f(-36.15f, 0, 62.61f), assetManager, g, new ColorRGBA(0.9f, 0.7f, 0.3f, 1f));

        Body earth = new Body("earth", 1f, 3f, new Vector3f(0, 0, 100), assetManager, g, ColorRGBA.Blue);
        Body moon = new Body("moon", .0123f, 1.5f, new Vector3f(0.26f, 0, 100), assetManager, g, ColorRGBA.LightGray);

        Body mars = new Body("mars", .107f, 2.5f, new Vector3f(-107.48f, 0, -107.48f), assetManager, g, ColorRGBA.Red);
        Body phobos = new Body("phobos", .0000017f, 1f, new Vector3f(-107.477f, 0, -107.48f), assetManager, g, ColorRGBA.DarkGray);
        Body deimos = new Body("deimos", .00000025f, 1f, new Vector3f(-107.472f, 0, -107.48f), assetManager, g, ColorRGBA.Gray);

        Body jupiter = new Body("jupiter", 317.8f, 5f, new Vector3f(0, 0, 520), assetManager, g, new ColorRGBA(0.8f, 0.5f, 0.25f, 1f));
        Body io = new Body("io", .015f, 1f, new Vector3f(0.022f, 0, 520), assetManager, g, ColorRGBA.Yellow);
        Body europa = new Body("europa", .008f, 1f, new Vector3f(0.035f, 0, 520), assetManager, g, ColorRGBA.White);
        Body ganymede = new Body("ganymede", .025f, 1.2f, new Vector3f(0.056f, 0, 520), assetManager, g, ColorRGBA.Gray);
        Body callisto = new Body("callisto", .018f, 1f, new Vector3f(0.098f, 0, 520), assetManager, g, ColorRGBA.DarkGray);

        Body saturn = new Body("saturn", 95.2f, 4.5f, new Vector3f(673.58f, 0, -673.58f), assetManager, g, new ColorRGBA(0.85f, 0.75f, 0.45f, 1f));
        Body rhea = new Body("rhea", .0004f, 1f, new Vector3f(673.615f, 0, -673.58f), assetManager, g, ColorRGBA.LightGray);
        Body titan = new Body("titan", .0225f, 1.5f, new Vector3f(673.661f, 0, -673.58f), assetManager, g, new ColorRGBA(0.85f, 0.55f, 0.25f, 1f));
        Body iapetus = new Body("iapetus", .00023f, 1f, new Vector3f(673.81f, 0, -673.58f), assetManager, g, ColorRGBA.Gray);

        Body uranus = new Body("uranus", 14.5f, 4f, new Vector3f(-1920, 0, 0), assetManager, g, ColorRGBA.Cyan);
        Body titania = new Body("titania", .000059f, 1f, new Vector3f(-1919.95f, 0, 0), assetManager, g, ColorRGBA.LightGray);
        Body oberon = new Body("oberon", .00005f, 1f, new Vector3f(-1919.925f, 0, 0), assetManager, g, ColorRGBA.Gray);

        Body neptune = new Body("neptune", 17.1f, 4f, new Vector3f(1500, 0, 2598.08f), assetManager, g, new ColorRGBA(0.15f, 0.3f, 0.9f, 1f));
        Body triton = new Body("triton", .00358f, 1f, new Vector3f(1500.031f, 0, 2598.08f), assetManager, g, ColorRGBA.LightGray);

        earth.addMoon(moon);

        mars.addMoon(phobos);
        mars.addMoon(deimos);

        jupiter.addMoon(io);
        jupiter.addMoon(europa);
        jupiter.addMoon(ganymede);
        jupiter.addMoon(callisto);

        saturn.addMoon(rhea);
        saturn.addMoon(titan);
        saturn.addMoon(iapetus);

        uranus.addMoon(titania);
        uranus.addMoon(oberon);

        neptune.addMoon(triton);

        bodies.add(mercury);
        bodies.add(venus);
        bodies.add(earth);
        bodies.add(moon);
        bodies.add(mars);
        bodies.add(phobos);
        bodies.add(deimos);
        bodies.add(jupiter);
        bodies.add(io);
        bodies.add(europa);
        bodies.add(ganymede);
        bodies.add(callisto);
        bodies.add(saturn);
        bodies.add(rhea);
        bodies.add(titan);
        bodies.add(iapetus);
        bodies.add(uranus);
        bodies.add(titania);
        bodies.add(oberon);
        bodies.add(neptune);
        bodies.add(triton);
        bodies.add(star);

        return star;
    }

    private Body scenario4(){
        Body star = new Body("star", 1000000, 6f, new Vector3f(0, 0, 0), assetManager, g, ColorRGBA.Yellow);

        Body mercury = new Body("mercury", .055f, 2f, new Vector3f(38.7f, 0, 0), assetManager, g, ColorRGBA.Gray);
        Body venus = new Body("venus", .815f, 2.5f, new Vector3f(0, 0, 72.3f), assetManager, g, new ColorRGBA(0.9f, 0.7f, 0.3f, 1f));

        Body earth = new Body("earth", 1f, 3f, new Vector3f(-100, 0, 0), assetManager, g, ColorRGBA.Blue);
        Body moon = new Body("moon", .0123f, 1.5f, new Vector3f(-99.74f, 0, 0), assetManager, g, ColorRGBA.LightGray);

        Body mars = new Body("mars", .107f, 2.5f, new Vector3f(0, 0, -152), assetManager, g, ColorRGBA.Red);
        Body phobos = new Body("phobos", .0000017f, 1f, new Vector3f(.003f, 0, -152), assetManager, g, ColorRGBA.DarkGray);
        Body deimos = new Body("deimos", .00000025f, 1f, new Vector3f(.008f, 0, -152), assetManager, g, ColorRGBA.Gray);

        // Jupiter deliberately placed at a different angle
        Body jupiter = new Body("jupiter", 317.8f, 5f, new Vector3f(-368.71f, 0, 368.71f), assetManager, g, new ColorRGBA(0.8f, 0.5f, 0.25f, 1f));
        Body io = new Body("io", .015f, 1f, new Vector3f(-368.688f, 0, 368.71f), assetManager, g, ColorRGBA.Yellow);
        Body europa = new Body("europa", .008f, 1f, new Vector3f(-368.675f, 0, 368.71f), assetManager, g, ColorRGBA.White);
        Body ganymede = new Body("ganymede", .025f, 1.2f, new Vector3f(-368.654f, 0, 368.71f), assetManager, g, ColorRGBA.Gray);
        Body callisto = new Body("callisto", .018f, 1f, new Vector3f(-368.612f, 0, 368.71f), assetManager, g, ColorRGBA.DarkGray);

        Body saturn = new Body("saturn", 95.2f, 4.5f, new Vector3f(673.58f, 0, 673.58f), assetManager, g, new ColorRGBA(0.85f, 0.75f, 0.45f, 1f));
        Body rhea = new Body("rhea", .0004f, 1f, new Vector3f(673.615f, 0, 673.58f), assetManager, g, ColorRGBA.LightGray);
        Body titan = new Body("titan", .0225f, 1.5f, new Vector3f(673.661f, 0, 673.58f), assetManager, g, new ColorRGBA(0.85f, 0.55f, 0.25f, 1f));
        Body iapetus = new Body("iapetus", .00023f, 1f, new Vector3f(673.81f, 0, 673.58f), assetManager, g, ColorRGBA.Gray);

        Body uranus = new Body("uranus", 14.5f, 4f, new Vector3f(-1920, 0, 0), assetManager, g, ColorRGBA.Cyan);
        Body titania = new Body("titania", .000059f, 1f, new Vector3f(-1919.95f, 0, 0), assetManager, g, ColorRGBA.LightGray);
        Body oberon = new Body("oberon", .00005f, 1f, new Vector3f(-1919.925f, 0, 0), assetManager, g, ColorRGBA.Gray);

        Body neptune = new Body("neptune", 17.1f, 4f, new Vector3f(1500, 0, -2598.08f), assetManager, g, new ColorRGBA(0.15f, 0.3f, 0.9f, 1f));
        Body triton = new Body("triton", .00358f, 1f, new Vector3f(1500.031f, 0, -2598.08f), assetManager, g, ColorRGBA.LightGray);



        earth.addMoon(moon);

        mars.addMoon(phobos);
        mars.addMoon(deimos);

        jupiter.addMoon(io);
        jupiter.addMoon(europa);
        jupiter.addMoon(ganymede);
        jupiter.addMoon(callisto);

        saturn.addMoon(rhea);
        saturn.addMoon(titan);
        saturn.addMoon(iapetus);

        uranus.addMoon(titania);
        uranus.addMoon(oberon);

        neptune.addMoon(triton);

        bodies.add(mercury);
        bodies.add(venus);
        bodies.add(earth);
        bodies.add(moon);
        bodies.add(mars);
        bodies.add(phobos);
        bodies.add(deimos);
        bodies.add(jupiter);
        bodies.add(io);
        bodies.add(europa);
        bodies.add(ganymede);
        bodies.add(callisto);
        bodies.add(saturn);
        bodies.add(rhea);
        bodies.add(titan);
        bodies.add(iapetus);
        bodies.add(uranus);
        bodies.add(titania);
        bodies.add(oberon);
        bodies.add(neptune);
        bodies.add(triton);
        bodies.add(star);

        return star;
    }


    public void initPlanets(){
//        star = scenario1();
        star = scenario2();

//        star = scenario3();
//        star = scenario4();

        star.isStar = true;

        for (Body b : bodies){

            Vector3f radius =  b.g.getLocalTranslation().subtract(star.g.getLocalTranslation());

            float distance = radius.length();

            float speed = (float) Math.sqrt(g * star.mass / distance);

            Vector3f tangent = new Vector3f(-radius.z,0,radius.x).normalizeLocal();

            b.currentVelocity = tangent.mult(speed);

        }

        star.currentVelocity = (new Vector3f(0.1f, 0, 0));

        for (Body b : bodies){
            if (!b.moons.isEmpty()){
                for (Body m : b.moons){
                    Vector3f radius =  m.g.getLocalTranslation().subtract(b.g.getLocalTranslation());

                    float distance = radius.length();

                    float speed = (float) Math.sqrt(g * b.mass / distance);

                    Vector3f tangent = new Vector3f(-radius.z,0,radius.x).normalizeLocal();

                    m.currentVelocity = b.currentVelocity.add(tangent.mult(speed));
                }

            }
        }




        for (Body b : bodies){
            rootNode.attachChild(b.getGeometry());
            rootNode.attachChild(b.createOrbitLine());
        }
    }

    private void initDebugText(){
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        debugText = new BitmapText(guiFont);
        debugText.setSize(guiFont.getCharSet().getRenderedSize());


        debugText.setLocalTranslation(10, settings.getHeight() - 10, 0);

        guiNode.attachChild(debugText);
    }

    public void initCamera(){
        cam.setLocation(new Vector3f(0, 10f, -50f));
        cam.lookAt(new Vector3f(0, 5f, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(50f);
        flyCam.setEnabled(false);
    }

    private void setTopDownView() {
        float height = 500f; // high enough to see Pluto's orbit with some margin
        cam.setLocation(new Vector3f(0, height, 0));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z); // look straight down, "up" reference along Z since Y is now forward
        cam.update();
        flyCam.setMoveSpeed(50f);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!paused) {
            for (Body b : bodies) {
                b.updateVelocity(bodies, tpf * timeScale);
            }

            for (Body b : bodies) {
                b.updatePosition(tpf * timeScale);
            }
        }

        Vector3f starPosition = star.g.getLocalTranslation();
        cam.setLocation(new Vector3f(
                starPosition.x,
                starPosition.y + cameraHeight,
                starPosition.z
        ));
        cam.lookAt(starPosition, Vector3f.UNIT_Z);

        planetPreviewNode.updateGeometricState();
    }

    private void initPlanetPreview() {
        planetPreviewNode = new Node("PlanetPreview");

        previewCamera = new Camera(400, 300);

        // Viewport = upper-right portion of the screen
        previewCamera.setViewPort(0.75f, 1.0f, 0.65f, 1.0f);

        // Camera looking straight toward the origin
        previewCamera.setLocation(new Vector3f(0, 0, 20));
        previewCamera.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        // Explicitly define the camera frustum
        previewCamera.setFrustumPerspective(
                45f,
                400f / 300f,
                1f,
                1000f
        );

        previewCamera.update();

        previewViewPort = renderManager.createMainView(
                "PlanetPreview",
                previewCamera
        );

        previewViewPort.setClearFlags(true, true, true);
        previewViewPort.setBackgroundColor(ColorRGBA.DarkGray);

        previewViewPort.attachScene(planetPreviewNode);

        previewViewPort.setEnabled(false);
    }

    private void showPlanetPreview(Body body) {

        planetPreviewNode.detachAllChildren();

        Sphere sphere = new Sphere(25, 25, body.radius);

        Geometry previewPlanet = new Geometry(
                body.name + "_preview",
                sphere
        );

        Material previewMaterial = new Material(
                assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md"
        );

        previewMaterial.setColor("Color", body.color);
        previewPlanet.setMaterial(previewMaterial);

        previewPlanet.setLocalTranslation(Vector3f.ZERO);
        previewPlanet.setLocalScale(1f);

        // Don't let the preview geometry get frustum culled
        previewPlanet.setCullHint(Spatial.CullHint.Never);

        planetPreviewNode.attachChild(previewPlanet);

        // The preview is its own scene graph, so update it manually
        planetPreviewNode.updateGeometricState();

        previewViewPort.setEnabled(true);
    }

    private void removePlanetPreview(){
        planetPreviewNode.detachAllChildren();
        planetPreviewNode.updateGeometricState();
        previewViewPort.setEnabled(false);
        debugText.setText("");
    }

    private void setupPauseKey(){
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Increase Timescale", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Decrease Timescale", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("pick target", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("deselect", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        inputManager.addListener(new AnalogListener() {
            @Override
            public void onAnalog(String name, float value, float tpf) {
                if (name.equals("ZoomIn")) {
                    cameraHeight -= 100f;
                }

                if (name.equals("ZoomOut")) {
                    cameraHeight += 100f;
                }

                cameraHeight = Math.max(100f, Math.min(cameraHeight, 10000f));
            }
        }, "ZoomIn", "ZoomOut");
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Pause") && isPressed) {
                    paused = !paused;
                    System.out.println("Toggling pause: " + paused);
                }
            }
        }, "Pause");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Increase Timescale") && isPressed) {
                    timeScale *= 1.25f;
                    System.out.println("Increased timescale to " + timeScale);
                }
            }
        }, "Increase Timescale");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Decrease Timescale") && isPressed) {
                    timeScale *= .75f;
                    System.out.println("Decrease timescale to " + timeScale);
                }
            }
        }, "Decrease Timescale");

        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("deselect") && isPressed) {
                    removePlanetPreview();
                }
            }
        }, "deselect");

        inputManager.addListener(analogListener, "pick target");

    }

    private ActionListener analogListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("pick target") && isPressed) {
                // Reset results list.
                CollisionResults results = new CollisionResults();
                // Convert screen click to 3d position
                Vector2f click2d = inputManager.getCursorPosition();
                Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
                // Aim the ray from the clicked spot forwards.
                Ray ray = new Ray(click3d, dir);
                // Collect intersections between ray and all nodes in results list.
                rootNode.collideWith(ray, results);
                // Use the results -- we rotate the selected geometry.
                if (results.size() > 0) {
                    //
                    Geometry result = results.getClosestCollision().getGeometry();
                    for (Body b : bodies){
                        if (b.g == result){
//                            enqueue(() -> {
//                                showPlanetPreview(b);
//                                return null;
//                            });
                            showPlanetPreview(b);
                            debugText.setText(b.toString());
                            System.out.println(b.toString());
                        }
                    }
                }
            } // else if ...
        }
    };


}
