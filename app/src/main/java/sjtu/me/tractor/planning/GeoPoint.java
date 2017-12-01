package sjtu.me.tractor.planning;

/**
 * 地理点类
 *
 * @author BillHu
 */
public class GeoPoint {

    private double xCoordinate;         //点位X坐标
    private double yCoordinate;         //点位Y坐标
    private double latitude;            //点位纬度，WGS-84坐标系
    private double longitude;           //点位经度，WGS-84坐标系

    public GeoPoint() {
        super();
    }

    public GeoPoint(GeoPoint point) {
        this.xCoordinate = point.xCoordinate;
        this.yCoordinate = point.yCoordinate;
    }

    public GeoPoint(double xCoordinate, double yCoordinate) {
        super();
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public GeoPoint(double latitude, double longitude, double xCoordinate, double yCoordinate) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public double getX() {
        return xCoordinate;
    }

    public void setX(double xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public double getY() {
        return yCoordinate;
    }

    public void setY(double yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public double getLat() {
        return latitude;
    }

    public void setLat(double latitude) {
        this.latitude = latitude;
    }

    public double getLng() {
        return longitude;
    }

    public void setLng(double longitude) {
        this.longitude = longitude;
    }

    public double distanceFromP(GeoPoint point) {
        return Math.sqrt(Math.pow((this.getX() - point.getX()), 2)
                + Math.pow((this.getY() - point.getY()), 2));
    }
    
    public double distanceP1FromP2(GeoPoint p1, GeoPoint p2) {
        return Math.sqrt(Math.pow((p2.getX() - p1.getX()), 2)
                + Math.pow((p2.getY() - p1.getY()), 2));
    }

    @Override
    public String toString() {
        return "(" + this.getX() + "," + this.getY() + ")";
    }


}
