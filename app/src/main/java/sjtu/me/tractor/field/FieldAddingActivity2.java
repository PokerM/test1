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
    private static final char END = '*'; // ����ͨ���ַ���������־
    private static final char START = '#'; // ����ͨ���ַ�����ʼ��־
    private static final char SEPARATOR = ','; // �ָ���
    private static final String ALBUM_NAME = "AutoTractorData";

    private MyApplication myApp; // ����ȫ�ֱ���
    private ArrayList<GeoPoint> fieldVertices = new ArrayList<>(); // ����ؿ鶥������
    private HashMap<Integer, GeoPoint> fieldMap = new HashMap<>();
    private List<LatLng> points = new ArrayList<>();//����εĵ�,��ļ�¼
    private List<LatLng> pointsTemporary = new ArrayList<>();//��ʱ��ŵĵ�

    private BitmapDescriptor bitmap;
    public static int CALIBRATION_FLAG = 0;
    private static final int UPDATE_TEXT = 1;//Handler��־λ
    private int flag = 0;//��ʼֹͣ�л���ť��־λ

    public volatile boolean exit = true;//���߳���ֹ��־λ
    private Button btnBack;//���ذ�ť
    private TextView txtLat;//��ʾγ���ı���
    private TextView txtLong;//��ʾ�����ı���
    private CheckBox checkboxManual;//�ֶ���ѡ��
    private CheckBox checkBoxAuto;//�Զ���ѡ��
    private Button btnCalibration;//�궨��ť
    private Button btnBackStep;//������һ����ť
    private Button btnSwitch;//��ʼֹͣ�л���ť
    private Button btnComplete;//��ɰ�ť
    private Button btnReset;//���ð�ť
    private Button btnSave;//���水ť
    private Button btnCancel;//�˳���ť

    private Button btnFab;//��ͼ�����İ�ť
    private MapView mapView;//�ٶȵ�ͼ�ؼ�
    private BaiduMap baiduMap;//�ٶȵ�ͼ
    private Polygon mPolygon;//���򸲸�
    private List<Polyline> polyLines = new ArrayList<>();//����
    private List<Marker> markers = new ArrayList<>();

    public final Handler mFieldHandler = new Handler() {
        StringBuilder readMessageSB = new StringBuilder();

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_RECEIVED:
                    // ������յ�������
                    String dataString = new String();
                    String message = (String) msg.obj;
                    readMessageSB.append(message);

                    // ��� ","�ĸ�����Ϊ8�������ַ�����ʼ�ͽ����ַ�����ָ���ģ���������Ч
                    if (!((MyApplication.countCharacter(readMessageSB, SEPARATOR) == 10)
                            && (readMessageSB.charAt(0) == START)
                            && (readMessageSB.charAt(readMessageSB.length() - 1) == END))) {
                        readMessageSB.delete(0, readMessageSB.length());
                    } else {
                        dataString = readMessageSB.substring(1, readMessageSB.length() - 1); // ��ȡ�ַ���
                        String[] dataArray = dataString.split(",", 11);
                        try {
                            String strLatitude = dataArray[0];
                            String strLongitude = dataArray[1];
                            String strXCoordinate = dataArray[2];
                            String strYCoordinate = dataArray[3];
                            double latitude = Double.parseDouble(strLatitude); // x����
                            double longitude = Double.parseDouble(strLongitude); // y����
                            double xCoordinate = Double.parseDouble(strXCoordinate); // x����
                            double yCoordinate = Double.parseDouble(strYCoordinate); // y����

                            txtLat.setText(strLatitude);
                            txtLong.setText(strLongitude);

                            // ��ͼ����ת����ǰ��
                            navigateTo(SJTU.latitude, SJTU.longitude);

                            navigatePoint(latitude, longitude);//���ϵ��ڵ�ͼ����ʾ��ǰ��

                            if (CALIBRATION_FLAG == 1) {
                                addFieldVertex(latitude, longitude, xCoordinate, yCoordinate);
                                String str = "�ѱ궨�� " + points.size() + " ����";
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
        fieldVertices.add(new GeoPoint(lat, lng, xx, yy));  //�����ض���

        OverlayOptions option = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .icon(bitmap);
        markers.add((Marker) baiduMap.addOverlay(option));//�ڵ�ͼ�����Marker������ʾ

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
        SDKInitializer.setCoordType(CoordType.GCJ02);//ʹ�� GCJ02 ����ϵ
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_field_adding2);

        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setIndoorEnable(true);//�����ڵ�ͼ�����Ŵ󼶱�22

        initViews();

        navigateTo(SJTU.latitude, SJTU.longitude); //��һ��Ϊ������ʾ��ͼ

        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);//����MarkerOption�������ڵ�ͼ�����Marker

        // ��ʼ��ȫ�ֱ���
        myApp = (MyApplication) getApplication();
        // �����������ӵ���Ϣ������Ϊ��ǰ���洦����
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

            case R.id.btnCalibration:  // �����궨����
                CALIBRATION_FLAG = 1;
                break;

            case R.id.btnBackStep: // ���˲���
                // �жϸ��Ƕ�����Ƿ��
                if (mPolygon == null) {
                    // �Ƴ����ǰһ������
                    if (fieldVertices != null) {
                        fieldVertices.remove(fieldVertices.size() - 1); // ���»��˼�ʱ���Ƴ���Ч����ض���
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
                        fieldVertices.remove(fieldVertices.size() - 1); // ���»��˼�ʱ���Ƴ���Ч����ض���
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
                ToastUtil.showToast("��ɾ��ǰһ������", true);
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

                                    mPolygon = (Polygon) baiduMap.addOverlay(polygonOption);//��ʾ�����

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
                                    // �Զ���ʱ�������ļ���
                                    String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                                    String fileField = "field_" + "_" + currentTime + input + ".txt";

                                    // ����������ض������갴��ʽд���ַ���
                                    String delimiter = ",";
                                    String fieldInfo = "vertex_ID,latitude,longitude,x_coordinate,y_coordinate \r\n";
                                    for (int i = 0; i < fieldVertices.size(); i++) {
                                        fieldInfo += i + delimiter + fieldVertices.get(i).getLatitude() + delimiter + fieldVertices.get(i).getLongitude()
                                                + delimiter + fieldVertices.get(i).getXCoordinate() + delimiter + fieldVertices.get(i).getYCoordinate() + "\r\n";
                                    }

                                    // ���������Ϣ���ⲿ�ļ�
                                    FileUtil.writeDataToExternalStorage(ALBUM_NAME, fileField, fieldInfo, false, false);

                                    //����ؿ鶥�����ݵ����ݿ�
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
        baiduMap.animateMapStatus(update);//�����Ϊ���ĵ���ʾ��ͼ

    }

    public void navigatePoint(double latitude, double longitude) {
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(latitude);
        locationBuilder.longitude(longitude);
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);//���������ʾ�ڵ�ͼ��
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

        // ���½������ʱ������Ϣ������Ϊ��ǰ������
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

