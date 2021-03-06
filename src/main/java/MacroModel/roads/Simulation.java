package MacroModel.roads;

import MacroModel.osm.BoundingBox;
import MacroModel.osm.OsmRoadNetworkParser;
import MacroModel.osm.core.OsmData;
import MacroModel.osm.core.OsmDataLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Simulation {

    private static long timestep = 0;
    private static long lastTimestep = 100;
    private static RoadNetwork roadNetwork = new RoadNetwork();
    private static Random rand = new Random();

    public static long getTimestep() {
        return timestep;
    }

    public void start() {
        Logger.initialize(this);
        initialize();
        run();
    }

    private void initializeTest() {
        Junction a = roadNetwork.createJunction(1, new Coordinates(0, 0), "A");
        Junction b = roadNetwork.createJunction(2, new Coordinates(0, 10), "B");
        Junction c = roadNetwork.createJunction(3, new Coordinates(10, 10), "C");
        Junction d = roadNetwork.createJunction(4, new Coordinates(10, 0), "D");

        roadNetwork.createRoad(a, b, 10, "AB");
        roadNetwork.createRoad(b, a, 10, "BA");
        roadNetwork.createRoad(b, c, 10, "BC");
        roadNetwork.createRoad(c, b, 10, "CB");
        roadNetwork.createRoad(c, d, 10, "CD");
        roadNetwork.createRoad(d, c, 10, "DC");
        roadNetwork.createRoad(d, a, 10, "DA");
        roadNetwork.createRoad(a, d, 10, "AD");

        List<Junction> route = new ArrayList<>();
        route.add(a);
        route.add(b);
        route.add(c);
        route.add(d);
    }

    private void initialize() {
//        OsmData data = OsmDataLoader.getData(new BoundingBox(51.0, 0, 51.1, 0.1)).get();
        OsmData data = new OsmData(new BoundingBox(51.32, -0.53, 51.67, 0.23));
//        OsmData data = new OsmData(new BoundingBox(51.0, 0, 51.1, 0.1));
//        OsmData data = new OsmData(new BoundingBox(51.32, -0.53, 51.67, 0.23));
        roadNetwork = OsmRoadNetworkParser.getRoadNetwork(data);
    }

    private void run() {
        makeRandomVehicles(1000);

        while (timestep <= lastTimestep) {
            roadNetwork.step();
            roadNetwork.report();
            Logger.info("");
            timestep++;
        }
    }

    private void makeRandomVehicles(int number) {
        for (int i = 0; i < number; i++) {
            List<Junction> route = getRandomRoute(roadNetwork);

            if (!route.isEmpty()) {
                Vehicle vehicle = new Vehicle(route, "v" + i);
                route.get(0).take(vehicle);
            }

            Logger.info((i + 1) + " vehicles created");
        }

        Logger.info("Finished creating " + number + " vehicles");
    }

    private static List<Junction> getRandomRoute(RoadNetwork roadNetwork) {
        Junction start = roadNetwork.getJunctions().get(rand.nextInt(roadNetwork.getJunctions().size()));
        Junction end = roadNetwork.getJunctions().get(rand.nextInt(roadNetwork.getJunctions().size()));
        List<Junction> routeSteps = new RouteSearch().findRoute(start, end).getRouteSteps();

        if (routeSteps.isEmpty()) {
            return getRandomRoute(roadNetwork);
        } else {
            return routeSteps;
        }
    }

    public static void main(String[] args) {
        new Simulation().start();
    }
}
