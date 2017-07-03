package MacroModel.osm.core;

import MacroModel.roads.Logger;
import MacroModel.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
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

    public void refresh() {
        this.cachedFiles = getCachedFiles();
    }

    public String makeCacheFilePath(String cacheFileBaseName, long timestamp) {
        return path + cacheFileBaseName + "_" + timestamp + fileExtension;
    }

    public String getExistingCachedFilePath(String name) {
        return makeCacheFilePath(name, cachedFiles.get(name));
    }

    public long getExistingCachedFileTimestamp(String name) {
        return cachedFiles.get(name);
    }

    public void cache(String cacheFileBaseName, long timestamp, String data) {
        cache(cacheFileBaseName, timestamp, data.getBytes());
    }

    public void cache(String cacheFileBaseName, long timestamp, byte[] data) {
        deleteIfExists(cacheFileBaseName);
        String path = makeCacheFilePath(cacheFileBaseName, timestamp);

        try {
            FileUtils.writeFile(path, data);
            cachedFiles = getCachedFiles();
        } catch (IOException ex) {
            Logger.info("Unable to write cache file: " + path);
        }
    }

    public boolean contains(String cacheFileBaseName) {
        return cachedFiles.containsKey(cacheFileBaseName);
    }

    public boolean isOutOfDate(String cacheFileBaseName) {
        return (cachedFiles.get(cacheFileBaseName) + maxCachedFileAgeMillis) < new Date().getTime();
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

    private void deleteIfExists(String cacheFileBaseName) {
        if (cachedFiles.containsKey(cacheFileBaseName)) {
            String path = getExistingCachedFilePath(cacheFileBaseName);
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
