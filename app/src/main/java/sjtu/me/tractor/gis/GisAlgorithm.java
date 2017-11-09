package sjtu.me.tractor.gis;

public final class GisAlgorithm {
    
    
    /**
     * 计算点到线段的最短距离
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
         distanceA2B = distanceFromPointToPoint(startX, startY, endX, endBY);// 线段的长度
         distance2A = distanceFromPointToPoint(startX, startY, pointX, pointY);// (x1,y1)到点的距离
         distance2B = distanceFromPointToPoint(endX, endBY, pointX, pointY);// (x2,y2)到点的距离
         
         if (distance2B <= 0.001 || distance2A <= 0.001) {
            space = 0;
         } else if (distanceA2B <= 0.001) {
            space = distance2A;
         } else if (distance2B * distance2B >= distanceA2B * distanceA2B + distance2A * distance2A) {
            space = distance2A;
         } else if (distance2A * distance2A >= distanceA2B * distanceA2B + distance2B * distance2B) {
            space = distance2B;
         } else {
             double p = (distanceA2B + distance2A + distance2B) / 2;// 半周长
             double s = Math.sqrt(p * (p - distanceA2B) * (p - distance2A) * (p - distance2B));// 海伦公式求面积
             space = 2 * s / distanceA2B;// 返回点到线的距离（利用三角形面积公式求高）
         }
         
         return ((int) (space * 1000))/1000.0;
     }
    
    
    /**
     * 计算两点之间的距离
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
