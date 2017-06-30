package MacroModel.osm.core;

import MacroModel.osm.BoundingBox;
import MacroModel.roads.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
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

    private Map<OsmRelation, List<XMLEvent>> relationRawContents = new HashMap<>();

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
            XMLEventReader xmlReader = osmXmlDataOpt.get().getReader();

            while (xmlReader.hasNext()) {
                try {
                    XMLEvent xmlEvent = xmlReader.nextEvent();
                    if (xmlEvent.isStartElement()) {
                        StartElement startElement = xmlEvent.asStartElement();
                        switch (startElement.getName().getLocalPart()) {
                            case "node":
                                parseNode(startElement, xmlReader);
                                break;
                            case "way":
                                parseWay(startElement, xmlReader);
                                break;
                            case "relation":
                                parseRelation(startElement, xmlReader);
                                break;
                            default:
                                break;
                        }
                    }
                } catch (XMLStreamException ex) {
                    ex.printStackTrace();
                }

            }

            processMembersForAllRelations();

            Logger.info("OSMData: Finished indexing " + nodes.size() + " nodes, " + ways.size() + " ways and " +
                    relations.size() + " relations");
        } else {
            Logger.error("OSMData: Unable to load raw data");
        }
    }

    private void parseNode(StartElement startElement, XMLEventReader xmlReader) throws XMLStreamException {
        long id = Long.parseLong(startElement.getAttributeByName(new QName("id")).getValue());
        double lat = Double.parseDouble(startElement.getAttributeByName(new QName("lat")).getValue());
        double lon = Double.parseDouble(startElement.getAttributeByName(new QName("lon")).getValue());
        List<XMLEvent> containedEvents = getEventsBeforeElementEnd("node", xmlReader);
        Map<String, Set<String>> tags = parseTags(containedEvents);
        OsmNode osmNode = new OsmNode(id, lat, lon, tags);


        String tagsStr = "";
        for (Map.Entry<String, Set<String>> entries : tags.entrySet()) {
            String key = entries.getKey();
            for (String value : entries.getValue()) {
                tagsStr += " " + key + "=" + value;
            }
        }

        Logger.info("Parsed node: " + id + ", " + lat + ", " + lon + ", " + tagsStr);

        nodes.put(id, osmNode);
        indexNodeByTags(osmNode);
    }

    private List<XMLEvent> getEventsBeforeElementEnd(String elementType, XMLEventReader xmlReader) throws XMLStreamException {
        List<XMLEvent> events = new ArrayList<>();
        XMLEvent xmlEvent = xmlReader.nextEvent();

        while (!isEndElementOfType(elementType, xmlEvent)) {
            events.add(xmlEvent);
            xmlEvent = xmlReader.nextEvent();
        }

//        while (xmlReader.hasNext()) {
//            if (isEndElementOfType(elementType, xmlEvent)) {
//                break;
//            }
//            events.add(xmlEvent);
//        }

        return events;
    }

    private boolean isEndElementOfType(String elementType, XMLEvent xmlEvent) {
        return xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(elementType);
    }

//    private Map<String, Set<String>> parseTags(String parentType, XMLEventReader xmlReader) throws XMLStreamException {
//        Map<String, Set<String>> tags = new HashMap<>();
//
//        while (xmlReader.hasNext()) {
//            XMLEvent xmlEvent = xmlReader.nextEvent();
//            if (xmlEvent.isStartElement()) {
//                StartElement startElement = xmlEvent.asStartElement();
//                String key = startElement.getAttributeByName(new QName("k")).getValue();
//                String value = startElement.getAttributeByName(new QName("v")).getValue();
//                tags.computeIfAbsent(key, k -> tags.put(key, new HashSet<>()));
//                tags.get(key).add(value);
//            }
//
//            if (xmlEvent.isEndElement()) {
//                EndElement endElement = xmlEvent.asEndElement();
//                if (endElement.getName().getLocalPart() == parentType) {
//                    break;
//                }
//            }
//        }
//
//        return tags;
//    }

    private Map<String, Set<String>> parseTags(List<XMLEvent> xmlEvents) {
        Map<String, Set<String>> tags = new HashMap<>();

        for (XMLEvent xmlEvent : xmlEvents) {
            if (isStartElementOfType("tag", xmlEvent)) {
                StartElement startElement = xmlEvent.asStartElement();
                String key = startElement.getAttributeByName(new QName("k")).getValue();
                String value = startElement.getAttributeByName(new QName("v")).getValue();
                tags.computeIfAbsent(key, k -> tags.put(key, new HashSet<>()));
                tags.get(key).add(value);
            }
        }

        return tags;
    }

    private boolean isStartElementOfType(String elementType, XMLEvent xmlEvent) {
        return xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals(elementType);
    }

    private void parseWay(StartElement startElement, XMLEventReader xmlReader) throws XMLStreamException {
        long id = Long.parseLong(startElement.getAttributeByName(new QName("id")).getValue());
        List<XMLEvent> containedEvents = getEventsBeforeElementEnd("way", xmlReader);
        List<OsmNode> wayNodes = parseWayNodes(containedEvents);
        Map<String, Set<String>> tags = parseTags(containedEvents);
        OsmWay osmWay = new OsmWay(id, wayNodes, tags);
        ways.put(id, osmWay);
        indexWayByTags(osmWay);
        indexWayByContainedNodes(osmWay);
    }

    private List<OsmNode> parseWayNodes(List<XMLEvent> xmlEvents) {
        List<OsmNode> wayNodes = new ArrayList<>();
        for (XMLEvent xmlEvent : xmlEvents) {
            if (isStartElementOfType("nd", xmlEvent)) {
                StartElement startElement = xmlEvent.asStartElement();
                long id = Long.parseLong(startElement.getAttributeByName(new QName("ref")).getValue());
                OsmNode node = nodes.get(id);
                if (node != null) {
                    wayNodes.add(node);
                }
            }
        }

        return wayNodes;
    }

    private void parseRelation(StartElement startElement, XMLEventReader xmlReader) throws XMLStreamException {
        long id = Long.parseLong(startElement.getAttributeByName(new QName("id")).getValue());
        List<XMLEvent> containedEvents = getEventsBeforeElementEnd("relation", xmlReader);
        Map<String, Set<String>> tags = parseTags(containedEvents);
        OsmRelation osmRelation = new OsmRelation(id, tags);
        relations.put(id, osmRelation);
        indexRelationByTags(osmRelation);
    }

    private void processMembersForAllRelations() {
        for (Map.Entry<OsmRelation, List<XMLEvent>> entry : relationRawContents.entrySet()) {
            OsmRelation osmRelation = entry.getKey();
            List<XMLEvent> containedEvents = entry.getValue();
            RelationMembers members = parseRelationMembers(containedEvents);
            osmRelation.setRelationMembers(members);
        }
    }

    private RelationMembers parseRelationMembers(List<XMLEvent> xmlEvents) {
        List<RelationMember<OsmNode>> memberNodes = new ArrayList<>();
        List<RelationMember<OsmWay>> memberWays = new ArrayList<>();
        List<RelationMember<OsmRelation>> memberRelations = new ArrayList<>();

        for (XMLEvent xmlEvent : xmlEvents) {
            if (isStartElementOfType("member", xmlEvent)) {
                StartElement startElement = xmlEvent.asStartElement();
                String type = startElement.getAttributeByName(new QName("type")).getValue();
                long id = Long.parseLong(startElement.getAttributeByName(new QName("ref")).getValue());
                String role = startElement.getAttributeByName(new QName("role")).getValue();

                switch (type) {
                    case "node":
                        if (nodes.containsKey(id)) {
                            memberNodes.add(new RelationMember<>(nodes.get(id), role));
                        }
                        break;
                    case "way":
                        if (ways.containsKey(id)) {
                            memberWays.add(new RelationMember<>(ways.get(id), role));
                        }
                        break;
                    case "relation":
                        if (relations.containsKey(id)) {
                            memberRelations.add(new RelationMember<>(relations.get(id), role));
                        }
                        break;
                    default:
                }
            }
        }

        return new RelationMembers(memberNodes, memberWays, memberRelations);
    }


//    private void parseNodes(Document osmXmlData) {
//        NodeList nodesRaw = osmXmlData.getElementsByTagName("node");
//
//        for (int i = 0; i < nodesRaw.getLength(); i++) {
//            Element node = (Element) nodesRaw.item(i);
//            long id = Long.parseLong(node.getAttribute("id"));
//            double lat = Double.parseDouble(node.getAttribute("lat"));
//            double lon = Double.parseDouble(node.getAttribute("lon"));
//            Map<String, Set<String>> tags = parseTags(node);
//            OsmNode osmNode = new OsmNode(id, lat, lon, tags);
//            nodes.put(id, osmNode);
//            indexNodeByTags(osmNode);
//        }
//    }

    private Map<String, Set<String>> parseTags(Element elem) {
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

//    private void parseWays(Document osmXmlData) {
//        NodeList waysRaw = osmXmlData.getElementsByTagName("way");
//
//        for (int i = 0; i < waysRaw.getLength(); i++) {
//            Element osmWay = (Element) waysRaw.item(i);
//            long id = Long.parseLong(osmWay.getAttribute("id"));
//            List<OsmNode> wayNodes = parseWayNodes(osmWay, id);
//            if (wayNodes.size() > 1) {
//                Map<String, Set<String>> tags = parseTags(osmWay);
//                OsmWay way = new OsmWay(id, wayNodes, tags);
//                ways.put(id, way);
//                indexWayByTags(way);
//                indexWayByContainedNodes(way);
//            }
//        }
//    }
//
//    private List<OsmNode> parseWayNodes(Element osmWay, long wayId) {
//        NodeList wayNodeElems = osmWay.getElementsByTagName("nd");
//        List<OsmNode> wayNodes = new ArrayList<>();
//        for (int i = 0; i < wayNodeElems.getLength(); i++) {
//            Element wayNode = (Element) wayNodeElems.item(i);
//            long id = Long.parseLong(wayNode.getAttribute("ref"));
//            OsmNode node = nodes.get(id);
//            if (node == null) {
//                continue;
//            }
//            wayNodes.add(node);
//        }
//
//        return wayNodes;
//    }

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

//    private void parseRelations(Document osmXmlData) {
//        NodeList relationsRaw = osmXmlData.getElementsByTagName("relation");
//
//        Map<Long, NodeList> membersByRelationId = new HashMap<>();
//
//        for (int i = 0; i < relationsRaw.getLength(); i++) {
//            Element relation = (Element) relationsRaw.item(i);
//            long id = Long.parseLong(relation.getAttribute("id"));
//            Map<String, Set<String>> tags = parseTags(relation);
//            OsmRelation osmRelation = new OsmRelation(id, tags);
//            relations.put(id, osmRelation);
//            indexRelationByTags(osmRelation);
//
//            // Stored for processing after all relations have been instantiated so that objects can be referenced.
//            NodeList membersRaw = relation.getElementsByTagName("member");
//            membersByRelationId.put(id, membersRaw);
//        }
//
//        // Processed after all relations have been instantiated so that objects can be referenced.
//        for (Map.Entry<Long, NodeList> entry : membersByRelationId.entrySet()) {
//            long id = entry.getKey();
//            NodeList membersRaw = entry.getValue();
//            RelationMembers relationMembers = parseRelationMembers(membersRaw);
//            OsmRelation relation = relations.get(id);
//            relation.setRelationMembers(relationMembers);
//        }
//    }
//
//    private RelationMembers parseRelationMembers(NodeList membersRaw) {
//        List<RelationMember<OsmNode>> memberNodes = new ArrayList<>();
//        List<RelationMember<OsmWay>> memberWays = new ArrayList<>();
//        List<RelationMember<OsmRelation>> memberRelations = new ArrayList<>();
//
//        for (int i = 0; i < membersRaw.getLength(); i++) {
//            Element member = (Element) membersRaw.item(i);
//            String type = member.getAttribute("type");
//            long id = Long.parseLong(member.getAttribute("ref"));
//            String role = member.getAttribute("role");
//
//            switch (type) {
//                case "node":
//                    if (nodes.containsKey(id)) {
//                        new RelationMember<>(nodes.get(id), role);
//                    }
//                    break;
//                case "way":
//                    if (ways.containsKey(id)) {
//                        new RelationMember<>(ways.get(id), role);
//                    }
//                    break;
//                case "relation":
//                    if (relations.containsKey(id)) {
//                        new RelationMember<>(relations.get(id), role);
//                    }
//                    break;
//                default:
//            }
//        }
//
//        return new RelationMembers(memberNodes, memberWays, memberRelations);
//    }

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
