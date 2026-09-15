package org.example.elevator;

import java.util.ArrayList;
import java.util.List;

public class PeopleManager {
    private int completedPeopleCount;
    private int pendingPeopleCount;
    private long avgWait;
    private long longestWait;
    private long shortestWait;
    private final List<People> people = new ArrayList<>();
    private ElevatorManager elevatorManager;
    private boolean debug = true;

    public PeopleManager(ElevatorManager elevatorManager){
        this.elevatorManager = elevatorManager;

        for (int x = 0; x < 3; x++){
            people.add(new People(elevatorManager.getFloors()));
            pendingPeopleCount++;
        }

        System.out.println(people);
    }

    public void update(){
        for (int x = 0; x < people.size(); x++){
            People p = people.get(x);

            // check and see if the person has arrived
            if (p.isArrived()){
                // if they're on the elevator
                // check and see if they've arrived
                // if they've arrived, set our stats w/e and remove from list
                completedPeopleCount++;
                pendingPeopleCount--;
                longestWait = Math.max(longestWait, System.currentTimeMillis() - p.getStart());
                shortestWait = Math.min(shortestWait, System.currentTimeMillis() - p.getStart());
                people.remove(p);
            } else {
                // if they aren't on the elevator, call the elevator unless we already called it
                if (p.getElevator() == null){
                    debug("Calling elevator from: " + p.getCurrentFloor());
                    p.setElevator(elevatorManager.sendClosest(p.getCurrentFloor(), p.getTargetFloor()));
                } else {
                    // if we called the elevator need to wait until it gets to our floor to get on
                    if (p.getElevator().getCurrentFloor() == p.getCurrentFloor() && !p.isOnElevator()){
                        debug("Getting on the elevator at: " + p.getCurrentFloor());
                        p.setOnElevator(true);
                    }
                    // if we're on the elevator and have reached the destination need to set arrived
                    if (p.getElevator().getCurrentFloor() == p.getTargetFloor() && p.isOnElevator()){
                        debug("Getting off the elevator at: " + p.getTargetFloor());
                        p.setArrived(true);
                        p.setOnElevator(false);
                    }
                }
            }
        }

    }

    @Override
    public String toString() {
        return "PeopleManager{" + "\n" +
                "completedPeopleCount=" + completedPeopleCount + "\n" +
                ", pendingPeopleCount=" + pendingPeopleCount + "\n" +
                ", avgWait=" + avgWait + "\n" +
                ", longestWait=" + longestWait + "\n" +
                ", shortestWait=" + shortestWait + "\n" +
                '}';
    }

    private void debug(String message){
        if (debug){
            System.out.println(message);
        }
    }
}
