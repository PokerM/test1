package sjtu.me.tractor.gis;
import sjtu.me.tractor.gis.GeoPoint;

/**
 * 线类
 * @author billhu
 *
 */
public class GeoField {
    
    private int fieldID = 0;         //田地编号
    private int dotNum;              //节点数
    private GeoPoint[] dotSerial;    //节点序列
    
    public GeoField(GeoPoint[] dotSerial) {
        super();
        fieldID++;
        this.dotSerial = dotSerial;
    }

    public int getLineID() {
        return fieldID;
    }

    public void setLineID(int lineID) {
        this.fieldID = lineID;
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