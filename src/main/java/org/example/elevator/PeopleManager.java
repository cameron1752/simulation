package org.example.elevator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeopleManager extends HelperClass{
    private int completedPeopleCount;
    private int pendingPeopleCount;
    private long avgWait;
    private long longestWait = 0;
    private long shortestWait = Long.MAX_VALUE;
    private final List<People> people = Collections.synchronizedList(new ArrayList<>());
    private final ElevatorManager elevatorManager;
    private static final boolean peopleOn = true;
    private static final boolean customPeople = false;
    private final static int maxPeople = 50;
    private final static long timeout = 60000L;
    private final long startTime = System.currentTimeMillis();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public PeopleManager(ElevatorManager elevatorManager){
        super("PeopleManager");
        this.elevatorManager = elevatorManager;

        if (customPeople){
            customPeople();
        }
        if (peopleOn) {
            executor.execute(() -> {
                try {
                    manageMethod();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restore, don't swallow
                }
            });
        }

        debug(people.toString());
    }

    private void customPeople(){
        // two people going up -> (2, 3, 5, 7)
//        People person_1 = new People(2, 5);
//        People person_2 = new People(3, 7);

        // two people going down -> (7, 5, 3, 2)
//        People person_1 = new People(5, 3);
//        People person_2 = new People(7, 2);

        // two people going opposite directions -> (2, 7, 5, 3)
        People person_1 = new People(5, 3);
        People person_2 = new People(2, 7);

        people.add(person_1);
        people.add(person_2);
    }

    // need to figure out how to "manage" a random flow of people
    private void manageMethod() throws InterruptedException {
        while (System.currentTimeMillis() - startTime < timeout) {
            synchronized (people) {
                while (people.size() < maxPeople) {
                    people.add(new People(elevatorManager.getFloors() - 1));
                    pendingPeopleCount++;
                }
            }
            Thread.sleep(1000);
        }
    }


    public List<People> getPeople(){return people;}

    public void update(){
        synchronized (people){
            for  (int x = people.size() - 1; x >= 0; x--){
                People p = people.get(x);
                handleArrived(p);
                handleCallElevator(p);
                handleGetOnElevator(p);
                handleGetOffElevator(p);
            }
        }
    }

    private void handleArrived(People p){
        // check and see if the person has arrived
        if (p.isArrived()){
            // if they're on the elevator
            // check and see if they've arrived
            // if they've arrived, set our stats w/e and remove from list
            completedPeopleCount++;
            pendingPeopleCount--;
            long now = System.currentTimeMillis();
            longestWait = Math.max(longestWait, now - p.getStart());
            shortestWait = Math.min(shortestWait, now - p.getStart());
            avgWait = avgWait + ((now - p.getStart()) - avgWait) / completedPeopleCount;
            people.remove(p);
        }
    }

    private void handleCallElevator(People p){
        if (p.getElevator() == null){
            p.setElevator(elevatorManager.sendClosest(p.getCurrentFloor(), p.getTargetFloor()));
            debug("Calling elevator from: " + p.getCurrentFloor());
        }
    }

    private void handleGetOnElevator(People p){
        if (p.getElevator() != null){
            if (p.getElevator().getCurrentFloor() == p.getCurrentFloor()
                    && !p.isOnElevator()
                    && p.getElevator().getState() == State.OPEN){
                debug("Getting on " + p.getElevator().getName() + " at: " + p.getCurrentFloor());
                p.setOnElevator(true);
                p.getElevator().addOccupants();
                p.getElevator().addTotalOccupants();
            }
        }
    }

    private void handleGetOffElevator(People p){
        if (p.getElevator() != null){
            // if we're on the elevator and have reached the destination need to set arrived
            if (p.getElevator().getCurrentFloor() == p.getTargetFloor() && p.isOnElevator()){
                debug("Getting off " + p.getElevator().getName() +  " at: " + p.getTargetFloor());
                p.setArrived(true);
                p.setOnElevator(false);
                p.getElevator().removeOccupants();
            }
        }
    }

    @Override
    public String toString() {
        return "PeopleManager{" + "\n" +
                "completedPeopleCount=" + completedPeopleCount + "\n" +
                ", pendingPeopleCount=" + pendingPeopleCount + "\n" +
                ", avgWait=" + avgWait + " ms\n" +
                ", longestWait=" + longestWait + " ms\n" +
                ", shortestWait=" + shortestWait + " ms\n" +
                '}';
    }

    public void shutdown() {
        executor.shutdownNow();
        debugAlways(this.toString());
    }
}
