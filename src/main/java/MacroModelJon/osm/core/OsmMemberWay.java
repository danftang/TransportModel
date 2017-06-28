package MacroModelJon.osm.core;

public class OsmMemberWay {

    private OsmNode node;
    private String role;

    public OsmMemberWay(OsmNode node, String role) {
        this.node = node;
        this.role = role;
    }

    public OsmNode getNode() {
        return node;
    }

    public String getRole() {
        return role;
    }
}
