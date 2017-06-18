package MacroModelJon.roads;

import java.util.List;

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

    public Junction getNextJunctionOnRoute() {
        return route.get(nextDestinationIdx);
    }

    public void arrivedAtJunction() {
        nextDestinationIdx = (nextDestinationIdx + 1) % route.size();
    }

    public int calcTimestepsToTraverseRoad(Road road) {
        return (int) Math.round(road.getLength());
    }
}
