package MacroModel.roads;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    private static Simulation simulation;

    public static void initialize(Simulation simulation) {
        Logger.simulation = simulation;
    }

    public static void info(String message) {
        System.out.println(constructMessage("INFO", message));
    }

    public static void error(String message) {
        System.out.println(constructMessage("ERROR", message));
    }

    private static String constructMessage(String type, String message) {
        return df.format(new Date()) + " (timestep " + simulation.getTimestep() + ") [" + type + "] " + message;
    }
}
