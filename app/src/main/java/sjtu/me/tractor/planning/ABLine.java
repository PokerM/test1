package sjtu.me.tractor.planning;

/**
 * Created by billhu on 2017/11/23.
 */

public class ABLine {
    public static final String FIELD_NAME = "fName"; // ����
    public static final String AB_LINE_DATE = "date"; // ��������
    public static final String A_POINT_X_COORDINATE = "ax"; // A��X����
    public static final String A_POINT_Y_COORDINATE = "ay"; // A��Y����
    public static final String B_POINT_X_COORDINATE = "bx"; // B��X����
    public static final String B_POINT_Y_COORDINATE = "by"; // B��Y����


    private Double aX;    //A��X����
    private Double aY;    //A��Y����
    private Double bX;    //B��X����
    private Double bY;    //B��Y����

    public ABLine(double ax, double ay, double bx, double by) {
        this.aX = ax;
        this.aY = ay;
        this.bX = bx;
        this.bY = by;
    }

    public Double getAX() {
        return aX;
    }

    public void setAX(Double aX) {
        this.aX = aX;
    }

    public Double getAY() {
        return aY;
    }

    public void setAY(Double aY) {
        this.aY = aY;
    }

    public Double getBX() {
        return bX;
    }

    public void setBX(Double bX) {
        this.bX = bX;
    }

    public Double getBY() {
        return bY;
    }

    public void setBY(Double bY) {
        this.bY = bY;
    }
}
