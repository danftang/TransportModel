package MacroModel.roads;

import java.io.Serializable;
import java.util.*;

public class RoadNetwork implements Serializable {

    private List<Junction> junctions = new ArrayList<>();
    private List<Road> roads = new ArrayList<>();

    public List<Junction> getJunctions() {
        return junctions;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public Junction createJunction(Coordinates coordinates, String name) {
        Junction junction = new Junction(coordinates, name);
        junctions.add(junction);
        return junction;
    }

    public Road createRoad(Junction start, Junction end, double length, String name) {
        Road road = new Road(start, end, length, name);
        roads.add(road);
        return road;
    }

    public void step() {
        junctions.forEach(j -> j.step(Simulation.getTimestep()));
        roads.forEach(r -> r.step(Simulation.getTimestep()));
    }

    public void report() {
        junctions.forEach(Junction::report);
        roads.forEach(Road::report);
    }

    public List<Junction> getJunctionsAccessibleFrom(Junction startJunction) {
        List<Junction> accessible = new ArrayList<>();
        List<Junction> open = new ArrayList<>();
        open.add(startJunction);

        while (!open.isEmpty()) {
            List<Junction> nextOpen = new ArrayList<>();
            for (Junction junction : open) {
                for (Map.Entry<Junction, Road> entry : junction.getOutgoingRoads().entrySet()) {
                    Junction j = entry.getKey();
                    if (!accessible.contains(j)) {
                        accessible.add(j);
                        nextOpen.add(j);
                    }
                }
            }

            open = nextOpen;
        }

        return accessible;
    }

    private void exploreFromJunction(Junction junction, List<Junction> accessible) {
        if (!accessible.contains(junction)) {
            accessible.add(junction);
            junction.getOutgoingRoads().forEach(
                    (Junction j, Road r) -> exploreFromJunction(j, accessible)
            );
        }
    }
}
