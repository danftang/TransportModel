package MacroModelJon.roads;

import java.util.List;
import java.util.Optional;

public class Vehicle {

    private String name;
    private List<Junction> route;
    private int nextDestinationIdx = 0;

    public Vehicle(List<Junction> route, String name) {
        this.route = route;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Optional<Junction> getNextJunctionOnRoute() {
        if (nextDestinationIdx < route.size()) {
            return Optional.of(route.get(nextDestinationIdx));
        } else {
            return Optional.empty();
        }
    }

    public void arrivedAtJunction() {
        nextDestinationIdx++;
    }

    public int calcTimestepsToTraverseRoad(Road road) {
        return (int) Math.round(road.getLength());
    }
}
