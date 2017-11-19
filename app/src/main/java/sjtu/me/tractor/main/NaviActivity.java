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

    private static final String TAG = "NaviActivity";        //调试日志标签
    private static final boolean D = true;

    private static final char END = '*'; // 串口通信字符串结束标志
    private static final char START = '#'; // 串口通信字符串开始标志
    private static final char SEPARATOR = ','; // 分隔符
    private static final int SEPARATOR_NUMBER = 12; // 分隔符个数

    private static final int REQUEST_SELECT_FIELD = 3;   // 跳转到开启蓝牙连接页面的标志

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

    ImageButton imgbtnBack;  //返回按钮
    ImageButton imgbtnConnectionStatus;  //连接状态显示按钮
    Button btnSetField;  //设置田地按钮
    Button btnSetTractor;  //设置田地按钮
    CheckBox chkboxABMode;  //AB线导航模式按钮
    Button btnPlanningMode; //规划导航模式按钮
    Button btnHistoryPath;  //历史轨迹按钮
    CheckBox chxboxStatistics; //统计数据按钮
    CheckBox chxboxRemoteMode; //遥控器模式按钮
    LinearLayout layoutABModePane;
    Button btnHistoryAB;  //历史AB线按钮
    Button btnSetA;  //设置A点按钮
    Button btnSetB;  //设置B点按钮
    CheckBox chkboxStartNavi; //启动导航
    LinearLayout layoutRemotePane;
    Button btnAccelerate; //加速按钮
    Button btnTurnLeft;  //左转按钮
    CheckBox chkboxStartSwitch;  //启动开关按钮
    Button btnTurnRight;  //右转按钮
    ImageButton imgbtnEmergencyStop;    //急停按钮

    TextView txtDeviance;  //横向偏差文本
    TextView txtSatellite;  //卫星数目文本
    TextView txtGpsState;  //GPS状态文本
    TextView txtDistance2Bound1;    //到边界距离文本
    TextView txtDistance2Bound2;    //到边界距离文本
    TextView txtFieldName;  //地块名称文本
    TextView txtTractorName;  //车辆名称文本
    TextView txtLineSpacing;    //行间距
    TextView txtLocationX;     //定位X坐标文本
    TextView txtLocationY;     //定位Y坐标文本
    TextView txtVelocity;  //速度文本
    TextView txtDirectionAngle;    //航向角文本
    TextView txtTurningAngle;  //转向角文本
    TextView txtPrecisionSeeding;  //精量播种数据文本
    TextView txtReceivedString;    //接收数据文本
    TextView txtReceivedStringNo;  //接收数据编号文本
    TextView txtSentString;    //发送数据文本


    Bundle bundle;
    private int dataNo = 0;
    private int currentState = 0; //当前指令状态
    private int preState = 0; //上一个指令状态

    private double locationX = 0;
    private double locationY = 0;
    private double lateralDeviation = 0;
    private double aX, aY, bX, bY; //AB点XY坐标
    private double aLat, aLng, bLat, bLng; //AB点经纬度
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
    private boolean isStartNavi = false; //启动导航标志
    private boolean isAutoNavi = false; //启动导航标志
    private boolean isToTurnRight = false;
    private boolean isToTurnLeft = false;

    private ArrayList<Double> pathListX, pathListY;
    // private ArrayList<Double> boundListX,boundListY;
    private HashMap<Integer, Double> boundTempMapX, boundTempMapY;
    private ArrayList<GeoPoint> fieldVertex;    //定义地块顶点数组

    private MyApplication myApp; // 程序全局变量
    private MySurfaceView myView; // 绘图显示控件
    private LineChart lineChart;  // 折线图控件
    private List<PointValue> mPointValues = new ArrayList<PointValue>(); // 折线图点数据集合

    private int myViewWidth;
    private int myViewHeight;
    protected int pathPointNo;
    protected Object mPath;

    /*创建消息处理器，处理通信线程发送过来的数据。*/
    MyNaviHandler mNaviHandler = new MyNaviHandler(this);

    /*handler的message字符串处理，使用StringBuilder类的性能远高于String类；
    另外，使用全局静态变量比使用局部变量速度快一倍以上。*/
    private static StringBuilder stringBuilder = new StringBuilder();

    /*使用静态变量保存解析的数据*/
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
     * 使用静态内部类避免Handler带来的内存泄漏问题;
     * 在handlerMessage()中写消息处理代码
     */
    private static class MyNaviHandler extends Handler {
        //持有弱引用MyFieldHandler，GC回收时会被回收掉
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
                        // 处理接收到的数据
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

        //实例化地块顶点列表对象
        fieldVertex = new ArrayList<GeoPoint>();

        //获取全局类实例
        myApp = (MyApplication) getApplication();
        //设置蓝牙连接的消息处理器为当前界面处理器
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

        // 重新进入界面时设置消息处理器为当前处理器
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
        // 退出Activity，清除MessageQueue还没处理的消息
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
                
                /*拷贝数据库文件到外部存储空间，开发测试时用*/
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
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_START_NAVI); //蓝牙发送开始导航指令
                } else {
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI); //蓝牙发送停止导航指令
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
     * 执行导航任务；
     * 处理发送过来的数据。
     *
     * @param msg 消息
     */
    private void doNavigationTask(Message msg) {
        String dataString;
        String msgString = (String) msg.obj;
        stringBuilder.append(msgString);

        if (stringBuilder.length() < 0) {
            return;
        }

        /*如果 ","的个数不为13，或者字符串开始和结束字符不是指定字符，则数据无效*/
        if ((SysUtil.countCharacter(stringBuilder, SEPARATOR) != SEPARATOR_NUMBER)
                || (stringBuilder.charAt(0) != START) || (stringBuilder.charAt(stringBuilder.length() - 1) != END)) {
            stringBuilder.delete(0, stringBuilder.length()); //清除无效内容
        } else {
            dataNo++;
            dataString = stringBuilder.substring(1, stringBuilder.length() - 1); // 提取字符串
            // 将收到的数据保存到外部存储器
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
                    /*根据GPS定位标识设置显示文本*/
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

                /*以时间横轴，横向偏差为纵轴，添加偏差数据到集合*/
                mPointValues.add(new PointValue((float) ((timeMillis - startTimeMillis) / 1000.0), (float) lateral));

                /*绘制当前点*/
                myView.setCurentPoint(dataNo, locationX, locationY);

                 /*计算到地头的距离*/
                double distance1 = GisAlgorithm.distanceFromPointToLine(622420.828635, 3423930.259849,
                        622422.2437, 3423929.17782, locationX, locationY);
                double distance2 = GisAlgorithm.distanceFromPointToLine(622423.89173, 3424003.981122,
                        622446.48542, 3424005.692487, locationX, locationY);
                txtDistance2Bound1.setText(getString(R.string.border_distance_1) + distance1);
                txtDistance2Bound2.setText(getString(R.string.border_distance_2) + distance2);

                /*设置A点*/
                if (isPointASet && currentState == CAR_SET_A_RESPONSE) {// 判断此时是否点击设置A点
                    if (preState != CAR_SET_A_RESPONSE) {
                        aX = locationX;
                        aY = locationY;
                        aLat = lat;
                        aLng = lng;
                        ToastUtil.showToast(getString(R.string.a_point_already_set), true);
                        isPointASet = false;
                    }
                    // 设置发送指令为确认收到A点坐标命令
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_A_AFFIRM);
                }

                /*设置B点*/
                if (isPointBSet && currentState == CAR_SET_B_RESPONSE) {// 判断此时是否点击设置B点
                    if (preState != CAR_SET_B_RESPONSE) {
                        bX = locationX;
                        bY = locationY;
                        bLat = lat;
                        bLat = lng;

                        if (aY == bY && aX == bX) {
                            //弹出AB点重合警告
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
                            // 保存AB线到外部文件
                            FileUtil.writeDataToExternalStorage(AB_LINE_DIRECTORY, fileABPoints, abLine, true, false);
                        }

                        isPointBSet = false;
                    }
                    // 设置发送指令为确认收到B点坐标命令
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_B_AFFIRM);
                }

                /*启动导航*/
                if (currentState == CAR_LINE_NAVI) {
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                /*右转*/
                if (isToTurnRight && currentState == CAR_TURNING_RIGHT) {// 判断此时是否右转
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                /*左转*/
                if (isToTurnLeft && currentState == CAR_TURNING_LEFT) {// 判断此时是否左转
                    isToTurnLeft = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                if (currentState == CAR_LINE_NAVI || currentState == HEADLAND_P1_RESPONSE
						        || currentState == HEADLAND_P2_RESPONSE) {
						    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
						}

                preState = currentState; // 保存当前状态
            }

            stringBuilder.delete(0, stringBuilder.length()); //清除效内容
        }
    }

    /**
     * 解析接收数据
     *
     * @param dataString 接收到的数据
     * @return 解析成功标志
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
     * 开始导航操作方法
     */
    private void startNavi() {
        isStartNavi = true; //设置导航状态为真
        startTimeMillis = System.currentTimeMillis(); //记录开始导航系统时间（毫秒）
        mPointValues.clear(); //导航横向偏差数据集合清零
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_START_NAVI); //蓝牙发送开始导航指令
    }

    /**
     * 停止导航操作方法
     */
    private void stopNavi() {
        isStartNavi = false; //
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI); //蓝牙发送停止导航指令
        lineChart.setPointValues(mPointValues); //将导航偏差数据集传到折线图
    }

    /**
     * 开始绘制轨迹操作方法
     */
    private void startPlotAndSavePath() {
        isPlotting = true; //设置开始绘制轨迹状态为真
        isDataToSave = true; //设置开始保存数据状态为真
        dataNo = 0; //将数据点编号重置为零
        myView.drawPath(1, true); //设置绘制轨迹状态为真
        currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()); //获取当前日期时间字符串
        fileNameToSave = currentTime + "_" + DATA_FILE_NAME_SUFFIX; //设置数据保存文件名
    }

    /**
     * 停止绘制轨迹操作方法
     */
    private void stopPlotAndSavePath() {
        isPlotting = false;
        myView.drawPath(1, false);
        isDataToSave = false;
    }

    /**
     * 判断是否需要拐弯
     * （这算法暂时是错误的，因为车辆来回两次穿越距离边界一定距离的直线，但是只有一次转弯）
     *
     * @param distance 距离边界实时距离
     * @param limit    拐弯距离限制
     * @return
     */
    public boolean isToTurn(double distance, double limit) {
        return distance > limit && (distance - limit) < 0.1;
    }

}
