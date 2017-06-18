package MacroModelJon.roads;

import MacroModelJon.utils.CoordinateUtils;

import java.util.*;

public class RouteSearch {

    private TreeMap<Double, List<Route>> routes = new TreeMap<>();
    private Map<Junction, Route> junctionBestRoutes = new HashMap<>();

    public Route findRoute(Junction origin, Junction destination) {
        if (origin.equals(destination)) {
            return new Route(origin, destination, new ArrayList<>(), 0);
        }

        List<Junction> routeSteps = new ArrayList<>();
        routeSteps.add(origin);
        Route route = new Route(origin, destination, routeSteps, 0);

        double estimatedCost = estimateTotalCost(route, destination);
        routes.put(estimatedCost, new ArrayList<>());
        routes.get(estimatedCost).add(route);

        while (true) {
            Logger.info("Route searching with " + routes.size() + " active routes");
            routeSearch(destination);

            if (routes.isEmpty() || junctionBestRoutes.containsKey(destination)) {
                Logger.info("Routes contains " + routes.size() + " entries. Is destination found? " + junctionBestRoutes.containsKey(destination));
                break;
            }
        }

        if (junctionBestRoutes.containsKey(destination)) {
            return junctionBestRoutes.get(destination);
        } else {
            return new Route(origin, destination, new ArrayList<>(), 0);
        }
    }

    private double estimateTotalCost(Route route, Junction destination) {
        double costSoFar = route.getCost();
        double estimatedCostToEnd = distBetweenJunctions(route.lastJunction(), destination);
        return costSoFar + estimatedCostToEnd;
    }

    private double distBetweenJunctions(Junction a, Junction b) {
        return CoordinateUtils.calculateDistance(a.getCoordinates().getLat(), a.getCoordinates().getLon(),
                b.getCoordinates().getLat(), b.getCoordinates().getLon());
    }

    private void routeSearch(Junction destination) {
        Logger.info("Most promising routes have an estimate score of " + routes.firstKey());
        List<Route> mostPromisingRoutes = routes.remove(routes.firstKey());
        for (Route route : mostPromisingRoutes) {
            Logger.info("Extending route with " + route.getRouteSteps().size() + " steps and a cost of " + route.getCost());
            advanceRoute(route, destination);
        }
    }

    private void advanceRoute(Route route, Junction destination) {
        Junction activeJunction = route.lastJunction();
        for (Map.Entry<Junction, Road> outgoingRoad : activeJunction.getOutgoingRoads().entrySet()) {
            Junction nextJunction = outgoingRoad.getKey();
            Road roadToJunction = outgoingRoad.getValue();
            double extraCost = roadToJunction.getLength();
            Route extendedRoute = route.makeExtendedRoute(nextJunction, extraCost);

            if (!junctionAlreadyVisitedWithBetterRoute(nextJunction, extendedRoute)) {
                Logger.info("Adding route to map");
                double estimatedTotalCost = estimateTotalCost(route, destination);
                routes.computeIfAbsent(estimatedTotalCost, e -> routes.put(e, new ArrayList<>()));
                routes.get(estimatedTotalCost).add(route);
                junctionBestRoutes.put(nextJunction, route);
            }
        }
    }

    private boolean junctionAlreadyVisitedWithBetterRoute(Junction junction, Route route) {
        return junctionBestRoutes.containsKey(junction) && route.getCost() >= junctionBestRoutes.get(junction).getCost();
    }

    public class Route {
        private Junction origin;
        private Junction destination;
        private List<Junction> routeSteps;
        private double cost;

        public Route(Junction origin, Junction destination, List<Junction> routeSteps, double cost) {
            this.origin = origin;
            this.destination = destination;
            this.routeSteps = routeSteps;
            this.cost = cost;
        }

        public Junction getOrigin() {
            return origin;
        }

        public Junction getDestination() {
            return destination;
        }

        public List<Junction> getRouteSteps() {
            return routeSteps;
        }

        public double getCost() {
            return cost;
        }

        public Junction lastJunction() {
            return routeSteps.get(routeSteps.size() - 1);
        }

        public Route makeExtendedRoute(Junction junction, double extraCost) {
            List<Junction> shallowCopy = new ArrayList<>();
            shallowCopy.addAll(routeSteps);
            shallowCopy.add(junction);
            double newCost = cost += extraCost;
            return new Route(origin, destination, shallowCopy, newCost);
        }
    }
}
