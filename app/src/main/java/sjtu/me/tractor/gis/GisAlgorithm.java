package sjtu.me.tractor.gis;

public final class GisAlgorithm {
    
    
    /**
     * ����㵽�߶ε���̾���
     * @param startX
     * @param startY
     * @param endX
     * @param endBY
     * @param pointX
     * @param pointY
     * @return
     */
    public static double distanceFromPointToLine(double startX, double startY, double endX, double endBY, double pointX, double pointY) {
         double space = 0;
         double distanceA2B, distance2A, distance2B;
         distanceA2B = distanceFromPointToPoint(startX, startY, endX, endBY);// �߶εĳ���
         distance2A = distanceFromPointToPoint(startX, startY, pointX, pointY);// (x1,y1)����ľ���
         distance2B = distanceFromPointToPoint(endX, endBY, pointX, pointY);// (x2,y2)����ľ���
         
         if (distance2B <= 0.001 || distance2A <= 0.001) {
            space = 0;
         } else if (distanceA2B <= 0.001) {
            space = distance2A;
         } else if (distance2B * distance2B >= distanceA2B * distanceA2B + distance2A * distance2A) {
            space = distance2A;
         } else if (distance2A * distance2A >= distanceA2B * distanceA2B + distance2B * distance2B) {
            space = distance2B;
         } else {
             double p = (distanceA2B + distance2A + distance2B) / 2;// ���ܳ�
             double s = Math.sqrt(p * (p - distanceA2B) * (p - distance2A) * (p - distance2B));// ���׹�ʽ�����
             space = 2 * s / distanceA2B;// ���ص㵽�ߵľ��루���������������ʽ��ߣ�
         }
         
         return ((int) (space * 1000))/1000.0;
     }
    
    
    /**
     * ��������֮��ľ���
     * @param point1X
     * @param point1Y
     * @param point2X
     * @param point2Y
     * @return
     */
    private static double distanceFromPointToPoint(double point1X, double point1Y, double point2X, double point2Y) {
        double lineLength = 0;
        lineLength = Math.sqrt((point1X - point2X) * (point1X - point2X) + (point1Y - point2Y)
               * (point1Y - point2Y));
        return lineLength;
    }
     
}
