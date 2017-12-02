package sjtu.me.tractor.planning;
import sjtu.me.tractor.planning.GeoPoint;

/**
 * ����
 * @author billhu
 *
 */
public class GeoField {
    
    private int fieldID = 0;         //��ر��d
    private int dotNum;              //�ڵ���
    private GeoPoint[] dotSerial;    //�ڵ�����
    
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