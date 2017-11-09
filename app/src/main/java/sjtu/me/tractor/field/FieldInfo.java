package sjtu.me.tractor.field;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FieldInfo implements Serializable {
    public static final String FIELD_ID = "fID";  // 编号
    public static final String FIELD_NAME = "fName"; // 名称
    public static final String FIELD_DATE = "fDate"; // 创建日期
    public static final String FIELD_POINT_NO = "fPNo"; // 顶点数
    public static final String FIELD_POINT_LATITUDE = "fPLat"; // 顶点纬度
    public static final String FIELD_POINT_LONGITUDE = "fPLng"; // 顶点经度
    public static final String FIELD_POINT_X_COORDINATE = "fPX"; // 顶点X坐标
    public static final String FIELD_POINT_Y_COORDINATE = "fPY"; // 顶点Y坐标

    private long fieldID;   //地块编号
    private String fieldName;   //地块名称
    private String fieldDate;   //地块生成日期（要求的格式是 ‘YYYY-MM-DD HH:MM:SS’,其他的东西被忽略）
    private Integer fieldPointNo;   //地块顶点序号
    private Double pointLatitude;   //地块顶点纬度
    private Double pointLongitude;   //地块顶点经度
    private Double pointXCoordinate;    //地块顶点X坐标
    private Double pointYCoordinate;    //地块顶点Y坐标

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
