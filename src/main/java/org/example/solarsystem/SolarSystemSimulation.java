package org.example.solarsystem;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class SolarSystemSimulation extends SimpleApplication {

    static AppSettings settings = new AppSettings(true);
    Star sun;
    Planet earth;
    Planet mars;
    Planet mercury;
    Planet venus;
    Planet jupiter;
    Planet saturn;
    Planet uranus;
    Planet neptune;
    Planet pluto;

    private List<Vector3f> planetLocations = new ArrayList<>();
    private List<String> planetNames = new ArrayList<>();
    private int currentPlanetIndex = 0;
    private List<Planet> planets = new ArrayList<>();
    public static void main(String[] args) {

        SolarSystemSimulation app = new SolarSystemSimulation();

        settings.setTitle("My Awesome Game");
        settings.setResolution(1920, 1080);
        app.setSettings(settings);

        app.start();

    }
    @Override
    public void simpleInitApp() {
//        initCamera();
        setTopDownView();
        initPlanets();
        setupPlanetLocations();
        setupPlanetCycleKeys();
    }

    public void initPlanets(){
        sun = new Star(0, 5899686011f, 20, assetManager, ColorRGBA.Yellow);
        mercury = new Planet( .2056f, 979.09102f, .35053f, assetManager, ColorRGBA.Brown);
        venus = new Planet(.0068f, 14435.55971f, .86951f, assetManager, ColorRGBA.Orange);
        earth = new Planet(.0167f, 17713.59147f, .9154f, assetManager, ColorRGBA.Blue);
        mars = new Planet(.0934f, 1903.26641f, .4880f, assetManager, ColorRGBA.Red);
        jupiter = new Planet(.0484f, 5629482.705f, 10.04468391f, assetManager, ColorRGBA.Orange);
        saturn = new Planet(.0542f, 1685582.203f, 8.366666667f, assetManager, ColorRGBA.Yellow);
        uranus = new Planet(.0472f, 257479.1326f, 3.643965517f, assetManager, ColorRGBA.Cyan);
        neptune = new Planet(.0086f, 303719.1934f, 3.537643678f, assetManager, ColorRGBA.Blue);
        pluto = new Planet(.2488f, 38.64708095f, 0.1707327586f, assetManager, ColorRGBA.Gray);

        mercury.setPosition(110.03f);
        mercury.setSun(sun);
        System.out.println("Mercury's Period: " + mercury.T);

        venus.setPosition(115.75f);
        venus.setSun(sun);
        System.out.println("Venus's Period: " + venus.T);

        earth.setPosition(121.85f);
        earth.setSun(sun);
        System.out.println("Earth's Period: " + earth.T);

        mars.setPosition(135.80f);
        mars.setSun(sun);
        System.out.println("Mars's Period: " + mars.T);

        jupiter.setPosition(217.3304598f);
        jupiter.setSun(sun);
        System.out.println("Jupiter's Period: " + jupiter.T);

        saturn.setPosition(317.6005747f);
        saturn.setSun(sun);
        System.out.println("Saturn's Period: " + saturn.T);

        uranus.setPosition(531.5545977f);
        uranus.setSun(sun);
        System.out.println("Uranus's Period: " + uranus.T);

        neptune.setPosition(753.1163793f);
        neptune.setSun(sun);
        System.out.println("Neptune's Period: " + neptune.T);

        pluto.setPosition(1159.727011f);
        pluto.setSun(sun);
        System.out.println("Pluto's Period: " + pluto.T);

        System.out.println("Mercury's degree / s " + (360 / mercury.T));
        System.out.println("Venus's degree / s " + (360 / venus.T));
        System.out.println("Earth's degree / s " + (360 / earth.T));
        System.out.println("Mars's degree / s " + (360 / mars.T));
        System.out.println("Jupiter's degree / s " + (360 / jupiter.T));
        System.out.println("Saturn's degree / s " + (360 / saturn.T));
        System.out.println("Uranus's degree / s " + (360 / uranus.T));
        System.out.println("Neptune's degree / s " + (360 / neptune.T));
        System.out.println("Pluto's degree / s " + (360 / pluto.T));


        planets.add(sun);
        planets.add(mercury);
        planets.add(venus);
        planets.add(earth);
        planets.add(mars);
        planets.add(jupiter);
        planets.add(saturn);
        planets.add(uranus);
        planets.add(neptune);
        planets.add(pluto);

        for (Planet p : planets){
            rootNode.attachChild(p.getGeometry());
            rootNode.attachChild(p.createOrbitLine());
        }
    }

    public void initCamera(){
        cam.setLocation(new Vector3f(0, 10f, -50f));
        cam.lookAt(new Vector3f(0, 5f, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(50f);
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
        for (Planet p : planets){
            p.update(tpf);
        }
    }

    private void setupPlanetCycleKeys() {
        inputManager.addMapping("NextPlanet", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("PrevPlanet", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addListener(planetCycleListener, "NextPlanet", "PrevPlanet");
    }

    private void setupPlanetLocations() {
        planetNames.add("Sun");
        planetLocations.add(sun.getGeometry().getLocalTranslation());
        planetNames.add("Mercury");
        planetLocations.add(mercury.getGeometry().getLocalTranslation());
        planetNames.add("Venus");
        planetLocations.add(venus.getGeometry().getLocalTranslation());
        planetNames.add("Earth");
        planetLocations.add(earth.getGeometry().getLocalTranslation());
        planetNames.add("Mars");
        planetLocations.add(mars.getGeometry().getLocalTranslation());
        planetNames.add("Jupiter");
        planetLocations.add(jupiter.getGeometry().getLocalTranslation());
        planetNames.add("Saturn");
        planetLocations.add(saturn.getGeometry().getLocalTranslation());
        planetNames.add("Uranus");
        planetLocations.add(uranus.getGeometry().getLocalTranslation());
        planetNames.add("Neptune");
        planetLocations.add(neptune.getGeometry().getLocalTranslation());
        planetNames.add("Pluto");
        planetLocations.add(pluto.getGeometry().getLocalTranslation());
    }

    private void moveCamera(Vector3f cameraPosition, Vector3f lookAtTarget){
        cam.setLocation(cameraPosition);
        cam.lookAt(lookAtTarget, Vector3f.UNIT_Y);
    }

    private final ActionListener planetCycleListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (!isPressed) return; // only fire on key-down, not release

            if (name.equals("NextPlanet")) {
                currentPlanetIndex = (currentPlanetIndex + 1) % planetLocations.size();
            } else if (name.equals("PrevPlanet")) {
                currentPlanetIndex = (currentPlanetIndex - 1 + planetLocations.size()) % planetLocations.size();
            }

            Planet planet = planets.get(currentPlanetIndex);
            float offsetDistance = planet.radius * 6f;

            Vector3f planetPosition = planetLocations.get(currentPlanetIndex).clone();
            Vector3f cameraPosition = planetPosition.add(offsetDistance, 0, 0);

            moveCamera(cameraPosition, planetPosition);
            System.out.println("Viewing: " + planetNames.get(currentPlanetIndex));
        }
    };
}
