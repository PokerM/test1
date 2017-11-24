package sjtu.me.tractor.surfaceview;

import java.util.ArrayList;
import java.util.HashMap;

import sjtu.me.tractor.R;
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
    private boolean isDrawing = true;
    private boolean isDrawingAB = false;
    private boolean isDrawingPath = false;
    private boolean isDrawingField = false;

    private SurfaceHolder holder;
    private Canvas canvas;
    private Path mPath;
    private Path lastPath;
    private Path fieldBoundPath; //�ؿ�߽�
    private Paint pathLinePaint; //���������Ĺ켣����
    private Paint operationPathPaint; //��ҵ�켣����
    private Paint pathHistoryPaint;  //��һ���켣����
    private Paint endCirclePaint; //Բ�㻭��
    private Paint fieldBoundPaint;
    private Paint fieldShaderPaint;
    private Paint paintABline;
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
    private double fieldX, fieldY; //�������
    private double APointX, APointY, BPointX, BPointY; //���A��B������
    private double originX, originY; //���ԭ������
    private double scale = 1; //���Ƶ�ͼ������
    private float fieldWidth = 60;
    private float fieldLength = 60;
    private double cotThelta;
    private float lineSpace = 0;

    private int REFRESH_PERIOD = 100;
    private int viewX, viewY; //��ͼ����
    private int viewAX, viewAY, viewBX, viewBY; //��ͼA��B������
    private final int canvasMargin = 40;
    private final int END_RECT_LENGTH = 8;
//    private int canvasWidth = 1200;
//    private int canvasHeight = 1200;

    private int canvasWidth = 1400;
    private int canvasHeight = 1300;


    /**
     * @param context
     */
    public MySurfaceView(Context context) {
        super(context);
        if (D) {
            Log.e(TAG, "MySurfaceView(Context context)������");
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
            Log.e(TAG, "MySurfaceView(Context context,AttributeSet attrs)������");
        }
        holder = getHolder();
        holder.addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        initializeViews();
    }

    /**
     * ��ʼ��SurfaceView����
     */
    private void initializeViews() {
        if (D) {
            Log.e(TAG, "initView()������");
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

        pathLinePaint = new Paint();
        pathLinePaint.setAntiAlias(true);
        pathLinePaint.setColor(Color.WHITE);
        pathLinePaint.setStyle(Style.STROKE);
        pathLinePaint.setStrokeWidth(3);

        operationPathPaint = new Paint();
        operationPathPaint.setAntiAlias(true);
        operationPathPaint.setDither(true);
        operationPathPaint.setColor(0x88ffff00);
        operationPathPaint.setStyle(Style.STROKE);
        operationPathPaint.setStrokeWidth(pathWidth);

        pathHistoryPaint = new Paint();
        pathHistoryPaint.setAntiAlias(true);
        pathHistoryPaint.setColor(Color.BLUE);
        pathHistoryPaint.setStyle(Style.STROKE);
        pathHistoryPaint.setStrokeWidth(5);

        endCirclePaint = new Paint();
        endCirclePaint.setAntiAlias(true);
        endCirclePaint.setColor(Color.YELLOW);

        fieldBoundPaint = new Paint();
        fieldBoundPaint.setAntiAlias(true);
        fieldBoundPaint.setColor(Color.RED);
        fieldBoundPaint.setStyle(Style.STROKE);
        fieldBoundPaint.setStrokeWidth(6);
        PathEffect effect = new DashPathEffect(new float[]{4, 4,}, 1);
        fieldBoundPaint.setPathEffect(effect);

        fieldShaderPaint = new Paint();
        Shader mShader = new BitmapShader(grassTexture, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        fieldShaderPaint.setShader(mShader);

        paintABline = new Paint();
        paintABline.setAntiAlias(true);
        paintABline.setColor(Color.RED);
        paintABline.setStyle(Style.STROKE);
        paintABline.setStrokeWidth(3);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * ���õ�ǰ�����
     *
     * @param n ��ǰ�����
     * @param x ��ǰ�����X����
     * @param y ��ǰ�����Y����
     */
    public void setCurentPoint(int n, double x, double y) {
        if (D) {
            Log.e(TAG, "setCurentPoint(int n,double x,double y)������");
        }

        this.pointNo = n;
        this.fieldX = x;
        this.fieldY = y;
    }

    /**
     * ���õ���ʼ�����
     *
     * @param x ��ǰ�����X����
     * @param y ��ǰ�����Y����
     */
    public void setInitPoint(double x, double y) {
        this.fieldX = x;
        this.fieldY = y;
    }

    /**
     * ��������ڵ�ͼ����ʾ�Ĵ�С
     *
     * @param xMap �������X����
     * @param yMap �������Y����
     * @param df   �Ƿ�����������
     */
    public void setFieldBoundary(HashMap<Integer, Double> xMap, HashMap<Integer, Double> yMap, boolean df) {
        if (D) {
            Log.e(TAG, "setFieldViewSize(HashMap<Integer,Double> xMap, HashMap<Integer,Double> yMap, boolean df)������");
        }

        double[] xx = {xMap.get(1), xMap.get(2), xMap.get(3), xMap.get(4)};
        double[] yy = {yMap.get(1), yMap.get(2), yMap.get(3), yMap.get(4)};
        //����X���������Ϊ���ؿ��
        this.fieldWidth = (float) (maximum(xx) - minimum(xx));
        //����Y���������Ϊ���س���
        this.fieldLength = (float) (maximum(yy) - minimum(yy));
        setOrigin((maximum(xx) + minimum(xx)) / 2, (maximum(yy) + minimum(yy)) / 2);

//    	//ˢ�µ�ͼ������
        scale = calculateScale(fieldLength, fieldWidth, canvasHeight - canvasMargin, canvasWidth - canvasMargin);
        if (D) {
            Log.e(TAG, "�����ߣ�" + scale);
        }

        this.isDrawingField = df;

        fieldBoundPath.reset();
        fieldBoundPath.moveTo(fieldToImage(xMap.get(1), yMap.get(1))[0], fieldToImage(xMap.get(1), yMap.get(1))[1]);
        fieldBoundPath.lineTo(fieldToImage(xMap.get(2), yMap.get(2))[0], fieldToImage(xMap.get(2), yMap.get(2))[1]);
        fieldBoundPath.lineTo(fieldToImage(xMap.get(3), yMap.get(3))[0], fieldToImage(xMap.get(3), yMap.get(3))[1]);
        fieldBoundPath.lineTo(fieldToImage(xMap.get(4), yMap.get(4))[0], fieldToImage(xMap.get(4), yMap.get(4))[1]);
        fieldBoundPath.close();

//    	Log.e("fWidth,fLength", fieldWidth + "," + fieldLength);
//    	Log.e("fp1x,fp1y", xMap.get(1) + "," + yMap.get(1));
//    	Log.e("fp2x,fp2y", xMap.get(2) + "," + yMap.get(2));
//    	Log.e("fp3x,fp3y", xMap.get(3) + "," + yMap.get(3));
//    	Log.e("fp4x,fp4y", xMap.get(4) + "," + yMap.get(4));
//    	Log.e("p1x,p1y", fieldToImage(xMap.get(1), yMap.get(1))[0]+ "," + fieldToImage(xMap.get(1), yMap.get(1))[1]);
//    	Log.e("p2x,p2y", fieldToImage(xMap.get(2), yMap.get(2))[0]+ "," + fieldToImage(xMap.get(2), yMap.get(2))[1]);
//    	Log.e("p3x,p3y", fieldToImage(xMap.get(3), yMap.get(3))[0]+ "," + fieldToImage(xMap.get(3), yMap.get(3))[1]);
//    	Log.e("p4x,p4y", fieldToImage(xMap.get(4), yMap.get(4))[0]+ "," + fieldToImage(xMap.get(4), yMap.get(4))[1]);

    }

    /**
     * ��������ڵ�ͼ����ʾ�Ĵ�С
     *
     * @param fieldVertex ������������б�
     * @param df          �Ƿ�����������
     * @return �ɹ���־
     */
    public boolean setFieldBoundary(ArrayList<GeoPoint> fieldVertex, boolean df) {
        if (D) {
            Log.e(TAG, "setFieldBoundary(ArrayList<GeoPoint> fieldVertex, boolean df)������");
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

       /* ��������ĵؿ鶥���������̫�����򷵻����ñ߽�ʧ�� */
        if (Math.abs(minXX - maxXX) < 1.0 != Math.abs(minYY - maxYY) < 1.0) {
            return false;
        }

        // ����X���������Ϊ���ؿ��
        this.fieldWidth = (float) (maxXX - minXX);

        // ����Y���������Ϊ���س���
        this.fieldLength = (float) (maxYY - minYY);

        // ����ԭ��Ϊ�������������
        setOrigin((minXX + maxXX) / 2, (minYY + maxYY) / 2);

        // ���µ�ͼ������
        scale = calculateScale(fieldLength, fieldWidth, canvasHeight - canvasMargin, canvasWidth - canvasMargin);
        if (D) {
            Log.e(TAG, "�����ߣ�" + scale);
        }

        this.isDrawingField = df;

        fieldBoundPath.reset();
        fieldBoundPath.moveTo(fieldToImage(fieldVertex.get(0))[0], fieldToImage(fieldVertex.get(0))[1]);
        for (int i = 1; i < fieldVertex.size(); i++) {
            fieldBoundPath.lineTo(fieldToImage(fieldVertex.get(i))[0], fieldToImage(fieldVertex.get(i))[1]);
        }
        fieldBoundPath.close();

//        Log.e("fWidth,fLength", fieldWidth + "," + fieldLength);
//        Log.e("fp1x,fp1y", fieldVertex.get(0).getX() + "," + fieldVertex.get(0).getY());
//        Log.e("fp2x,fp2y", fieldVertex.get(1).getX() + "," + fieldVertex.get(1).getY());
//        Log.e("fp3x,fp3y", fieldVertex.get(2).getX() + "," + fieldVertex.get(2).getY());
//        Log.e("fp4x,fp4y", fieldVertex.get(3).getX() + "," + fieldVertex.get(3).getY());
//        Log.e("p1x,p1y", fieldToImage(fieldVertex.get(0))[0] + "," + fieldToImage(fieldVertex.get(0))[1]);
//        Log.e("p2x,p2y", fieldToImage(fieldVertex.get(1))[0] + "," + fieldToImage(fieldVertex.get(1))[1]);
//        Log.e("p3x,p3y", fieldToImage(fieldVertex.get(2))[0] + "," + fieldToImage(fieldVertex.get(2))[1]);
//        Log.e("p4x,p4y", fieldToImage(fieldVertex.get(3))[0] + "," + fieldToImage(fieldVertex.get(3))[1]);
        return true;
    }

    /**
     * ������ҵ·�����
     * @param width
     */
    public void setOperationPathWidth(double width) {
        this.pathWidth = (int) (width * scale + 0.5);
        operationPathPaint.setStrokeWidth(pathWidth);
    }

    /**
     * ���û�ͼ���򻭲��ߴ�
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
     * ���õ�ͼԭ��
     *
     * @param oX ԭ�����X����
     * @param oY ԭ�����Y����
     */
    public void setOrigin(double oX, double oY) {
        if (D) {
            Log.e(TAG, "setOrigin(double oX, double oY)������");
        }

        this.originX = oX;
        this.originY = oY;

        Log.e("fOx,fOy", originX + "," + originY);
    }

    /**
     * ���Ƶ�ͼAB��
     *
     * @param ApX         A�����X����
     * @param ApY         A�����Y����
     * @param BpX         B�����X����
     * @param BpY         B�����Y����
     * @param isDrawingAB �Ƿ����AB��
     */
    public void drawABline(double ApX, double ApY, double BpX, double BpY, boolean isDrawingAB) {
        if (D) {
            Log.e(TAG, "drawABline()������");
        }

        this.isDrawingAB = isDrawingAB;
        this.APointX = ApX;
        this.APointY = ApY;
        this.BPointX = BpX;
        this.BPointY = BpY;

        viewAX = fieldToImage(APointX, APointY)[0];
        viewAY = fieldToImage(APointX, APointY)[1];
        viewBX = fieldToImage(BPointX, BPointY)[0];
        viewBY = fieldToImage(BPointX, BPointY)[1];

//    	viewAX = calculateABline(field2Image(APointX, APointY)[0], field2Image(APointX, APointY)[1], 
//    			field2Image(BPointX, BPointY)[0],field2Image(BPointX, BPointY)[1])[0];
//    	viewAY = calculateABline(field2Image(APointX, APointY)[0], field2Image(APointX, APointY)[1], 
//    			field2Image(BPointX, BPointY)[0],field2Image(BPointX, BPointY)[1])[1];
//    	viewBX = calculateABline(field2Image(APointX, APointY)[0], field2Image(APointX, APointY)[1], 
//    			field2Image(BPointX, BPointY)[0],field2Image(BPointX, BPointY)[1])[2];
//    	viewBY = calculateABline(field2Image(APointX, APointY)[0], field2Image(APointX, APointY)[1], 
//    			field2Image(BPointX, BPointY)[0],field2Image(BPointX, BPointY)[1])[3];

    }

    //����AB���ڻ�������ʼ�������
    public float[] calculateABline(float aX, float aY, float bX, float bY) {
        if (D) {
            Log.e(TAG, "calculateABline(float aX, float aY, float bX, float bY)������");
        }

        float startX, startY, endX, endY;
        if (aY == bY) {
            startX = 0;
            startY = aY;
            endX = canvasWidth;
            endY = aY;
        } else {
            startY = canvasHeight;
            endY = 0;
            startX = aX + (startY - aY) * (aX - bX) / (aY - bY);
            endX = bX + (endY - bY) * (bX - aX) / (bY - aY);
        }

        return new float[]{startX, startY, endX, endY};
    }

    /**
     * ���Ƶ�ǰ���ƶ��켣
     *
     * @param pathPointsNo  ��ǰ�����
     * @param isDrawingPath �Ƿ���ƹ켣
     */
    public void drawPointToPath(int pathPointsNo, boolean isDrawingPath) {
        if (D) {
            Log.e(TAG, "drawPointToPath(int pathPointsNo, boolean isDrawingPath)������");
        }

        this.pathPointNo = pathPointsNo;
        this.isDrawingPath = isDrawingPath;
    }


    /**
     * �����ͼ������
     *
     * @param fieldLength ��ص�����
     * @param fieldWidth ��ص�����
     * @param viewHeight ��ʾ����߶�
     * @param viewWidth ��ʾ������
     * @return �����ߣ�ͼ��ߴ�ȵ���ߴ磩
     */
    private double calculateScale(double fieldLength, double fieldWidth, float viewHeight, float viewWidth) {
        if (D) {
            Log.e(TAG, "calculateScale(double fieldLength, double fw, float vh, float vw)������");
        }

        if ((fieldLength / fieldWidth) < (viewHeight / viewWidth)) {
            return (viewWidth / fieldWidth);
        } else {
            return (viewHeight / fieldLength);
        }
    }

    /**
     * �ѵ�������ת��Ϊ��Ļ����
     *
     * @param xF ��������
     * @param yF �������
     * @return ��ͼ���������
     */
    private int[] fieldToImage(double xF, double yF) {

        //ˢ�µ�ͼ������
//        scale = calculateScale(fieldLength, fieldWidth, canvasHeight - canvasMargin, canvasWidth - canvasMargin);

        int xV = canvasWidth / 2 + (int) ((xF - originX) * scale + 0.5);
        int yV = canvasHeight / 2 - (int) ((yF - originY) * scale + 0.5);
        int[] viewPoint = {xV, yV};
        return viewPoint;
    }

    /**
     * �ѵ�������ת��Ϊ��Ļ����
     *
     * @param point ��������
     * @return ��ͼ���������
     */
    private int[] fieldToImage(GeoPoint point) {

        int xV = canvasWidth / 2 + (int) ((point.getX() - originX) * scale + 0.5);
        int yV = canvasHeight / 2 - (int) ((point.getY() - originY) * scale + 0.5);
        int[] viewPoint = {xV, yV};
        return viewPoint;
    }

    @Override
    public void run() {
        if (D) {
            Log.e(TAG, "run()������");
        }


        while (isDrawing) {
            long start = System.currentTimeMillis();
            //�ѵ�������ת����Ļ���꿪ʼ��ͼ
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
            if (end - start < REFRESH_PERIOD) {
                try {
                    Thread.sleep(REFRESH_PERIOD - (end - start));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public void drawCanvas() {

        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                //���Ʊ���
                canvas.drawColor(Color.rgb(135, 206, 234)); //ǳ��ɫ����
//                canvas.drawColor(0x887ccd7c);

                //�������
                if (isDrawingField) {
                    canvas.drawPath(fieldBoundPath, fieldBoundPaint);
                    canvas.drawPath(fieldBoundPath, fieldShaderPaint);
                }

                //���ƹ켣
                if (isDrawingPath) {
                    canvas.drawPath(mPath, operationPathPaint);
                    canvas.drawPath(mPath, pathLinePaint);
                } else {
                    canvas.drawPath(lastPath, pathHistoryPaint);
                }

                //����AB���Լ�A��B���
                if (isDrawingAB) {
                    canvas.drawLine(calculateABline(viewAX, viewAY, viewBX, viewBY)[0],
                            calculateABline(viewAX, viewAY, viewBX, viewBY)[1],
                            calculateABline(viewAX, viewAY, viewBX, viewBY)[2],
                            calculateABline(viewAX, viewAY, viewBX, viewBY)[3],
                            paintABline);
                    canvas.drawBitmap(pointAMarker, viewAX - pointAWidth / 2, viewAY - pointAHeight / 2, null);
                    canvas.drawBitmap(pointBMarker, viewBX - pointBWidth / 2, viewBY - pointBHeight / 2, null);
                }

                //������������ʱλ�ñ�־
                canvas.drawCircle(viewX, viewY, 5, endCirclePaint);
                canvas.drawBitmap(tractorMarker, viewX - markerWidth / 2, viewY - markerHeight, null);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void setLineDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    /**
     * �����ֵ
     *
     * @param numbers ���Ƚ�����
     * @return ���ֵ
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
     * ����Сֵ
     *
     * @param numbers ���Ƚ�����
     * @return ��Сֵ
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
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDrawing = false;
    }
}
