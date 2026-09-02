package org.example;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class SolarSystemSimulationTwo extends SimpleApplication {

    static AppSettings settings = new AppSettings(true);

    Body star;
    Body planet_1;
    Body planet_2;

    Body planet_3;
    float g = 1f;

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
//        setupPlanetLocations();
//        setupPlanetCycleKeys();
    }

    public void initPlanets(){
        star = new Body(100, 6f, new Vector3f(0, 0, 0), assetManager, g);

        planet_1 = new Body(5f, 1f, new Vector3f(80, 0, 0), assetManager, g);
        planet_2 = new Body(1, 3f, new Vector3f(-80, 0, 0), assetManager, g);
        planet_3 = new Body(10f, 1f, new Vector3f(120, 0, 0), assetManager, g);

        bodies.add(planet_1);
        bodies.add(planet_2);
        bodies.add(planet_3);
        bodies.add(star);

        for (Body b : bodies){

            Vector3f radius =  b.g.getLocalTranslation().subtract(star.g.getLocalTranslation());

            float distance = radius.length();

            float speed = (float) Math.sqrt(g * star.mass / distance);

            Vector3f tangent = new Vector3f(-radius.z,0,radius.x).normalizeLocal();

            b.currentVelocity = tangent.mult(speed);

        }

        star.currentVelocity = (new Vector3f(.1f, 0, 0));






        for (Body b : bodies){
            rootNode.attachChild(b.getGeometry());
            rootNode.attachChild(b.createOrbitLine());
        }
    }

    public void initCamera(){
        cam.setLocation(new Vector3f(0, 10f, -50f));
        cam.lookAt(new Vector3f(0, 5f, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(50f);
    }

    private void setTopDownView() {
        float height = 150f; // high enough to see Pluto's orbit with some margin
        cam.setLocation(new Vector3f(0, height, 0));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z); // look straight down, "up" reference along Z since Y is now forward
        cam.update();
        flyCam.setMoveSpeed(50f);
    }

    @Override
    public void simpleUpdate(float tpf) {
        float timeScale = 100f;
        for (Body b : bodies){
            b.updateVelocity(bodies, tpf*timeScale);
        }

        for (Body b : bodies){
            b.updatePosition(tpf*timeScale);
        }
    }
//
//    private void setupPlanetCycleKeys() {
//        inputManager.addMapping("NextPlanet", new KeyTrigger(KeyInput.KEY_RIGHT));
//        inputManager.addMapping("PrevPlanet", new KeyTrigger(KeyInput.KEY_LEFT));
//        inputManager.addListener(planetCycleListener, "NextPlanet", "PrevPlanet");
//    }

//    private void setupPlanetLocations() {
//        planetNames.add("Sun");
//        planetLocations.add(sun.getGeometry().getLocalTranslation());
//        planetNames.add("Mercury");
//        planetLocations.add(mercury.getGeometry().getLocalTranslation());
//        planetNames.add("Venus");
//        planetLocations.add(venus.getGeometry().getLocalTranslation());
//        planetNames.add("Earth");
//        planetLocations.add(earth.getGeometry().getLocalTranslation());
//        planetNames.add("Mars");
//        planetLocations.add(mars.getGeometry().getLocalTranslation());
//        planetNames.add("Jupiter");
//        planetLocations.add(jupiter.getGeometry().getLocalTranslation());
//        planetNames.add("Saturn");
//        planetLocations.add(saturn.getGeometry().getLocalTranslation());
//        planetNames.add("Uranus");
//        planetLocations.add(uranus.getGeometry().getLocalTranslation());
//        planetNames.add("Neptune");
//        planetLocations.add(neptune.getGeometry().getLocalTranslation());
//        planetNames.add("Pluto");
//        planetLocations.add(pluto.getGeometry().getLocalTranslation());
//    }
//
//    private void moveCamera(Vector3f cameraPosition, Vector3f lookAtTarget){
//        cam.setLocation(cameraPosition);
//        cam.lookAt(lookAtTarget, Vector3f.UNIT_Y);
//    }
//
//    private final ActionListener planetCycleListener = new ActionListener() {
//        @Override
//        public void onAction(String name, boolean isPressed, float tpf) {
//            if (!isPressed) return; // only fire on key-down, not release
//
//            if (name.equals("NextPlanet")) {
//                currentPlanetIndex = (currentPlanetIndex + 1) % planetLocations.size();
//            } else if (name.equals("PrevPlanet")) {
//                currentPlanetIndex = (currentPlanetIndex - 1 + planetLocations.size()) % planetLocations.size();
//            }
//
//            Body planet = bodies.get(currentPlanetIndex);
//            float offsetDistance = planet.radius * 6f;
//
//            Vector3f planetPosition = planetLocations.get(currentPlanetIndex).clone();
//            Vector3f cameraPosition = planetPosition.add(offsetDistance, 0, 0);
//
//            moveCamera(cameraPosition, planetPosition);
//            System.out.println("Viewing: " + planetNames.get(currentPlanetIndex));
//        }
//    };
}
