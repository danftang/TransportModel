package MacroModel.utils;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class ObjectSerializer<T> {

    private Deflater def = new Deflater(Deflater.BEST_SPEED);

    public void serializeToFile(T obj, String filePath) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            DeflaterOutputStream zipOut = new DeflaterOutputStream(fileOut, def);
            ObjectOutputStream out = new ObjectOutputStream(zipOut);
            out.writeObject(obj);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public T deserializeFromFile(String filePath) {
        T obj = null;
        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            InflaterInputStream zipIn = new InflaterInputStream(fileIn);
            ObjectInputStream in = new ObjectInputStream(zipIn);
            obj = (T) in.readObject();
            in.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return obj;
    }

    public byte[] serialize(T obj) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        try {
            DeflaterOutputStream zipOut = new DeflaterOutputStream(byteOut, def);
            ObjectOutputStream out = new ObjectOutputStream(zipOut);
            out.writeObject(obj);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return byteOut.toByteArray();
    }

    public T deserialize(byte[] serialized) {
        T obj = null;
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(serialized);
            InflaterInputStream zipIn = new InflaterInputStream(byteIn);
            ObjectInputStream in = new ObjectInputStream(zipIn);
            obj = (T) in.readObject();
            in.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return obj;
    }
}
