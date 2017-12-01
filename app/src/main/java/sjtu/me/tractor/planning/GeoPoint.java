package sjtu.me.tractor.planning;

/**
 * �������
 *
 * @author BillHu
 */
public class GeoPoint {

    private double xCoordinate;         //��λX����
    private double yCoordinate;         //��λY����
    private double latitude;            //��λγ�ȣ�WGS-84����ϵ
    private double longitude;           //��λ���ȣ�WGS-84����ϵ

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
