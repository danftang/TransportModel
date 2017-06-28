package MacroModelJon.osm.core;

import java.util.Map;
import java.util.Set;

public class OsmElement {

    private long id;
    private Map<String, Set<String>> tags;

    public OsmElement(long id, Map<String, Set<String>> tags) {
        this.id = id;
        this.tags = tags;
    }

    public long getId() {
        return id;
    }

    public Map<String, Set<String>> getTags() {
        return tags;
    }
}
