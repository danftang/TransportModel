package MacroModel.osm.core;

import java.util.Map;
import java.util.Set;

public class OsmRelation extends OsmElement {

    private RelationMembers members;

    public OsmRelation(long id, Map<String, Set<String>> tags) {
        super(id, tags);
    }

    public void setRelationMembers(RelationMembers members) {
        this.members = members;
    }

    public RelationMembers getMembers() {
        return members;
    }
}
