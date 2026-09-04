package org.example;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Scenario {
    AssetManager assetManager;
    float g;
    Sun star;
    List<Body> bodies = new ArrayList<>();
    
    public Scenario(AssetManager assetManager, float g){
        this.assetManager = assetManager;
        this.g = g;
    }

    public List<Body> getRandomScenario() throws InvocationTargetException, IllegalAccessException {
        Scenario obj = this;
        // 1. Get all declared methods of this class
        Method[] methods = obj.getClass().getDeclaredMethods();

        // 2. Pick a random index
        Random random = new Random();
        Method randomMethod;

        // Loop ensures we don't accidentally pick the "main" method
        do {
            int randomIndex = random.nextInt(methods.length);
            randomMethod = methods[randomIndex];
        } while (randomMethod.getName().equals("getRandomScenario") || randomMethod.getName().equals("getScenario"));

        // 3. Invoke the random method on our object instance
        System.out.println("Calling: " + randomMethod.getName());
        return (List<Body>) randomMethod.invoke(obj);
    }

    public List<Body> getScenario(int index) throws InvocationTargetException, IllegalAccessException {
        Scenario obj = this;
        // 1. Get all declared methods of this class
        Method[] methods = obj.getClass().getDeclaredMethods();
        Method method = null;

        for (Method m : methods){
            if (m.getName().equals("scenario"+index)){
                method = m;
                break;
            }
        }

        return (List<Body>) method.invoke(obj);
    }

    private List<Body> scenario1(){
        
        Sun star = new Sun("star", 1000, 6f, new Vector3f(0, 0, 0), assetManager, g, true);

        Body planet_1 = new Body("planet_1", 10f, 3f, new Vector3f(80, 0, 0), assetManager, g, false);
        Moon moon_1 = new Moon("moon_1", .0002f, 2f, new Vector3f(86, 0, 0), assetManager, g, false);
        Moon moon_2 = new Moon("moon_2", .0001f, 1f, new Vector3f(74, 0, 0), assetManager, g, false);

        Body planet_4 = new Body("planet_4", 18f, 3f, new Vector3f(-60, 0, 0), assetManager, g, false);

        planet_1.addMoon(moon_1);
        planet_1.addMoon(moon_2);

        this.star = star;

        bodies.add(planet_1);
        bodies.add(moon_1);
        bodies.add(moon_2);
        bodies.add(planet_4);
        bodies.add(star);

        return bodies;
    }

    private List<Body> scenario2(){
        Sun star = new Sun("star", 1000, 6f, new Vector3f(0, 0, 0), assetManager, g, true);

        Body planet_1 = new Body("planet_1", 10f, 3f, new Vector3f(80, 0, 0), assetManager, g, false);
        Moon moon_1 = new Moon("moon_1", .0002f, 2f, new Vector3f(84, 0, 0), assetManager, g, false);
        Moon moon_2 = new Moon("moon_2", .0001f, 1f, new Vector3f(76, 0, 0), assetManager, g, false);

        Body planet_4 = new Body("planet_4", 12f, 3f, new Vector3f(-80, 0, 0), assetManager, g, false);
        Moon moon_3 = new Moon("moon_3", .0001f, 1f, new Vector3f(-82, 0, 0), assetManager, g, false);

        planet_1.addMoon(moon_1);
        planet_1.addMoon(moon_2);
        planet_4.addMoon(moon_3);

        this.star = star;

        bodies.add(planet_1);
        bodies.add(moon_1);
        bodies.add(moon_2);
        bodies.add(planet_4);
        bodies.add(moon_3);
        bodies.add(star);

        return bodies;
    }

    private List<Body> scenario3(){
        Sun star = new Sun("star", 1000000, 6f, new Vector3f(0, 0, 0), assetManager, g, true);

        Body mercury = new Body("mercury", .055f, 2f, new Vector3f(19.35f, 0, 33.51f), assetManager, g, false);
        Body venus = new Body("venus", .815f, 2.5f, new Vector3f(-36.15f, 0, 62.61f), assetManager, g, false);

        Body earth = new Body("earth", 1f, 3f, new Vector3f(0, 0, 100), assetManager, g, false);
        Moon moon = new Moon("moon", .0123f, 1.5f, new Vector3f(0.26f, 0, 100), assetManager, g, false);

        Body mars = new Body("mars", .107f, 2.5f, new Vector3f(-107.48f, 0, -107.48f), assetManager, g, false);
        Moon phobos = new Moon("phobos", .0000017f, 1f, new Vector3f(-107.477f, 0, -107.48f), assetManager, g, false);
        Moon deimos = new Moon("deimos", .00000025f, 1f, new Vector3f(-107.472f, 0, -107.48f), assetManager, g,  false);

        Body jupiter = new Body("jupiter", 317.8f, 5f, new Vector3f(0, 0, 520), assetManager, g,  false);
        Moon io = new Moon("io", .015f, 1f, new Vector3f(0.022f, 0, 520), assetManager, g, false);
        Moon europa = new Moon("europa", .008f, 1f, new Vector3f(0.035f, 0, 520), assetManager, g, false);
        Moon ganymede = new Moon("ganymede", .025f, 1.2f, new Vector3f(0.056f, 0, 520), assetManager, g,  false);
        Moon callisto = new Moon("callisto", .018f, 1f, new Vector3f(0.098f, 0, 520), assetManager, g, false);

        Body saturn = new Body("saturn", 95.2f, 4.5f, new Vector3f(673.58f, 0, -673.58f), assetManager, g,  false);
        Moon rhea = new Moon("rhea", .0004f, 1f, new Vector3f(673.615f, 0, -673.58f), assetManager, g, false);
        Moon titan = new Moon("titan", .0225f, 1.5f, new Vector3f(673.661f, 0, -673.58f), assetManager, g,  false);
        Moon iapetus = new Moon("iapetus", .00023f, 1f, new Vector3f(673.81f, 0, -673.58f), assetManager, g, false);

        Body uranus = new Body("uranus", 14.5f, 4f, new Vector3f(-1920, 0, 0), assetManager, g, false);
        Moon titania = new Moon("titania", .000059f, 1f, new Vector3f(-1919.95f, 0, 0), assetManager, g, false);
        Moon oberon = new Moon("oberon", .00005f, 1f, new Vector3f(-1919.925f, 0, 0), assetManager, g, false);

        Body neptune = new Body("neptune", 17.1f, 4f, new Vector3f(1500, 0, 2598.08f), assetManager, g,  false);
        Moon triton = new Moon("triton", .00358f, 1f, new Vector3f(1500.031f, 0, 2598.08f), assetManager, g,  false);

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

        this.star = star;

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

        return bodies;
    }

    private List<Body> scenario4(){
        Sun star = new Sun("star", 1000000, 6f, new Vector3f(0, 0, 0), assetManager, g, true);

        Body mercury = new Body("mercury", .055f, 2f, new Vector3f(38.7f, 0, 0), assetManager, g, false);
        Body venus = new Body("venus", .815f, 2.5f, new Vector3f(0, 0, 72.3f), assetManager, g, false);

        Body earth = new Body("earth", 1f, 3f, new Vector3f(-100, 0, 0), assetManager, g, false);
        Moon moon = new Moon("moon", .0123f, 1.5f, new Vector3f(-99.74f, 0, 0), assetManager, g, false);

        Body mars = new Body("mars", .107f, 2.5f, new Vector3f(0, 0, -152), assetManager, g, false);
        Body phobos = new Body("phobos", .0000017f, 1f, new Vector3f(.003f, 0, -152), assetManager, g, false);
        Body deimos = new Body("deimos", .00000025f, 1f, new Vector3f(.008f, 0, -152), assetManager, g, false);

        // Jupiter deliberately placed at a different angle
        Body jupiter = new Body("jupiter", 317.8f, 5f, new Vector3f(-368.71f, 0, 368.71f), assetManager, g,  false);
        Moon io = new Moon("io", .015f, 1f, new Vector3f(-368.688f, 0, 368.71f), assetManager, g, false);
        Moon europa = new Moon("europa", .008f, 1f, new Vector3f(-368.675f, 0, 368.71f), assetManager, g, false);
        Moon ganymede = new Moon("ganymede", .025f, 1.2f, new Vector3f(-368.654f, 0, 368.71f), assetManager, g, false);
        Moon callisto = new Moon("callisto", .018f, 1f, new Vector3f(-368.612f, 0, 368.71f), assetManager, g, false);

        Body saturn = new Body("saturn", 95.2f, 4.5f, new Vector3f(673.58f, 0, 673.58f), assetManager, g, false);
        Moon rhea = new Moon("rhea", .0004f, 1f, new Vector3f(673.615f, 0, 673.58f), assetManager, g, false);
        Moon titan = new Moon("titan", .0225f, 1.5f, new Vector3f(673.661f, 0, 673.58f), assetManager, g, false);
        Moon iapetus = new Moon("iapetus", .00023f, 1f, new Vector3f(673.81f, 0, 673.58f), assetManager, g, false);

        Body uranus = new Body("uranus", 14.5f, 4f, new Vector3f(-1920, 0, 0), assetManager, g, false);
        Moon titania = new Moon("titania", .000059f, 1f, new Vector3f(-1919.95f, 0, 0), assetManager, g, false);
        Moon oberon = new Moon("oberon", .00005f, 1f, new Vector3f(-1919.925f, 0, 0), assetManager, g, false);

        Body neptune = new Body("neptune", 17.1f, 4f, new Vector3f(1500, 0, -2598.08f), assetManager, g, false);
        Moon triton = new Moon("triton", .00358f, 1f, new Vector3f(1500.031f, 0, -2598.08f), assetManager, g, false);



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

        this.star = star;

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

        return bodies;
    }

    private List<Body> scenario5(){
        Sun star = new Sun("star", 500, 6f, new Vector3f(0, 0, 0), assetManager, g, true);

        Body planet_1 = new Body("planet_1", 10f, 3f, new Vector3f(80, 0, 0), assetManager, g, false);
        Moon moon_1 = new Moon("moon_1", .0002f, 2f, new Vector3f(84, 0, 0), assetManager, g, false);
        Moon moon_2 = new Moon("moon_2", .0001f, 1f, new Vector3f(76, 0, 0), assetManager, g, false);

        Body planet_4 = new Body("planet_4", 12f, 3f, new Vector3f(-80, 0, 0), assetManager, g, false);
        Moon moon_3 = new Moon("moon_3", .0001f, 1f, new Vector3f(-84, 0, 0), assetManager, g, false);
        Moon moon_4 = new Moon("moon_2", .0002f, 2f, new Vector3f(-76, 0, 0), assetManager, g, false);

        planet_1.addMoon(moon_1);
        planet_1.addMoon(moon_2);
        planet_4.addMoon(moon_3);
        planet_4.addMoon(moon_4);

        this.star = star;

        bodies.add(planet_1);
        bodies.add(moon_1);
        bodies.add(moon_2);
        bodies.add(planet_4);
        bodies.add(moon_3);
        bodies.add(moon_4);
        bodies.add(star);

        return bodies;
    }

    private List<Body> scenario6(){
        Sun star = new Sun("star", 500, 6f, new Vector3f(100, 0, 0), assetManager, g, false);

        Sun star_2 = new Sun("star", 1000, 6f, new Vector3f(-100, 0, 0), assetManager, g, false);

        this.star = star;

        bodies.add(star);
        bodies.add(star_2);

        return bodies;
    }

    private List<Body> scenario7(){

        // Central star — fairly massive so inner planets orbit fast and tight
        Sun star = new Sun("star", 1400, 7f, new Vector3f(0, 0, 0), assetManager, g, true);

        // Tight inner planet, fast orbit
        Body planet_scorch = new Body("scorch", 6f, 1.5f, new Vector3f(35, 0, 0), assetManager, g, false);

        // Twin planets close together — should do a slow gravitational dance with each other
        // while both orbit the star
        Body planet_twin_a = new Body("twin_a", 14f, 2.5f, new Vector3f(90, 0, 0), assetManager, g, false);
        Body planet_twin_b = new Body("twin_b", 14f, 2.5f, new Vector3f(100, 0, 0), assetManager, g, false);

        // A ringed giant with three moons of wildly different sizes
        Body planet_titan = new Body("titan", 60f, 5f, new Vector3f(180, 0, 0), assetManager, g, false);
        Moon moon_a = new Moon("titan_moon_a", .0005f, 1.8f, new Vector3f(188, 0, 0), assetManager, g, false);
        Moon moon_b = new Moon("titan_moon_b", .0002f, 1.2f, new Vector3f(195, 0, 0), assetManager, g, false);
        Moon moon_c = new Moon("titan_moon_c", .00005f, .6f, new Vector3f(205, 0, 0), assetManager, g, false);

        // A distant loner on a long, lazy orbit
        Body planet_wanderer = new Body("wanderer", 22f, 3.2f, new Vector3f(-260, 0, 0), assetManager, g, false);

        // The wildcard: a heavy rogue planet entering from far outside the system,
        // positioned to eventually cross paths with the inner planets and perturb everything
        Body rogue = new Body("rogue_intruder", 90f, 4f, new Vector3f(-500, 40, 0), assetManager, g, false);

        planet_titan.addMoon(moon_a);
        planet_titan.addMoon(moon_b);
        planet_titan.addMoon(moon_c);

        this.star = star;

        bodies.add(planet_scorch);
        bodies.add(planet_twin_a);
        bodies.add(planet_twin_b);
        bodies.add(planet_titan);
        bodies.add(moon_a);
        bodies.add(moon_b);
        bodies.add(moon_c);
        bodies.add(planet_wanderer);
        bodies.add(rogue);
        bodies.add(star);

        return bodies;
    }

    public List<Body> scenario8(){

        // Central star
        Sun star = new Sun("star", 1200, 6.5f, new Vector3f(0, 0, 0), assetManager, g, true);

        // A cluster of three small, closely-spaced inner planets —
        // packed tight enough that they'll constantly perturb each other
        Body planet_alpha = new Body("alpha", 8f, 2f, new Vector3f(45, 0, 0), assetManager, g, false);
        Body planet_beta  = new Body("beta", 9f, 2f, new Vector3f(52, 0, 0), assetManager, g, false);
        Body planet_gamma = new Body("gamma", 7f, 1.8f, new Vector3f(59, 0, 0), assetManager, g, false);

        // Two "twin giants" — similar mass to each other, orbiting further out,
        // massive enough to rival the star's influence on nearby bodies
        Body giant_east = new Body("giant_east", 220f, 7f, new Vector3f(160, 0, 0), assetManager, g, false);
        Body giant_west = new Body("giant_west", 210f, 7f, new Vector3f(-160, 0, 0), assetManager, g, false);

        // A moon orbiting giant_east but placed unusually close —
        // likely to get torn away or crash given the giant's mass
        Moon moon_reckless = new Moon("moon_reckless", .0008f, 1.5f, new Vector3f(165, 0, 0), assetManager, g, false);

        // A tiny, fragile outer planet way out on the edge — mostly just there
        // to see how long it takes for the giants' influence to eventually reach it
        Body planet_fringe = new Body("fringe", 4f, 1.2f, new Vector3f(400, 0, 0), assetManager, g, false);

        giant_east.addMoon(moon_reckless);

        this.star = star;

        bodies.add(planet_alpha);
        bodies.add(planet_beta);
        bodies.add(planet_gamma);
        bodies.add(giant_east);
        bodies.add(giant_west);
        bodies.add(moon_reckless);
        bodies.add(planet_fringe);
        bodies.add(star);

        return bodies;
    }

}
