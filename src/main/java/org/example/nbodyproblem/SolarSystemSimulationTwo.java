package org.example.nbodyproblem;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SolarSystemSimulationTwo extends SimpleApplication {

    static AppSettings settings = new AppSettings(true);


    float g = 1f;
    float timeScale = 10f;
    private boolean paused = false;
    Sun star;
    float cameraHeight = 1000f;
    BitmapText debugText;
    private Node planetPreviewNode;
    private Camera previewCamera;
    private ViewPort previewViewPort;
    boolean zoomOn;
    Vector3f starPosition;
    private List<Body> bodies = new ArrayList<>();
    Scenario scenario;

    public static void main(String[] args) {

        SolarSystemSimulationTwo app = new SolarSystemSimulationTwo();

        settings.setTitle("My Awesome Game");
        settings.setResolution(2560, 1440);
        settings.setFullscreen(true);
        app.setSettings(settings);

        app.start();

    }
    @Override
    public void simpleInitApp() {
        scenario = new Scenario(this.assetManager, g);


        try {
            initCamera();
            setTopDownView();
            initPlanets();
            setupPauseKey();
            initDebugText();
            initPlanetPreview();
            initLighting();
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }



    }


    public void initLighting(){
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.3f));
        rootNode.addLight(ambient);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);
    }


    public void initPlanets() throws InvocationTargetException, IllegalAccessException {

//        bodies = scenario.getRandomScenario();
        bodies = scenario.getScenario(5);

        star = scenario.star;

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
        cam.setFrustumFar(10000f);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!paused) {
            for (Body b : bodies) {
                b.updateVelocity(bodies, tpf * timeScale);
            }

            for (Body b : bodies) {
                b.updatePosition(tpf * timeScale);
                b.g.rotate(0,.01f, 0);
            }
        }



        if (!zoomOn){
            starPosition = star.g.getLocalTranslation();
            cam.setLocation(new Vector3f(
                    starPosition.x,
                    starPosition.y + cameraHeight,
                    starPosition.z
            ));
            cam.lookAt(starPosition, Vector3f.UNIT_Z);
        }

        planetPreviewNode.rotate(0, .01f, 0);
        planetPreviewNode.updateGeometricState();
    }

    // default to look at star
    // on zoom in I want to zoom on mouse
    // do not want it to follow the mouse

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

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.3f));
        planetPreviewNode.addLight(ambient);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        fpp.addFilter(bloom);
        previewViewPort.addProcessor(fpp);

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

        Material previewMaterial = body.material;

//        previewMaterial.setColor("Color", body.color);
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
    private Vector3f zoomTarget = null;
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
                    zoomOn = true;

                    if (zoomTarget == null) {
                        Vector2f cursor2d = inputManager.getCursorPosition();
                        Vector3f nearPoint = cam.getWorldCoordinates(cursor2d, 0f);
                        Vector3f farPoint  = cam.getWorldCoordinates(cursor2d, 1f);
                        Vector3f rayDir = farPoint.subtract(nearPoint).normalizeLocal();

                        Ray ray = new Ray(nearPoint, rayDir);
                        CollisionResults results = new CollisionResults();
                        rootNode.collideWith(ray, results);

                        if (results.size() > 0) {
                            // hit an actual body — zoom toward that point
                            zoomTarget = results.getClosestCollision().getContactPoint();
                        } else {
                            // nothing under cursor — fall back to a point along the ray
                            // at the same distance as your current view target
                            float fallbackDistance = cam.getLocation().distance(starPosition);
                            zoomTarget = nearPoint.add(rayDir.mult(fallbackDistance));
                        }
                    }

                    Vector3f camPos = cam.getLocation();
                    Vector3f toTarget = zoomTarget.subtract(camPos);
                    float distance = toTarget.length();
                    System.out.println("zoomTarget " + zoomTarget);
                    System.out.println("distance " + distance);
                    float minZoomDistance = 5f;
                    if (distance > minZoomDistance) {
                        Vector3f step = toTarget.normalize().mult(100f);
                        cam.setLocation(camPos.add(step));
                    }
                }

                if (name.equals("ZoomOut")) {
                    cameraHeight += 100f;
                    zoomOn = false;
                    zoomTarget = null;
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
                    planetPreviewNode.detachAllChildren();
                    planetPreviewNode.updateGeometricState();
                    previewViewPort.setEnabled(false);
                    debugText.setText("");
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
