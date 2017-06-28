package MacroModel.osm.core;

import MacroModel.roads.Logger;
import MacroModel.utils.FileUtils;
import MacroModel.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheDirectory {

    private String path;
    private String fileExtension;
    private long maxCachedFileAgeMillis;
    private Map<String, Long> cachedFiles;

    public CacheDirectory(String path, String fileExtension, long maxCachedFileAgeMillis) {
        this.path = path;
        new File(path).mkdirs();
        this.cachedFiles = getCachedFiles();
        this.fileExtension = fileExtension;
        this.maxCachedFileAgeMillis = maxCachedFileAgeMillis;
    }

    public String loadFromCache(String name) {
        String filePath = getExistingCachedFilePath(name);
        try {
            List<String> lines = FileUtils.readFileLines(filePath);
            return StringUtils.mkString(lines, "");
        } catch (IOException ex) {
            Logger.error("Unable to load cached data from " + filePath);
            return "";
        }
    }

    public String getExistingCachedFilePath(String name) {
        return makeCacheFilePath(name, cachedFiles.get(name));
    }

    public void cache(String name, String data) {
        deleteIfExists(name);
        long timestamp = new Date().getTime();
        String path = makeCacheFilePath(name, timestamp);

        try {
            FileUtils.writeFile(path, data);
            cachedFiles = getCachedFiles();
        } catch (IOException ex) {
            Logger.info("Unable to write cache file: " + path);
        }
    }

    public boolean contains(String name) {
        return cachedFiles.containsKey(name);
    }

    public boolean isOutOfDate(String name) {
        return (cachedFiles.get(name) + maxCachedFileAgeMillis) < new Date().getTime();
    }

    private Map<String, Long> getCachedFiles() {
        File[] files = new File(path).listFiles();

        Map<String, Long> cachedFiles = new HashMap<>();

        for (File file : files) {
            String[] tokens = FileUtils.getNameWithoutExtension(file).split("_");
            String name = tokens[0];
            long timestamp = Long.parseLong(tokens[1]);
            cachedFiles.put(name, timestamp);
        }

        return cachedFiles;
    }

    private void deleteIfExists(String name) {
        if (cachedFiles.containsKey(name)) {
            String path = getExistingCachedFilePath(name);
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private String makeCacheFilePath(String name, long timestamp) {
        return path + name + "_" + timestamp + fileExtension;
    }

}
