package MacroModelJon.roads;

import MacroModelJon.utils.CoordinateUtils;

import java.util.*;

public class RouteSearch {

    private TreeMap<Double, List<Route>> routes = new TreeMap<>();
    private Map<Junction, Route> bestRoutesToJunctions = new HashMap<>();

    public Route findRoute(Junction origin, Junction destination) {
        if (origin.equals(destination)) {
            return new Route(origin, destination, new ArrayList<>(), 0);
        }

        initialize(origin, destination);

        while (continueRouteSearch(destination)) {
            routeSearch(destination);
        }

        if (bestRoutesToJunctions.containsKey(destination)) {
            return bestRoutesToJunctions.get(destination);
        } else {
            return new Route(origin, destination, new ArrayList<>(), 0);
        }
    }

    private void initialize(Junction origin, Junction destination) {
        List<Junction> routeSteps = new ArrayList<>();
        routeSteps.add(origin);
        Route route = new Route(origin, destination, routeSteps, 0);

        double estimatedCost = estimateTotalCost(route, destination);
        routes.put(estimatedCost, new ArrayList<>());
        routes.get(estimatedCost).add(route);
        bestRoutesToJunctions.put(origin, route);
    }

    private double estimateTotalCost(Route route, Junction destination) {
        double costSoFar = route.getCost();
        double estimatedCostToDestination = distBetweenJunctions(route.lastJunction(), destination);
        return costSoFar + estimatedCostToDestination;
    }

    private boolean continueRouteSearch(Junction destination) {
        return !routes.isEmpty() && !bestRoutesToJunctions.containsKey(destination);
    }

    private double distBetweenJunctions(Junction a, Junction b) {
        return CoordinateUtils.calculateDistance(a.getCoordinates().getLat(), a.getCoordinates().getLon(),
                b.getCoordinates().getLat(), b.getCoordinates().getLon());
    }

    private void routeSearch(Junction destination) {
        List<Route> mostPromisingRoutes = routes.remove(routes.firstKey());
        for (Route route : mostPromisingRoutes) {
            advanceRoute(route, destination);
        }
    }

    private void advanceRoute(Route route, Junction destination) {
        Junction activeJunction = route.lastJunction();
        for (Map.Entry<Junction, Road> outgoingRoad : activeJunction.getOutgoingRoads().entrySet()) {
            Junction nextJunction = outgoingRoad.getKey();
            Road roadToJunction = outgoingRoad.getValue();
            double additionalCost = roadToJunction.getLength();
            Route extendedRoute = route.copyAndExtendRoute(nextJunction, additionalCost);

            if (bestRouteToNextJunction(nextJunction, extendedRoute)) {
                double estimatedTotalCost = estimateTotalCost(extendedRoute, destination);
                routes.computeIfAbsent(estimatedTotalCost, etc -> routes.put(etc, new ArrayList<>()));
                routes.get(estimatedTotalCost).add(extendedRoute);
                bestRoutesToJunctions.put(nextJunction, extendedRoute);
            }
        }
    }

    private boolean bestRouteToNextJunction(Junction junction, Route newRoute) {
        return !bestRoutesToJunctions.containsKey(junction) ||
                newRoute.getCost() < bestRoutesToJunctions.get(junction).getCost();
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

        public Route copyAndExtendRoute(Junction junction, double extraCost) {
            List<Junction> shallowCopy = new ArrayList<>();
            shallowCopy.addAll(routeSteps);
            shallowCopy.add(junction);
            double newCost = cost + extraCost;
            Route extendedRoute = new Route(origin, destination, shallowCopy, newCost);
            return extendedRoute;
        }
    }
}
