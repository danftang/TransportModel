package MacroModel.roads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Road {

    private Junction start;
    private Junction end;
    private double length;

    private Map<Vehicle, Long> travellingAlongRoad = new HashMap<>();
    private List<Vehicle> waitingToExitingRoad = new ArrayList<>();

    private String name;

    public Road(Junction start, Junction end, double length, String name) {
        this.start = start;
        this.end = end;
        this.length = length;
        start.addOutgoingRoad(end, this);
        end.addIncomingRoad(start, this);
        this.name = name;
    }

    public double getLength() {
        return length;
    }

    public void take(Vehicle vehicle) {
        long exitTimestep = Simulation.getTimestep() + vehicle.calcTimestepsToTraverseRoad(this);
        travellingAlongRoad.put(vehicle, exitTimestep);
    }

    public void step(long timestep) {
        updateVehiclesAwaitingExit(timestep);
        exitVehiclesFromRoad();
    }

    public void report() {
        if (travellingAlongRoad.size() + waitingToExitingRoad.size() > 0) {
            Logger.info("Road " + name + " has " + travellingAlongRoad.size() + " vehicles travelling along it and " +
                    waitingToExitingRoad.size() + " waiting to exit.");
        }
    }

    private void updateVehiclesAwaitingExit(long timestep) {
        for (Map.Entry<Vehicle, Long> entry : travellingAlongRoad.entrySet()) {
            Vehicle vehicle = entry.getKey();
            long exitTimestep = entry.getValue();
            if (exitTimestep == timestep) {
                waitingToExitingRoad.add(vehicle);
            }
        }

        waitingToExitingRoad.forEach(vehicle -> travellingAlongRoad.remove(vehicle));
    }

    private void exitVehiclesFromRoad() {
        int junctionCanAccept = end.canAccept();
        while (junctionCanAccept > 0 && waitingToExitingRoad.size() > 0) {
            Vehicle vehicle = waitingToExitingRoad.remove(0);
            end.take(vehicle);
            junctionCanAccept--;
        }
    }
}
