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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sjtu.me.tractor.R;
import sjtu.me.tractor.bluetooth.BluetoothService;
import sjtu.me.tractor.gis.GeoPoint;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.util.FileUtil;
import sjtu.me.tractor.util.ToastUtil;


public class FieldAddingActivity2 extends Activity implements View.OnClickListener {

    private static final LatLng SHANGHAI = new LatLng(31.238068, 121.501654);
    private static final LatLng SJTU = new LatLng(31.031866, 121.452982);
    private static final String TAG = "FieldAddingActivity2";
    private static final boolean D = true;
    private static final char END = '*'; // 串口通信字符串结束标志
    private static final char START = '#'; // 串口通信字符串开始标志
    private static final char SEPARATOR = ','; // 分隔符
    private static final String ALBUM_NAME = "AutoTractorData";

    private MyApplication myApp; // 程序全局变量
    private ArrayList<GeoPoint> fieldVertices = new ArrayList<>(); // 定义地块顶点数组
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
    private List<Marker> markers = new ArrayList<>();

    public final Handler mFieldHandler = new Handler() {
        StringBuilder readMessageSB = new StringBuilder();

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_RECEIVED:
                    // 处理接收到的数据
                    String dataString = new String();
                    String message = (String) msg.obj;
                    readMessageSB.append(message);

                    // 如果 ","的个数不为8，或者字符串开始和结束字符不是指定的，则数据无效
                    if (!((MyApplication.countCharacter(readMessageSB, SEPARATOR) == 10)
                            && (readMessageSB.charAt(0) == START)
                            && (readMessageSB.charAt(readMessageSB.length() - 1) == END))) {
                        readMessageSB.delete(0, readMessageSB.length());
                    } else {
                        dataString = readMessageSB.substring(1, readMessageSB.length() - 1); // 提取字符串
                        String[] dataArray = dataString.split(",", 11);
                        try {
                            String strLatitude = dataArray[0];
                            String strLongitude = dataArray[1];
                            String strXCoordinate = dataArray[2];
                            String strYCoordinate = dataArray[3];
                            double latitude = Double.parseDouble(strLatitude); // x坐标
                            double longitude = Double.parseDouble(strLongitude); // y坐标
                            double xCoordinate = Double.parseDouble(strXCoordinate); // x坐标
                            double yCoordinate = Double.parseDouble(strYCoordinate); // y坐标

                            txtLat.setText(strLatitude);
                            txtLong.setText(strLongitude);

                            // 地图中心转到当前点
                            navigateTo(SJTU.latitude, SJTU.longitude);

                            navigatePoint(latitude, longitude);//不断地在地图上显示当前点

                            if (CALIBRATION_FLAG == 1) {
                                addFieldVertex(latitude, longitude, xCoordinate, yCoordinate);
                                String str = "已标定第 " + points.size() + " 个点";
                                ToastUtil.showToast(str, true);
                                CALIBRATION_FLAG = 0;
                            }
                        } catch (NumberFormatException e) {
                            ToastUtil.showToast(getString(R.string.receiving_data_format_error), true);
                        }

                        // clear all string data
                        readMessageSB.delete(0, readMessageSB.length());
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void addFieldVertex(double lat, double lng, double xx, double yy) {

        points.add(new LatLng(lat, lng));
        fieldVertices.add(new GeoPoint(lat, lng, xx, yy));  //添加田地顶点

        OverlayOptions option = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .icon(bitmap);
        markers.add((Marker) baiduMap.addOverlay(option));//在地图上添加Marker，并显示

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

        setContentView(R.layout.activity_field_adding2);

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
        myApp.getBluetoothService().setHandler(mFieldHandler);

        if (D) {
            Log.e(TAG, "+++ setHandler: mFieldHandler +++");
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
                    if (fieldVertices != null) {
                        fieldVertices.remove(fieldVertices.size() - 1); // 按下回退键时，移除无效的田地顶点
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

                    markers.get(markers.size() - 1).remove();
                    markers.remove(markers.size() - 1);
                }

                if (mPolygon != null) {
                    mPolygon.remove();
                    if (fieldVertices != null) {
                        fieldVertices.remove(fieldVertices.size() - 1); // 按下回退键时，移除无效的田地顶点
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

            case R.id.btnSwitchMode:
                if (flag == 1) {
                    flag--;
                    btnSwitch.setBackgroundResource(R.drawable.transfer);
                } else {
                    flag++;
                    btnSwitch.setBackgroundResource(R.drawable.transfer_press);
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
                for (int i = 0; i < markers.size(); i++) {
                    markers.get(i).remove();
                }
                markers.clear();
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

                                String input = editText.getText().toString();
                                if (input.isEmpty()) {
//                                    ToastUtil.showToast(getString(R.string.field_name_should_not_be_null), true);
                                    new AlertDialog.Builder(FieldAddingActivity2.this)
                                            .setTitle(R.string.alert_title)
                                            .setMessage(R.string.please_input_field_name)
                                            .setIcon(R.drawable.alert)
                                            .setPositiveButton(R.string.affirm, null)
                                            .show();
                                } else {
                                    // 自动按时间生成文件名
                                    String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                                    String fileField = "field_" + "_" + currentTime + input + ".txt";

                                    // 读出所有田地顶点坐标按格式写到字符串
                                    String delimiter = ",";
                                    String fieldInfo = "vertex_ID,latitude,longitude,x_coordinate,y_coordinate \r\n";
                                    for (int i = 0; i < fieldVertices.size(); i++) {
                                        fieldInfo += i + delimiter + fieldVertices.get(i).getLatitude() + delimiter + fieldVertices.get(i).getLongitude()
                                                + delimiter + fieldVertices.get(i).getXCoordinate() + delimiter + fieldVertices.get(i).getYCoordinate() + "\r\n";
                                    }

                                    // 保存田地信息到外部文件
                                    FileUtil.writeDataToExternalStorage(ALBUM_NAME, fileField, fieldInfo, false, false);

                                    //保存地块顶点数据到数据库
                                    String fNo = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
                                    String fName = input;
                                    String fDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                                    for (int i = 0; i < fieldVertices.size(); i++) {
                                        String fPNo = String.valueOf(i + 1);
                                        String fPLat = String.valueOf(fieldVertices.get(i).getLatitude());
                                        String fPLng = String.valueOf(fieldVertices.get(i).getLongitude());
                                        String fPX = String.valueOf(fieldVertices.get(i).getXCoordinate());
                                        String fPY = String.valueOf(fieldVertices.get(i).getYCoordinate());
                                        myApp.getDatabaseManager().insertDataToField(fNo, fName, fDate, fPNo, fPLat, fPLng, fPX, fPY);
                                    }
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
        ;
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(19);
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

        btnSwitch = (Button) findViewById(R.id.btnSwitchMode);
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
                } else {

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
                } else {

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

        // 重新进入界面时设置消息处理器为当前处理器
        myApp.getBluetoothService().setHandler(mFieldHandler);
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
                polyline.remove();
            }
        }
        if (mPolygon != null) {
            mPolygon.remove();
        }
        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).remove();
        }
        markers.clear();
        points.clear();
        pointsTemporary.clear();
        polyLines.clear();
        fieldVertices.clear();

        if (D) {
            Log.e(TAG, "*** ON STOP ***");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}

