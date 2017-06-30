package MacroModel.osm;

import MacroModel.osm.core.CacheDirectory;
import MacroModel.osm.core.OsmData;
import MacroModel.osm.core.OsmNode;
import MacroModel.osm.core.OsmWay;
import MacroModel.roads.Coordinates;
import MacroModel.roads.Junction;
import MacroModel.roads.Logger;
import MacroModel.roads.RoadNetwork;
import MacroModel.utils.CoordinateUtils;
import MacroModel.utils.FileUtils;
import MacroModel.utils.ObjectSerializer;

import java.io.IOException;
import java.util.*;

public class OsmRoadNetworkParser {

    private static long cacheValidForMillis = 60 * 24 * 60 * 60 * 1000;
    private static CacheDirectory cache = new CacheDirectory("cache/roads/", ".ser", cacheValidForMillis);

    public static Optional<RoadNetwork> getRoadNetwork(OsmData osmData) {
        BoundingBox boundingBox = osmData.getBoundingBox();
        String cacheFileBaseName = boundingBox.getCacheFileBaseName();

        Logger.info("OsmRoadNetworkParser: Getting road network for bounding box " + boundingBox +
                " (cache base name " + cacheFileBaseName + ")");

        updateCacheIfNecessary(osmData, cacheFileBaseName);

        if (cache.contains(cacheFileBaseName)) {
            Logger.info("OsmRoadNetworkParser: Road network successfully loaded.");
            return loadDataFromCache(cacheFileBaseName);
        } else {
            Logger.error("OsmRoadNetworkParser: Could not load road network.");
            return Optional.empty();
        }
    }

    private static void updateCacheIfNecessary(OsmData osmData, String cacheFileBaseName) {
        if (!cache.contains(cacheFileBaseName) || cache.isOutOfDate(cacheFileBaseName)) {
            tryCreateAndCacheRoadNetwork(osmData, cacheFileBaseName);
        }
    }

    private static void tryCreateAndCacheRoadNetwork(OsmData osmData, String cacheFileBaseName) {
        Set<OsmWay> osmRoads = getRoads(osmData);

        RoadNetwork roadNetwork = new RoadNetwork();
        Map<OsmNode, Junction> junctionsByOsmNode = createJunctions(roadNetwork, osmRoads);
        createRoads(roadNetwork, osmRoads, junctionsByOsmNode);

        ObjectSerializer<RoadNetwork> serializer = new ObjectSerializer<>();
        byte[] serialized = serializer.serialize(roadNetwork);

        long underlyingDataTimestamp = osmData.getCacheTimestamp();
        cache.cache(cacheFileBaseName, underlyingDataTimestamp, serialized);
    }

    private static Set<OsmWay> getRoads(OsmData osmData) {
        Map<String, Set<OsmWay>> highways = osmData.getWaysByTag().get("highway");
        Set<OsmWay> roads = highways.getOrDefault("living_street", new HashSet<>());
        roads.addAll(highways.getOrDefault("motorway", new HashSet<>()));
        roads.addAll(highways.getOrDefault("motorway_link", new HashSet<>()));
        roads.addAll(highways.getOrDefault("primary", new HashSet<>()));
        roads.addAll(highways.getOrDefault("trunk", new HashSet<>()));
        roads.addAll(highways.getOrDefault("road", new HashSet<>()));
        roads.addAll(highways.getOrDefault("primary_link", new HashSet<>()));
        roads.addAll(highways.getOrDefault("secondary", new HashSet<>()));
        roads.addAll(highways.getOrDefault("tertiary", new HashSet<>()));
        roads.addAll(highways.getOrDefault("unclassified", new HashSet<>()));
        roads.addAll(highways.getOrDefault("residential", new HashSet<>()));
        roads.addAll(highways.getOrDefault("service", new HashSet<>()));
        roads.addAll(highways.getOrDefault("trunk_link", new HashSet<>()));
        roads.addAll(highways.getOrDefault("secondary_link", new HashSet<>()));
        roads.addAll(highways.getOrDefault("tertiary_link", new HashSet<>()));
        roads.addAll(highways.getOrDefault("track", new HashSet<>()));
        return roads;
    }

    private static Map<OsmNode, Junction> createJunctions(RoadNetwork roadNetwork, Set<OsmWay> osmRoads) {
        Set<OsmNode> roadEndNodes = getAllRoadEndNodes(osmRoads);
        Set<OsmNode> roadIntersectNodes = getAllNodesIncludedInMultipleWays(osmRoads);

        Set<OsmNode> junctionNodes = new HashSet<>();
        junctionNodes.addAll(roadEndNodes);
        junctionNodes.addAll(roadIntersectNodes);

        Map<OsmNode, Junction> junctions = new HashMap<>();
        for (OsmNode junctionOsmNode : junctionNodes) {
            Coordinates coordinates = new Coordinates(junctionOsmNode.getLat(), junctionOsmNode.getLon());
            Junction junction = roadNetwork.createJunction(coordinates, Long.toString(junctionOsmNode.getId()));
            junctions.put(junctionOsmNode, junction);
        }

        return junctions;
    }

    private static Set<OsmNode> getAllRoadEndNodes(Set<OsmWay> osmRoads) {
        Set<OsmNode> junctionOsmNodes = new HashSet<>();
        for (OsmWay road : osmRoads) {
            List<OsmNode> roadNodes = road.getNodes();
            junctionOsmNodes.add(roadNodes.get(0));
            junctionOsmNodes.add(roadNodes.get(roadNodes.size() - 1));
        }

        return junctionOsmNodes;
    }

    private static Set<OsmNode> getAllNodesIncludedInMultipleWays(Set<OsmWay> osmRoads) {
        Set<OsmNode> junctionOsmNodes = new HashSet<>();

        Map<OsmNode, Set<OsmWay>> roadsByContainedNodes = new HashMap<>();
        for (OsmWay road : osmRoads) {
            List<OsmNode> roadNodes = road.getNodes();
            junctionOsmNodes.add(roadNodes.get(0));
            for (OsmNode node : roadNodes) {
                roadsByContainedNodes.computeIfAbsent(node, n -> roadsByContainedNodes.put(n, new HashSet<>()));
                roadsByContainedNodes.get(node).add(road);
            }
        }

        for (Map.Entry<OsmNode, Set<OsmWay>> entry : roadsByContainedNodes.entrySet()) {
            OsmNode node = entry.getKey();
            Set<OsmWay> waysUsingNode = entry.getValue();
            if (waysUsingNode.size() > 1) {
                junctionOsmNodes.add(node);
            }
        }

        return junctionOsmNodes;
    }

    private static void createRoads(RoadNetwork roadNetwork, Set<OsmWay> osmRoads, Map<OsmNode, Junction> junctionsByOsmNode) {
        for (OsmWay osmRoad : osmRoads) {
            addRoadsFromOsmWayToNetwork(roadNetwork, osmRoad, junctionsByOsmNode);
        }
    }

    private static void addRoadsFromOsmWayToNetwork(RoadNetwork roadNetwork, OsmWay osmRoad,
                                                    Map<OsmNode, Junction> junctionsByOsmNode) {

        Iterator<OsmNode> roadNodeIterator = osmRoad.getNodes().iterator();
        OsmNode prevNode = roadNodeIterator.next();
        Junction startJunction = junctionsByOsmNode.get(prevNode);
        double cumulativeMetres = 0;

        while (roadNodeIterator.hasNext()) {
            // Walk along each road calculating cumulative distance. Make a road when a junction is reached.
            // This may result in an OSM way being split into multiple roads.
            OsmNode nextNode = roadNodeIterator.next();
            cumulativeMetres += CoordinateUtils.calculateDistance(prevNode.getLat(), prevNode.getLon(),
                    nextNode.getLat(), nextNode.getLon());

            if (junctionsByOsmNode.containsKey(nextNode)) {
                Junction endJunction = junctionsByOsmNode.get(nextNode);
                addRoad(osmRoad, roadNetwork, startJunction, endJunction, cumulativeMetres);
                startJunction = endJunction;
            }

            prevNode = nextNode;
        }

        if (cumulativeMetres > 0) {
            OsmNode lastNode = osmRoad.getNodes().get(osmRoad.getNodes().size() - 1);
            Junction endJunction = junctionsByOsmNode.get(lastNode);
            addRoad(osmRoad, roadNetwork, startJunction, endJunction, cumulativeMetres);
        }
    }

    private static void addRoad(OsmWay osmRoad, RoadNetwork roadNetwork, Junction startJunction, Junction endJunction,
                                double cumulativeMetres) {

        String name = Long.toString(osmRoad.getId());

        roadNetwork.createRoad(startJunction, endJunction, cumulativeMetres, name);

        boolean isOneway = osmRoad.getTags().containsKey("oneway") && osmRoad.getTags().get("oneway").contains("yes");
        if (!isOneway) {
            roadNetwork.createRoad(endJunction, startJunction, cumulativeMetres, name);
        }
    }

    private static Optional<RoadNetwork> loadDataFromCache(String cacheFileBaseName) {
        String path = cache.getExistingCachedFilePath(cacheFileBaseName);
        ObjectSerializer<RoadNetwork> serializer = new ObjectSerializer<>();

        try {
            byte[] serialized = FileUtils.readFileBytes(path);
            RoadNetwork roadNetwork = serializer.deserialize(serialized);
            return Optional.of(roadNetwork);
        } catch (IOException ex) {
            Logger.error("OsmRoadNetworkParser: Unable to load cached road network");
        }

        return Optional.empty();
    }
}
