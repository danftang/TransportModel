package MacroModelJon.roads;

import java.util.ArrayList;
import java.util.List;

public class Simulation {

    private static long timestep = 0;
    private static long lastTimestep = 100;
    private static RoadNetwork roadNetwork = new RoadNetwork();

    public static long getTimestep() {
        return timestep;
    }

    public void start() {
        Logger.initialize(this);
        List<Junction> route = initialize();
        run(route);
    }

    private List<Junction> initialize() {
        Junction a = new Junction(roadNetwork, new Coordinates(0, 0), "A");
        Junction b = new Junction(roadNetwork, new Coordinates(0, 10), "B");
        Junction c = new Junction(roadNetwork, new Coordinates(10, 10), "C");
        Junction d = new Junction(roadNetwork, new Coordinates(10, 0), "D");

        new Road(roadNetwork, a, b, 10, "AB");
        new Road(roadNetwork, b, a, 10, "BA");
        new Road(roadNetwork, b, c, 10, "BC");
        new Road(roadNetwork, c, b, 10, "CB");
        new Road(roadNetwork, c, d, 10, "CD");
        new Road(roadNetwork, d, c, 10, "DC");
        new Road(roadNetwork, d, a, 10, "DA");
        new Road(roadNetwork, a, d, 10, "AD");

        List<Junction> route = new ArrayList<>();
        route.add(a);
        route.add(b);
        route.add(c);
        route.add(d);

        return route;
    }

    private void run(List<Junction> route) {
        while (timestep <= lastTimestep) {
            makeVehicles(2, route);
            roadNetwork.step();
            Logger.info("");
            timestep++;
        }
    }

    private void makeVehicles(int number, List<Junction> route) {
        for (int i = 0; i < number; i++) {
            Vehicle vehicle = new Vehicle(route, "v1");
            route.get(0).take(vehicle);
        }
    }

    public static void main(String[] args) {
        new Simulation().start();
    }
}
