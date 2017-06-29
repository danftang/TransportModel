package MacroModel.osm.core;

import MacroModel.osm.BoundingBox;
import MacroModel.roads.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

public class OsmData {

    private BoundingBox boundingBox;
    private boolean isLoaded = false;
    private long cacheTimestamp;

    private Map<Long, OsmNode> nodes = new HashMap<>();
    private Map<String, Map<String, Set<OsmNode>>> nodesByTag = new HashMap<>();
    private Map<Long, OsmWay> ways = new HashMap<>();
    private Map<String, Map<String, Set<OsmWay>>> waysByTag = new HashMap<>();
    private Map<OsmNode, Set<OsmWay>> waysByContainedNode = new HashMap<>();
    private Map<Long, OsmRelation> relations = new HashMap<>();
    private Map<String, Map<String, Set<OsmRelation>>> relationsByTag = new HashMap<>();

    public OsmData(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public long getCacheTimestamp() {
        loadIfNecessary();
        return cacheTimestamp;
    }

    public Map<Long, OsmNode> getNodes() {
        loadIfNecessary();
        return nodes;
    }

    public Map<String, Map<String, Set<OsmNode>>> getNodesByTag() {
        loadIfNecessary();
        return nodesByTag;
    }

    public Map<Long, OsmWay> getWays() {
        loadIfNecessary();
        return ways;
    }

    public Map<String, Map<String, Set<OsmWay>>> getWaysByTag() {
        loadIfNecessary();
        return waysByTag;
    }

    public Map<OsmNode, Set<OsmWay>> getWaysByContainedNode() {
        loadIfNecessary();
        return waysByContainedNode;
    }

    public Map<Long, OsmRelation> getRelations() {
        loadIfNecessary();
        return relations;
    }

    public Map<String, Map<String, Set<OsmRelation>>> getRelationsByTag() {
        loadIfNecessary();
        return relationsByTag;
    }

    private void loadIfNecessary() {
        if (!isLoaded) {
            load();
            isLoaded = true;
        }
    }

    private void load() {
        Optional<OsmXmlData> osmXmlDataOpt = OsmDataLoader.getData(boundingBox);

        if (osmXmlDataOpt.isPresent()) {
            cacheTimestamp = osmXmlDataOpt.get().getTimestamp();
            Document xml = osmXmlDataOpt.get().getXmlDocument();
            parseNodes(xml);
            parseWays(xml);
            parseRelations(xml);

            Logger.info("OSMData: Finished indexing " + nodes.size() + " nodes, " + ways.size() + " ways and " +
                    relations.size() + " relations");
        } else {
            Logger.error("OSMData: Unable to load raw data");
        }
    }

    private void parseNodes(Document osmXmlData) {
        NodeList nodesRaw = osmXmlData.getElementsByTagName("node");

        for (int i = 0; i < nodesRaw.getLength(); i++) {
            Element node = (Element) nodesRaw.item(i);
            long id = Long.parseLong(node.getAttribute("id"));
            double lat = Double.parseDouble(node.getAttribute("lat"));
            double lon = Double.parseDouble(node.getAttribute("lon"));
            Map<String, Set<String>> tags = parseTags(node, id);
            OsmNode osmNode = new OsmNode(id, lat, lon, tags);
            nodes.put(id, osmNode);
            indexNodeByTags(osmNode);
        }
    }

    private Map<String, Set<String>> parseTags(Element elem, long id) {
        NodeList tagElems = elem.getElementsByTagName("tag");
        Map<String, Set<String>> tags = new HashMap<>();

        for (int i = 0; i < tagElems.getLength(); i++) {
            Element tag = (Element) tagElems.item(i);
            String key = tag.getAttribute("k");
            String value = tag.getAttribute("v");

            tags.computeIfAbsent(key, k -> tags.put(k, new HashSet<>()));
            tags.get(key).add(value);
        }

        return tags;
    }

    private void indexNodeByTags(OsmNode osmNode) {
        Map<String, Set<String>> tags = osmNode.getTags();
        for (Map.Entry<String, Set<String>> entry : tags.entrySet()) {
            String key = entry.getKey();
            nodesByTag.computeIfAbsent(key, k -> nodesByTag.put(k, new HashMap<>()));
            Map<String, Set<OsmNode>> allValuesForKey = nodesByTag.get(key);

            for (String value : entry.getValue()) {
                allValuesForKey.computeIfAbsent(value, v -> allValuesForKey.put(v, new HashSet<>()));
                allValuesForKey.get(value).add(osmNode);
            }
        }
    }

    private void parseWays(Document osmXmlData) {
        NodeList waysRaw = osmXmlData.getElementsByTagName("way");

        for (int i = 0; i < waysRaw.getLength(); i++) {
            Element osmWay = (Element) waysRaw.item(i);
            long id = Long.parseLong(osmWay.getAttribute("id"));
            List<OsmNode> wayNodes = parseWayNodes(osmWay, id);
            if (wayNodes.size() > 1) {
                Map<String, Set<String>> tags = parseTags(osmWay, id);
                OsmWay way = new OsmWay(id, wayNodes, tags);
                ways.put(id, way);
                indexWayByTags(way);
                indexWayByContainedNodes(way);
            }
        }
    }

    private List<OsmNode> parseWayNodes(Element osmWay, long wayId) {
        NodeList wayNodeElems = osmWay.getElementsByTagName("nd");
        List<OsmNode> wayNodes = new ArrayList<>();
        for (int i = 0; i < wayNodeElems.getLength(); i++) {
            Element wayNode = (Element) wayNodeElems.item(i);
            long id = Long.parseLong(wayNode.getAttribute("ref"));
            OsmNode node = nodes.get(id);
            if (node == null) {
                continue;
            }
            wayNodes.add(node);
        }

        return wayNodes;
    }

    private void indexWayByTags(OsmWay osmWay) {
        Map<String, Set<String>> tags = osmWay.getTags();
        for (Map.Entry<String, Set<String>> entry : tags.entrySet()) {
            String key = entry.getKey();
            waysByTag.computeIfAbsent(key, k -> waysByTag.put(k, new HashMap<>()));
            Map<String, Set<OsmWay>> allValuesForKey = waysByTag.get(key);

            for (String value : entry.getValue()) {
                allValuesForKey.computeIfAbsent(value, v -> allValuesForKey.put(v, new HashSet<>()));
                allValuesForKey.get(value).add(osmWay);
            }
        }
    }

    private void indexWayByContainedNodes(OsmWay osmWay) {
        for (OsmNode wayNode : osmWay.getNodes()) {
            waysByContainedNode.computeIfAbsent(wayNode, wn -> new HashSet<>());
            waysByContainedNode.get(wayNode).add(osmWay);
        }
    }

    private void parseRelations(Document osmXmlData) {
        NodeList relationsRaw = osmXmlData.getElementsByTagName("relation");

        Map<Long, NodeList> membersByRelationId = new HashMap<>();

        for (int i = 0; i < relationsRaw.getLength(); i++) {
            Element relation = (Element) relationsRaw.item(i);
            long id = Long.parseLong(relation.getAttribute("id"));
            Map<String, Set<String>> tags = parseTags(relation, id);
            OsmRelation osmRelation = new OsmRelation(id, tags);
            relations.put(id, osmRelation);
            indexRelationByTags(osmRelation);

            // Stored for processing after all relations have been instantiated so that objects can be referenced.
            NodeList membersRaw = relation.getElementsByTagName("member");
            membersByRelationId.put(id, membersRaw);
        }

        // Processed after all relations have been instantiated so that objects can be referenced.
        for (Map.Entry<Long, NodeList> entry : membersByRelationId.entrySet()) {
            long id = entry.getKey();
            NodeList membersRaw = entry.getValue();
            RelationMembers relationMembers = parseRelationMembers(membersRaw);
            OsmRelation relation = relations.get(id);
            relation.setRelationMembers(relationMembers);
        }
    }

    private RelationMembers parseRelationMembers(NodeList membersRaw) {
        List<RelationMember<OsmNode>> memberNodes = new ArrayList<>();
        List<RelationMember<OsmWay>> memberWays = new ArrayList<>();
        List<RelationMember<OsmRelation>> memberRelations = new ArrayList<>();

        for (int i = 0; i < membersRaw.getLength(); i++) {
            Element member = (Element) membersRaw.item(i);
            String type = member.getAttribute("type");
            long id = Long.parseLong(member.getAttribute("ref"));
            String role = member.getAttribute("role");

            switch (type) {
                case "node":
                    if (nodes.containsKey(id)) {
                        new RelationMember<>(nodes.get(id), role);
                    }
                    break;
                case "way":
                    if (ways.containsKey(id)) {
                        new RelationMember<>(ways.get(id), role);
                    }
                    break;
                case "relation":
                    if (relations.containsKey(id)) {
                        new RelationMember<>(relations.get(id), role);
                    }
                    break;
                default:
            }
        }

        return new RelationMembers(memberNodes, memberWays, memberRelations);
    }

    private void indexRelationByTags(OsmRelation relation) {
        Map<String, Set<String>> tags = relation.getTags();
        for (Map.Entry<String, Set<String>> entry : tags.entrySet()) {
            String key = entry.getKey();
            relationsByTag.computeIfAbsent(key, k -> relationsByTag.put(k, new HashMap<>()));
            Map<String, Set<OsmRelation>> allValuesForKey = relationsByTag.get(key);

            for (String value : entry.getValue()) {
                allValuesForKey.computeIfAbsent(value, v -> allValuesForKey.put(v, new HashSet<>()));
                allValuesForKey.get(value).add(relation);
            }
        }
    }
}
