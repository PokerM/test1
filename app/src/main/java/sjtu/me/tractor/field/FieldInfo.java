package sjtu.me.tractor.field;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FieldInfo implements Serializable {
    public static final String FIELD_ID = "fID";  // ���
    public static final String FIELD_NAME = "fName"; // ����
    public static final String FIELD_DATE = "fDate"; // ��������
    public static final String FIELD_POINT_NO = "fPNo"; // ������
    public static final String FIELD_POINT_LATITUDE = "fPLat"; // ����γ��
    public static final String FIELD_POINT_LONGITUDE = "fPLng"; // ���㾭��
    public static final String FIELD_POINT_X_COORDINATE = "fPX"; // ����X����
    public static final String FIELD_POINT_Y_COORDINATE = "fPY"; // ����Y����

    private long fieldID;   //�ؿ���
    private String fieldName;   //�ؿ�����
    private String fieldDate;   //�ؿ��������ڣ�Ҫ��ĸ�ʽ�� ��YYYY-MM-DD HH:MM:SS��,�����Ķ��������ԣ�
    private Integer fieldPointNo;   //�ؿ鶥�����
    private Double pointLatitude;   //�ؿ鶥��γ��
    private Double pointLongitude;   //�ؿ鶥�㾭��
    private Double pointXCoordinate;    //�ؿ鶥��X����
    private Double pointYCoordinate;    //�ؿ鶥��Y����

    /**
     * @return
     */
    public long getFieldID() {
        return fieldID;
    }
    
    /**
     * @param fieldID
     */
    public void setFieldID(long fieldID) {
        this.fieldID = fieldID;
    }
    
    /**
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * @param fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    /**
     * @return
     */
    public String getFieldDate() {
        return fieldDate;
    }
    
    /**
     * @param fieldDate
     */
    public void setFieldDate(String fieldDate) {
        this.fieldDate = fieldDate;
    }
    
    /**
     * @return
     */
    public Integer getFieldPointNo() {
        return fieldPointNo;
    }
    
    /**
     * @param fieldPointNo
     */
    public void setFieldPointNo(Integer fieldPointNo) {
        this.fieldPointNo = fieldPointNo;
    }
    
    /**
     * @return
     */
    public Double getPointXCoordinate() {
        return pointXCoordinate;
    }
    
    /**
     * @param pointXCoordinate
     */
    public void setPointXCoordinate(Double pointXCoordinate) {
        this.pointXCoordinate = pointXCoordinate;
    }
    
    /**
     * @return
     */
    public Double getPointYCoordinate() {
        return pointYCoordinate;
    }
    
    /**
     * @param pointYCoordinate
     */
    public void setPointYCoordinate(Double pointYCoordinate) {
        this.pointYCoordinate = pointYCoordinate;
    }
    
    /**
     * @return
     */
    public Double getPointLongitude() {
        return pointLongitude;
    }
    
    /**
     * @param pointLongitude
     */
    public void setPointLongitude(Double pointLongitude) {
        this.pointLongitude = pointLongitude;
    }
    
    /**
     * @return
     */
    public Double getPointLatitude() {
        return pointLatitude;
    }
    
    /**
     * @param pointLatitude
     */
    public void setPointLatitude(Double pointLatitude) {
        this.pointLatitude = pointLatitude;
    }
    
}
