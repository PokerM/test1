package sjtu.me.tractor.planning;

/**
 * Created by billhu on 2017/11/23.
 */

public class ABLine {
    public static final String FIELD_NAME = "fName"; // 名称
    public static final String AB_LINE_DATE = "date"; // 创建日期
    public static final String A_POINT_X_COORDINATE = "ax"; // A点X坐标
    public static final String A_POINT_Y_COORDINATE = "ay"; // A点Y坐标
    public static final String B_POINT_X_COORDINATE = "bx"; // B点X坐标
    public static final String B_POINT_Y_COORDINATE = "by"; // B点Y坐标


    private Double aX;    //A点X坐标
    private Double aY;    //A点Y坐标
    private Double bX;    //B点X坐标
    private Double bY;    //B点Y坐标

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
