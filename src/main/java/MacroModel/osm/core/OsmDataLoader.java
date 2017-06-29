package MacroModel.osm.core;

import MacroModel.osm.BoundingBox;
import MacroModel.roads.Logger;
import MacroModel.utils.FileUtils;
import MacroModel.utils.StringUtils;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OsmDataLoader {

    private static long aWeekMillis = 7 * 24 * 60 * 60 * 1000;
    private static CacheDirectory cache = new CacheDirectory("cache/osm/", ".xml", aWeekMillis);
    private static List<String> osmEndpoints = new ArrayList<>();

    static {
        osmEndpoints.add("http://overpass-api.de/api/");
        osmEndpoints.add("http://overpass.preprocessors.osm.rambler.ru/cgi/");
    }

    static Optional<OsmXmlData> getData(BoundingBox boundingBox) {
        Logger.info("OsmDataLoader: Loading OSM data for bounding box " + boundingBox +
                " (cache file base name " + boundingBox.getCacheFileBaseName() + ")");

        tryUpdateCacheIfNecessary(boundingBox);

        if (cache.contains(boundingBox.getCacheFileBaseName())) {
            Logger.info("OsmDataLoader: Data successfully loaded.");
            return loadDataFromCache(boundingBox.getCacheFileBaseName());
        } else {
            Logger.error("OsmDataLoader: Could not load data from cache or web.");
            return Optional.empty();
        }
    }

    private static void tryUpdateCacheIfNecessary(BoundingBox boundingBox) {
        String cacheFileBaseName = boundingBox.getCacheFileBaseName();
        if (!cache.contains(cacheFileBaseName) || cache.isOutOfDate(cacheFileBaseName)) {
            Logger.info("OsmDataLoader: Cached OSM data is out of date. Trying to renew.");
            downloadAndCacheData(boundingBox);
        } else {
            Logger.info("OsmDataLoader: Recently cached OSM data can be loaded for the study area.");
        }
    }

    private static void downloadAndCacheData(BoundingBox boundingBox) {
        String data = "";
        for (String osmEndpoint : osmEndpoints) {
            data = downloadRawDataFromEndpoint(osmEndpoint, boundingBox);
            if (!data.isEmpty()) {
                break;
            }
        }

        if (data.isEmpty()) {
            Logger.error("OsmDataLoader: Couldn't download OSM data. Are you connected to the internet?");
        } else {
            long timestamp = new Date().getTime();
            cache.cache(boundingBox.getCacheFileBaseName(), timestamp, data);
            Logger.info("OsmDataLoader: OSM data successfully downloaded and cached");
        }
    }

    private static String downloadRawDataFromEndpoint(String osmEndpoint, BoundingBox boundingBox) {
        // For information on the query syntax, visit http://wiki.openstreetmap.org/wiki/Overpass_API/Language_Guide
        String urlStr = osmEndpoint + "interpreter?data=(node(" + boundingBox + ");<;);out%20body;";

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

    private static Optional<OsmXmlData> loadDataFromCache(String studyAreaCacheName) {
        String filePath = cache.getExistingCachedFilePath(studyAreaCacheName);
        long timestamp = cache.getExistingCachedFileTimestamp(studyAreaCacheName);

        try {
            Document xml = FileUtils.readXmlFile(filePath);
            OsmXmlData osmXmlData = new OsmXmlData(xml, timestamp);
            return Optional.of(osmXmlData);
        } catch (IOException ex) {
            Logger.error("OsmDataLoader: Could not read cached OSM file: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.error("OsmDataLoader: Could not parse cached OSM file as XML: " + ex.getMessage());
        }

        return Optional.empty();
    }
}
