package sjtu.me.tractor.planning;

/**
 * 线类
 * @author billhu
 *
 */
public class GeoLine {

    private GeoPoint P1 = new GeoPoint();
    private GeoPoint P2 = new GeoPoint();
    public GeoLine() {
        super();
    }

    public GeoLine(double X1, double Y1, double X2, double Y2) {
        this.P1.setX(X1);
        this.P1.setY(Y1);
        this.P2.setX(X2);
        this.P2.setY(Y2);
    }

    public GeoLine(GeoPoint P1, GeoPoint P2) {
        this.P1.setX(P1.getX());
        this.P1.setY(P1.getY());
        this.P2.setX(P2.getX());
        this.P2.setY(P2.getY());
    }

    public GeoPoint getP1() {
        return P1;
    }

    public GeoPoint getP2() {
        return P2;
    }

    /**
     * 获取直线方程3系数
     * 过两点的直线方程为：aX + bY + c = 0;
     * a = Y2 - Y1;
     * b = X1 - X2;
     * c = X2 * Y1 - X1 * Y2;
     * @return 系数数组
     */
    public double[] getLineCoeffients( ) {
        double a = P2.getY() - P1.getY();
        double b = P1.getX() - P2.getX();
        double c = P2.getX() * P1.getY() - P1.getX() * P2.getY();
        return new double[] {a, b, c};
    }

}
