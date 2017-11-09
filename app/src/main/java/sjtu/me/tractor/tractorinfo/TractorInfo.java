package sjtu.me.tractor.tractorinfo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TractorInfo implements Serializable {

    // ���������������������Ƴ������Ա��ڲ�ͬ�ļ���ͳһ����
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

    private String tractorName; //����������
    private Double wheelbase;   //���������
    private Double antennaLateralDeviation;  //���ߺ���ƫ��
    private Double antennaToRearAxisDeviation;  //���ߵ����������
    private Double antennaHeight;   //���߸߶�
    private Double minTurnningRadius;   //��С����뾶
    private Double angularSensorCorrection;  //�Ƕȴ�����У��ֵ
    private Double implementWidth;  //ũ�߷���
    private Double implementOffset;   //ũ����װƫ�Ƴߴ�
    private Double implementLength;   //ũ�����߳���
    private Double operationLineSpacing;   //��ҵ�м��

    /**
     * ���캯��
     */
    public TractorInfo() {
        super();
    }

    /**
     * ���캯��
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
