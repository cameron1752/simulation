package org.example.elevator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElevatorManager {
    private int floors;
    private List<Elevator> elevatorList = new ArrayList<>();
    private static final String IDLE = "IDLE";
    private static final String OPEN = "OPEN";


    public ElevatorManager(int floors){
        this.floors = floors;
    }

    public void addElevator(Elevator elevator){
        elevatorList.add(elevator);

        if (elevatorList.size() % 2 == 0){
            elevator.setIdleFloor(0);
        } else {
            elevator.setIdleFloor(0);
        }
    }

    public Elevator sendClosest(int currentFloor, int targetFloor){
        String targetDirection;
        int closest = 0;
        int min = floors;

        if (currentFloor > targetFloor){
            targetDirection = "DOWN";
        } else {
            targetDirection = "UP";
        }

        for (Elevator e : elevatorList){

            // if an elevator is already going in that direction send that one
            if (targetDirection.equals(e.getState()) || IDLE.equals(e.getState())){
                // add to queue
                e.setTargetFloor(currentFloor);
                e.setTargetFloor(targetFloor);
                e.setStatus(targetDirection);
                // fix the queue
                e.fixQueue(targetDirection);
                // return the elevator being ridden
                return e;
            }

        }
        return null;
    }

    public void sendClosest(int targetFloor){
        int closest = 0;
        int min = floors;

        for (int x = 0; x < elevatorList.size(); x++){
            if (Math.abs(elevatorList.get(x).getCurrentFloor() - targetFloor) < min && smellTest(elevatorList.get(x), targetFloor)){
                min = Math.min(min, Math.abs(elevatorList.get(x).getCurrentFloor() - targetFloor));
                System.out.println("Setting min to: " + min + " for elevator: " + x);
                closest = x;
            }
        }

        System.out.println("Sending elevator: " + closest
                + " coming from : " + elevatorList.get(closest).getCurrentFloor()
                + " going to: " + targetFloor);


        // if target floor is less than AND state = DOWN
        if (targetFloor < elevatorList.get(closest).getCurrentFloor() && elevatorList.get(closest).getState().equals("DOWN")){
            Elevator elevator = elevatorList.get(closest);
            // list of targets already requested
            List<Integer> targets = new ArrayList<>();
            // newly tracked target
            targets.add(targetFloor);
            // currently tracked target
            int temp = elevator.pollTargetFloor();
            // empty rest of queue
            while (temp != 0){
                targets.add(temp);
                temp = elevator.pollTargetFloor();
            }
            // sort descending order
            targets.sort(Collections.reverseOrder());
            // reset queue
            elevator.clearTarget();
            // add all targets back
            for (int x : targets){
                elevator.setTargetFloor(x);
            }
            // add base for return
            elevator.setTargetFloor(0);
            elevator.printQueue();
        } else if (targetFloor > elevatorList.get(closest).getCurrentFloor() && elevatorList.get(closest).getState().equals("UP")){
            Elevator elevator = elevatorList.get(closest);
            // list of targets already requested
            List<Integer> targets = new ArrayList<>();
            // newly tracked target
            targets.add(targetFloor);
            // currently tracked target
            int temp = elevator.pollTargetFloor();
            // empty rest of queue
            while (temp != 0){
                targets.add(temp);
                temp = elevator.pollTargetFloor();
            }
            // sort descending order
            targets.sort(Collections.reverseOrder());
            // reset queue
            elevator.clearTarget();
            // add all targets back
            for (int x : targets){
                elevator.setTargetFloor(x);
            }
            // add base for return
            elevator.setTargetFloor(0);
            elevator.printQueue();
        } else {
            elevatorList.get(closest).setTargetFloor(targetFloor);
            elevatorList.get(closest).setTargetFloor(0);
        }


    }

    private boolean smellTest(Elevator elevator, int targetFloor){
        // need to check if we're already past a target, then send next closest
        // if closest.status == down && target > current -> skip this one go to next
        // if closest.status == up && target < current -> skip this one send the next
        if ("DOWN".equals(elevator.getState()) && targetFloor > elevator.getCurrentFloor()){
            return false;
        }
        if ("UP".equals(elevator.getState()) && targetFloor < elevator.getCurrentFloor()){
            return false;
        }

        return true;
    }

    public void update(float tpf){
        for (Elevator e : elevatorList){
            e.update(tpf);
        }
    }

    public String getInfo(){
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < elevatorList.size(); x++){
            sb.append("Elevator: ").append(x).append("\n");
            sb.append("Current Floor: ").append(elevatorList.get(x).getCurrentFloor()).append("\n");
            sb.append("Target Floor: ").append(elevatorList.get(x).getTargetFloor()).append("\n");
            sb.append("State: ").append(elevatorList.get(x).getState()).append("\n");
            sb.append("-------------------------").append("\n");
        }

        return sb.toString();
    }

    public int getFloors(){return floors;}

}
