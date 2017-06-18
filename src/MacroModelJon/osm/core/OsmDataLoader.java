package MacroModelJon.osm.core;

import org.w3c.dom.Document;
import MacroModelJon.osm.OsmRoadNetworkParser;
import MacroModelJon.roads.Junction;
import MacroModelJon.roads.Logger;
import MacroModelJon.roads.RoadNetwork;
import MacroModelJon.roads.RouteSearch;
import MacroModelJon.utils.FileUtils;
import MacroModelJon.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class OsmDataLoader {

    private static long aWeekMillis = 7 * 24 * 60 * 60 * 1000;
    private static CacheDirectory cache = new CacheDirectory("cache/osm/", ".xml", aWeekMillis);
    private static List<String> osmEndpoints = new ArrayList<>();

    static {
        osmEndpoints.add("http://overpass-api.de/api/");
        osmEndpoints.add("http://overpass.preprocessors.osm.rambler.ru/cgi/");
    }

    private static Optional<Document> getData(double swLat, double swLon, double neLat, double neLon) {
        String studyArea = swLat + "," + swLon + "," + neLat + "," + neLon;
        String studyAreaCacheName = makeStudyAreaCacheName(studyArea);
        Logger.info("OsmDataLoader: Loading OSM data for bounding box " + swLat + ", " + swLon + " to " + neLat +
                ", " + neLon + " (cache name " + studyAreaCacheName + ")");

        if (cache.contains(studyAreaCacheName)) {
            updateCacheIfNecessary(studyAreaCacheName, studyArea);
        } else {
            downloadAndCacheData(studyArea);
        }

        if (cache.contains(studyAreaCacheName)) {
            Logger.info("OsmDataLoader: Data successfully loaded.");
            return Optional.of(loadDataFromCache(studyAreaCacheName));
        } else {
            Logger.error("OsmDataLoader: Could not load data from cache or web.");
            return Optional.empty();
        }
    }

    private static String makeStudyAreaCacheName(String studyArea) {
        return "CF" + Integer.toString(studyArea.hashCode());
    }

    private static void updateCacheIfNecessary(String studyAreaCacheName, String studyArea) {
        if (cache.isOutOfDate(studyAreaCacheName)) {
            Logger.info("OsmDataLoader: Cached OSM data is out of date. Trying to renew.");
            downloadAndCacheData(studyArea);
        } else {
            Logger.info("OsmDataLoader: Recently cached OSM data can be loaded for the study area.");
        }
    }

    private static void downloadAndCacheData(String studyArea) {
        String data = "";
        for (String osmEndpoint : osmEndpoints) {
            data = downloadRawDataFromEndpoint(osmEndpoint, studyArea);
            if (!data.isEmpty()) {
                break;
            }
        }

        if (data.isEmpty()) {
            Logger.error("OsmDataLoader: Couldn't download OSM data. Are you connected to the internet?");
        } else {
            cache.cache(makeStudyAreaCacheName(studyArea), data);
            Logger.info("OsmDataLoader: OSM data successfully downloaded and cached");
        }
    }

    private static String downloadRawDataFromEndpoint(String osmEndpoint, String studyArea) {
        // http://wiki.openstreetmap.org/wiki/Overpass_API/Language_Guide#Recursion_clauses_.28.22recursion_filters.22.29
        String urlStr = osmEndpoint + "interpreter?data=(node(" + studyArea + ");<;);out%20body;";

        String rawData = "";
        try {
            Logger.info("OsmDataLoader: Attempting to download OSM data from: " + urlStr);
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            rawData = StringUtils.mkString(in.lines().collect(Collectors.toList()), "");
            in.close();
        } catch (MalformedURLException ex) {
            Logger.error("OsmDataLoader: The URL was malformed: " + urlStr);
        } catch (IOException ex) {
            Logger.error("OsmDataLoader: Could not download data from " + urlStr);
        }

        return rawData;
    }

    private static Document loadDataFromCache(String studyAreaCacheName) {
        String filePath = cache.getExistingCachedFilePath(studyAreaCacheName);

        try {
            return FileUtils.readXmlFile(filePath);
        } catch (IOException ex) {
            Logger.error("OsmDataLoader: Could not read cached OSM file: " +  ex.getMessage());
        } catch (Exception ex) {
            Logger.error("OsmDataLoader: Could not parse cached OSM file as XML: " +  ex.getMessage());
        }

        return null;
    }

    public static void main(String[] args) {
        Optional<Document> rawData = OsmDataLoader.getData(51.0, 0, 51.1, 0.1);
        if (rawData.isPresent()) {
            OsmData data = new OsmData(rawData.get());
            RoadNetwork roadNetwork = OsmRoadNetworkParser.makeRoadNetwork(data);
            Logger.info("Made a road network with " + roadNetwork.getJunctions().size() + " junctions and " +
                    roadNetwork.getRoads().size() + " roads");

            for (int i = 0; i < 100; i++) {
                RouteSearch.Route route = getRandomRoute(roadNetwork);
                if (route.getRouteSteps().isEmpty()) {
                    Logger.info("Could not find route a route from " + route.getOrigin().getCoordinates() + " to " + route.getDestination().getCoordinates() +
                            " with cost " + route.getCost() + " and steps " + route.getRouteSteps().size());
                } else {
                    Logger.info("Found a route from " + route.getOrigin().getCoordinates() + " to " + route.getDestination().getCoordinates() +
                            " with cost " + route.getCost() + " and steps " + route.getRouteSteps().size());
                }
            }
        }
    }

    public static RouteSearch.Route getRandomRoute(RoadNetwork roadNetwork) {
        List<Junction> junctions = roadNetwork.getJunctions();
        Random random = new Random();
        Junction junctionA = junctions.get(random.nextInt(junctions.size()));
        Junction junctionB = junctions.get(random.nextInt(junctions.size()));
        return new RouteSearch().findRoute(junctionA, junctionB);
    }
}
