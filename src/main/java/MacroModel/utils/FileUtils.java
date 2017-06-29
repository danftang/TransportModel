package MacroModel.utils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {

    public static void writeFile(String path, String data) throws IOException {
        writeFile(path, data.getBytes(StandardCharsets.UTF_8));
    }

    public static void writeFile(String path, byte[] data) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.close();
    }

    public static List<String> readFileLines(String filePath) throws IOException {
        return new BufferedReader(new FileReader(new File(filePath))).lines().collect(Collectors.toList());
    }

    public static byte[] readFileBytes(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream reader = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        reader.read(fileBytes);
        return fileBytes;
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
