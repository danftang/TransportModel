package MacroModel.osm.core;

import MacroModel.roads.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

public class OsmData {

    private Map<Long, OsmNode> nodes = new HashMap<>();
    private Map<String, Map<String, Set<OsmNode>>> nodesByTag = new HashMap<>();
    private Map<Long, OsmWay> ways = new HashMap<>();
    private Map<String, Map<String, Set<OsmWay>>> waysByTag = new HashMap<>();
    private Map<OsmNode, Set<OsmWay>> waysByContainedNode = new HashMap<>();
    private Map<Long, OsmRelation> relations = new HashMap<>();
    private Map<String, Map<String, Set<OsmRelation>>> relationsByTag = new HashMap<>();

    private Map<OsmRelation, List<XMLEvent>> relationRawContents = new HashMap<>();

    public OsmData(XMLEventReader xmlReader) {
        parseXml(xmlReader);
        processMembersForAllRelations();

        Logger.info("OSMData: Finished parsing and indexing " + nodes.size() + " nodes, " + ways.size() +
                " ways and " + relations.size() + " relations");
    }

    public Map<Long, OsmNode> getNodes() {
        return nodes;
    }

    public Map<String, Map<String, Set<OsmNode>>> getNodesByTag() {
        return nodesByTag;
    }

    public Map<Long, OsmWay> getWays() {
        return ways;
    }

    public Map<String, Map<String, Set<OsmWay>>> getWaysByTag() {
        return waysByTag;
    }

    public Map<OsmNode, Set<OsmWay>> getWaysByContainedNode() {
        return waysByContainedNode;
    }

    public Map<Long, OsmRelation> getRelations() {
        return relations;
    }

    public Map<String, Map<String, Set<OsmRelation>>> getRelationsByTag() {
        return relationsByTag;
    }

    private void parseXml(XMLEventReader xmlReader) {
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
    }

    private void parseNode(StartElement startElement, XMLEventReader xmlReader) throws XMLStreamException {
        long id = Long.parseLong(startElement.getAttributeByName(new QName("id")).getValue());
        double lat = Double.parseDouble(startElement.getAttributeByName(new QName("lat")).getValue());
        double lon = Double.parseDouble(startElement.getAttributeByName(new QName("lon")).getValue());
        List<XMLEvent> containedEvents = getEventsBeforeElementEnd("node", xmlReader);
        Map<String, Set<String>> tags = parseTags(containedEvents);
        OsmNode osmNode = new OsmNode(id, lat, lon, tags);
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

        return events;
    }

    private boolean isEndElementOfType(String elementType, XMLEvent xmlEvent) {
        return xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(elementType);
    }

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
