package MacroModelJon.utils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {

    public static void writeFile(String path, String data) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter writer = new FileWriter(file);
        writer.append(data);
        writer.close();
    }

    public static List<String> readFileLines(String filePath) throws IOException {
        return new BufferedReader(new FileReader(new File(filePath))).lines().collect(Collectors.toList());
    }

    public static Document readXmlFile(String path) throws IOException, ParserConfigurationException, SAXException {
        File xmlFile = new File(path);
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        return doc;
    }

    public static String getNameWithoutExtension(File file) {
        return file.getName().substring(0, file.getName().indexOf("."));
    }
}