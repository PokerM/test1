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
 * �ؿ����ģ��
 * ��ע�⣺��ģ���е����˰ٶȵ�ͼAPK���������һ������������Ҫ����������Կ������manifest�ļ����滻������
 * ���򣬰ٶȵ�ͼ��ֻ��ʾ���񣬲��ܼ��ص�ͼ����
 */
public class FieldAddingActivity extends Activity implements View.OnClickListener {

    private static final boolean BLUETOOTH = true;
    private static final LatLng SHANGHAI = new LatLng(31.238068, 121.501654);
    private static final LatLng SJTU = new LatLng(31.031866, 121.452982);
    private static final String TAG = "FieldAddingActivity";
    private static final boolean D = true;
    private static final char END = '*'; // ����ͨ���ַ���������־
    private static final char START = '#'; // ����ͨ���ַ�����ʼ��־
    private static final char SEPARATOR = ','; // �ָ���
    private static final int SEPARATOR_NUMBER = 8; // �ָ�������
    private static final String FIELD_DIRECTORY = "fields";

    private MyApplication myApp; // ����ȫ�ֱ���
    private ArrayList<GeoPoint> fieldVerticesList = new ArrayList<>(); // ����ؿ鶥���б�
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
    private List<Marker> markerList = new ArrayList<>();

    // ��Ϣ������
    MyFieldHandler mFieldHandler = new MyFieldHandler(this);

    /*
    * ʹ�þ�̬�ڲ������Handler�������ڴ�й©����;
    * ��handlerMessage()��д��Ϣ������롣
    */
    private static class MyFieldHandler extends Handler {
        //����������MyFieldHandler��GC����ʱ�ᱻ���յ�
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
                //ִ��ҵ���߼�
                switch (msg.what) {
                    case BluetoothService.MESSAGE_RECEIVED:
                        // ������յ�������
                        String dataString;
                        String message = (String) msg.obj;
                        readMessageSB.append(message);
                        Log.e(TAG,"received");

                        // ��� ","�ĸ�����Ϊ12�������ַ�����ʼ�ͽ����ַ�����ָ���ģ���������Ч
                        if (!((SysUtil.countCharacter(readMessageSB, SEPARATOR) == SEPARATOR_NUMBER)
                                && (readMessageSB.charAt(0) == START)
                                && (readMessageSB.charAt(readMessageSB.length() - 1) == END))) {
                            readMessageSB.delete(0, readMessageSB.length());
                            Log.e(TAG,"no");
                        } else {
                            dataString = readMessageSB.substring(1, readMessageSB.length() - 1); // ��ȡ�ַ���
                            String[] dataArray = dataString.split(",", 8);
                            try {
//                                String strLatitude = dataArray[0];
//                                String strLongitude = dataArray[1];

                                Log.e(TAG,"ok");
                                String strXCoordinate = dataArray[0];
                                String strYCoordinate = dataArray[1];
//                                double latitude = Double.parseDouble(strLatitude); // x����
//                                double longitude = Double.parseDouble(strLongitude); // y����
                                double xCoordinate = Double.parseDouble(strXCoordinate); // x����
                                double yCoordinate = Double.parseDouble(strYCoordinate); // y����

                                double[] temp = GaussToBL(xCoordinate,yCoordinate);
                                double longitude = temp[0];
                                double latitude = temp[1];

                                activity.txtLat.setText(String.valueOf(latitude));
                                activity.txtLong.setText(String.valueOf(longitude));

                                /*��ͼ����ת����ǰ��,���ϵ��ڵ�ͼ����ʾ��ǰ��*/
                                activity.navigateTo(latitude, longitude);
                                activity.navigatePoint(latitude, longitude);

                                if (CALIBRATION_FLAG == 1) {
                                    activity.addFieldVertex(latitude, longitude, xCoordinate, yCoordinate);
                                    String str = "�ѱ궨�� " + activity.points.size() + " ����";
                                    ToastUtil.showToast(str, true);
                                    CALIBRATION_FLAG = 0;
                                }
                            } catch (NumberFormatException e) {
                                ToastUtil.showToast(activity.getString(R.string.receiving_data_format_error), true);
                            }

                            readMessageSB.delete(0, readMessageSB.length()); // �������
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }

    //������ض���
    private void addFieldVertex(double lat, double lng, double xx, double yy) {

        points.add(new LatLng(lat, lng));//�涥�㾭γ�ȵ��б�
        fieldVerticesList.add(new GeoPoint(lat, lng, xx, yy));  //�����ض���

        //�ڵ�ͼ�����Marker������ʾ��ǰ��
        OverlayOptions option = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .icon(bitmap);
        markerList.add((Marker) baiduMap.addOverlay(option));

        //pointsTemporary���ڻ��ߣ��������2ʱ˵���Ѿ��������ˣ�Ȼ����Ż�
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

        setContentView(R.layout.field_adding_activity);

        if (D) {
            Log.e(TAG, "*** ON CREATE ***");
        }

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

            case R.id.btnCalibration:  // �����궨����
                CALIBRATION_FLAG = 1;
                break;

            case R.id.btnBackStep: // ���˲���
                // �жϸ��Ƕ�����Ƿ��
                if (mPolygon == null) {
                    // �Ƴ����ǰһ������
                    if (fieldVerticesList != null && fieldVerticesList.size() > 0) {
                        fieldVerticesList.remove(fieldVerticesList.size() - 1); // ���»��˼�ʱ���Ƴ���Ч����ض���
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
                        fieldVerticesList.remove(fieldVerticesList.size() - 1); // ���»��˼�ʱ���Ƴ���Ч����ض���
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
                                    // �Զ���ʱ�������ļ���
                                    String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                                    String fileField = "field_" + fName + "_" + currentTime + ".shp";
                                    String fNo = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
                                    String fDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

                                    // ����������ض������갴��ʽд���ַ���
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

                                    // ���������Ϣ���ⲿ�ļ�
                                    FileUtil.writeDataToExternalStorage(FIELD_DIRECTORY, fileField, fieldInfo.toString(), false, false);

                                    //����ؿ鶥�����ݵ����ݿ�
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
                                        ToastUtil.showToast("�ѳɹ���ӵؿ鵽���ݿ⣡", true);
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
            // ���½������ʱ������Ϣ������Ϊ��ǰ������
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
        // �˳�Activity�����MessageQueue��û�������Ϣ
        mFieldHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public static double[] GaussToBL(double X, double Y) {
        int ProjNo;
        int ZoneWide; // ����
        double[] output = new double[2];
        double longitude1, latitude1, longitude0, X0, Y0, xval, yval;// latitude0,
        double e1, e2, f, a, ee, NN, T, C, M, D, R, u, fai, iPI;
        iPI = 0.0174532925199433; // 3.1415926535898/180.0;

        // 54�걱������ϵ����
        a = 6378245.0;
        f = 1.0 / 298.3;

		/*
		 * // 80����������ϵ����
		 * a = 6378140.0;
		 * f = 1 / 298.257;
		 */

        ZoneWide = 6; // 6�ȴ���
        ProjNo = (int) (X / 1000000L); // ���Ҵ���
        longitude0 = (ProjNo - 1) * ZoneWide + ZoneWide / 2;
        longitude0 = longitude0 * iPI; // ���뾭��

        X0 = ProjNo * 1000000L + 500000L;
        Y0 = 0;
        xval = X - X0;
        yval = Y - Y0; // ���ڴ������
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
        // ���㾭��(Longitude) γ��(Latitude)
        longitude1 = longitude0 + (D - (1 + 2 * T + C) * D * D * D / 6
                + (5 - 2 * C + 28 * T - 3 * C * C + 8 * ee + 24 * T * T) * D * D * D * D * D / 120) / Math.cos(fai);
        latitude1 = fai
                - (NN * Math.tan(fai) / R) * (D * D / 2 - (5 + 3 * T + 10 * C - 4 * C * C - 9 * ee) * D * D * D * D / 24
                + (61 + 90 * T + 298 * C + 45 * T * T - 256 * ee - 3 * C * C) * D * D * D * D * D * D / 720);
        // ת��Ϊ�� DD
        output[0] = longitude1 / iPI;
        output[0] = 120 - output[0];
        output[1] = latitude1 / iPI;
        return output;
    }
}

