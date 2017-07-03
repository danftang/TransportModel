package MacroModel.roads;

import java.util.*;

public class Junction {

    private int id;
    private Coordinates coordinates;
    private String name;
    private Map<Junction, Road> incomingRoads = new HashMap<>();
    private Map<Junction, Road> outgoingRoads = new HashMap<>();
    private Map<Junction, Map<Vehicle, Long>> vehiclesByExit = new HashMap<>();

    private int timestepsToTraverseJunction = 1;
    private int vehiclesAcceptedPerIncomingPerTimestep = 1;

    private List<Vehicle> vehiclesToRemove = new ArrayList<>();

    public Junction(int id, Coordinates coordinates, String name) {
        this.id = id;
        this.coordinates = coordinates;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String getName() {
        return name;
    }

    public Map<Junction, Road> getOutgoingRoads() {
        return outgoingRoads;
    }

    @Override
    public String toString() {
        return name;
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
        Optional<Junction> nextJunctionOnRoute = vehicle.getNextJunctionOnRoute();
        if (nextJunctionOnRoute.isPresent()) {
            long exitTimestep = Simulation.getTimestep() + timestepsToTraverseJunction;
            vehiclesByExit.get(nextJunctionOnRoute.get()).put(vehicle, exitTimestep);
        } else {
            Logger.info("Vehicle " + vehicle.getName() + " has reached its destination junction of " + name);
        }
    }

    public void step(long timestep) {
        for (Junction junction : vehiclesByExit.keySet()) {
            exitVehiclesTowardsJunction(timestep, junction);
        }
    }

    public void report() {
        for (Junction junction : vehiclesByExit.keySet()) {
            Map<Vehicle, Long> vehiclesExitingTowardsJunction = vehiclesByExit.get(junction);

            if (vehiclesExitingTowardsJunction.size() > 0) {
                Logger.info("Junction " + name + " contains " + vehiclesExitingTowardsJunction.size() +
                        " vehicles travelling towards " + junction.name);
            }
        }
    }

    private void exitVehiclesTowardsJunction(long timestep, Junction junction) {
        Map<Vehicle, Long> vehiclesExitingTowardsJunction = vehiclesByExit.get(junction);

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
