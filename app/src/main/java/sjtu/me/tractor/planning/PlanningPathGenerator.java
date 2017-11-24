package sjtu.me.tractor.planning;

import java.util.List;

import sjtu.me.tractor.gis.GeoPoint;

/**
 * 用于规划路径生成
 * Created by billhu on 2017/11/23.
 */

public class PlanningPathGenerator {
    private List<GeoPoint> fieldVertex;
    private ABLine abLine;
    private double lineSpacing;
    private List<GeoPoint> generatedPath;
    private List<GeoPoint> headLand1;
    private List<GeoPoint> headLand2;

//    public int getMostParalleEdge() {
//        if (fieldVertex == null || fieldVertex.size() < 2) {
//            return -1;
//        }
//        double minIncludedAnle = Double.MIN_VALUE;
//        for (int i = 0; i < fieldVertex.size(); i++) {
//            GeoPoint p1 = fieldVertex.get(i);
//            GeoPoint p2 = fieldVertex.get((i + 1) % fieldVertex.size());
//            double angle =
//        }
//    }
}
