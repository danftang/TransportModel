package MacroModel.osm.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OsmWay extends OsmElement {

    private List<OsmNode> nodes;

    public OsmWay(long id, List<OsmNode> nodes, Map<String, Set<String>> tags) {
        super(id, tags);
        this.nodes = nodes;
    }

    public List<OsmNode> getNodes() {
        return nodes;
    }
}
