package org.example.elevator;

import java.util.ArrayList;
import java.util.List;

public class ElevatorManager extends HelperClass{
    private final int floors;
    private final List<Elevator> elevatorList = new ArrayList<>();
    private static final boolean featureFlag = false;


    public ElevatorManager(int floors){
        super("ElevatorManager");
        this.floors = floors;
    }

    public void addElevator(Elevator elevator){
        elevatorList.add(elevator);

        if (elevatorList.size() % 2 == 0){
            elevator.setIdleFloor(floors / 2);
        } else {
            elevator.setIdleFloor(0);
        }
    }

    public Elevator sendClosest(int currentFloor, int targetFloor){
        if (featureFlag){
            return sendClosestNew(currentFloor, targetFloor);
        } else {
            return sendClosestOld(currentFloor, targetFloor);
        }
    }

    public Elevator sendClosestOld(int currentFloor, int targetFloor){
        State direction = (currentFloor > targetFloor) ? State.DOWN : State.UP;

        Elevator best = null;
        int bestCost = Integer.MAX_VALUE;

        Elevator fallbackBest = null;
        int fallbackCost = Integer.MAX_VALUE;

        for (Elevator e : elevatorList) {
            boolean eligibleNow =
                    e.getState() == State.IDLE
                            || (e.getState() == direction && isAheadOf(e, currentFloor, direction));

            int cost = Math.abs(e.getCurrentFloor() - currentFloor);
            // small tie-break: penalize elevators with longer existing queues
            cost += e.getQueue().size();

            if (eligibleNow) {
                if (cost < bestCost) {
                    bestCost = cost;
                    best = e;
                }
            } else if (cost < fallbackCost) {
                fallbackCost = cost;
                fallbackBest = e;
            }
        }

        Elevator chosen = (best != null) ? best : fallbackBest;
        if (chosen == null) {
            return null; // no elevators registered at all
        }

        chosen.setTargetFloor(currentFloor);
        chosen.setTargetFloor(targetFloor);
        chosen.setStatus(direction);

        if (chosen.getState() == direction && !chosen.getQueue().isEmpty()) {
            chosen.fixQueue(direction);
        }

        return chosen;
    }

    private boolean isAheadOf(Elevator e, int requestFloor, State direction) {
        return direction == State.UP
                ? e.getCurrentFloor() <= requestFloor
                : e.getCurrentFloor() >= requestFloor;
    }

    public Elevator sendClosestNew(int currentFloor, int targetFloor){
        State targetDirection;

        if (currentFloor > targetFloor){
            targetDirection = State.DOWN;
        } else {
            targetDirection = State.UP;
        }

        // if there's an idle elevator just send that one
        for (Elevator e : elevatorList){
            if (State.IDLE == e.getState()){
                e.setTargetFloor(currentFloor);
                e.setTargetFloor(targetFloor);
                e.setStatus(targetDirection);
                return e;
            }

            // if the elevator is going up already
            // e.current < current && e.status == UP
            if (State.UP == targetDirection
                    && e.getCurrentFloor() < currentFloor
                    && e.getState() == State.UP){
                e.setTargetFloor(currentFloor);
                e.setTargetFloor(targetFloor);
                e.setStatus(targetDirection);
                // need to fix the queue so that we pick up and drop off in ascending order
                // i.e. current floor (1) target floor (4, 6) new request (3, 5)
                // queue should look like (3, 4, 5, 6)
                e.fixQueue(targetDirection);
                return e;
            }

            // if the elevator is going down already
            // e.current > current && e.status == DOWN
            if (State.DOWN == targetDirection
                    && e.getCurrentFloor() > currentFloor
                    && e.getState() == State.DOWN){
                e.setTargetFloor(currentFloor);
                e.setTargetFloor(targetFloor);
                e.setStatus(targetDirection);
                // need to fix the queue so that we pick up and drop off in descending order
                // i.e. current floor (7) target floor (6, 2) new request (5, 3)
                // queue should look like (6, 5, 3, 2)
                e.fixQueue(targetDirection);
                return e;
            }
        }
        return null;
    }

    public void update(float tpf){
        for (Elevator e : elevatorList){
            e.update(tpf);
        }
    }

    public String getInfo(){
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < elevatorList.size(); x++){
            Elevator e = elevatorList.get(x);
            sb.append(e.toString());
        }

        return sb.toString();
    }

    public void shutdown() {
        debugAlways(this.getInfo());
    }

    public int getFloors(){return floors;}

}
