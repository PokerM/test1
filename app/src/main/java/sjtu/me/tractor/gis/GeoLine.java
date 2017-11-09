package sjtu.me.tractor.gis;

import sjtu.me.tractor.gis.GeoPoint;

/**
 * ����
 * @author billhu
 *
 */
public class GeoLine {
    
    private int lineID = 0;          //�߱��
    private int dotNum;              //�ڵ���
    private GeoPoint[] dotSerial;    //�ڵ�����
    
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
