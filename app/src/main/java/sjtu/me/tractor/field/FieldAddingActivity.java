package sjtu.me.tractor.field;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sjtu.me.tractor.R;
import sjtu.me.tractor.connection.BluetoothService;
import sjtu.me.tractor.planning.GeoPoint;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.utils.FileUtil;
import sjtu.me.tractor.utils.SysUtil;
import sjtu.me.tractor.utils.ToastUtil;


/**
 * 地块添加模块
 * （注意：本模块中调用了百度地图APK，如果换了一个开发环境需要重新申请密钥，并在manifest文件中替换过来；
 * 否则，百度地图会只显示网格，不能加载地图。）
 */
public class FieldAddingActivity extends Activity implements View.OnClickListener {

    private static final boolean BLUETOOTH = true;
    private static final LatLng SHANGHAI = new LatLng(31.238068, 121.501654);
    private static final LatLng SJTU = new LatLng(31.031866, 121.452982);
    private static final String TAG = "FieldAddingActivity";
    private static final boolean D = true;
    private static final char END = '*'; // 串口通信字符串结束标志
    private static final char START = '#'; // 串口通信字符串开始标志
    private static final char SEPARATOR = ','; // 分隔符
    private static final int SEPARATOR_NUMBER = 8; // 分隔符个数
    private static final String FIELD_DIRECTORY = "fields";

    private MyApplication myApp; // 程序全局变量
    private ArrayList<GeoPoint> fieldVerticesList = new ArrayList<>(); // 定义地块顶点列表
    private HashMap<Integer, GeoPoint> fieldMap = new HashMap<>();

    private List<LatLng> points = new ArrayList<>();//多边形的点,点的记录
    private List<LatLng> pointsTemporary = new ArrayList<>();//暂时存放的点


    private BitmapDescriptor bitmap;
    public static int CALIBRATION_FLAG = 0;
    private static final int UPDATE_TEXT = 1;//Handler标志位
    private int flag = 0;//开始停止切换按钮标志位

    public volatile boolean exit = true;//子线程终止标志位
    private Button btnBack;//返回按钮
    private TextView txtLat;//显示纬度文本框
    private TextView txtLong;//显示经度文本框
    private CheckBox checkboxManual;//手动复选框
    private CheckBox checkBoxAuto;//自动复选框
    private Button btnCalibration;//标定按钮
    private Button btnBackStep;//撤销上一步按钮
    private Button btnSwitch;//开始停止切换按钮
    private Button btnComplete;//完成按钮
    private Button btnReset;//重置按钮
    private Button btnSave;//保存按钮
    private Button btnCancel;//退出按钮

    private Button btnFab;//地图回中心按钮
    private MapView mapView;//百度地图控件
    private BaiduMap baiduMap;//百度地图
    private Polygon mPolygon;//区域覆盖
    private List<Polyline> polyLines = new ArrayList<>();//画线
    private List<Marker> markerList = new ArrayList<>();

    // 消息处理器
    MyFieldHandler mFieldHandler = new MyFieldHandler(this);

    /*
    * 使用静态内部类避免Handler带来的内存泄漏问题;
    * 在handlerMessage()中写消息处理代码。
    */
    private static class MyFieldHandler extends Handler {
        //持有弱引用MyFieldHandler，GC回收时会被回收掉
        private final WeakReference<FieldAddingActivity> mReferenceActivity;
        StringBuilder readMessageSB = new StringBuilder();

        public MyFieldHandler(FieldAddingActivity activity) {
            mReferenceActivity = new WeakReference<FieldAddingActivity>(activity);
        }


        @Override
        public void handleMessage(android.os.Message msg) {
            final FieldAddingActivity activity = mReferenceActivity.get();
            super.handleMessage(msg);
            if (activity != null) {
                //执行业务逻辑
                switch (msg.what) {
                    case BluetoothService.MESSAGE_RECEIVED:
                        // 处理接收到的数据
                        String dataString;
                        String message = (String) msg.obj;
                        readMessageSB.append(message);
                        Log.e(TAG,"received");

                        // 如果 ","的个数不为12，或者字符串开始和结束字符不是指定的，则数据无效
                        if (!((SysUtil.countCharacter(readMessageSB, SEPARATOR) == SEPARATOR_NUMBER)
                                && (readMessageSB.charAt(0) == START)
                                && (readMessageSB.charAt(readMessageSB.length() - 1) == END))) {
                            readMessageSB.delete(0, readMessageSB.length());
                            Log.e(TAG,"no");
                        } else {
                            dataString = readMessageSB.substring(1, readMessageSB.length() - 1); // 提取字符串
                            String[] dataArray = dataString.split(",", 8);
                            try {
//                                String strLatitude = dataArray[0];
//                                String strLongitude = dataArray[1];

                                Log.e(TAG,"ok");
                                String strXCoordinate = dataArray[0];
                                String strYCoordinate = dataArray[1];
//                                double latitude = Double.parseDouble(strLatitude); // x坐标
//                                double longitude = Double.parseDouble(strLongitude); // y坐标
                                double xCoordinate = Double.parseDouble(strXCoordinate); // x坐标
                                double yCoordinate = Double.parseDouble(strYCoordinate); // y坐标

                                double[] temp = GaussToBL(xCoordinate,yCoordinate);
                                double longitude = temp[0];
                                double latitude = temp[1];

                                activity.txtLat.setText(String.valueOf(latitude));
                                activity.txtLong.setText(String.valueOf(longitude));

                                /*地图中心转到当前点,不断地在地图上显示当前点*/
                                activity.navigateTo(latitude, longitude);
                                activity.navigatePoint(latitude, longitude);

                                if (CALIBRATION_FLAG == 1) {
                                    activity.addFieldVertex(latitude, longitude, xCoordinate, yCoordinate);
                                    String str = "已标定第 " + activity.points.size() + " 个点";
                                    ToastUtil.showToast(str, true);
                                    CALIBRATION_FLAG = 0;
                                }
                            } catch (NumberFormatException e) {
                                ToastUtil.showToast(activity.getString(R.string.receiving_data_format_error), true);
                            }

                            readMessageSB.delete(0, readMessageSB.length()); // 清除数据
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }

    //增加田地顶点
    private void addFieldVertex(double lat, double lng, double xx, double yy) {

        points.add(new LatLng(lat, lng));//存顶点经纬度的列表
        fieldVerticesList.add(new GeoPoint(lat, lng, xx, yy));  //添加田地顶点

        //在地图上添加Marker，并显示当前点
        OverlayOptions option = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .icon(bitmap);
        markerList.add((Marker) baiduMap.addOverlay(option));

        //pointsTemporary用于画线，当其等于2时说明已经画过线了，然后接着画
        if ((points.size() >= 2) && (pointsTemporary.size() == 2)) {
            LatLng ll = pointsTemporary.get(1);
            pointsTemporary.clear();
            pointsTemporary.add(ll);
            pointsTemporary.add(new LatLng(lat, lng));
            OverlayOptions ooPolyline = new PolylineOptions()
                    .width(10)
                    .points(pointsTemporary);
            polyLines.add((Polyline) baiduMap.addOverlay(ooPolyline));
        }

        if ((points.size() == 2) && (pointsTemporary.size() == 1)) {
            pointsTemporary.add(new LatLng(lat, lng));
            OverlayOptions ooPolyline = new PolylineOptions()
                    .width(10)
                    .points(pointsTemporary);
            polyLines.add((Polyline) baiduMap.addOverlay(ooPolyline));
        }
        if (pointsTemporary.size() == 0) {
            pointsTemporary.add(new LatLng(lat, lng));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.setCoordType(CoordType.GCJ02);//使用 GCJ02 坐标系
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.field_adding_activity);

        if (D) {
            Log.e(TAG, "*** ON CREATE ***");
        }

        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setIndoorEnable(true);//打开室内地图，最大放大级别22

        initViews();

        navigateTo(SJTU.latitude, SJTU.longitude); //以一点为中心显示地图

        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);//构建MarkerOption，用于在地图上添加Marker

        // 初始化全局变量
        myApp = (MyApplication) getApplication();
        // 设置蓝牙连接的消息处理器为当前界面处理器
        if(myApp.getBluetoothService().getBluetoothState()) {
            myApp.getBluetoothService().setHandler(mFieldHandler);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;

            case R.id.btnCalibration:  // 启动标定操作
                CALIBRATION_FLAG = 1;
                break;

            case R.id.btnBackStep: // 回退操作
                // 判断覆盖多边形是否空
                if (mPolygon == null) {
                    // 移除田地前一个顶点
                    if (fieldVerticesList != null && fieldVerticesList.size() > 0) {
                        fieldVerticesList.remove(fieldVerticesList.size() - 1); // 按下回退键时，移除无效的田地顶点
                    }

                    if (points.size() == 1) {
                        points.clear();
                        pointsTemporary.clear();
                    }

                    if (points.size() == 2) {
                        polyLines.get(0).remove();
                        polyLines.remove(0);
                        points.remove(1);
                        pointsTemporary.remove(1);
                    }

                    if (points.size() >= 3) {
                        polyLines.get(polyLines.size() - 1).remove();
                        polyLines.remove(polyLines.size() - 1);
                        LatLng i = points.get(points.size() - 3);
                        points.remove(points.size() - 1);
                        LatLng j = pointsTemporary.get(0);
                        pointsTemporary.clear();
                        pointsTemporary.add(i);
                        pointsTemporary.add(j);
                    }

                    if (markerList != null && markerList.size() > 0) {
                        markerList.get(markerList.size() - 1).remove();
                        markerList.remove(markerList.size() - 1);
                    }
                }

                if (mPolygon != null) {
                    mPolygon.remove();
                    if (fieldVerticesList != null && fieldVerticesList.size() > 0) {
                        fieldVerticesList.remove(fieldVerticesList.size() - 1); // 按下回退键时，移除无效的田地顶点
                    }

                    for (int i = 0; i <= points.size() - 2; i++) {

                        List<LatLng> pointsT = new ArrayList<>();
                        pointsT.add(points.get(i));
                        pointsT.add(points.get(i + 1));

                        OverlayOptions polylineOverLay = new PolylineOptions()
                                .width(10)
                                .color(0xFF0000)
                                .points(pointsT);
                        polyLines.add((Polyline) baiduMap.addOverlay(polylineOverLay));
                    }

                    mPolygon = null;
                }
                ToastUtil.showToast("已删除前一个顶点", true);
                break;

            case R.id.btnSwitch:
                if (flag == 1) {
                    flag--;
                    btnSwitch.setBackgroundResource(R.drawable.transfer);
                } else {
                    flag++;
                    btnSwitch.setBackgroundResource(R.drawable.transfer_pressed);
                }
                break;

            case R.id.btnComplete:
                new AlertDialog.Builder(this).setTitle(R.string.start_to_set_field)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(R.string.affirm, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                if (points.size() > 2) {
                                    OverlayOptions polygonOption = new PolygonOptions()
                                            .points(points)
                                            .stroke(new Stroke(5, 0xAA00FF00))
                                            .fillColor(0x88436EEE);

                                    mPolygon = (Polygon) baiduMap.addOverlay(polygonOption);//显示多边形

                                    for (int i = 0; i < polyLines.size(); i++) {
                                        Polyline polyline = polyLines.get(i);
                                        polyline.remove();
                                    }
                                    polyLines.clear();
                                } else {
                                    ToastUtil.showToast(getString(R.string.field_vertices_are_not_enough), true);
                                }

                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;

            case R.id.btnReset:
                if (polyLines.size() >= 1) {
                    for (int i = 0; i < polyLines.size(); i++) {
                        Polyline polyline = polyLines.get(i);
                        polyline.remove();
                    }
                }
                if (mPolygon != null) {
                    mPolygon.remove();
                }
                for (int i = 0; i < markerList.size(); i++) {
                    markerList.get(i).remove();
                }
                markerList.clear();
                points.clear();
                pointsTemporary.clear();
                polyLines.clear();
                break;

            case R.id.btnSave:
                final EditText editText = new EditText(this);

                new AlertDialog.Builder(this).setTitle(R.string.input_field_name)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(editText)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                String fName = editText.getText().toString();
                                if (fName.isEmpty()) {
//                                    ToastUtil.showToast(getString(R.string.field_name_should_not_be_null), true);
                                    new AlertDialog.Builder(FieldAddingActivity.this)
                                            .setTitle(R.string.alert_title)
                                            .setMessage(R.string.please_input_field_name)
                                            .setIcon(R.drawable.alert)
                                            .setPositiveButton(R.string.affirm, null)
                                            .show();
                                } else {
                                    // 自动按时间生成文件名
                                    String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                                    String fileField = "field_" + fName + "_" + currentTime + ".shp";
                                    String fNo = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
                                    String fDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

                                    // 读出所有田地顶点坐标按格式写到字符串
                                    String delimiter = ",";
                                    StringBuilder fieldInfo = new StringBuilder();
                                    fieldInfo.append(fName).append("\r\n")
                                            .append(fNo).append("\r\n");
                                    for (int i = 0; i < fieldVerticesList.size(); i++) {
                                        fieldInfo.append(i + 1).append(delimiter)
                                                .append(fieldVerticesList.get(i).getLat()).append(delimiter)
                                                .append(fieldVerticesList.get(i).getLng()).append(delimiter)
                                                .append(fieldVerticesList.get(i).getX()).append(delimiter)
                                                .append(fieldVerticesList.get(i).getY()).append("\r\n");
                                    }

                                    // 保存田地信息到外部文件
                                    FileUtil.writeDataToExternalStorage(FIELD_DIRECTORY, fileField, fieldInfo.toString(), false, false);

                                    //保存地块顶点数据到数据库
                                    boolean flag = false;
                                    for (int i = 0; i < fieldVerticesList.size(); i++) {
                                        String fPNo = String.valueOf(i + 1);
                                        String fPLat = String.valueOf(fieldVerticesList.get(i).getLat());
                                        String fPLng = String.valueOf(fieldVerticesList.get(i).getLng());
                                        String fPX = String.valueOf(fieldVerticesList.get(i).getX());
                                        String fPY = String.valueOf(fieldVerticesList.get(i).getY());
                                        flag = myApp.getDatabaseManager().insertDataToField(new String[]{fNo, fName, fDate, fPNo, fPLat, fPLng, fPX, fPY});
                                    }
                                    if (flag) {
                                        ToastUtil.showToast("已成功添加地块到数据库！", true);
                                    }
                                    finish();
//                                    Runtime runtime = Runtime.getRuntime();
//                                    try {
//                                        runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
//                                    } catch (IOException e) {
//                                        // TODO Auto-generated catch block
//                                        e.printStackTrace();
//                                    }
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;

            case R.id.btnCancel:
                finish();
                break;

            case R.id.fab:
//                navigateTo(SHANGHAI.latitude, SHANGHAI.longitude);
                navigateTo(SJTU.latitude, SJTU.longitude);
                break;

            default:
                break;
        }
    }


    public void navigateTo(double latitude, double longitude) {

        LatLng ll = new LatLng(latitude, longitude);
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(update);
//        update = MapStatusUpdateFactory.zoomTo(19);
        baiduMap.animateMapStatus(update);//以这个为中心点显示地图

    }

    public void navigatePoint(double latitude, double longitude) {
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(latitude);
        locationBuilder.longitude(longitude);
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);//将这个点显示在地图上
    }

    void initViews() {
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        txtLat = (TextView) findViewById(R.id.txtLat);
        txtLong = (TextView) findViewById(R.id.txtLong);

        checkboxManual = (CheckBox) findViewById(R.id.checkboxManual);
        checkBoxAuto = (CheckBox) findViewById(R.id.checkboxAuto);

        btnCalibration = (Button) findViewById(R.id.btnCalibration);
        btnCalibration.setOnClickListener(this);

        btnBackStep = (Button) findViewById(R.id.btnBackStep);
        btnBackStep.setOnClickListener(this);

        btnSwitch = (Button) findViewById(R.id.btnSwitch);
        btnSwitch.setOnClickListener(this);

        btnComplete = (Button) findViewById(R.id.btnComplete);
        btnComplete.setOnClickListener(this);

        btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(this);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        btnFab = (Button) findViewById(R.id.fab);
        btnFab.setOnClickListener(this);

        checkboxManual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkBoxAuto.setChecked(false);
                    btnCalibration.setVisibility(View.VISIBLE);
                    btnBackStep.setVisibility(View.VISIBLE);
                    btnSwitch.setVisibility(View.GONE);
                }
            }
        });
        checkBoxAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkboxManual.setChecked(false);
                    btnCalibration.setVisibility(View.GONE);
                    btnBackStep.setVisibility(View.GONE);
                    btnSwitch.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (D) {
            Log.e(TAG, "*** ON START ***");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (D) {
            Log.e(TAG, "*** ON RESTART ***");
        }

        if(myApp.getBluetoothService().getBluetoothState()) {
            // 重新进入界面时设置消息处理器为当前处理器
            myApp.getBluetoothService().setHandler(mFieldHandler);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        mapView.onResume();
        if (D) {
            Log.e(TAG, "*** ON RESUME ***");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (D) {
            Log.e(TAG, "*** ON PAUSE ***");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exit) {
            exit = false;
        }
        if (polyLines.size() >= 1) {
            for (int i = 0; i < polyLines.size(); i++) {
                Polyline polyline = polyLines.get(i);
                polyline.remove();//?????
            }
        }
        if (mPolygon != null) {
            mPolygon.remove();
        }
        for (int i = 0; i < markerList.size(); i++) {
            markerList.get(i).remove();
        }
        markerList.clear();
        points.clear();
        pointsTemporary.clear();
        polyLines.clear();
        fieldVerticesList.clear();

        if (D) {
            Log.e(TAG, "*** ON STOP ***");
        }
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        // 退出Activity，清除MessageQueue还没处理的消息
        mFieldHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public static double[] GaussToBL(double X, double Y) {
        int ProjNo;
        int ZoneWide; // 带宽
        double[] output = new double[2];
        double longitude1, latitude1, longitude0, X0, Y0, xval, yval;// latitude0,
        double e1, e2, f, a, ee, NN, T, C, M, D, R, u, fai, iPI;
        iPI = 0.0174532925199433; // 3.1415926535898/180.0;

        // 54年北京坐标系参数
        a = 6378245.0;
        f = 1.0 / 298.3;

		/*
		 * // 80年西安坐标系参数
		 * a = 6378140.0;
		 * f = 1 / 298.257;
		 */

        ZoneWide = 6; // 6度带宽
        ProjNo = (int) (X / 1000000L); // 查找带号
        longitude0 = (ProjNo - 1) * ZoneWide + ZoneWide / 2;
        longitude0 = longitude0 * iPI; // 中央经线

        X0 = ProjNo * 1000000L + 500000L;
        Y0 = 0;
        xval = X - X0;
        yval = Y - Y0; // 带内大地坐标
        e2 = 2 * f - f * f;
        e1 = (1.0 - Math.sqrt(1 - e2)) / (1.0 + Math.sqrt(1 - e2));
        ee = e2 / (1 - e2);
        M = yval;
        u = M / (a * (1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256));
        fai = u + (3 * e1 / 2 - 27 * e1 * e1 * e1 / 32) * Math.sin(2 * u)
                + (21 * e1 * e1 / 16 - 55 * e1 * e1 * e1 * e1 / 32) * Math.sin(4 * u)
                + (151 * e1 * e1 * e1 / 96) * Math.sin(6 * u) + (1097 * e1 * e1 * e1 * e1 / 512) * Math.sin(8 * u);
        C = ee * Math.cos(fai) * Math.cos(fai);
        T = Math.tan(fai) * Math.tan(fai);
        NN = a / Math.sqrt(1.0 - e2 * Math.sin(fai) * Math.sin(fai));
        R = a * (1 - e2) / Math.sqrt((1 - e2 * Math.sin(fai) * Math.sin(fai)) * (1 - e2 * Math.sin(fai) * Math.sin(fai))
                * (1 - e2 * Math.sin(fai) * Math.sin(fai)));
        D = xval / NN;
        // 计算经度(Longitude) 纬度(Latitude)
        longitude1 = longitude0 + (D - (1 + 2 * T + C) * D * D * D / 6
                + (5 - 2 * C + 28 * T - 3 * C * C + 8 * ee + 24 * T * T) * D * D * D * D * D / 120) / Math.cos(fai);
        latitude1 = fai
                - (NN * Math.tan(fai) / R) * (D * D / 2 - (5 + 3 * T + 10 * C - 4 * C * C - 9 * ee) * D * D * D * D / 24
                + (61 + 90 * T + 298 * C + 45 * T * T - 256 * ee - 3 * C * C) * D * D * D * D * D * D / 720);
        // 转换为度 DD
        output[0] = longitude1 / iPI;
        output[0] = 120 - output[0];
        output[1] = latitude1 / iPI;
        return output;
    }
}

