package org.example.elevator;

import java.util.ArrayList;
import java.util.List;

public class PeopleManager {
    private int completedPeopleCount;
    private int pendingPeopleCount;
    private long avgWait;
    private long longestWait = 0;
    private long shortestWait = 9999999999999L;
    private final List<People> people = new ArrayList<>();
    private ElevatorManager elevatorManager;
    private boolean debug = true;

    public PeopleManager(ElevatorManager elevatorManager){
        this.elevatorManager = elevatorManager;

        for (int x = 0; x < 2; x++){
            people.add(new People(elevatorManager.getFloors() - 1));
            pendingPeopleCount++;
        }

        System.out.println(people);
    }

    public List<People> getPeople(){return people;}

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
                avgWait = avgWait + ((System.currentTimeMillis() - p.getStart()) - avgWait) / completedPeopleCount;
                people.remove(p);
            } else {
                // if they aren't on the elevator, call the elevator unless we already called it
                if (p.getElevator() == null){
                    p.setElevator(elevatorManager.sendClosest(p.getCurrentFloor(), p.getTargetFloor()));
                    debug("Calling " + p.getElevator().getName() + " from: " + p.getCurrentFloor());
                } else {
                    // if we called the elevator need to wait until it gets to our floor to get on
                    if (p.getElevator().getCurrentFloor() == p.getCurrentFloor() && !p.isOnElevator()){
                        debug("Getting on " + p.getElevator().getName() + " at: " + p.getCurrentFloor());
                        p.setOnElevator(true);
                    }
                    // if we're on the elevator and have reached the destination need to set arrived
                    if (p.getElevator().getCurrentFloor() == p.getTargetFloor() && p.isOnElevator()){
                        debug("Getting off  " + p.getElevator().getName() +  " at: " + p.getTargetFloor());
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
