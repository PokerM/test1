package sjtu.me.tractor.gis;

import java.util.List;

public final class GisAlgorithm {


    /**
     * ����㵽�߶ε���̾���
     *
     * @param startX
     * @param startY
     * @param endX
     * @param endBY
     * @param pointX
     * @param pointY
     * @return
     */
    public static double distanceFromPointToLine(double startX, double startY, double endX, double endBY, double pointX, double pointY) {
        double space;
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

        return ((int) (space * 1000)) / 1000.0;
    }


    /**
     * ��������֮��ľ���
     *
     * @param point1X p1������
     * @param point1Y p1������
     * @param point2X p2������
     * @param point2Y p2������
     * @return �������
     */
    public static double distanceFromPointToPoint(double point1X, double point1Y, double point2X, double point2Y) {
        double lineLength = 0;
        lineLength = Math.sqrt((point1X - point2X) * (point1X - point2X) + (point1Y - point2Y)
                * (point1Y - point2Y));
        return lineLength;
    }

    /**
     * ��������ֱ�ߵļн�
     * ֱ��1��a1X + b1Y + c1 = 0;
     * ֱ��2��a2X + b2Y + c2 = 0;
     * ��н�Ϊ��alpha = arctan ((a1*b2-a2*b1) / (a1*a2+b1*b2))
     *
     * @param line1
     * @param line2
     * @return
     */
    public static double includedAngleBetweenTwoLines(GeoLine line1, GeoLine line2) {
        double[] co1 = line1.getLineCoeffients();
        double[] co2 = line2.getLineCoeffients();
        double numerator = co1[0] * co2[1] - co2[0] * co1[1];
        double denominator = co1[0] * co2[0] + co1[1] * co2[1];

        if (numerator == 0) {
            return 0.0;
        }

        if (denominator == 0) {
            return Math.PI / 2;
        } else {
            return Math.atan(numerator / denominator);
        }
    }

    /**
     * @param fieldVertices
     * @return
     */
    public static double[] getBoundaryLimits(List<GeoPoint> fieldVertices) {
        if (fieldVertices == null) {
            return null;
        }
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (int i = 0; i < fieldVertices.size(); i++) {
            if (minX > fieldVertices.get(i).getX()) {
                minX = fieldVertices.get(i).getX();
            }

            if (maxX < fieldVertices.get(i).getX()) {
                minX = fieldVertices.get(i).getX();
            }

            if (minY > fieldVertices.get(i).getY()) {
                minY = fieldVertices.get(i).getY();
            }

            if (maxY < fieldVertices.get(i).getY()) {
                maxY = fieldVertices.get(i).getY();
            }
        }

        return new double[]{minX, maxX, minY, maxY};
    }


    /**
     * @param fieldVertices
     * @param point
     * @return
     */
    public static boolean pointInPolygon(List<GeoPoint> fieldVertices, GeoPoint point) {
        if (fieldVertices == null || point == null) {
            return false;
        }

        int number = fieldVertices.size();
        //�߽綥��������3���ܹ�������
        if (number < 3) {
            return false;
        }

        if (point.getX() < getBoundaryLimits(fieldVertices)[0]
                || point.getX() > getBoundaryLimits(fieldVertices)[1]
                || point.getY() < getBoundaryLimits(fieldVertices)[2]
                || point.getY() > getBoundaryLimits(fieldVertices)[3]) {
            return false;
        }

        boolean isIn = false;
        double x = point.getX();
        double y = point.getY();
        for (int i = 0, j = number - 1; i < number; j = i++) {
            double yi = fieldVertices.get(i).getY();
            double yj = fieldVertices.get(j).getY();
            double xi = fieldVertices.get(i).getX();
            double xj = fieldVertices.get(j).getX();
            if (((yi < y && yj >= y) || (yj < y && yi >= y)) && (xi <= x || xj <= x)) {
                if ((xi + (y - yi) / (yj - yi) * (xj - xi) < x)) {
                    isIn = !isIn;
                }
            }
        }
        return isIn;
    }

    /**
     * @param line1
     * @param line2
     * @return
     */
    public static GeoPoint intersectionPoint(GeoLine line1, GeoLine line2) {
        /*��line1��line2������ֱ��������*/
        double[] co1 = line1.getLineCoeffients();
        double[] co2 = line2.getLineCoeffients();
        double d1 = co1[0] * co2[1] - co2[0] * co1[1];
        if (d1 == 0) {
            return null;
        } else {
            double d2 = co1[2] * co2[1] - co1[1] * co2[2];
            double d3 = co1[0] * co2[2] - co1[2] * co2[0];
            return new GeoPoint(d2 / d1, d3 / d1);
        }
    }

    public static GeoLine parallelLine(GeoLine line, double distance) {
        double[] co = line.getLineCoeffients();
        double dx = distance * co[1] / Math.sqrt(co[0] * co[0] + co[1] * co[1]);
        double dy = distance * co[0] / Math.sqrt(co[0] * co[0] + co[1] * co[1]);
        double x1 = line.getP1().getX() + dx;
        double y1 = line.getP1().getY() + dy;
        double x2 = line.getP2().getX() + dx;
        double y2 = line.getP2().getY() + dy;
        return new GeoLine(x1, y1, x2, y2);
    }

}
