package MacroModel.osm.core;

import javax.xml.stream.XMLEventReader;

public class OsmXmlData {

    private XMLEventReader reader;
    private long timestamp;

    public OsmXmlData(XMLEventReader reader, long timestamp) {
        this.reader = reader;
        this.timestamp = timestamp;
    }

    public XMLEventReader getReader() {
        return reader;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
