package sjtu.me.tractor.tractorinfo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TractorInfo implements Serializable {

    // 定义拖拉机车辆属性名称常量，以便在不同文件中统一名称
    public static final String T_NAME = "tName";
    public static final String T_TYPE = "tractorType";
    public static final String T_MADE = "tractorMade";
    public static final String T_TYPE_NUMBER = "tractorTypeNumber";
    public static final String T_WHEELBASE = "wheelbase";
    public static final String T_ANTENNA_LATERAL = "antennaLateral";
    public static final String T_ANTENNA_REAR = "antennaRear";
    public static final String T_ANTENNA_HEIGHT = "antennaHeight";
    public static final String T_MIN_TURNING_RADIUS = "minTurningRadius";
    public static final String T_ANGLE_CORRECTION = "angleCorrection";
    public static final String T_IMPLEMENT_WIDTH = "implementWidth";
    public static final String T_IMPLEMENT_OFFSET = "implementOffset";
    public static final String T_IMPLEMENT_LENGTH = "implementLength";
    public static final String T_OPERATION_LINESPACING = "operationLineSpacing";

    private String tractorName; //拖拉机名称
    private Double wheelbase;   //拖拉机轴距
    private Double antennaLateralDeviation;  //天线横向偏差
    private Double antennaToRearAxisDeviation;  //天线到后轮轴距离
    private Double antennaHeight;   //天线高度
    private Double minTurnningRadius;   //最小拐弯半径
    private Double angularSensorCorrection;  //角度传感器校正值
    private Double implementWidth;  //农具幅宽
    private Double implementOffset;   //农机安装偏移尺寸
    private Double implementLength;   //农机机具长度
    private Double operationLineSpacing;   //作业行间距

    /**
     * 构造函数
     */
    public TractorInfo() {
        super();
    }

    /**
     * 构造函数
     *
     * @param tractorName
     * @param wheelbase
     * @param antennaLateralDeviation
     * @param antennaToRearAxisDeviation
     * @param antennaHeight
     * @param minTurnningRadius
     * @param angularSensorCorrection
     * @param implementWidth
     * @param implementOffset
     * @param implementLength
     * @param operationLineSpacing
     */
    public TractorInfo(String tractorName, Double wheelbase, Double antennaLateralDeviation,
                       Double antennaToRearAxisDeviation, Double antennaHeight, Double minTurnningRadius,
                       Double angularSensorCorrection, Double implementWidth, Double implementOffset,
                       Double implementLength, Double operationLineSpacing) {
        super();
        this.tractorName = tractorName;
        this.wheelbase = wheelbase;
        this.antennaLateralDeviation = antennaLateralDeviation;
        this.antennaToRearAxisDeviation = antennaToRearAxisDeviation;
        this.antennaHeight = antennaHeight;
        this.minTurnningRadius = minTurnningRadius;
        this.angularSensorCorrection = angularSensorCorrection;
        this.implementWidth = implementWidth;
        this.implementOffset = implementOffset;
        this.implementLength = implementLength;
        this.operationLineSpacing = operationLineSpacing;
    }

    public Double getImplementOffset() {
        return implementOffset;
    }

    public void setImplementOffset(Double implementOffset) {
        this.implementOffset = implementOffset;
    }

    public Double getImplementLength() {
        return implementLength;
    }

    public void setImplementLength(Double implementLength) {
        this.implementLength = implementLength;
    }

    public Double getOperationLineSpacing() {
        return operationLineSpacing;
    }

    public void setOperationLineSpacing(Double operationLineSpacing) {
        this.operationLineSpacing = operationLineSpacing;
    }

    /**
     * @return
     */
    public String getTractorName() {
        return tractorName;
    }

    /**
     * @param tractorName
     */
    public void setTractorName(String tractorName) {
        this.tractorName = tractorName;
    }

    /**
     * @return
     */
    public Double getWheelbase() {
        return wheelbase;
    }

    /**
     * @param wheelbase
     */
    public void setWheelbase(Double wheelbase) {
        this.wheelbase = wheelbase;
    }

    /**
     * @return
     */
    public Double getAntennaLateralDeviation() {
        return antennaLateralDeviation;
    }

    /**
     * @param antennaLateralDeviation
     */
    public void setAntennaLateralDeviation(Double antennaLateralDeviation) {
        this.antennaLateralDeviation = antennaLateralDeviation;
    }

    /**
     * @return
     */
    public Double getAntennaToRearAxisDeviation() {
        return antennaToRearAxisDeviation;
    }

    /**
     * @param antennaToRearAxisDeviation
     */
    public void setAntennaToRearAxisDeviation(Double antennaToRearAxisDeviation) {
        this.antennaToRearAxisDeviation = antennaToRearAxisDeviation;
    }

    /**
     * @return
     */
    public Double getAntennaHeight() {
        return antennaHeight;
    }

    /**
     * @param antennaHeight
     */
    public void setAntennaHeight(Double antennaHeight) {
        this.antennaHeight = antennaHeight;
    }

    /**
     * @return
     */
    public Double getMinTurnningRadius() {
        return minTurnningRadius;
    }

    /**
     * @param minTurnningRadius
     */
    public void setMinTurnningRadius(Double minTurnningRadius) {
        this.minTurnningRadius = minTurnningRadius;
    }

    /**
     * @return
     */
    public Double getAngularSensorCorrection() {
        return angularSensorCorrection;
    }

    /**
     * @param angularSensorCorrection
     */
    public void setAngularSensorCorrection(Double angularSensorCorrection) {
        this.angularSensorCorrection = angularSensorCorrection;
    }

    /**
     * @return
     */
    public Double getImplementWidth() {
        return implementWidth;
    }

    /**
     * @param implementWidth
     */
    public void setImplementWidth(Double implementWidth) {
        this.implementWidth = implementWidth;
    }


    @Override
    public String toString() {
        return this.getTractorName();
    }


}
