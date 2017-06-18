package MacroModelJon.roads;

import java.util.ArrayList;
import java.util.List;

public class RoadNetwork {

    private List<Junction> junctions = new ArrayList<>();
    private List<Road> roads = new ArrayList<>();

    public List<Junction> getJunctions() {
        return junctions;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public void add(Junction junction) {
        junctions.add(junction);
    }

    public void add(Road road) {
        roads.add(road);
    }

    public void step() {
        junctions.forEach(j -> j.step(Simulation.getTimestep()));
        roads.forEach(j -> j.step(Simulation.getTimestep()));
    }
}
