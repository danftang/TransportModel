package MacroModelJon.roads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Junction {

    private Coordinates coordinates;
    private Map<Junction, Road> incomingRoads = new HashMap<>();
    private Map<Junction, Road> outgoingRoads = new HashMap<>();
    private Map<Junction, Map<Vehicle, Long>> vehiclesByExit = new HashMap<>();

    private String name;

    private int timeToTraverseJunction = 1;
    private int vehiclesAcceptedPerIncomingPerTimestep = 1;

    private List<Vehicle> vehiclesToRemove = new ArrayList<>();

    public Junction(RoadNetwork roadNetwork, Coordinates coordinates, String name) {
        roadNetwork.add(this);
        this.coordinates = coordinates;
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Map<Junction, Road> getOutgoingRoads() {
        return outgoingRoads;
    }

    public void addIncomingRoad(Junction fromJunction, Road road) {
        incomingRoads.put(fromJunction, road);
    }

    public void addOutgoingRoad(Junction toJunction, Road road) {
        outgoingRoads.put(toJunction, road);
        vehiclesByExit.put(toJunction, new HashMap<>());
    }

    public int canAccept() {
        return vehiclesAcceptedPerIncomingPerTimestep;
    }

    public void take(Vehicle vehicle) {
        vehicle.arrivedAtJunction();
        Junction nextJunctionOnRoute = vehicle.getNextJunctionOnRoute();
        long exitTimestep = Simulation.getTimestep() + timeToTraverseJunction;

//        Logger.info("Vehicle " + vehicle.getName() + " is at junction " + name +
//                " and is trying to go to " + nextJunctionOnRoute.name + " with exit timestep " + exitTimestep);

        vehiclesByExit.get(nextJunctionOnRoute).put(vehicle, exitTimestep);
    }

    public void step(long timestep) {
        for (Junction junction : vehiclesByExit.keySet()) {
            exitVehiclesTowardsJunction(timestep, junction);
        }
    }

    private void exitVehiclesTowardsJunction(long timestep, Junction junction) {
        Map<Vehicle, Long> vehiclesExitingTowardsJunction = vehiclesByExit.get(junction);

        if (vehiclesExitingTowardsJunction.size() > 0) {
            Logger.info("Junction " + name + " contains " + vehiclesExitingTowardsJunction.size() +
                    " vehicles travelling towards " + junction.name);
        }

        vehiclesToRemove.clear();
        for (Map.Entry<Vehicle, Long> entry : vehiclesExitingTowardsJunction.entrySet()) {
            Vehicle vehicle = entry.getKey();
            long exitTimestep = entry.getValue();
            if (timestep == exitTimestep) {
                vehiclesToRemove.add(vehicle);
                outgoingRoads.get(junction).take(vehicle);
            }
        }

        vehiclesToRemove.forEach(vehicle -> vehiclesExitingTowardsJunction.remove(vehicle));
    }
}
