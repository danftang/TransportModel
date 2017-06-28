package MacroModelJon.osm.core;

import java.util.Map;
import java.util.Set;

public class OsmNode extends OsmElement {

    private double lat;
    private double lon;

    public OsmNode(long id, double lat, double lon, Map<String, Set<String>> tags) {
        super(id, tags);
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
