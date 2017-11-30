package sjtu.me.tractor.surfaceview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sjtu.me.tractor.R;
import sjtu.me.tractor.gis.GeoLine;
import sjtu.me.tractor.gis.GeoPoint;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.graphics.Paint.Style;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author billhu
 */

/**
 * @author BillHu
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private final static String TAG = "MySurfaceView";
    private final static boolean D = false;
    private boolean isDrawing = false;
    private boolean isDrawingAB = false;
    private boolean isOnHistoryDrawing = false;
    private boolean isDrawingPlannedPath = false;
    private boolean isDrawingPath = false;
    private boolean isDrawingField = false;

    private SurfaceHolder holder;
    private Canvas canvas;
    private Path mPath;
    private Path lastPath;
    private Path historyPath;
    private Path headlandPath1;
    private Path headlandPath2;
    private Path planedPath;
    private Path fieldBoundPath; //地块边界
    private Paint pathLinePaint; //拖拉机中心轨迹画笔
    private Paint operationPathPaint; //作业轨迹画笔
    private Paint prePathPaint;  //前一个轨迹画笔
    private Paint historyPathPaint;  //历史轨迹画笔
    private Paint endCirclePaint; //圆点画笔
    private Paint fieldBoundPaint;
    private Paint fieldShaderPaint;
    private Paint paintABLine;
    private Paint paintPlannedLines;
    private Paint paintHeadland;
    private Resources res = getResources();
    private Bitmap tractorMarker = BitmapFactory.decodeResource(res, R.drawable.tractor_marker);
    //    private Bitmap grassTexture = BitmapFactory.decodeResource(res, R.drawable.field_texture);
    private Bitmap grassTexture = BitmapFactory.decodeResource(res, R.drawable.grass_texture);
    private Bitmap pointAMarker = BitmapFactory.decodeResource(res, R.drawable.point_a);
    private Bitmap pointBMarker = BitmapFactory.decodeResource(res, R.drawable.point_b);
    private int markerWidth;
    private int markerHeight;
    private int pointAWidth;
    private int pointAHeight;
    private int pointBWidth;
    private int pointBHeight;

    private int pointNo;
    private int pathWidth = 45;
    private int pathPointNo = 0;
    private double fieldX, fieldY; //田地坐标
    private double fieldOriginX, fieldOriginY; //田地原点坐标
    private double scale = 1; //绘制地图比例尺
    private float fieldWidth = 60;
    private float fieldLength = 60;

    private int viewX, viewY; //视图坐标
    private int viewAX, viewAY, viewBX, viewBY; //视图A、B点坐标
    private int lineStartX, lineStartY, lineEndX, lineEndY; //视图A、B点坐标
    private final int canvasMargin = 40;
    private int canvasWidth = 1400;
    private int canvasHeight = 1300;


    /**
     * @param context
     */
    public MySurfaceView(Context context) {
        super(context);
        if (D) {
            Log.e(TAG, "MySurfaceView(Context context)被调用");
        }
        holder = getHolder();
        holder.addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }

    /**
     * @param context
     * @param attrs
     */
    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (D) {
            Log.e(TAG, "MySurfaceView(Context context,AttributeSet attrs)被调用");
        }
        holder = getHolder();
        holder.addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        initializeViews();
    }

    /**
     * 初始化SurfaceView画笔
     */
    private void initializeViews() {
        if (D) {
            Log.e(TAG, "initView()被调用");
        }

        markerWidth = tractorMarker.getWidth();
        markerHeight = tractorMarker.getHeight();
        pointAWidth = pointAMarker.getWidth();
        pointAHeight = pointAMarker.getHeight();
        pointBWidth = pointBMarker.getWidth();
        pointBHeight = pointBMarker.getHeight();

        isDrawing = true;

        mPath = new Path();
        lastPath = new Path();
        fieldBoundPath = new Path();
        historyPath = new Path();
        planedPath = new Path();
        headlandPath1 = new Path();
        headlandPath2 = new Path();

        pathLinePaint = new Paint();
        pathLinePaint.setAntiAlias(true);
        pathLinePaint.setDither(true);
        pathLinePaint.setColor(Color.WHITE);
        pathLinePaint.setStyle(Style.STROKE);
        pathLinePaint.setStrokeWidth(3);

        operationPathPaint = new Paint();
        operationPathPaint.setAntiAlias(true);
        operationPathPaint.setDither(true);
        operationPathPaint.setColor(0x88ffff00);
        operationPathPaint.setStyle(Style.STROKE);
        operationPathPaint.setStrokeWidth(pathWidth);

        prePathPaint = new Paint();
        prePathPaint.setAntiAlias(true);
        prePathPaint.setColor(Color.YELLOW);
        prePathPaint.setStyle(Style.STROKE);
        prePathPaint.setStrokeWidth(5);

        historyPathPaint = new Paint();
        historyPathPaint.setAntiAlias(true);
        historyPathPaint.setColor(Color.WHITE);
        historyPathPaint.setStyle(Style.STROKE);
        historyPathPaint.setStrokeWidth(2);

        endCirclePaint = new Paint();
        endCirclePaint.setAntiAlias(true);
        endCirclePaint.setColor(Color.BLUE);

        fieldBoundPaint = new Paint();
        fieldBoundPaint.setAntiAlias(true);
        fieldBoundPaint.setColor(Color.RED);
        fieldBoundPaint.setStyle(Style.STROKE);
        fieldBoundPaint.setStrokeWidth(30);
        PathEffect effect = new DashPathEffect(new float[]{4,4}, 1);
        fieldBoundPaint.setPathEffect(effect);

        fieldShaderPaint = new Paint();
        Shader mShader = new BitmapShader(grassTexture, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        fieldShaderPaint.setShader(mShader);

        paintHeadland = new Paint();
        paintHeadland.setAntiAlias(true);
        paintHeadland.setColor(Color.GRAY);
        paintHeadland.setStyle(Style.FILL);

        paintABLine = new Paint();
        paintABLine.setAntiAlias(true);
        paintABLine.setColor(Color.RED);
        paintABLine.setStyle(Style.STROKE);
        paintABLine.setStrokeWidth(3);

        paintPlannedLines = new Paint();
        paintPlannedLines.setAntiAlias(true);
        paintPlannedLines.setColor(Color.BLACK);
        paintPlannedLines.setStyle(Style.STROKE);
        PathEffect effect2 = new DashPathEffect(new float[]{4, 4,}, 1);
        paintPlannedLines.setPathEffect(effect2);
        paintPlannedLines.setStrokeWidth(4);

    }

    /**
     * 设置当前坐标点
     *
     * @param n 当前点序号
     * @param x 当前点地理X坐标
     * @param y 当前点地理Y坐标
     */
    public void setCurrentPoint(int n, double x, double y) {
        if (D) {
            Log.e(TAG, "setCurrentPoint(int n,double x,double y)被调用");
        }

        this.pointNo = n;
        this.fieldX = x;
        this.fieldY = y;
    }

    /**
     * 设置当初始坐标点
     *
     * @param x 当前点地理X坐标
     * @param y 当前点地理Y坐标
     */
    public void setInitPoint(double x, double y) {
        this.fieldX = x;
        this.fieldY = y;
    }

    /**
     * 设置田地在地图中显示的大小
     *
     * @param xMap 田地轮廓X坐标
     * @param yMap 田地轮廓Y坐标
     * @param df   是否绘制田地轮廓
     */
    public void setFieldBoundary(HashMap<Integer, Double> xMap, HashMap<Integer, Double> yMap, boolean df) {
        if (D) {
            Log.e(TAG, "setFieldViewSize(HashMap<Integer,Double> xMap, HashMap<Integer,Double> yMap, boolean df)被调用");
        }

        double[] xx = {xMap.get(1), xMap.get(2), xMap.get(3), xMap.get(4)};
        double[] yy = {yMap.get(1), yMap.get(2), yMap.get(3), yMap.get(4)};
        //设置X方向最大跨度为场地宽度
        this.fieldWidth = (float) (maximum(xx) - minimum(xx));
        //设置Y方向最大跨度为场地长度
        this.fieldLength = (float) (maximum(yy) - minimum(yy));
        setOrigin((maximum(xx) + minimum(xx)) / 2, (maximum(yy) + minimum(yy)) / 2);

//    	//刷新地图比例尺
        scale = calculateScale(fieldLength, fieldWidth, canvasHeight - canvasMargin, canvasWidth - canvasMargin);
        if (D) {
            Log.e(TAG, "比例尺：" + scale);
        }

        this.isDrawingField = df;

        fieldBoundPath.reset();
        fieldBoundPath.moveTo(fieldToImage(xMap.get(1), yMap.get(1))[0], fieldToImage(xMap.get(1), yMap.get(1))[1]);
        fieldBoundPath.lineTo(fieldToImage(xMap.get(2), yMap.get(2))[0], fieldToImage(xMap.get(2), yMap.get(2))[1]);
        fieldBoundPath.lineTo(fieldToImage(xMap.get(3), yMap.get(3))[0], fieldToImage(xMap.get(3), yMap.get(3))[1]);
        fieldBoundPath.lineTo(fieldToImage(xMap.get(4), yMap.get(4))[0], fieldToImage(xMap.get(4), yMap.get(4))[1]);
        fieldBoundPath.close();
    }

    /**
     * 设置田地在地图中显示的大小
     *
     * @param fieldVertex 田地轮廓顶点列表
     * @param df          是否绘制田地轮廓
     * @return 成功标志
     */
    public boolean setFieldBoundary(ArrayList<GeoPoint> fieldVertex, boolean df) {
        if (D) {
            Log.e(TAG, "setFieldBoundary(ArrayList<GeoPoint> fieldVertex, boolean df)被调用");
        }

        if (fieldVertex.size() < 3) {
            return false;
        }

        double[] xx = new double[fieldVertex.size()];
        for (int i = 0; i < fieldVertex.size(); i++) {
            xx[i] = fieldVertex.get(i).getX();
        }

        double[] yy = new double[fieldVertex.size()];
        for (int i = 0; i < fieldVertex.size(); i++) {
            yy[i] = fieldVertex.get(i).getY();
        }
        double minXX = minimum(xx);
        double maxXX = maximum(xx);
        double minYY = minimum(yy);
        double maxYY = maximum(yy);

       /* 如果给定的地块顶点坐标相距太近，则返回设置边界失败 */
        if (Math.abs(minXX - maxXX) < 1.0 != Math.abs(minYY - maxYY) < 1.0) {
            return false;
        }

        // 设置X方向最大跨度为场地宽度
        this.fieldWidth = (float) (maxXX - minXX);

        // 设置Y方向最大跨度为场地长度
        this.fieldLength = (float) (maxYY - minYY);

        // 设置原点为最大包络矩形中心
        setOrigin((minXX + maxXX) / 2, (minYY + maxYY) / 2);

        // 更新地图比例尺
        scale = calculateScale(fieldLength, fieldWidth, canvasHeight - canvasMargin, canvasWidth - canvasMargin);
        if (D) {
            Log.e(TAG, "比例尺：" + scale);
        }

        this.isDrawingField = df;

        fieldBoundPath.reset();
        fieldBoundPath.moveTo(fieldToImage(fieldVertex.get(0))[0], fieldToImage(fieldVertex.get(0))[1]);
        for (int i = 1; i < fieldVertex.size(); i++) {
            fieldBoundPath.lineTo(fieldToImage(fieldVertex.get(i))[0], fieldToImage(fieldVertex.get(i))[1]);
        }
        fieldBoundPath.close();

        return true;
    }

    /**
     * 设置作业路径宽度
     *
     * @param width
     */
    public void setOperationPathWidth(double width) {
        this.pathWidth = (int) (width * scale + 0.5);
        operationPathPaint.setStrokeWidth(pathWidth);
    }

    /**
     * 设置绘图区域画布尺寸
     *
     * @param canvasHeight
     */
    public void setCanvasSize(int canvasWidth, int canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
    }

    /**
     * @return
     */
    public int getCanvasWidth() {
        return canvasWidth;
    }

    /**
     * @return
     */
    public int getCanvasHeight() {
        return canvasHeight;
    }

    /**
     * 设置地图原点
     *
     * @param oX 原点地理X坐标
     * @param oY 原点地理Y坐标
     */
    public void setOrigin(double oX, double oY) {
        if (D) {
            Log.e(TAG, "setOrigin(double oX, double oY)被调用");
        }

        this.fieldOriginX = oX;
        this.fieldOriginY = oY;

        Log.e("fOx,fOy", fieldOriginX + "," + fieldOriginY);
    }

    /**
     * 绘制地图AB线
     *
     * @param fieldAX     A点地理X坐标
     * @param fieldAY     A点地理Y坐标
     * @param fieldBX     B点地理X坐标
     * @param fieldBY     B点地理Y坐标
     * @param isDrawingAB 是否绘制AB线
     */
    public void drawABline(double fieldAX, double fieldAY, double fieldBX, double fieldBY, boolean isDrawingAB) {
        if (D) {
            Log.e(TAG, "drawABline()被调用");
        }

        this.isDrawingAB = isDrawingAB;
        viewAX = fieldToImage(fieldAX, fieldAY)[0];
        viewAY = fieldToImage(fieldAX, fieldAY)[1];
        viewBX = fieldToImage(fieldBX, fieldBY)[0];
        viewBY = fieldToImage(fieldBX, fieldBY)[1];

        if (fieldAY == fieldBY) {
            lineStartX = 0;
            lineStartY = viewAY;
            lineEndX = canvasWidth;
            lineEndY = viewAY;
        } else {
            lineStartY = canvasHeight;
            lineEndY = 0;
            double fieldStartY = fieldOriginY - (lineStartY - canvasHeight / 2) / scale;
            double fieldEndY = fieldOriginY - (lineEndY - canvasHeight / 2) / scale;
            double fieldStartX = fieldAX + (fieldStartY - fieldAY) * (fieldAX - fieldBX) / (fieldAY - fieldBY);
            double fieldEndX = fieldBX + (fieldEndY - fieldBY) * (fieldBX - fieldAX) / (fieldBY - fieldAY);
            lineStartX = fieldToImage(fieldStartX, fieldStartY)[0];
            lineEndX = fieldToImage(fieldEndX, fieldEndY)[0];
        }
    }

    /**
     * 绘制当前点移动轨迹
     *
     * @param pathPointsNo  当前点序号
     * @param isDrawingPath 是否绘制轨迹
     */
    public void drawPointToPath(int pathPointsNo, boolean isDrawingPath) {
        if (D) {
            Log.e(TAG, "drawPointToPath(int pathPointsNo, boolean isDrawingPath)被调用");
        }

        this.pathPointNo = pathPointsNo;
        this.isDrawingPath = isDrawingPath;
    }

    /**
     * 绘制历史轨迹
     *
     * @param historyPathPoints
     */
    public void drawHistoryPath(List<GeoPoint> historyPathPoints) {
        this.isOnHistoryDrawing = true;
        boolean isFirstPoint = true;
        if (historyPathPoints != null && historyPathPoints.size() != 0) {
            for (GeoPoint hPoint : historyPathPoints) {
                if (isFirstPoint) {
                    this.historyPath.reset();
                    this.historyPath.moveTo(fieldToImage(hPoint)[0], fieldToImage(hPoint)[1]);
                    isFirstPoint = false;
                } else {
                    this.historyPath.lineTo(fieldToImage(hPoint)[0], fieldToImage(hPoint)[1]);
                }
            }
        }
    }

    /**
     * 绘制地头
     *
     * @param headland1
     */
    public void drawHeadland1(List<GeoPoint> headland1) {
        this.isDrawingPlannedPath = true;
        boolean isFirstPoint = true;
        if (headland1 != null && headland1.size() != 0) {
            for (GeoPoint hPoint : headland1) {
                if (isFirstPoint) {
                    this.headlandPath1.reset();
                    this.headlandPath1.moveTo(fieldToImage(hPoint)[0], fieldToImage(hPoint)[1]);
                    isFirstPoint = false;
                } else {
                    this.headlandPath1.lineTo(fieldToImage(hPoint)[0], fieldToImage(hPoint)[1]);
                }
            }
        }
    }

    /**
     * 绘制地头
     *
     * @param headland2
     */
    public void drawHeadland2(List<GeoPoint> headland2) {
        this.isDrawingPlannedPath = true;
        boolean isFirstPoint = true;
        if (headland2 != null && headland2.size() != 0) {
            for (GeoPoint hPoint : headland2) {
                if (isFirstPoint) {
                    this.headlandPath2.reset();
                    this.headlandPath2.moveTo(fieldToImage(hPoint)[0], fieldToImage(hPoint)[1]);
                    isFirstPoint = false;
                } else {
                    this.headlandPath2.lineTo(fieldToImage(hPoint)[0], fieldToImage(hPoint)[1]);
                }
            }
        }
    }

    /**
     * 绘制规划轨迹
     *
     * @param lines
     */
    public void drawPlannedPath(List<GeoLine> lines) {
        this.isDrawingPlannedPath = true;
        boolean isFirst = true;
        boolean flag = true;
        if (lines != null && lines.size() != 0) {
            for (GeoLine line : lines) {
                if (isFirst) {
                    this.planedPath.reset();
                    this.planedPath.moveTo(fieldToImage(line.getP1())[0], fieldToImage(line.getP1())[1]);
                    this.planedPath.lineTo(fieldToImage(line.getP2())[0], fieldToImage(line.getP2())[1]);
                    isFirst = false;
                } else {
                    if (flag) {
                        this.planedPath.lineTo(fieldToImage(line.getP2())[0], fieldToImage(line.getP2())[1]);
                        this.planedPath.lineTo(fieldToImage(line.getP1())[0], fieldToImage(line.getP1())[1]);
                        flag = !flag;
                    } else {
                        this.planedPath.lineTo(fieldToImage(line.getP1())[0], fieldToImage(line.getP1())[1]);
                        this.planedPath.lineTo(fieldToImage(line.getP2())[0], fieldToImage(line.getP2())[1]);
                        flag = !flag;
                    }
                }
            }
        }
    }

    /**
     * 隐藏规划轨迹
     */
    public void hidePlannedPath() {
        this.isDrawingPlannedPath = false;
    }

    /**
     * 隐藏历史轨迹
     */
    public void hideHistoryPath() {
        this.isOnHistoryDrawing = false;
        historyPath.reset();
    }


    /**
     * 计算地图比例尺
     *
     * @param fieldLength 田地地理长度
     * @param fieldWidth  田地地理宽度
     * @param viewHeight  显示区域高度
     * @param viewWidth   显示区域宽度
     * @return 比例尺（图像尺寸比地理尺寸）
     */
    private double calculateScale(double fieldLength, double fieldWidth, float viewHeight, float viewWidth) {
        if (D) {
            Log.e(TAG, "calculateScale(double fieldLength, double fw, float vh, float vw)被调用");
        }

        if ((fieldLength / fieldWidth) < (viewHeight / viewWidth)) {
            return (viewWidth / fieldWidth);
        } else {
            return (viewHeight / fieldLength);
        }
    }

    /**
     * 把地理坐标转换为屏幕坐标
     *
     * @param fieldX 地理坐标
     * @param fieldY 田地坐标
     * @return 地图坐标点数组
     */
    private int[] fieldToImage(double fieldX, double fieldY) {

        //刷新地图比例尺
//        scale = calculateScale(fieldLength, fieldWidth, canvasHeight - canvasMargin, canvasWidth - canvasMargin);

        int imgX = canvasWidth / 2 + (int) ((fieldX - fieldOriginX) * scale + 0.5);
        int imgY = canvasHeight / 2 - (int) ((fieldY - fieldOriginY) * scale + 0.5);
        return new int[]{imgX, imgY};
    }


    /**
     * 把地理坐标转换为屏幕坐标
     *
     * @param point 地理坐标
     * @return 地图坐标点数组
     */
    private int[] fieldToImage(GeoPoint point) {

        int xV = canvasWidth / 2 + (int) ((point.getX() - fieldOriginX) * scale + 0.5);
        int yV = canvasHeight / 2 - (int) ((point.getY() - fieldOriginY) * scale + 0.5);
        return new int[]{xV, yV};
    }

    @Override
    public void run() {
        if (D) {
            Log.e(TAG, "run()被调用");
        }


        while (isDrawing) {
            long start = System.currentTimeMillis();
            //把地理坐标转换屏幕坐标开始绘图
            viewX = fieldToImage(fieldX, fieldY)[0];
            viewY = fieldToImage(fieldX, fieldY)[1];

            //draw the path
            if (isDrawingPath) {
                if (pathPointNo == 1) {
                    mPath.reset();
                    mPath.moveTo(viewX, viewY);
                } else {
                    mPath.lineTo(viewX, viewY);
                }
                pathPointNo++;
                lastPath = mPath;
            }

            drawCanvas();

            long end = System.currentTimeMillis();
            int REFRESHING_PERIOD = 100; //设置绘图刷新周期（这个周期要和数据刷新频率一致或者大于数据刷新频率）
            if (end - start < REFRESHING_PERIOD) {
                try {
                    Thread.sleep(REFRESHING_PERIOD - (end - start));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * 绘制各种曲线
     */
    public void drawCanvas() {
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                //绘制背景
                canvas.drawColor(Color.rgb(135, 206, 234)); //浅蓝色背景
                //canvas.drawColor(0x887ccd7c);

                //绘制田地
                if (isDrawingField) {
                    canvas.drawPath(fieldBoundPath, fieldBoundPaint);
                    canvas.drawPath(fieldBoundPath, fieldShaderPaint);
                }

                if (!isOnHistoryDrawing) {

                    //绘制规划路径及地头区域
                    if (isDrawingPlannedPath) {
                        canvas.drawPath(headlandPath1, paintHeadland);
                        canvas.drawPath(headlandPath2, paintHeadland);
                        canvas.drawPath(planedPath, paintPlannedLines);
                    }

                    //绘制AB线以及A、B标记
                    if (isDrawingAB) {
                        canvas.drawLine(lineStartX, lineStartY, lineEndX, lineEndY, paintABLine);
                        canvas.drawBitmap(pointAMarker, viewAX - pointAWidth / 2, viewAY - pointAHeight / 2, null);
                        canvas.drawBitmap(pointBMarker, viewBX - pointBWidth / 2, viewBY - pointBHeight / 2, null);
                    }

                    //绘制轨迹
                    if (isDrawingPath) {
//                        canvas.drawPath(mPath, operationPathPaint);
                        canvas.drawPath(mPath, pathLinePaint);
                    } else {
                        canvas.drawPath(lastPath, prePathPaint);
                    }

                    //绘制拖拉机即时位置标志
                    canvas.drawCircle(viewX, viewY, 7, endCirclePaint);
                    canvas.drawBitmap(tractorMarker, viewX - markerWidth / 2, viewY - markerHeight, null);

                } else {

                    canvas.drawPath(historyPath, historyPathPaint);
//                    canvas.drawPath(historyPath, operationPathPaint);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * @param isDrawing 是否开启绘图标志
     */
    public void setOnDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    /**
     * 求最大值
     *
     * @param numbers 待比较数组
     * @return 最大值
     */
    public double maximum(double[] numbers) {
        int n = numbers.length;
        double max = numbers[0];
        for (int i = 1; i < n; i++) {
            if (max < numbers[i]) {
                max = numbers[i];
            }
        }
        return max;
    }

    /**
     * 求最小值
     *
     * @param numbers 待比较数组
     * @return 最小值
     */
    public double minimum(double[] numbers) {
        int n = numbers.length;
        double min = numbers[0];
        for (int i = 1; i < n; i++) {
            if (min > numbers[i]) {
                min = numbers[i];
            }
        }
        return min;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.isDrawing = true; //设置绘制标志为true
        new Thread(this).start(); //开启绘图线程
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDrawing = false; //把线程运行标志设置为false;
    }
}
