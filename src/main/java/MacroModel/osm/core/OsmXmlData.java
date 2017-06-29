package MacroModel.osm.core;

import org.w3c.dom.Document;

public class OsmXmlData {

    private Document xmlDocument;
    private long timestamp;

    public OsmXmlData(Document xmlDocument, long timestamp) {
        this.xmlDocument = xmlDocument;
        this.timestamp = timestamp;
    }

    public Document getXmlDocument() {
        return xmlDocument;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
