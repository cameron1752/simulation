package org.example.elevator;

import java.util.concurrent.ThreadLocalRandom;

public class People {

    private int currentFloor;
    private int targetFloor;
    private final long start;
    private Elevator elevator;
    private boolean onElevator = false;
    private boolean arrived = false;

    public People(int floors){
        currentFloor = ThreadLocalRandom.current().nextInt(0, floors + 1);
        targetFloor = ThreadLocalRandom.current().nextInt(0, floors + 1);

        if (targetFloor == currentFloor){
            while (targetFloor == currentFloor){
                targetFloor = ThreadLocalRandom.current().nextInt(0, floors + 1);
            }
        }
        start = System.currentTimeMillis();
    }

    public int getCurrentFloor(){
        if (onElevator){
            return elevator.getCurrentFloor();
        } else {
            return currentFloor;
        }

    }
    public void setOnElevator(boolean onElevator){
        this.onElevator = onElevator;
    }
    public int getTargetFloor(){return targetFloor;}
    public void setElevator(Elevator elevator){
        this.elevator = elevator;
    }
    public Elevator getElevator(){
        if (this.elevator == null){
            return null;
        } else {
            return this.elevator;
        }
    }
    public long getStart(){return start;}
    public boolean isArrived(){
        return arrived;
    }

    public void setArrived(boolean arrived){
        this.arrived = arrived;
    }

    private String direction(){
        if (currentFloor > targetFloor){
            return "DOWN";
        } else {
            return "UP";
        }
    }

    public boolean isOnElevator(){
        return onElevator;
    }

    @Override
    public String toString() {
        return "People{" +
                "currentFloor=" + currentFloor +
                ", targetFloor=" + targetFloor +
                ", onElevator=" + onElevator +
                ", isArrived=" + arrived +
                ", direction=" + direction() +
                "} \n";
    }
}
