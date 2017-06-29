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

    public List<Junction> getJunctionsAccessibleFrom(Junction junction) {
        List<Junction> accessible = new ArrayList<>();
        exploreFromJunction(junction, accessible);
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
