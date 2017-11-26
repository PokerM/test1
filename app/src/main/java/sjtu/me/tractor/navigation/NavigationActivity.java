package sjtu.me.tractor.navigation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import sjtu.me.tractor.R;
import sjtu.me.tractor.bluetooth.BluetoothService;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.gis.GeoPoint;
import sjtu.me.tractor.gis.GisAlgorithm;
import sjtu.me.tractor.hellochart.LineChart;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.surfaceview.MySurfaceView;
import sjtu.me.tractor.tractorinfo.TractorInfo;
import sjtu.me.tractor.util.AlertDialogUtil;
import sjtu.me.tractor.util.FileUtil;
import sjtu.me.tractor.util.SysUtil;
import sjtu.me.tractor.util.ToastUtil;

/**
 * @author billhu
 */
@SuppressLint("UseSparseArrays")
public class NavigationActivity extends Activity implements OnClickListener {

    private static final String TAG = "NavigationActivity";        //������־��ǩ
    private static final boolean D = true;

    private static final int SURFACE_VIEW_WIDTH = 705;
    private static final int SURFACE_VIEW_HEIGHT = 660;

    private static final char END = '*'; // ����ͨ���ַ���������־
    private static final char START = '#'; // ����ͨ���ַ�����ʼ��־
    private static final char SEPARATOR = ','; // �ָ���
    private static final int SEPARATOR_NUMBER = 12; // �ָ�������

    private static final int REQUEST_SELECT_FIELD = 3;   // ��ת��ѡ��ؿ��б����������
    private static final int REQUEST_SELECT_TRACTOR = 4;   // ��ת��ѡ�����б����������
    private static final int REQUEST_SELECT_AB_LINE = 5;   // ��ת��ѡ��AB���б����������
    private static final int REQUEST_SELECT_HISTORY_PATH = 6;   // ��ת��ѡ����ʷ�켣�б����������

    public static final int DEFAULT_STATE_RESPONSE = 99999;
    public static final int SET_A_RESPONSE = 40103;
    public static final int SET_B_RESPONSE = 40104;
    public static final int LINE_NAVI_RESPONSE = 10010;
    public static final int TURNING_RIGHT_RESPONSE = 10020;
    public static final int TURNING_LEFT_RESPONSE = 10030;

    public static final int HEADLAND_P1_RESPONSE = 50101;
    public static final int HEADLAND_P2_RESPONSE = 50102;

    private static final String DATA_DIRECTORY = "data";
    private static final String AB_LINE_DIRECTORY = "ab_lines";

    private static final String QUERY_ALL = "%%";

    private static final String DEFAULT_FIELD = "default_field";
    private static final String DEFAULT_TRACTOR = "default_tractor";

    ImageButton imgbtnBack;  //���ذ�ť
    ImageButton imgbtnConnectionStatus;  //����״̬��ʾ��ť
    Button btnSetField;  //������ذ�ť
    Button btnSetTractor;  //������ذ�ť
    CheckBox chkboxABMode;  //AB�ߵ���ģʽ��ť
    Button btnPlanningMode; //�滮����ģʽ��ť
    CheckBox chkboxHistory;  //��ʷ��¼��ť
    CheckBox chxboxStatistics; //ͳ�����ݰ�ť
    CheckBox chxboxRemoteMode; //ң����ģʽ��ť
    LinearLayout layoutABModePane;
    Button btnHistoryAB;  //��ʷAB�߰�ť
    Button btnSetA;  //����A�㰴ť
    Button btnSetB;  //����B�㰴ť
    CheckBox chkboxStartNavi; //��������
    LinearLayout layoutRemotePane;
    Button btnAccelerate; //���ٰ�ť
    Button btnTurnLeft;  //��ת��ť
    CheckBox chkboxStartSwitch;  //�������ذ�ť
    Button btnTurnRight;  //��ת��ť
    ImageButton imgbtnEmergencyStop;    //��ͣ��ť

    TextView txtDeviance;  //����ƫ���ı�
    TextView txtSatellite;  //������Ŀ�ı�
    TextView txtGpsState;  //GPS״̬�ı�
    TextView txtFieldName;  //�ؿ������ı�
    TextView txtTractorName;  //���������ı�
    TextView txtLineSpacing;    //�м��
    TextView txtLocationX;     //��λX�����ı�
    TextView txtLocationY;     //��λY�����ı�
    TextView txtVelocity;  //�ٶ��ı�
    TextView txtDirectionAngle;    //������ı�
    TextView txtTurningAngle;  //ת����ı�
    TextView txtPrecisionSeeding;  //�������������ı�
    TextView txtReceivedString;    //���������ı�
    TextView txtReceivedStringNo;  //�������ݱ���ı�
    TextView txtSentString;    //���������ı�


    Bundle bundle;
    private int dataNo = 0;
    private int currentState = 0; //��ǰָ��״̬
    private int preState = 0; //��һ��ָ��״̬

    private double locationX = 0;
    private double locationY = 0;
    private double aX, aY, bX, bY; //AB��XY����
    private double aLat, aLng, bLat, bLng; //AB�㾭γ��
    private long startNavigationTime; //ÿ�ε�������ʱ��
    private long stopNavigationTime; //ÿ�ε����ر�ʱ��
    private String fileNameToSave;
    private String currentTime;

    private boolean isDataToSave = false;
    private boolean isPointASet = false;
    private boolean isPointBSet = false;
    private boolean isBoundP1Set = false;
    private boolean isBoundP2Set = false;
    private boolean isBoundP3Set = false;
    private boolean isBoundP4Set = false;
    private boolean isStartNavi = false;
    private boolean isStopNavi = false;
    private boolean isToTurnRight = false;
    private boolean isToTurnLeft = false;
    private boolean isOnQueryingHistory = false; //�Ƿ���ʷ��¼


    private ArrayList<Double> pathListX, pathListY;
    // private ArrayList<Double> boundListX,boundListY;
    private HashMap<Integer, Double> boundTempMapX, boundTempMapY;
    private ArrayList<GeoPoint> fieldVertex;    //����ؿ鶥������

    private MyApplication myApp; // ����ȫ�ֱ���
    private SharedPreferences myPreferences; //Ĭ��ƫ�ò����洢ʵ��
    private MySurfaceView myView; // ��ͼ��ʾ�ؼ�
    private LinearLayout statisticsView;
    private LineChart lineChart;  // ����ͼ�ؼ�
    private TextView txtAverageLateral;
    private ScaleAnimation showAnimation;
    private ScaleAnimation hideAnimation;
    private List<PointValue> mPointValues = new ArrayList<PointValue>(); // ����ͼ�����ݼ���
    List<GeoPoint> historyPathPoints = new ArrayList<>(); //��ʷ��¼�켣�㼯��
    List<PointValue> historyPointValues = new ArrayList<>(); //��ʷ��¼����ͼ�����ݼ���
    private String defaultFieldName;
    private String defaultTractorName;
    private double linespacing = 2.5; //��ҵ�м��
    /*������Ϣ������������ͨ���̷߳��͹��������ݡ�*/
    MyNaviHandler mNaviHandler = new MyNaviHandler(this);

    /*handler��message�ַ���������ʹ��StringBuilder�������Զ����String�ࣻ
    ���⣬ʹ��ȫ�־�̬������ʹ�þֲ������ٶȿ�һ�����ϡ�*/
    private static StringBuilder stringBuilder = new StringBuilder();

    /*ʹ�þ�̬�����������������*/
    private static double lat;
    private static double lng;
    private static double xx;
    private static double yy;
    private static int satellite;
    private static int gps;
    private static double velocity;
    private static int command;
    private static double sensor;
    private static double direction;
    private static double lateral;
    private static double turnning;
    private static double seeding;

    public NavigationActivity() {
    }

    /**
     * ʹ�þ�̬�ڲ������Handler�������ڴ�й©����;
     * ��handlerMessage()��д��Ϣ��������
     */
    private static class MyNaviHandler extends Handler {
        //����������MyFieldHandler��GC����ʱ�ᱻ���յ�
        private final WeakReference<NavigationActivity> mReferenceActivity;

        MyNaviHandler(NavigationActivity activity) {
            mReferenceActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            final NavigationActivity activity = mReferenceActivity.get();
            super.handleMessage(msg);
            if (activity != null) {
                switch (msg.what) {
                    case BluetoothService.MESSAGE_RECEIVED:
                        // �������յ�������
                        activity.doNavigationTask(msg);
                        break;

                    case BluetoothService.MESSAGE_SENT:
                        String writeMessage = msg.obj.toString();
                        String[] arrays = (writeMessage == null ? null : writeMessage.split(","));
                        String sentMsg = ((arrays != null && arrays.length > 1) ? arrays[1] : "NULL");
                        activity.txtSentString.setText(sentMsg);
                        break;

                    case BluetoothService.MESSAGE_CONNECT_RESULT:
                        if (BluetoothService.CONNECTION_BROKEN_MESSAGE.equals(msg.obj.toString())) {
                            activity.imgbtnConnectionStatus.setBackgroundResource(R.drawable.connection_broken);
                        }
                        ToastUtil.showToast(msg.obj.toString(), true);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.navi_activity);
        if (D) {
            Log.e(TAG, "+++ ON CREATE +++");
        }

        //ʵ�����ؿ鶥���б�����
        fieldVertex = new ArrayList<GeoPoint>();

        //��ȡȫ����ʵ��
        myApp = (MyApplication) getApplication();
        //�����������ӵ���Ϣ������Ϊ��ǰ���洦����
        myApp.getBluetoothService().setHandler(mNaviHandler);
        if (D) {
            Log.e(TAG, "+++ setHandler: mNaviHandler +++");
        }

        //ʵ����Ĭ��ƫ������
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        initViews(); //��ʼ�������ؼ�

         /* ��ȡĬ��ƫ�������� */
        defaultTractorName = myPreferences.getString(DEFAULT_TRACTOR, null);
        if (!TextUtils.isEmpty(defaultTractorName)) {
            Cursor resultCursor = myApp.getDatabaseManager().queryTractorByName(defaultTractorName);
            Map<String, String> map = DatabaseManager.cursorToMap(resultCursor);
            txtTractorName.setText(defaultTractorName);
            txtLineSpacing.setText(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING) + "m");
            try {
                linespacing = Double.parseDouble(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING));
            } catch (NumberFormatException e) {
                ToastUtil.showToast("��ȡ��ҵ�м�����ָ�ʽ����!", true);
            }
        } else {
            ToastUtil.showToast(getString(R.string.toast_set_defaut_tractor_tip), true);
        }

        // ---CheckBox---
        CheckBox checkBox1 = (CheckBox) findViewById(R.id.starTopLeft2);
        checkBox1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    isBoundP1Set = true;
                }
                myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_HEADLAND_POINT_1);
            }
        });

        CheckBox checkBox2 = (CheckBox) findViewById(R.id.starTopRight2);
        checkBox2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    isBoundP2Set = true;
                }
                myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_HEADLAND_POINT_2);
            }
        });

        CheckBox checkBox4 = (CheckBox) findViewById(R.id.starBottomRight2);
        checkBox4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    isBoundP3Set = true;
                }
            }
        });

        CheckBox checkBox3 = (CheckBox) findViewById(R.id.starBottomLeft2);
        checkBox3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    isBoundP4Set = true;
                }
            }
        });


        pathListX = new ArrayList<Double>();
        pathListY = new ArrayList<Double>();
        // boundListX = new ArrayList<Double>();
        // boundListY = new ArrayList<Double>();


        boundTempMapX = new HashMap<Integer, Double>();
        boundTempMapY = new HashMap<Integer, Double>();


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (D) {
            Log.e(TAG, "+++ ON START +++");
        }

        if (myApp.getBluetoothService().getState() == BluetoothService.STATE_NONE) {
            imgbtnConnectionStatus.setBackgroundResource(R.drawable.connection_broken);
        } else {
            imgbtnConnectionStatus.setBackgroundResource(R.drawable.connection_ok);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (D) {
            Log.e(TAG, "+++ ON RESUME +++");
        }

        //����Ĭ�ϵؿ�
        defaultFieldName = myPreferences.getString(DEFAULT_FIELD, null);
        if (!TextUtils.isEmpty(defaultFieldName)) {
            Cursor resultCursor = myApp.getDatabaseManager().queryFieldWithPointsByName(defaultFieldName);
            List<Map<String, String>> resultList = DatabaseManager.cursorToList(resultCursor);

            fieldVertex.clear();
            for (int i = 0; i < resultList.size(); i++) {
                GeoPoint vertex = new GeoPoint(Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_X_COORDINATE)),
                        Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_Y_COORDINATE)));
                fieldVertex.add(vertex);
            }
            boolean b = myView.setFieldBoundary(fieldVertex, true);
            if (b) {
                txtFieldName.setText(defaultFieldName);
            } else {
                ToastUtil.showToast(getString(R.string.toast_set_defaut_field_tip), true);
            }
            if (D) {
                Log.e(TAG, "set default field: " + defaultFieldName);
            }
        } else {
            ToastUtil.showToast(getString(R.string.toast_set_defaut_field_tip), true);
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        if (D) {
            Log.e(TAG, "+++ ON PAUSE +++");
        }
    }

    @Override
    protected void onRestart() {

        super.onRestart();
        if (D) {
            Log.e(TAG, "+++ ON RESTART +++");
        }

        // ���½������ʱ������Ϣ������Ϊ��ǰ������
        myApp.getBluetoothService().setHandler(mNaviHandler);

        if (myApp.getBluetoothService().getState() == BluetoothService.STATE_NONE) {
            imgbtnConnectionStatus.setBackgroundResource(R.drawable.connection_broken);
        } else {
            imgbtnConnectionStatus.setBackgroundResource(R.drawable.connection_ok);
        }
    }

    @Override
    protected void onStop() {

        super.onStop();
        if (D) {
            Log.e(TAG, "+++ ON STOP +++");
        }
    }

    @Override
    protected void onDestroy() {

        if (D) {
            Log.e(TAG, "+++ ON DESTROY +++");
        }
        // �˳�Activity�����MessageQueue��û��������Ϣ
        mNaviHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_FIELD:
                if (resultCode == RESULT_OK) {
                    bundle = data.getExtras();
                    String name = (bundle == null ? null : bundle.getString(FieldInfo.FIELD_NAME));
                    Log.e("selected item", name);
                    /* ����ǰѡ��ĵؿ����ƴ浽Ĭ��ƫ�����ã��´γ����Զ�ѡ��˴�ѡ��ĵؿ� */

                    Cursor resultCursor = myApp.getDatabaseManager().queryFieldWithPointsByName(name);
                    List<Map<String, String>> resultList = DatabaseManager.cursorToList(resultCursor);

                    fieldVertex.clear();
                    for (int i = 0; i < resultList.size(); i++) {
                        GeoPoint vertex = new GeoPoint(Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_X_COORDINATE)),
                                Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_Y_COORDINATE)));
                        fieldVertex.add(vertex);
                    }

                    boolean b = myView.setFieldBoundary(fieldVertex, true);
                    if (b) {
                        myPreferences.edit().putString(DEFAULT_FIELD, name).commit();
                        txtFieldName.setText(name);
                    } else {
                        ToastUtil.showToast(getString(R.string.toast_reset_field_tip), true);
                    }
                }
                break;

            case REQUEST_SELECT_TRACTOR:
                if (resultCode == RESULT_OK) {
                    bundle = data.getExtras();
                    String name = (bundle == null ? null : bundle.getString(TractorInfo.TRACTOR_NAME));
                    Log.e("selected item", name);
                    txtTractorName.setText(name);

                    /* ����ǰѡ��ĵؿ����ƴ浽Ĭ��ƫ�����ã��´γ����Զ�ѡ��˴�ѡ��ĵؿ� */
                    myPreferences.edit().putString(DEFAULT_TRACTOR, name).commit();

                    Cursor resultCursor = myApp.getDatabaseManager().queryTractorByName(name);
                    Map<String, String> map = DatabaseManager.cursorToMap(resultCursor);
                    txtLineSpacing.setText(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING) + " m");
                    try {
                        linespacing = Double.parseDouble(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING));
                    } catch (NumberFormatException e) {
                        ToastUtil.showToast("��ȡ��ҵ�м�����ָ�ʽ����!", true);
                    }
                }
                break;

            case REQUEST_SELECT_AB_LINE:
                if (resultCode == RESULT_OK) {
                    bundle = data.getExtras();
                    String name = (bundle == null ? null : bundle.getString(ABLine.AB_LINE_NAME_BY_DATE));
                    // TODO
                }
                break;

            case REQUEST_SELECT_HISTORY_PATH:
                if (resultCode == RESULT_OK) {
                    bundle = data.getExtras();
                    Map<String, String> map = (Map<String, String>) bundle.getSerializable("history_entry");
                    String name = (map == null ? null : map.get(HistoryPath.HISTORY_RECORD_FILE_NAME));
                    String field = (map == null ? null : map.get(HistoryPath.FIELD_NAME));
                    Log.e(TAG, "HISTORY FIELD IS " + field);

                    getHistoryDataFromFiles(name, historyPathPoints, historyPointValues); //��ȡ��ʷ��¼��

                    if (!TextUtils.isEmpty(field)) {
                        Cursor resultCursor = myApp.getDatabaseManager().queryFieldWithPointsByName(field);
                        List<Map<String, String>> resultList = DatabaseManager.cursorToList(resultCursor);

                        //��ȡ��ʷ�켣���ڵؿ飻
                        ArrayList<GeoPoint> hField = new ArrayList<>();
                        for (int i = 0; i < resultList.size(); i++) {
                            GeoPoint vertex = new GeoPoint(Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_X_COORDINATE)),
                                    Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_Y_COORDINATE)));
                            hField.add(vertex);
                        }

                        //������ʷ�켣�ؿ�;
                        boolean b = myView.setFieldBoundary(hField, true);
                        if (b) {
                            txtFieldName.setText(field);
                        } else {
                            ToastUtil.showToast("�Ҳ����ؿ�����!", true);
                        }
                    }
                    myView.drawHistoryPath(historyPathPoints);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imgbtnBack:
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnSetField:
                Cursor cursor = myApp.getDatabaseManager().queryFieldByName(QUERY_ALL);
                Bundle data = new Bundle();
                data.putSerializable("data", DatabaseManager.cursorToList(cursor));
                Intent intent = new Intent("sjtu.me.tractor.navigation.FieldResultActivity");
                intent.putExtras(data);
                startActivityForResult(intent, REQUEST_SELECT_FIELD);
                break;

            case R.id.btnSetTractor:
                Cursor cursor2 = myApp.getDatabaseManager().queryTractorByName(QUERY_ALL);
                Bundle data2 = new Bundle();
                data2.putSerializable("data", DatabaseManager.cursorToList(cursor2));
                Intent intent2 = new Intent("sjtu.me.tractor.navigation.TractorResultActivity");
                intent2.putExtras(data2);
                startActivityForResult(intent2, REQUEST_SELECT_TRACTOR);
                break;

            case R.id.chkboxABMode:
                if (((CheckBox) v).isChecked()) {
                    layoutABModePane.setVisibility(View.VISIBLE);
                } else {
                    layoutABModePane.setVisibility(View.INVISIBLE);
                }
                break;

            case R.id.btnHistoryAB:
                Cursor cursor3 = myApp.getDatabaseManager().queryABlineByDate(QUERY_ALL);
                Bundle data3 = new Bundle();
                data3.putSerializable("data", DatabaseManager.cursorToList(cursor3));
                Intent intent3 = new Intent("sjtu.me.tractor.navigation.ABLineResultActivity");
                intent3.putExtras(data3);
                startActivityForResult(intent3, REQUEST_SELECT_AB_LINE);
                break;

            case R.id.btnSetA:
                isPointASet = true;
                myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_A_REQUEST);
                break;

            case R.id.btnSetB:
                isPointBSet = true;
                myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_B_REQUEST);
                break;

            case R.id.chkboxStartNavi:
                if (((CheckBox) v).isChecked()) {
                    startNavigation();
                    startPlotAndSavePath();
                    isStartNavi = true;
                    isStopNavi = false;
                    /*����������������ʷ��ѯ��ťΪ���ɵ��״̬*/
                    chkboxHistory.setClickable(false);
                    chkboxHistory.setChecked(false);
                } else {
                    stopNavigation();
                    stopPlotAndSavePath();
                    isStartNavi = false;
                    isStopNavi = true;
                    /*����������������ʷ��ѯ��ťΪ�ɵ��״̬*/
                    chkboxHistory.setClickable(true);
                }
                break;

            case R.id.btnPlanningMode:
                AlertDialog planningDialog = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.alert_title_field_tip))
                        .setMessage(getString(R.string.alert_message_navi_download))
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(getString(R.string.affirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();
                planningDialog.show();
                AlertDialogUtil.changeDialogTheme(planningDialog);
                break;

            case R.id.chkboxHistory:
                if (((CheckBox) v).isChecked()) {
                    Cursor cursor4 = myApp.getDatabaseManager().queryHistoryEntries(QUERY_ALL);
                    Bundle data4 = new Bundle();
                    data4.putSerializable("data", DatabaseManager.cursorToList(cursor4));
                    Intent intent4 = new Intent("sjtu.me.tractor.navigation.HistoryPathResultActivity");
                    intent4.putExtras(data4);
                    startActivityForResult(intent4, REQUEST_SELECT_HISTORY_PATH);

                    isOnQueryingHistory = true; //�л��Ƿ���ʷ��¼��־
                    /*������ð�ť��Ϊ���ɵ��״̬*/
                    btnSetField.setClickable(false);
                } else {
                    isOnQueryingHistory = false;
                    myView.hideHistoryPath();
                    myView.setFieldBoundary(fieldVertex, true);
                     /*������ð�ť��Ϊ�ɵ��״̬*/
                    btnSetField.setClickable(true);
                }

                break;

            case R.id.chkboxStatistics:
                if (((CheckBox) v).isChecked()) {
                    if (!isOnQueryingHistory) {
                        lineChart.setPointValues(mPointValues);
                        if (mPointValues != null && mPointValues.size() > 0) {
                            float totalLateral = 0.0f;
                            for (PointValue mPointValue : mPointValues) {
                                totalLateral += mPointValue.getY();
                            }
                            float average = totalLateral / mPointValues.size();
                            txtAverageLateral.setText("ƽ������ƫ��Ϊ��" + String.valueOf(average) + " m");
                        }
                    } else {
                        if (historyPointValues.size() != 0) {
                            lineChart.setPointValues(historyPointValues);
                            Log.e(TAG, "HISTORY LINECHART POINTVALUES SET");
                            if (historyPointValues != null && historyPointValues.size() > 0) {
                                float totalLateral = 0.0f;
                                for (PointValue mPointValue : historyPointValues) {
                                    totalLateral += mPointValue.getY();
                                }
                                float average = totalLateral / historyPathPoints.size();
                                txtAverageLateral.setText("ƽ������ƫ��Ϊ��" + String.valueOf(average) + " m");
                                lineChart.setPointValues(historyPointValues);
                                Log.e(TAG, "HISTORY AVERAGE SET");
                            }
                        }
                    }
                    showChartView(statisticsView);
                } else {
                    hideChartView(statisticsView);
                }
                break;

            case R.id.chkboxRemoteMode:
                if (((CheckBox) v).isChecked()) {
                    layoutRemotePane.setVisibility(View.VISIBLE);
                } else {
                    layoutRemotePane.setVisibility(View.INVISIBLE);
                }
                break;

            case R.id.btnAccelerate:
                break;

            case R.id.btnTurnLeft:
                isToTurnLeft = true;
                myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_TURN_LEFT);
                break;

            case R.id.chkboxStartSwitch:
                if (((CheckBox) v).isChecked()) {
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_START_NAVI); //�������Ϳ�ʼ����ָ��
                } else {
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI); //��������ֹͣ����ָ��
                }
                break;

            case R.id.btnTurnRight:
                isToTurnRight = true;
                myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_TURN_RIGHT);
                break;

            case R.id.imgbtnEmergencyStop:
                isStopNavi = true;
                myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI);
                break;

            default:
                break;
        }
    }

    private void initViews() {
        myView = (MySurfaceView) findViewById(R.id.mySurfaceView);
        myView.setCanvasSize(SURFACE_VIEW_WIDTH * 2, SURFACE_VIEW_HEIGHT * 2);

        statisticsView = (LinearLayout) findViewById(R.id.statisticView);
        txtAverageLateral = (TextView) findViewById(R.id.txtAverageLateral);
        lineChart = new LineChart((LineChartView) findViewById(R.id.lineChartView));
        showAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        showAnimation.setDuration(200);

        hideAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        hideAnimation.setDuration(200);

        txtDeviance = ((TextView) findViewById(R.id.txtDeviance));
        txtSatellite = (TextView) findViewById(R.id.txtSatellite);
        txtGpsState = (TextView) findViewById(R.id.txtGpsState);
        txtFieldName = (TextView) findViewById(R.id.txtFieldName);
        txtTractorName = (TextView) findViewById(R.id.txtTractorName);
        txtLineSpacing = (TextView) findViewById(R.id.txtLineSpacing);
        txtLocationX = (TextView) findViewById(R.id.txtLocationX);
        txtLocationY = (TextView) findViewById(R.id.txtLocationY);
        txtVelocity = (TextView) findViewById(R.id.txtVelocity);
        txtDirectionAngle = (TextView) findViewById(R.id.txtDirectionAngle);
        txtDeviance = (TextView) findViewById(R.id.txtDeviance);
        txtTurningAngle = (TextView) findViewById(R.id.txtTurningAngle);
        txtPrecisionSeeding = (TextView) findViewById(R.id.txtPrecsionSeeding);
        txtReceivedString = (TextView) findViewById(R.id.txtReceivedString);
        txtReceivedStringNo = (TextView) findViewById(R.id.txtReceivedStringNo);
        txtSentString = (TextView) findViewById(R.id.txtSentString);

        imgbtnBack = (ImageButton) findViewById(R.id.imgbtnBack);
        imgbtnBack.setOnClickListener(this);

        imgbtnConnectionStatus = (ImageButton) findViewById(R.id.imgbtnConnectionStatus);

        btnSetField = (Button) findViewById(R.id.btnSetField);
        btnSetField.setOnClickListener(this);

        btnSetTractor = (Button) findViewById(R.id.btnSetTractor);
        btnSetTractor.setOnClickListener(this);

        chkboxABMode = (CheckBox) findViewById(R.id.chkboxABMode);
        chkboxABMode.setOnClickListener(this);

        btnPlanningMode = (Button) findViewById(R.id.btnPlanningMode);
        btnPlanningMode.setOnClickListener(this);

        chkboxHistory = (CheckBox) findViewById(R.id.chkboxHistory);
        chkboxHistory.setOnClickListener(this);

        chxboxStatistics = (CheckBox) findViewById(R.id.chkboxStatistics);
        chxboxStatistics.setOnClickListener(this);

        chxboxRemoteMode = (CheckBox) findViewById(R.id.chkboxRemoteMode);
        chxboxRemoteMode.setOnClickListener(this);

        layoutABModePane = (LinearLayout) findViewById(R.id.layoutABMode);

        btnHistoryAB = (Button) findViewById(R.id.btnHistoryAB);
        btnHistoryAB.setOnClickListener(this);

        btnSetA = (Button) findViewById(R.id.btnSetA);
        btnSetA.setOnClickListener(this);

        btnSetB = (Button) findViewById(R.id.btnSetB);
        btnSetB.setOnClickListener(this);

        chkboxStartNavi = (CheckBox) findViewById(R.id.chkboxStartNavi);
        chkboxStartNavi.setOnClickListener(this);

        layoutRemotePane = (LinearLayout) findViewById(R.id.layoutRemotePane);

        btnAccelerate = (Button) findViewById(R.id.btnAccelerate);
        btnAccelerate.setOnClickListener(this);

        btnTurnLeft = (Button) findViewById(R.id.btnTurnLeft);
        btnTurnLeft.setOnClickListener(this);

        chkboxStartSwitch = (CheckBox) findViewById(R.id.chkboxStartSwitch);
        chkboxStartSwitch.setOnClickListener(this);

        btnTurnRight = (Button) findViewById(R.id.btnTurnRight);
        btnTurnRight.setOnClickListener(this);

        imgbtnEmergencyStop = (ImageButton) findViewById(R.id.imgbtnEmergencyStop);
        imgbtnEmergencyStop.setOnClickListener(this);

    }

    /**
     * ִ�е�������
     * �������͹��������ݡ�
     *
     * @param msg ��Ϣ
     */
    private void doNavigationTask(Message msg) {
        String dataString;
        String msgString = (String) msg.obj;
        stringBuilder.append(msgString);

        if (stringBuilder.length() < 0) {
            return;
        }

        /*��� ","�ĸ�����Ϊ13�������ַ�����ʼ�ͽ����ַ�����ָ���ַ�����������Ч*/
        if ((SysUtil.countCharacter(stringBuilder, SEPARATOR) != SEPARATOR_NUMBER)
                || (stringBuilder.charAt(0) != START) || (stringBuilder.charAt(stringBuilder.length() - 1) != END)) {
            stringBuilder.delete(0, stringBuilder.length()); //�����Ч����
        } else {
            dataNo++;
            dataString = stringBuilder.substring(1, stringBuilder.length() - 1); // ��ȡ�ַ���
            // ���յ������ݱ��浽�ⲿ�洢��
            if (isDataToSave) {
                FileUtil.writeDataToExternalStorage(DATA_DIRECTORY, fileNameToSave, dataString, true, true);
            }

            // set the TextView with the received data
            txtReceivedStringNo.setText(String.valueOf(dataNo));

            if (parseReceivedMessage(dataString)) {
                txtDeviance.setText(String.valueOf(lateral));
                txtSatellite.setText(String.valueOf(satellite));
                switch (gps) {
                    /*����GPS��λ��ʶ������ʾ�ı�*/
                    case 0:
                        txtGpsState.setText(R.string.satellite_gps_no_location);
                        break;

                    case 1:
                        txtGpsState.setText(R.string.satellite_gps_single_point);
                        break;

                    case 2:
                        txtGpsState.setText(R.string.satellite_gps_rtk);
                        break;

                    case 4:
                        txtGpsState.setText(R.string.satellite_gps_rtk_fixed);
                        break;

                    case 5:
                        txtGpsState.setText(R.string.satellite_gps_float);
                        break;

                    default:
                        txtGpsState.setText(R.string.satellite_gps_no_location);
                        break;
                }
                txtLocationX.setText(String.valueOf(xx));
                txtLocationY.setText(String.valueOf(yy));
                txtVelocity.setText(String.valueOf(velocity) + "Km/h");
                txtDirectionAngle.setText(String.valueOf(direction) + "��");
                txtTurningAngle.setText(String.valueOf(turnning));
                txtPrecisionSeeding.setText(String.valueOf(seeding));
                currentState = command;
                txtReceivedString.setText(String.valueOf(command));
                locationX = xx;
                locationY = yy;

                if (!isStopNavi) { //��¼����ƫ��
                    long timeMillis = System.currentTimeMillis();
                /*��ʱ����ᣬ����ƫ��Ϊ���ᣬ����ƫ�����ݵ�����*/
                    if (Math.abs(lateral) < 3) {//����¼̫��ĺ���ƫ������������ģ�
                        mPointValues.add(new PointValue((float) ((timeMillis - startNavigationTime) / 1000.0), (float) lateral));
                    }
                }

                /*���Ƶ�ǰ��*/
                myView.setCurentPoint(dataNo, locationX, locationY);
                Log.e(TAG, "setCurrentPoint");

                 /*���㵽��ͷ�ľ���*/
                double distance1 = GisAlgorithm.distanceFromPointToLine(622420.828635, 3423930.259849,
                        622422.2437, 3423929.17782, locationX, locationY);
                double distance2 = GisAlgorithm.distanceFromPointToLine(622423.89173, 3424003.981122,
                        622446.48542, 3424005.692487, locationX, locationY);

                /*����A��*/
                if (isPointASet && currentState == SET_A_RESPONSE) {// �жϴ�ʱ�Ƿ�������A��
                    if (preState != SET_A_RESPONSE) {
                        aX = locationX;
                        aY = locationY;
                        aLat = lat;
                        aLng = lng;
                        ToastUtil.showToast(getString(R.string.a_point_already_set), true);
                        isPointASet = false;
                    }
                    // ���÷���ָ��Ϊȷ���յ�A����������
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_A_AFFIRM);
                }

                /*����B��*/
                if (isPointBSet && currentState == SET_B_RESPONSE) {// �жϴ�ʱ�Ƿ�������B��
                    if (preState != SET_B_RESPONSE) {
                        bX = locationX;
                        bY = locationY;
                        bLat = lat;
                        bLng = lng;

                        if (aY == bY && aX == bX) {
                            //����AB���غϾ���
                            ToastUtil.showToast(getString(R.string.ab_overlay_error_warning), true);
                            return;
                        } else {
                            ToastUtil.showToast(getString(R.string.b_point_already_set), true);
                            myView.drawABline(aX, aY, bX, bY, true);
                            currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                            String fileABPoints = currentTime + "_ab_points.ab";
                            String abLine = new StringBuilder()
                                    .append(aX).append(",").append(aY).append(",").append(aLat).append(",").append(aLng)
                                    .append("\r\n")
                                    .append(bX).append(",").append(bY).append(",").append(bLat).append(",").append(bLng)
                                    .toString();
                            // ����AB�ߵ��ⲿ�ļ�
                            FileUtil.writeDataToExternalStorage(AB_LINE_DIRECTORY, fileABPoints, abLine, true, false);

                            myApp.getDatabaseManager().insertABline(currentTime, new ABLine(aX, aY, bX, bY), defaultFieldName);
                        }

                        isPointBSet = false;
                    }
                    // ���÷���ָ��Ϊȷ���յ�B����������
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_B_AFFIRM);
                }

                /*�����������յ�ֱ����ʻ��Ӧ���л�ΪĬ�Ϸ���ָ��*/
                if (isStartNavi == true && currentState == LINE_NAVI_RESPONSE) {
                    isStartNavi = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                /*ֹͣ�������յ�Ĭ����Ӧ���л�ΪĬ�Ϸ���ָ��*/
                if (isStopNavi == true && currentState == DEFAULT_STATE_RESPONSE) {
                    isStopNavi = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                /*��ת���յ���ת��Ӧ���л�ΪĬ�Ϸ���ָ��*/
                if (isToTurnRight && currentState == TURNING_RIGHT_RESPONSE) {
                    isToTurnRight = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                /*��ת���յ���ת��Ӧ���л�ΪĬ�Ϸ���ָ��*/
                if (isToTurnLeft && currentState == TURNING_LEFT_RESPONSE) {
                    isToTurnLeft = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                if (!isStopNavi) {
                    if (currentState == LINE_NAVI_RESPONSE || currentState == HEADLAND_P1_RESPONSE
                            || currentState == HEADLAND_P2_RESPONSE) {
                        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                    }
                }

                preState = currentState; // ���浱ǰ״̬
            }

            stringBuilder.delete(0, stringBuilder.length()); //���Ч����
        }
    }

    /**
     * ������������
     *
     * @param dataString ���յ�������
     * @return �����ɹ���־
     */
    private boolean parseReceivedMessage(String dataString) {
        String[] dataArray = dataString.split(String.valueOf(SEPARATOR), SEPARATOR_NUMBER + 1);
        if (dataArray == null || dataArray.length < 13) {
            return false;
        }
        try {
            lat = Double.parseDouble(dataArray[0]);
            lng = Double.parseDouble(dataArray[1]);
            xx = Double.parseDouble(dataArray[2]);
            yy = Double.parseDouble(dataArray[3]);
            satellite = Integer.parseInt(dataArray[4]);
            gps = Integer.parseInt(dataArray[5]);
            direction = Double.parseDouble(dataArray[6]);
            velocity = Double.parseDouble(dataArray[7]);
            command = Integer.parseInt(dataArray[8]);
            sensor = Double.parseDouble(dataArray[9]);
            lateral = Double.parseDouble(dataArray[10]);
            if ("nan".equalsIgnoreCase(dataArray[11])) {
                turnning = 99999.999;
            } else {
                turnning = Double.parseDouble(dataArray[11]);
            }
            seeding = Double.parseDouble(dataArray[12]);
            return true;

        } catch (NumberFormatException e) {
            ToastUtil.showToast(getString(R.string.received_data_type_error), true);
            return false;
        }
    }

    /**
     * ��ʼ������������
     */
    private void startNavigation() {
        /*����������������*/
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_START_NAVI); //�������Ϳ�ʼ����ָ��
        startNavigationTime = System.currentTimeMillis(); //��¼��ʼ����ϵͳʱ�䣨���룩
        mPointValues.clear(); //��������ƫ�����ݼ�������
        isStartNavi = true; //���õ���״̬Ϊ��
    }

    /**
     * ֹͣ������������
     */
    private void stopNavigation() {
        /*����ֹͣ��������*/
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI); //��������ֹͣ����ָ��
        lineChart.setPointValues(mPointValues); //������ƫ�����ݼ���������ͼ
        Log.e(TAG, "VALUES SIZE() IS :" + mPointValues.size());
        isStopNavi = true;
        stopNavigationTime = System.currentTimeMillis();
        /*������ֹͣ�����͵����ʼ����ʱ������20���򱣴�ôι켣�����ݿ�*/
        if ((stopNavigationTime - startNavigationTime) / 1000 > 20) {
            myApp.getDatabaseManager().insertHistoryEntry(fileNameToSave, defaultFieldName);
        }
    }

    /**
     * ��ʼ���ƹ켣��������
     */
    private void startPlotAndSavePath() {
        isDataToSave = true; //���ÿ�ʼ��������״̬Ϊ��
        dataNo = 0; //�����ݵ�������Ϊ��
        myView.setOperationPathWidth(linespacing);
        myView.drawPointToPath(1, true); //���û��ƹ켣״̬Ϊ��
        currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()); //��ȡ��ǰ����ʱ���ַ���
        fileNameToSave = "data_" + currentTime + ".dt"; //�������ݱ����ļ���
    }

    /**
     * ֹͣ���ƹ켣��������
     */
    private void stopPlotAndSavePath() {
        myView.drawPointToPath(1, false);
        isDataToSave = false;
    }

    public void showChartView(View view) {
        view.startAnimation(showAnimation);
        view.setVisibility(View.VISIBLE);
    }

    public void hideChartView(View view) {
        view.setAnimation(hideAnimation);
        view.startAnimation(hideAnimation);
        view.setVisibility(View.GONE);
    }

    /**
     * �ж��Ƿ���Ҫ����
     * ���ⷽ����ʱ�ǲ����õģ���Ϊ�����������δ�Խ����߽�һ�������ֱ�ߣ�����ֻ��һ��ת�䣩
     *
     * @param distance ����߽�ʵʱ����
     * @param limit    �����������
     * @return
     */
    public boolean isToTurn(double distance, double limit) {
        return distance > limit && (distance - limit) < 0.1;
    }

    /**
     * @param fileName           ��ʷ��¼�ļ���
     * @param historyPathPoints  ��ȡ��ʷ�ļ��󱣴���ʷ�켣�Ļ����б�
     * @param historyPointValues ��ȡ��ʷ�ļ��󱣴���ʷ����ƫ���¼�Ļ����б�
     * @return �ɹ���־
     */
    public static boolean getHistoryDataFromFiles(String fileName,
                                                  List<GeoPoint> historyPathPoints,
                                                  List<PointValue> historyPointValues) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS", Locale.SIMPLIFIED_CHINESE);
        String dataDirectory = FileUtil.getAlbumStorageDir(NavigationActivity.DATA_DIRECTORY).toString();
        Log.e(TAG, "data dir is: " + dataDirectory);
        String filePath = dataDirectory + File.separator + fileName;
        String[] lines = FileUtil.readFileFromExternalStorage(filePath, null);
        if (lines == null || lines.length == 0) {
            return false;
        }
        historyPathPoints.clear();
        historyPointValues.clear();
        long startTime = 0;
        try {
            startTime = sdf.parse(lines[0].split(",")[13]).getTime();
            for (int i = 0; i < lines.length; i++) {
                String[] arrays = lines[i].split(",");
                if (arrays.length == 14) {
                    try {
                        double hX = Double.parseDouble(arrays[2]);
                        double hY = Double.parseDouble(arrays[3]);
                        double hLateral = Double.parseDouble(arrays[10]);
                        long hTime = sdf.parse(arrays[13]).getTime();
                        historyPathPoints.add(new GeoPoint(hX, hY)); //������ʷ�켣������
                        if (hLateral < 2.0) {
                            /*���Թ���ĺ���ƫ��(�����������������)*/
                            historyPointValues.add(new PointValue((float) ((hTime - startTime) / 1000.0), (float) hLateral));
                        }
                    } catch (NumberFormatException e) {
                        ToastUtil.showToast("��ʷ�������ָ�ʽ����!", true);
                        return false;
                    } catch (java.text.ParseException e) {
                        ToastUtil.showToast("��ʷ�������ڸ�ʽ��������!", true);
                        return false;
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}