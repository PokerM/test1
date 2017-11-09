package sjtu.me.tractor.gis;

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

    public double getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(double xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public double getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(double yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "(" + this.getXCoordinate() + "," + this.getYCoordinate() + ")";
    }


}
