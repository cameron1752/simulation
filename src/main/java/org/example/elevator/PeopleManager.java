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

    public PeopleManager(ElevatorManager elevatorManager){
        this.elevatorManager = elevatorManager;

        for (int x = 0; x < 5; x++){
            people.add(new People(elevatorManager.getFloors()));
            pendingPeopleCount++;
        }

        System.out.println(people);
    }

    public void update(){
        for (int x = 0; x < people.size(); x++){
            People p = people.get(x);
            if (p.isArrived()){
                // increment count
                completedPeopleCount++;
                // decrement pending
                pendingPeopleCount--;
                // calculate wait time
                long elapsedTime = System.currentTimeMillis() - p.getStart();
                // update the average
                avgWait = ((avgWait * (completedPeopleCount - 1)) + elapsedTime)/ completedPeopleCount;
                // update longest
                longestWait = Math.max(longestWait, elapsedTime);
                // update shortest
                shortestWait = Math.min(shortestWait, elapsedTime);
                // remove from list
                people.remove(p);
            } else if (!p.isOnElevator()) {
                // if they haven't pressed their button yet press the button
                Elevator e = elevatorManager.sendClosest(p.getCurrentFloor(), p.getTargetFloor());
                // assign it to the person
                p.setElevator(e);
            }
        }

        if (people.isEmpty()){
            System.out.println("All people have been delivered!");
        }
    }

    @Override
    public String toString() {
        return "PeopleManager{" +
                "completedPeopleCount=" + completedPeopleCount +
                ", pendingPeopleCount=" + pendingPeopleCount +
                ", avgWait=" + avgWait +
                ", longestWait=" + longestWait +
                ", shortestWait=" + shortestWait +
                '}';
    }
}
