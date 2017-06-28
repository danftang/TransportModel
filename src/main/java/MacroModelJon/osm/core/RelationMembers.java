package MacroModelJon.osm.core;

import java.util.List;

public class RelationMembers {

    private List<RelationMember<OsmNode>> nodes;
    private List<RelationMember<OsmWay>> ways;
    private List<RelationMember<OsmRelation>> relations;

    public RelationMembers(List<RelationMember<OsmNode>> nodes, List<RelationMember<OsmWay>> ways,
                           List<RelationMember<OsmRelation>> relations) {

        this.nodes = nodes;
        this.ways = ways;
        this.relations = relations;
    }

    public List<RelationMember<OsmNode>> getMemberNodes() {
        return nodes;
    }

    public List<RelationMember<OsmWay>> getMemberWays() {
        return ways;
    }

    public List<RelationMember<OsmRelation>> getMemberRelations() {
        return relations;
    }
}
