package sjtu.me.tractor.gis;

import sjtu.me.tractor.gis.GeoPoint;

/**
 * 线类
 * @author billhu
 *
 */
public class GeoLine {
    
    private int lineID = 0;          //线编号
    private int dotNum;              //节点数
    private GeoPoint[] dotSerial;    //节点序列
    
    public GeoLine(GeoPoint[] dotSerial) {
        super();
        lineID++;
        this.dotSerial = dotSerial;
    }

    public int getLineID() {
        return lineID;
    }

    public void setLineID(int lineID) {
        this.lineID = lineID;
    }

    public int getDotNum() {
        return dotNum;
    }

    public void setDotNum(int dotNum) {
        this.dotNum = dotNum;
    }

    public GeoPoint[] getDotSerial() {
        return dotSerial;
    }

    public void setDotSerial(GeoPoint[] dotSerial) {
        this.dotSerial = dotSerial;
    }
    
}
