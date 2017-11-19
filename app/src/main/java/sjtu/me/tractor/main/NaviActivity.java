package sjtu.me.tractor.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import sjtu.me.tractor.surfaceview.MySurfaceView;
import sjtu.me.tractor.util.AlertDialogUtil;
import sjtu.me.tractor.util.FileUtil;
import sjtu.me.tractor.util.SysUtil;
import sjtu.me.tractor.util.ToastUtil;

/**
 * @author billhu
 */
@SuppressLint("UseSparseArrays")
public class NaviActivity extends Activity implements OnClickListener {

    private static final String TAG = "NaviActivity";        //������־��ǩ
    private static final boolean D = true;

    private static final char END = '*'; // ����ͨ���ַ���������־
    private static final char START = '#'; // ����ͨ���ַ�����ʼ��־
    private static final char SEPARATOR = ','; // �ָ���
    private static final int SEPARATOR_NUMBER = 12; // �ָ�������

    private static final int REQUEST_SELECT_FIELD = 3;   // ��ת��������������ҳ��ı�־

    public static final int CAR_STATE_IDLE = 99999;
    public static final int CAR_SET_A_RESPONSE = 40103;
    public static final int CAR_SET_B_RESPONSE = 40104;
    public static final int CAR_LINE_NAVI = 10010;
    public static final int CAR_TURNING_RIGHT = 10020;
    public static final int CAR_TURNING_LEFT = 10030;

    public static final int HEADLAND_P1_RESPONSE = 50101;
    public static final int HEADLAND_P2_RESPONSE = 50102;

    private static final String DATA_FILE_NAME_SUFFIX = "data.txt";
    private static final String DATA_DIRECTORY = "data";
    private static final String AB_LINE_DIRECTORY = "ab_lines";

    private static final String QUERY_ALL = "%%";

    ImageButton imgbtnBack;  //���ذ�ť
    ImageButton imgbtnConnectionStatus;  //����״̬��ʾ��ť
    Button btnSetField;  //������ذ�ť
    Button btnSetTractor;  //������ذ�ť
    CheckBox chkboxABMode;  //AB�ߵ���ģʽ��ť
    Button btnPlanningMode; //�滮����ģʽ��ť
    Button btnHistoryPath;  //��ʷ�켣��ť
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
    TextView txtDistance2Bound1;    //���߽�����ı�
    TextView txtDistance2Bound2;    //���߽�����ı�
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
    private double lateralDeviation = 0;
    private double aX, aY, bX, bY; //AB��XY����
    private double aLat, aLng, bLat, bLng; //AB�㾭γ��
    private float lineSpace = 0;
    private long startTimeMillis;
    private String fileNameToSave;
    private String currentTime;

    private boolean isDataReceiving = false;
    private boolean isDataToSave = false;
    private boolean isPlotting = false;
    private boolean isOriginSet = false;
    private boolean isPointASet = false;
    private boolean isPointBSet = false;
    private boolean isBoundP1Set = false;
    private boolean isBoundP2Set = false;
    private boolean isBoundP3Set = false;
    private boolean isBoundP4Set = false;
    private boolean isStartNavi = false; //����������־
    private boolean isAutoNavi = false; //����������־
    private boolean isToTurnRight = false;
    private boolean isToTurnLeft = false;

    private ArrayList<Double> pathListX, pathListY;
    // private ArrayList<Double> boundListX,boundListY;
    private HashMap<Integer, Double> boundTempMapX, boundTempMapY;
    private ArrayList<GeoPoint> fieldVertex;    //����ؿ鶥������

    private MyApplication myApp; // ����ȫ�ֱ���
    private MySurfaceView myView; // ��ͼ��ʾ�ؼ�
    private LineChart lineChart;  // ����ͼ�ؼ�
    private List<PointValue> mPointValues = new ArrayList<PointValue>(); // ����ͼ�����ݼ���

    private int myViewWidth;
    private int myViewHeight;
    protected int pathPointNo;
    protected Object mPath;

    /*������Ϣ������������ͨ���̷߳��͹��������ݡ�*/
    MyNaviHandler mNaviHandler = new MyNaviHandler(this);

    /*handler��message�ַ�������ʹ��StringBuilder�������Զ����String�ࣻ
    ���⣬ʹ��ȫ�־�̬������ʹ�þֲ������ٶȿ�һ�����ϡ�*/
    private static StringBuilder stringBuilder = new StringBuilder();

    /*ʹ�þ�̬�����������������*/
    private static double lat;
    private static double lng;
    private static double xx;
    private static double yy;
    private static int satellite;
    private static int gps;
    private static double north;
    private static double velocity;
    private static int command;
    private static double direction;
    private static double lateral;
    private static double turnning;
    private static double seeding;

    /**
     * ʹ�þ�̬�ڲ������Handler�������ڴ�й©����;
     * ��handlerMessage()��д��Ϣ�������
     */
    private static class MyNaviHandler extends Handler {
        //����������MyFieldHandler��GC����ʱ�ᱻ���յ�
        private final WeakReference<NaviActivity> mReferenceActivity;

        MyNaviHandler(NaviActivity activity) {
            mReferenceActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            final NaviActivity activity = mReferenceActivity.get();
            super.handleMessage(msg);
            if (activity != null) {
                switch (msg.what) {
                    case BluetoothService.MESSAGE_RECEIVED:
                        // ������յ�������
                        activity.doNavigationTask(msg);
                        break;

                    case BluetoothService.MESSAGE_SENT:
                        String writeMessage = msg.obj.toString();
                        activity.txtSentString.setText(writeMessage);
                        break;

                    case BluetoothService.MESSAGE_CONNECT_RESULT:
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

        //ʵ�����ؿ鶥���б����
        fieldVertex = new ArrayList<GeoPoint>();

        //��ȡȫ����ʵ��
        myApp = (MyApplication) getApplication();
        //�����������ӵ���Ϣ������Ϊ��ǰ���洦����
        myApp.getBluetoothService().setHandler(mNaviHandler);
        if (D) {
            Log.e(TAG, "+++ setHandler: mNaviHandler +++");
        }

        initViews();

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
            imgbtnConnectionStatus.setBackgroundResource(R.drawable.connection_broken);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (D) {
            Log.e(TAG, "+++ ON RESUME +++");
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
            imgbtnConnectionStatus.setBackgroundResource(R.drawable.connection_broken);
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

        super.onDestroy();
        if (D) {
            Log.e(TAG, "+++ ON DESTROY +++");
        }
        // �˳�Activity�����MessageQueue��û�������Ϣ
        mNaviHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_FIELD:
                if (resultCode == RESULT_OK) {
                    bundle = data.getExtras();
                    String name = (bundle == null ? null : bundle.getString(FieldInfo.FIELD_NAME));
                    Log.e("selected item", name);
                    Cursor resultCursor = myApp.getDatabaseManager().queryFieldWithPointsByName(name);
                    List<Map<String, String>> resultList = DatabaseManager.cursorToList(resultCursor);

                    for (int i = 0; i < resultList.size(); i++) {
                        GeoPoint vertex = new GeoPoint(Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_X_COORDINATE)),
                                Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_Y_COORDINATE)));
                        fieldVertex.add(vertex);
                    }

                    myView.setViewFieldBoundary(fieldVertex, true);
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
                Intent intent = new Intent("sjtu.me.tractor.fieldsetting.FieldResultActivity");
                intent.putExtras(data);
                startActivityForResult(intent, REQUEST_SELECT_FIELD);
                break;

            case R.id.btnSetTractor:
                Cursor cursor2 = myApp.getDatabaseManager().queryFieldByName(QUERY_ALL);
                Bundle data2 = new Bundle();
                data2.putSerializable("data", DatabaseManager.cursorToList(cursor2));
                Intent intent2 = new Intent("sjtu.me.tractor.fieldsetting.FieldResultActivity");
                intent2.putExtras(data2);
                startActivityForResult(intent2, REQUEST_SELECT_FIELD);
                break;

            case R.id.chkboxABMode:
                if (((CheckBox) v).isChecked()) {
                    layoutABModePane.setVisibility(View.VISIBLE);
                } else {
                    layoutABModePane.setVisibility(View.INVISIBLE);
                }
                break;

            case R.id.btnHistoryAB:
                Cursor cursor3 = myApp.getDatabaseManager().queryFieldByName(QUERY_ALL);
                Bundle data3 = new Bundle();
                data3.putSerializable("data", DatabaseManager.cursorToList(cursor3));
                Intent intent3 = new Intent("sjtu.me.tractor.fieldsetting.FieldResultActivity");
                intent3.putExtras(data3);
                startActivityForResult(intent3, REQUEST_SELECT_FIELD);
                
                /*�������ݿ��ļ����ⲿ�洢�ռ䣬��������ʱ��*/
                /*
                Log.e(TAG, "HISTORY_AB IS PRESSED ");
                String dbPath = getApplication().getDatabasePath(MyDatabaseHelper.DB_NAME).toString();
                Log.e(TAG, "DB PATH IS " + dbPath);
                boolean flag = FileUtil.copyDbFilesToExternalStorage(dbPath);
                Log.e(TAG, "COPY DB FILES SUCCESSFULLY? " + flag);
                */
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
                if (!isStartNavi) {
                    startNavi();
                    startPlotAndSavePath();
                } else {
                    stopNavi();
                    stopPlotAndSavePath();
                }
                break;

            case R.id.btnPlanningMode:
                AlertDialog planningDialog = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.alert_title_field_tip))
                        .setMessage(getString(R.string.alert_message_field_import))
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

            case R.id.btnHistoryPath:
                Cursor cursor4 = myApp.getDatabaseManager().queryFieldByName(QUERY_ALL);
                Bundle data4 = new Bundle();
                data4.putSerializable("data", DatabaseManager.cursorToList(cursor4));
                Intent intent4 = new Intent("sjtu.me.tractor.fieldsetting.FieldResultActivity");
                intent4.putExtras(data4);
                startActivityForResult(intent4, REQUEST_SELECT_FIELD);
                break;

            case R.id.chkboxStatistics:
                if (((CheckBox) v).isChecked()) {
                    lineChart.showChartView(true);
                } else {
                    lineChart.showChartView(false);
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
                myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI);
                break;

            default:
                break;
        }
    }

    private void initViews() {
        myView = (MySurfaceView) findViewById(R.id.mySurfaceView);
        myView.setCanvasSize(1400, 1330);

        lineChart = new LineChart((LineChartView) findViewById(R.id.lineChartView));

        txtDeviance = ((TextView) findViewById(R.id.txtDeviance));
        txtSatellite = (TextView) findViewById(R.id.txtSatellite);
        txtGpsState = (TextView) findViewById(R.id.txtGpsState);
        txtDistance2Bound1 = (TextView) findViewById(R.id.txtDistance2Bound1);
        txtDistance2Bound2 = (TextView) findViewById(R.id.txtDistance2Bound2);
        txtFieldName = (TextView) findViewById(R.id.txtFieldName);
        txtTractorName = (TextView) findViewById(R.id.txtTractorName);
        txtLineSpacing = (TextView) findViewById(R.id.txtLineSpacing);
        txtLocationX = (TextView) findViewById(R.id.txtLocationX);
        txtLocationY = (TextView) findViewById(R.id.txtLocationY);
        txtVelocity = (TextView) findViewById(R.id.txtVelocity);
        txtDirectionAngle = (TextView) findViewById(R.id.txtDirectionAngle);
        txtDeviance = (TextView) findViewById(R.id.txtDeviance);
        txtTurningAngle = (TextView) findViewById(R.id.txtTurningAngle);
        txtPrecisionSeeding = (TextView) findViewById(R.id.txtLineSpacing);
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

        btnHistoryPath = (Button) findViewById(R.id.btnHistoryPath);
        btnHistoryPath.setOnClickListener(this);

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
     * �����͹��������ݡ�
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
            txtReceivedString.setText(dataString);
            txtReceivedStringNo.setText(getString(R.string.number) + dataNo);

            if (parseReceivedMessage(dataString)) {
                txtDeviance.setText(String.valueOf(lateral));
                txtSatellite.setText(String.valueOf(satellite));
                switch (satellite) {
                    /*����GPS��λ��ʶ������ʾ�ı�*/
                    case 0:
                        txtSatellite.setText(R.string.satellite_gps_no_location);
                        break;

                    case 1:
                        txtSatellite.setText(R.string.satellite_gps_single_point);
                        break;

                    case 2:
                        txtSatellite.setText(R.string.satellite_gps_rtk);
                        break;

                    case 4:
                        txtSatellite.setText(R.string.satellite_gps_rtk_fixed);
                        break;

                    case 5:
                        txtSatellite.setText(R.string.satellite_gps_float);
                        break;

                    default:
                        txtSatellite.setText(R.string.satellite_gps_no_location);
                        break;
                }
                txtLocationX.setText(String.valueOf(xx));
                txtLocationY.setText(String.valueOf(yy));
                txtVelocity.setText(String.valueOf(velocity));
                txtDirectionAngle.setText(String.valueOf(direction));
                txtTurningAngle.setText(String.valueOf(turnning));
                txtPrecisionSeeding.setText(String.valueOf(seeding));
                currentState = command;
                locationX = xx;
                locationY = yy;
                long timeMillis = System.currentTimeMillis();

                /*��ʱ����ᣬ����ƫ��Ϊ���ᣬ���ƫ�����ݵ�����*/
                mPointValues.add(new PointValue((float) ((timeMillis - startTimeMillis) / 1000.0), (float) lateral));

                /*���Ƶ�ǰ��*/
                myView.setCurentPoint(dataNo, locationX, locationY);

                 /*���㵽��ͷ�ľ���*/
                double distance1 = GisAlgorithm.distanceFromPointToLine(622420.828635, 3423930.259849,
                        622422.2437, 3423929.17782, locationX, locationY);
                double distance2 = GisAlgorithm.distanceFromPointToLine(622423.89173, 3424003.981122,
                        622446.48542, 3424005.692487, locationX, locationY);
                txtDistance2Bound1.setText(getString(R.string.border_distance_1) + distance1);
                txtDistance2Bound2.setText(getString(R.string.border_distance_2) + distance2);

                /*����A��*/
                if (isPointASet && currentState == CAR_SET_A_RESPONSE) {// �жϴ�ʱ�Ƿ�������A��
                    if (preState != CAR_SET_A_RESPONSE) {
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
                if (isPointBSet && currentState == CAR_SET_B_RESPONSE) {// �жϴ�ʱ�Ƿ�������B��
                    if (preState != CAR_SET_B_RESPONSE) {
                        bX = locationX;
                        bY = locationY;
                        bLat = lat;
                        bLat = lng;

                        if (aY == bY && aX == bX) {
                            //����AB���غϾ���
                            ToastUtil.showToast(getString(R.string.ab_overlay_error_warning), true);
                            return;
                        } else {
                            ToastUtil.showToast(getString(R.string.b_point_already_set), true);
                            myView.drawABline(aX, aY, bX, bY, true);
                            currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                            String fileABPoints = currentTime + "_AB_Points.txt";
                            String abLine = new StringBuilder()
                                    .append(aX).append(",").append(aY).append(",").append(aLat).append(",").append(aLng)
                                    .append("\r\n")
                                    .append(bX).append(",").append(bY).append(",").append(bLat).append(",").append(bLng)
                                    .toString();
                            // ����AB�ߵ��ⲿ�ļ�
                            FileUtil.writeDataToExternalStorage(AB_LINE_DIRECTORY, fileABPoints, abLine, true, false);
                        }

                        isPointBSet = false;
                    }
                    // ���÷���ָ��Ϊȷ���յ�B����������
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_B_AFFIRM);
                }

                /*��������*/
                if (currentState == CAR_LINE_NAVI) {
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                /*��ת*/
                if (isToTurnRight && currentState == CAR_TURNING_RIGHT) {// �жϴ�ʱ�Ƿ���ת
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                /*��ת*/
                if (isToTurnLeft && currentState == CAR_TURNING_LEFT) {// �жϴ�ʱ�Ƿ���ת
                    isToTurnLeft = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                if (currentState == CAR_LINE_NAVI || currentState == HEADLAND_P1_RESPONSE
						        || currentState == HEADLAND_P2_RESPONSE) {
						    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
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
            north = Double.parseDouble(dataArray[6]);
            velocity = Double.parseDouble(dataArray[7]);
            command = Integer.parseInt(dataArray[8]);
            direction = Double.parseDouble(dataArray[9]);
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
    private void startNavi() {
        isStartNavi = true; //���õ���״̬Ϊ��
        startTimeMillis = System.currentTimeMillis(); //��¼��ʼ����ϵͳʱ�䣨���룩
        mPointValues.clear(); //��������ƫ�����ݼ�������
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_START_NAVI); //�������Ϳ�ʼ����ָ��
    }

    /**
     * ֹͣ������������
     */
    private void stopNavi() {
        isStartNavi = false; //
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI); //��������ֹͣ����ָ��
        lineChart.setPointValues(mPointValues); //������ƫ�����ݼ���������ͼ
    }

    /**
     * ��ʼ���ƹ켣��������
     */
    private void startPlotAndSavePath() {
        isPlotting = true; //���ÿ�ʼ���ƹ켣״̬Ϊ��
        isDataToSave = true; //���ÿ�ʼ��������״̬Ϊ��
        dataNo = 0; //�����ݵ�������Ϊ��
        myView.drawPath(1, true); //���û��ƹ켣״̬Ϊ��
        currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()); //��ȡ��ǰ����ʱ���ַ���
        fileNameToSave = currentTime + "_" + DATA_FILE_NAME_SUFFIX; //�������ݱ����ļ���
    }

    /**
     * ֹͣ���ƹ켣��������
     */
    private void stopPlotAndSavePath() {
        isPlotting = false;
        myView.drawPath(1, false);
        isDataToSave = false;
    }

    /**
     * �ж��Ƿ���Ҫ����
     * �����㷨��ʱ�Ǵ���ģ���Ϊ�����������δ�Խ����߽�һ�������ֱ�ߣ�����ֻ��һ��ת�䣩
     *
     * @param distance ����߽�ʵʱ����
     * @param limit    �����������
     * @return
     */
    public boolean isToTurn(double distance, double limit) {
        return distance > limit && (distance - limit) < 0.1;
    }

}
