package sjtu.me.tractor.planning;

import java.util.ArrayList;
import java.util.List;

import sjtu.me.tractor.gis.GeoLine;
import sjtu.me.tractor.gis.GeoPoint;
import sjtu.me.tractor.gis.GisAlgorithm;

/**
 * 用于规划路径生成
 * Created by billhu on 2017/11/23.
 */

public class PlanningPathGenerator {
    private List<GeoPoint> fieldVertices;
    private GeoLine lineAB;
    private double lineSpacing;
    private double headLineWidth;
    private List<GeoLine> generatedPathList;
    private List<GeoPoint> headLand1;
    private List<GeoPoint> headLand2;


    public PlanningPathGenerator(List<GeoPoint> fieldVertices, GeoLine ab, double space, double minTurning) {
        this.fieldVertices = fieldVertices;
        this.lineAB = ab;
        this.lineSpacing = space;
        this.headLineWidth = minTurning;
        generatedPathList = new ArrayList<>();
        headLand1 = new ArrayList<>();
        headLand2 = new ArrayList<>();
    }

    private int getMostParallelEdge(GeoLine lineAB) {
        if (fieldVertices == null || fieldVertices.size() < 2) {
            return -1;
        }
        double minIncludedAngle = 2* Math.PI;
        int index = -1;
        for (int i = 0; i < fieldVertices.size(); i++) {
            GeoPoint p1 = fieldVertices.get(i);
            GeoPoint p2 = fieldVertices.get((i + 1) % fieldVertices.size());
            double angle = GisAlgorithm.includedAngleBetweenTwoLines(lineAB, new GeoLine(p1, p2));
            if (Math.abs(angle) < minIncludedAngle) {
                minIncludedAngle = Math.abs(angle);
                index = i;
            }
        }
        return index;
    }

    private int getLongestEdge() {
        if (fieldVertices == null || fieldVertices.size() < 2) {
            return -1;
        }
        double maxLength = Double.MIN_VALUE;
        int index = -1;
        for (int i = 0; i < fieldVertices.size(); i++) {
            GeoPoint p1 = fieldVertices.get(i);
            GeoPoint p2 = fieldVertices.get((i + 1) % fieldVertices.size());
            double distance = GisAlgorithm.distanceFromPointToPoint(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            if (distance > maxLength) {
                maxLength = distance;
                index = i;
            }
        }
        return index;
    }

    private void planningField() {
        if (fieldVertices != null && fieldVertices.size() == 4 && lineSpacing > 0 && headLineWidth > 0) {
            int size = fieldVertices.size();
            if (lineAB == null) {
                int index = getLongestEdge();
                GeoLine longestEdge = new GeoLine(fieldVertices.get(index), fieldVertices.get((index + 1) % size));
                GeoLine top = new GeoLine(fieldVertices.get((index + 1) % size), fieldVertices.get((index + 2) % size));
                GeoLine topHeadlandLine = GisAlgorithm.parallelLine(top, headLineWidth);
                GeoLine lineAfterTop = new GeoLine(fieldVertices.get((index + 2) % size), fieldVertices.get((index + 3) % size));
                GeoPoint intersection1 = GisAlgorithm.intersectionPoint(longestEdge, topHeadlandLine);
                GeoPoint intersection2 = GisAlgorithm.intersectionPoint(topHeadlandLine, lineAfterTop);
                if (intersection1 == null || intersection2 == null) {
                    return;
                }
                GeoPoint center1 = new GeoPoint((intersection1.getX() + intersection2.getX()) / 2,
                        (intersection1.getY() + intersection2.getY()) / 2);
                if (!GisAlgorithm.pointInPolygon(fieldVertices, center1)) {
                    topHeadlandLine = GisAlgorithm.parallelLine(top, -1 * headLineWidth);
                    intersection1 = GisAlgorithm.intersectionPoint(longestEdge, topHeadlandLine);
                    intersection2 = GisAlgorithm.intersectionPoint(topHeadlandLine, lineAfterTop);
                }
                headLand1.clear();
                headLand1.add(intersection1);
                headLand1.add(fieldVertices.get((index + 1) % size));
                headLand1.add(fieldVertices.get((index + 2) % size));
                headLand1.add(intersection2);

                GeoLine bottom = new GeoLine(fieldVertices.get((index - 1 + size) % size), fieldVertices.get(index));
                GeoLine bottomHeadLandLine = GisAlgorithm.parallelLine(bottom, headLineWidth);
                GeoLine lineBeforeBottom = new GeoLine(fieldVertices.get((index - 2 + size) % size), fieldVertices.get((index - 1 + size) % size));
                GeoPoint intersection3 = GisAlgorithm.intersectionPoint(lineBeforeBottom, bottomHeadLandLine);
                GeoPoint intersection4 = GisAlgorithm.intersectionPoint(bottomHeadLandLine, longestEdge);
                if (intersection3 == null || intersection4 == null){
                    return;
                }
                GeoPoint center2 = new GeoPoint((intersection3.getX() + intersection4.getX()) / 2,
                        (intersection3.getY() + intersection4.getY()) / 2);
                if (!GisAlgorithm.pointInPolygon(fieldVertices, center2)) {
                    bottomHeadLandLine = GisAlgorithm.parallelLine(bottom, -1 * headLineWidth);
                    intersection3 = GisAlgorithm.intersectionPoint(lineBeforeBottom, bottomHeadLandLine);
                    intersection4 = GisAlgorithm.intersectionPoint(bottomHeadLandLine, longestEdge);
                }
                headLand2.clear();
                headLand2.add(intersection3);
                headLand2.add(fieldVertices.get((index - 1 + size) % size));
                headLand2.add(fieldVertices.get(index));
                headLand2.add(intersection4);

                double stepDistance = lineSpacing;
                GeoLine navigationLine = GisAlgorithm.parallelLine(longestEdge, stepDistance * 0.5);
                GeoPoint p1 = GisAlgorithm.intersectionPoint(navigationLine, topHeadlandLine);
                GeoPoint p2 = GisAlgorithm.intersectionPoint(bottomHeadLandLine, navigationLine);
                if (p1 == null || p2 == null) {
                    return;
                }
                GeoPoint cen = new GeoPoint((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
                if (!GisAlgorithm.pointInPolygon(fieldVertices, cen)) {
                    navigationLine = GisAlgorithm.parallelLine(longestEdge, -0.5 * stepDistance);
                    stepDistance = -1 * stepDistance;
                }

                generatedPathList.clear();
                generatedPathList.add(navigationLine);

                navigationLine = GisAlgorithm.parallelLine(lineAB, stepDistance);
                p1 = GisAlgorithm.intersectionPoint(topHeadlandLine, navigationLine);
                p2 = GisAlgorithm.intersectionPoint(bottomHeadLandLine, navigationLine);
                //延一个方向平行扫描
                while (GisAlgorithm.pointInPolygon(fieldVertices, p1)
                        && GisAlgorithm.pointInPolygon(fieldVertices, p2)) {
                    generatedPathList.add(navigationLine);
                    navigationLine = GisAlgorithm.parallelLine(navigationLine, lineSpacing);
                    p1 = GisAlgorithm.intersectionPoint(topHeadlandLine, navigationLine);
                    p2 = GisAlgorithm.intersectionPoint(bottomHeadLandLine, navigationLine);
                }


            } else {
                int index = getMostParallelEdge(lineAB);
                GeoLine edgeMostlyParallelToAB = new GeoLine(fieldVertices.get(index), fieldVertices.get((index + 1) % size));

                GeoLine top = new GeoLine(fieldVertices.get((index + 1) % size), fieldVertices.get((index + 2) % size));
                GeoLine topHeadlandLine = GisAlgorithm.parallelLine(top, headLineWidth);
                GeoLine lineAfterTop = new GeoLine(fieldVertices.get((index + 2) % size), fieldVertices.get((index + 3) % size));
                GeoPoint intersection1 = GisAlgorithm.intersectionPoint(edgeMostlyParallelToAB, topHeadlandLine);
                GeoPoint intersection2 = GisAlgorithm.intersectionPoint(topHeadlandLine, lineAfterTop);
                if (intersection1 == null || intersection2 == null) {
                    return;
                }
                GeoPoint center1 = new GeoPoint((intersection1.getX() + intersection2.getX()) / 2,
                        (intersection1.getY() + intersection2.getY()) / 2);
                if (!GisAlgorithm.pointInPolygon(fieldVertices, center1)) {
                    topHeadlandLine = GisAlgorithm.parallelLine(top, -1 * headLineWidth);
                    intersection1 = GisAlgorithm.intersectionPoint(edgeMostlyParallelToAB, topHeadlandLine);
                    intersection2 = GisAlgorithm.intersectionPoint(topHeadlandLine, lineAfterTop);
                }
                headLand1.clear();
                headLand1.add(intersection1);
                headLand1.add(fieldVertices.get((index + 1) % size));
                headLand1.add(fieldVertices.get((index + 2) % size));
                headLand1.add(intersection2);

                GeoLine bottom = new GeoLine(fieldVertices.get((index - 1 + size) % size), fieldVertices.get(index));
                GeoLine bottomHeadLandLine = GisAlgorithm.parallelLine(bottom, headLineWidth);
                GeoLine lineBeforeBottom = new GeoLine(fieldVertices.get((index - 2 + size) % size), fieldVertices.get((index - 1 + size) % size));
                GeoPoint intersection3 = GisAlgorithm.intersectionPoint(lineBeforeBottom, bottomHeadLandLine);
                GeoPoint intersection4 = GisAlgorithm.intersectionPoint(bottomHeadLandLine, edgeMostlyParallelToAB);
                if (intersection3 == null || intersection4 == null) {
                    return;
                }
                GeoPoint center2 = new GeoPoint((intersection3.getX() + intersection4.getX()) / 2,
                        (intersection3.getY() + intersection4.getY()) / 2);
                if (!GisAlgorithm.pointInPolygon(fieldVertices, center2)) {
                    bottomHeadLandLine = GisAlgorithm.parallelLine(bottom, -1 * headLineWidth);
                    intersection3 = GisAlgorithm.intersectionPoint(lineBeforeBottom, bottomHeadLandLine);
                    intersection4 = GisAlgorithm.intersectionPoint(bottomHeadLandLine, edgeMostlyParallelToAB);
                }
                headLand2.clear();
                headLand2.add(intersection3);
                headLand2.add(fieldVertices.get((index - 1 + size) % size));
                headLand2.add(fieldVertices.get(index));
                headLand2.add(intersection4);

                List<GeoLine> list1 = new ArrayList<>();
                List<GeoLine> list2 = new ArrayList<>();
                GeoLine parallelAB = GisAlgorithm.parallelLine(lineAB, lineSpacing);
                GeoPoint intersectionTop = GisAlgorithm.intersectionPoint(topHeadlandLine, parallelAB);
                GeoPoint intersectionBottom = GisAlgorithm.intersectionPoint(bottomHeadLandLine, parallelAB);
                //延一个方向平行扫描
                while (GisAlgorithm.pointInPolygon(fieldVertices, intersectionTop)
                        && GisAlgorithm.pointInPolygon(fieldVertices, intersectionBottom)) {
                    list1.add(parallelAB);
                    parallelAB = GisAlgorithm.parallelLine(parallelAB, lineSpacing);
                    intersectionTop = GisAlgorithm.intersectionPoint(topHeadlandLine, parallelAB);
                    intersectionBottom = GisAlgorithm.intersectionPoint(bottomHeadLandLine, parallelAB);
                }
                //反向平行扫描
                parallelAB = GisAlgorithm.parallelLine(lineAB, -1 * lineSpacing);
                intersectionTop = GisAlgorithm.intersectionPoint(topHeadlandLine, parallelAB);
                intersectionBottom = GisAlgorithm.intersectionPoint(bottomHeadLandLine, parallelAB);
                while (GisAlgorithm.pointInPolygon(fieldVertices, intersectionTop)
                        && GisAlgorithm.pointInPolygon(fieldVertices, intersectionBottom)) {
                    list2.add(parallelAB);
                    parallelAB = GisAlgorithm.parallelLine(parallelAB, -1 * lineSpacing);
                    intersectionTop = GisAlgorithm.intersectionPoint(topHeadlandLine, parallelAB);
                    intersectionBottom = GisAlgorithm.intersectionPoint(bottomHeadLandLine, parallelAB);
                }

                //合并list1和list2的路径
                generatedPathList.clear();
                for (int i = 0; i < list1.size(); i++) {
                    generatedPathList.add(list1.get(list1.size() - 1 - i));
                }
                generatedPathList.add(lineAB);
                generatedPathList.addAll(list2);
            }

        }
    }

    public List<GeoLine> getGeneratedPathList() {
        return generatedPathList;
    }

    public List<GeoPoint> getHeadLand1() {
        return headLand1;
    }

    public List<GeoPoint> getHeadLand2() {
        return headLand2;
    }

}
