package MacroModelJon.utils;

public class CoordinateUtils {

    private static final double earthEquatorialRadiusMetres = 6378137.0;

    /**
     * Calculate the distance in metres between two points on the Earth's surface defined by decimal latitude and longitude.
     */
    public static double calculateDistance(double latA, double lonA, double latB, double lonB) {
        double rLatA = Math.toRadians(latA);
        double rLatB = Math.toRadians(latB);
        double dLat = rLatB - rLatA;
        double dLon = Math.toRadians(lonB - lonA);

        double a = Math.pow(Math.sin(dLat/2.0), 2.0) + Math.pow(Math.sin(dLon/2.0), 2.0) * Math.cos(rLatA) * Math.cos(rLatB);
        a = Math.min(a, 1);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthEquatorialRadiusMetres * c;
    }
}
