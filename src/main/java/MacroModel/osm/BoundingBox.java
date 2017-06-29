package MacroModel.osm;

public class BoundingBox {

    private double swLat;
    private double swLon;
    private double neLat;
    private double neLon;
    private String cacheFileNameBase;

    public BoundingBox(double swLat, double swLon, double neLat, double neLon) {
        this.swLat = swLat;
        this.swLon = swLon;
        this.neLat = neLat;
        this.neLon = neLon;
        this.cacheFileNameBase = "CF" + Integer.toString(toString().hashCode());
    }

    public double getSwLat() {
        return swLat;
    }

    public double getSwLon() {
        return swLon;
    }

    public double getNeLat() {
        return neLat;
    }

    public double getNeLon() {
        return neLon;
    }

    public String getCacheFileBaseName() {
        return this.cacheFileNameBase;
    }

    @Override
    public String toString() {
        return swLat + "," + swLon + "," + neLat + "," + neLon;
    }
}
