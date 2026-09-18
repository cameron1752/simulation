package org.example.elevator;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.*;

public class Elevator extends HelperClass{
    private final Geometry elevator;
    private final int floors;
    private final float wallHeight;
    private final List<Float> floorHeights = new ArrayList<>();
    private float counter;
    private State state = State.IDLE;
    private int idleFloor = 0;
    private List<Integer> journey = new ArrayList<>();
    private int occupants;
    private int totalOccupants;
    private int currentFloor = 0;
    private final Queue<Integer> targetFloor = new LinkedList<Integer>();
    private static final float SPEED = .25f;

    public Elevator(AssetManager assetManager, float wallLength, float wallHeight, int floors, int offSet, int name){
        super("Elevator " + name);
        this.floors = floors;
        this.wallHeight = wallHeight;

        this.floorHeights.addAll(calculateFloors());
        this.elevator = initGeometry(assetManager, wallLength, wallHeight, offSet, floorHeights.get(idleFloor));
    }

    private static Geometry initGeometry(AssetManager assetManager, float wallLength, float wallHeight, float offSet, float floorHeight){
        Material materialElevator = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialElevator.setColor("Color", ColorRGBA.randomColor());

        Box elevatorBox = new Box(wallLength - .1f,  wallHeight,  wallLength - .1f);
        Geometry elevator = new Geometry("Elevator", elevatorBox);
        elevator.setLocalTranslation(offSet, floorHeight, 0);
        elevator.setMaterial(materialElevator);

        return elevator;
    }

    private List<Float> calculateFloors(){
        List<Float> heights = new ArrayList<>();
        for (int x = 0; x <= (floors); x++){
            heights.add(-(wallHeight * floors) + (2f * wallHeight * x));
        }

        Collections.sort(heights);

        debug(heights.toString());
        return heights;
    }

    public void setIdleFloor(int floor){
        this.idleFloor = floor;
        setTargetFloor(floor);
    }


    public void update(float tpf){
        Vector3f position = elevator.getLocalTranslation();

        // if targetFloor is empty
        if (targetFloor.isEmpty()){
            if ((currentFloor) < (idleFloor)){
                // we're below idleFloor
                position.y += SPEED * tpf;
                currentFloor = getFloor(position.y);
                state = State.UP;
            } else if ((currentFloor) > (idleFloor)){
                // we're above the idle floor go down
                position.y -= SPEED * tpf;
                currentFloor = getFloor(position.y);
                state = State.DOWN;
            } else if ((currentFloor) == (idleFloor)){
                // we're at the idle floor
                position.y = floorHeights.get(currentFloor);
                currentFloor = getFloor(position.y);
                state = State.IDLE;
                debug(journey.toString());
            }

        } else {
            // otherwise we have somewhere to go
            if ((currentFloor) < (targetFloor.peek())){
                // we're below need to move up
                position.y += SPEED * tpf;
                currentFloor = getFloor(position.y);
                state = State.UP;
            }

            if ((currentFloor) > (targetFloor.peek())){
                // we're above need to move down
                position.y -= SPEED * tpf;
                currentFloor = getFloor(position.y);
                state = State.DOWN;
            }

            if ((currentFloor) == (targetFloor.peek())){
                // we're at the floor, take a beat & poll
                if (counter > 90f){
                    counter = 0;
                    journey.add(targetFloor.poll());
                } else {
                    state = State.OPEN;
                    counter += tpf;
                }
            }
        }

        // move the elevator
        elevator.setLocalTranslation(position);
    }

    private int getFloor(float y) {

        for (int x = 0; x <= floors; x++){
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

    public void fixQueue(State targetDirection){
        // fixQueue to put the floors in ascending or descending order based on targetDirection
        // need to remove duplicates
        List<Integer> orderedFloors = new ArrayList<>();

        while (targetFloor.peek() != null){
            int tempFloor = targetFloor.poll();
            orderedFloors.add(tempFloor);
        }

        if (State.UP == targetDirection) {
            Collections.sort(orderedFloors);
        } else {
            orderedFloors.sort(Collections.reverseOrder());
        }

        // remove in order duplicates here
        // i.e. 1, 1, 2, 2 -> 1, 2
        // i.e. 1, 1, 2, 2, 1, 1 -> 1, 2, 1

        targetFloor.addAll(orderedFloors);

        debug(targetFloor.toString());

    }

    public void attachTo(Node parent){
        parent.attachChild(this.elevator);
    }
    public int getCurrentFloor(){return currentFloor;}
    public int getTargetFloor(){
        if (targetFloor.peek() != null){
            return targetFloor.peek();
        } else {
            return (floors / 2);
        }
    }
    public Queue<Integer> getQueue(){return targetFloor;}
    public List<Integer> getJourney(){return journey;}
    public State getState(){return state;}
    public void setStatus(State targetDirection) {
        this.state = targetDirection;
    }
    public String getName(){return name;}
    public void addOccupants(){
        occupants++;
    }
    public int getOccupants(){
        return occupants;
    }
    public void removeOccupants(){
        occupants--;
    }
    public void addTotalOccupants(){totalOccupants++;}
    public int getTotalOccupants(){return totalOccupants;}
    public String toString(){

        return name + "\n" +
                "Occupants: " + getOccupants() + "\n" +
                "Total Occupants: " + getTotalOccupants() + "\n" +
                "Current Floor: " + getCurrentFloor() + "\n" +
                "Target Floor: " + getTargetFloor() + "\n" +
                "State: " + getState() + "\n" +
                "Current Queue: " + getQueue() + "\n" +
                "Journey: " + getJourney() + "\n" +
                "-------------------------" + "\n";
    }
}
