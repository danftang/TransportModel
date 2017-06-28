package MacroModelJon.osm.core;

public class OsmMemberNode {

    private OsmNode node;
    private String role;

    public OsmMemberNode(OsmNode node, String role) {
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
