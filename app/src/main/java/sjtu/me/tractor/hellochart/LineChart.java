package sjtu.me.tractor.hellochart;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class LineChart {
    private LineChartView mLineChartView;

    public void setPointValues(List<PointValue> mPointValues) {
        this.mPointValues = mPointValues;
    }

    private List<PointValue> mPointValues;
    private ScaleAnimation showAnimation;
    private ScaleAnimation hideAnimation;

    public LineChart(LineChartView lineChartView) {
        super();
        this.mLineChartView = lineChartView;
        mLineChartView.setVisibility(View.INVISIBLE);
        initLineChart();

    }

    private void initLineChart() {
        
        showAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        showAnimation.setDuration(200);
        
        hideAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        hideAnimation.setDuration(200);

        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41")); // 折线的颜色（橙色）
        List<Line> lines = new ArrayList<Line>();
        line.setStrokeWidth(1); // 设置线条宽度
        line.setPointRadius(2); // 设置圆点半径
        line.setColor(Color.RED);  // 设置线条颜色
        line.setPointColor(Color.BLUE); // 设置圆点颜色
        line.setShape(ValueShape.CIRCLE);// 折线图上每个数据点的形状 这里是圆形 （有三种：方形，圆形和菱形）
        line.setCubic(false);// 曲线是否平滑，即是曲线还是折线
        line.setFilled(false);// 是否填充曲线的面积
        line.setHasLabels(false);// 曲线的数据坐标是否加上备注
        // line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);// 是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);// 是否显示圆点 如果为false 则没有圆点只有线显示

        lines.add(line);
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        // 坐标轴
        Axis axisX = new Axis(); // X轴
        axisX.setHasTiltedLabels(true); // X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setLineColor(Color.BLACK);
        axisX.setTextColor(Color.BLACK); // 设置字体颜色
        axisX.setName("时间"); // 表格名称
        axisX.setTextSize(10);// 设置字体大小
        axisX.setMaxLabelChars(15); // 设置轴标签可现实的最大字符个数，范围在0~32区间
        // axisX.setValues(mAxisXValues); //填充X轴的坐标名称
        axisX.setAutoGenerated(true); //设置是否自动生成轴对象，自动适应表格的范围
        lineChartData.setAxisXBottom(axisX); // x 轴在底部
        // lineChartData.setAxisXTop(axisX); //x 轴在顶部
        axisX.setHasLines(true); // 设置是否显示坐标网格
        axisX.setLineColor(Color.LTGRAY); //设置网格线的颜色
        axisX.setHasSeparationLine(true);  //设置是否显示轴标签与图表之间的分割线

        // Y轴是根据数据的大小自动设置Y轴上限
        Axis axisY = new Axis(); // Y轴
        axisY.setName("横向偏差");// y轴标注
        axisY.setTextColor(Color.BLACK);
        axisY.setTextSize(10);// 设置字体大小
        axisY.setHasLines(true); // 设置是否显示坐标网格
        axisY.setLineColor(Color.LTGRAY); //设置网格线的颜色
        axisY.setAutoGenerated(true); //设置是否自动生成轴对象，自动适应表格的范围
        lineChartData.setAxisYLeft(axisY); // Y轴设置在左边
        // lineChartData.setAxisYRight(axisY); //y轴设置在右边

        // 设置行为属性，支持缩放、滑动以及平移
        mLineChartView.setInteractive(true);
//        mLineChartView.setZoomType(ZoomType.HORIZONTAL);
        mLineChartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        mLineChartView.setMaxZoom((float) 3);// 最大方法比例
        mLineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        mLineChartView.setLineChartData(lineChartData);
//        mLineChartView.setVisibility(View.VISIBLE);
        /**
         * 注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */
        Viewport v = new Viewport(mLineChartView.getMaximumViewport());
        v.left = 0;
        v.right = 7;
        mLineChartView.setCurrentViewport(v);
    }
    
    public void showChartView(boolean isShow) {
        if (isShow) {
            
            mLineChartView.startAnimation(showAnimation);
            mLineChartView.setVisibility(View.VISIBLE);
        } else {
            mLineChartView.setAnimation(hideAnimation);
            mLineChartView.startAnimation(hideAnimation);
//            mLineChartView.setVisibility(View.INVISIBLE);
            mLineChartView.setVisibility(View.GONE);
        }
    }

}
