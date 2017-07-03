package MacroModel.osm.core;

import javax.xml.stream.XMLEventReader;

public class OsmCachedXmlData {

    private XMLEventReader xmlReader;
    private long timestamp;

    public OsmCachedXmlData(XMLEventReader xmlReader, long timestamp) {
        this.xmlReader = xmlReader;
        this.timestamp = timestamp;
    }

    public XMLEventReader getXmlReader() {
        return xmlReader;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
