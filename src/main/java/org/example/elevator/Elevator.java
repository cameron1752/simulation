package org.example.elevator;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import java.util.*;

public class Elevator {
    private final Geometry elevator;
    private final boolean debug = false;
    private final int floors;
    private final float wallHeight;
    private final List<Float> floorHeights = new ArrayList<>();
    private float counter;
    private String state = "START";
    private int idleFloor = 0;

    private int currentFloor = 0;
    private final Queue<Integer> targetFloor;

    public Elevator(AssetManager assetManager, float wallLength, float wallHeight, int floors, int offSet){
        Material materialElevator = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialElevator.setColor("Color", ColorRGBA.Yellow);

        this.floors = floors;
        this.wallHeight = wallHeight;

        Box elevatorBox = new Box(wallLength - .1f,  wallHeight,  wallLength - .1f);
        elevator = new Geometry("Elevator", elevatorBox);
        elevator.setLocalTranslation(offSet, (-wallHeight * floors) + (wallHeight), 0);
        elevator.setMaterial(materialElevator);

        calculateFloors();

        targetFloor = new LinkedList<Integer>();
    }

    private void calculateFloors(){
        for (int x = 1; x <= (floors); x = x + 2){

            floorHeights.add(-(wallHeight * x) - (wallHeight));
            floorHeights.add((wallHeight * x) + (wallHeight));
        }
        floorHeights.add(0f);
        Collections.sort(floorHeights);

        debug(floorHeights.toString());
    }

    public void setIdleFloor(int floor){
        this.idleFloor = floor;
        setTargetFloor(floor);
    }


    public void update(float tpf){
        Vector3f position = elevator.getLocalTranslation();

        if (targetFloor.peek() != null){
            if (position.y < (floorHeights.get(targetFloor.peek()) + (wallHeight))){
                state = "UP";
                position.y = position.y + (.25f * tpf);
                elevator.setLocalTranslation(position);
                currentFloor = getFloor(position.y);
            } else if (position.y > (floorHeights.get(targetFloor.peek()) + (wallHeight))) {
                state = "DOWN";
                position.y = position.y - (.25f * tpf);
                elevator.setLocalTranslation(position);
                currentFloor = getFloor(position.y);
            } else{
                if (counter > 90){
                    counter = 0;
                    targetFloor.poll();
                } else {
                    counter += tpf;
                }
            }
        } else {
            state = "IDLE";
            setTargetFloor(idleFloor);
        }
    }

    private int getFloor(float y) {

        for (int x = 0; x < floors; x++){
            if (y == floorHeights.get(x)){
                return x;
            }
        }

        return currentFloor;
    }

    public void setTargetFloor(int floor){
        targetFloor.add(floor);

        debug("current: " + currentFloor);
        if (elevator.getLocalTranslation().y < (floorHeights.get(targetFloor.peek()) + wallHeight)){
            debug("target: " + (floorHeights.get(targetFloor.peek()) - wallHeight));
            debug("going up");
        } else if (elevator.getLocalTranslation().y > (floorHeights.get(targetFloor.peek()) + (wallHeight))){
            debug("target: " + (floorHeights.get(targetFloor.peek()) + wallHeight));
            debug("going down");
        }
    }

    public void debug(String message){
        if (debug){
            System.out.println(message);
        }
    }

    public Geometry getElevator(){return elevator;}
    public int getCurrentFloor(){return currentFloor;}
    public void clearTarget(){targetFloor.clear();}
    public int getTargetFloor(){
        if (targetFloor.peek() != null){
            return targetFloor.peek();
        } else {
            return (floors / 2);
        }
    }
    public int pollTargetFloor(){
        if (targetFloor.peek() != null){
            return targetFloor.poll();
        } else {
            return 0;
        }
    }

    public void printQueue(){
        debug(targetFloor.toString());
    }
    public String getState(){return state;}

}
