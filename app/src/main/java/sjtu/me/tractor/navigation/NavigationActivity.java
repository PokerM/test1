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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import sjtu.me.tractor.R;
import sjtu.me.tractor.connection.BluetoothService;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.planning.GeoLine;
import sjtu.me.tractor.planning.GeoPoint;
import sjtu.me.tractor.planning.GisAlgorithm;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.planning.PlanningPathGenerator;
import sjtu.me.tractor.surfaceview.MySurfaceView;
import sjtu.me.tractor.tractorinfo.TractorInfo;
import sjtu.me.tractor.utils.AlertDialogUtil;
import sjtu.me.tractor.utils.FileUtil;
import sjtu.me.tractor.utils.SysUtil;
import sjtu.me.tractor.utils.ToastUtil;

/**
 * @author billhu
 */
@SuppressLint("UseSparseArrays")
public class NavigationActivity extends Activity implements OnClickListener {

    private static final String TAG = "NavigationActivity";        //调试日志标签
    private static final boolean D = true;

    private static final int SURFACE_VIEW_WIDTH = 705;
    private static final int SURFACE_VIEW_HEIGHT = 660;

    private double LATERAL_THRESHOLD = 10; //设置横向偏差的阈值
    private double BOUNDS_THRESHOLD = 10; //设置一个阈值


    private static final char END = '*'; // 串口通信字符串结束标志
    private static final char START = '#'; // 串口通信字符串开始标志
    private static final char SEPARATOR = ','; // 分隔符
    private static final int SEPARATOR_NUMBER = 12; // 分隔符个数

    private static final int REQUEST_SELECT_FIELD = 3;   // 跳转到选择地块列表的请求序号
    private static final int REQUEST_SELECT_TRACTOR = 4;   // 跳转到选择车辆列表的请求序号
    private static final int REQUEST_SELECT_AB_LINE = 5;   // 跳转到选择AB线列表的请求序号
    private static final int REQUEST_SELECT_HISTORY_PATH = 6;   // 跳转到选择历史轨迹列表的请求序号

    public static final int DEFAULT_STATE_RESPONSE = 99999;
    public static final int SET_A_RESPONSE = 40103;
    public static final int SET_B_RESPONSE = 40104;
    public static final int LINE_NAVIGATION_RESPONSE = 10010;
    public static final int TURNING_RIGHT_RESPONSE = 10020;
    public static final int TURNING_LEFT_RESPONSE = 10030;

    /*调试时用到的一些指令状态*/
    public static final int HEADLAND_P1_RESPONSE = 50101;
    public static final int HEADLAND_P2_RESPONSE = 50102;

    private static final String DATA_DIRECTORY = "data";
    private static final String AB_LINE_DIRECTORY = "ab_lines";

    private static final String QUERY_ALL = "%%";

    private static final String DEFAULT_FIELD = "default_field";
    private static final String DEFAULT_TRACTOR = "default_tractor";

    ImageButton imgbtnBack;  //返回按钮
    ImageButton imgbtnConnectionStatus;  //连接状态显示按钮
    Button btnSetField;  //设置田地按钮
    Button btnSetTractor;  //设置田地按钮
    CheckBox checkboxABMode;  //AB线导航模式按钮
    Button btnPlanningMode; //规划导航模式按钮
    CheckBox checkboxHistory;  //历史记录按钮
    CheckBox checkboxStatistics; //统计数据按钮
    CheckBox checkboxRemoteMode; //遥控器模式按钮
    LinearLayout layoutABModePane;
    Button btnHistoryAB;  //历史AB线按钮
    Button btnSetA;  //设置A点按钮
    Button btnSetB;  //设置B点按钮
    CheckBox checkboxStartNavigation; //启动导航
    LinearLayout layoutRemotePane;
    Button btnAccelerate; //加速按钮
    Button btnTurnLeft;  //左转按钮
    CheckBox checkboxStartSwitch;  //启动开关按钮
    Button btnTurnRight;  //右转按钮
    ImageButton imgbtnEmergencyStop;    //急停按钮

    TextView txtDeviance;  //横向偏差文本
    TextView txtSatellite;  //卫星数目文本
    TextView txtGpsState;  //GPS状态文本
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
    private double aX, aY, bX, bY; //AB点XY坐标
    private double aLat, aLng, bLat, bLng; //AB点经纬度
    private long startNavigationTime; //每次导航启动时间
    private String fileNameToSave;
    private String currentTime;

    private boolean isDataToSave = false;
    private boolean isPointASet = false;
    private boolean isPointBSet = false;
    private boolean isBoundP1Set = false;
    private boolean isBoundP2Set = false;
    private boolean isBoundP3Set = false;
    private boolean isBoundP4Set = false;
    private boolean isStartNavigation = false;
    private boolean isStopNavigation = false;
    private boolean isToTurnRight = false;
    private boolean isToTurnLeft = false;
    private boolean isOnQueryingHistory = false; //是否历史记录


    private MyApplication myApp; // 程序全局变量

    private SharedPreferences myPreferences; //默认偏好参数存储实例
    private MySurfaceView myView; // 绘图显示控件
    private LinearLayout statisticsView;
    private LineChart lineChart;  // 折线图控件
    private TextView txtAverageLateral;
    private ScaleAnimation showAnimation;
    private ScaleAnimation hideAnimation;
    private List<PointValue> mPointValues = new ArrayList<PointValue>(); // 折线图点数据集合
    List<GeoPoint> historyPathPoints = new ArrayList<>(); //历史记录轨迹点集合
    List<PointValue> historyPointValues = new ArrayList<>(); //历史记录折线图点数据集合
    private String defaultFieldName;
    private String defaultTractorName;
    private ArrayList<GeoPoint> defaultFieldVertexList;    //定义地块顶点数组
    private double[] fieldBoundsLimits = new double[]{0, 1000000, 0, 10000000};
    private double linespacing = 2.5; //作业行间距
    private GeoLine lineAB;
    private double minTurning;

    /*创建消息处理器，处理通信线程发送过来的数据。*/
    MyNavigationHandler mNavigationHandler = new MyNavigationHandler(this);

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
    private static double velocity;
    private static int command;
    private static double sensor;
    private static double direction;
    private static double lateral;
    private static double turnning;
    private static double seeding;

    public NavigationActivity() {
        super();
    }

    /**
     * 使用静态内部类避免Handler带来的内存泄漏问题;
     * 在handlerMessage()中写消息处理代码
     */
    private static class MyNavigationHandler extends Handler {
        //持有弱引用MyFieldHandler，GC回收时会被回收掉
        private final WeakReference<NavigationActivity> mReferenceActivity;

        MyNavigationHandler(NavigationActivity activity) {
            mReferenceActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            final NavigationActivity activity = mReferenceActivity.get();
            super.handleMessage(msg);
            if (activity != null) {
                switch (msg.what) {
                    case BluetoothService.MESSAGE_RECEIVED:
                        // 处理接收到的数据
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
        setContentView(R.layout.navigation_activity);
        if (D) {
            Log.e(TAG, "+++ ON CREATE +++");
        }

        //实例化地块顶点列表对象
        defaultFieldVertexList = new ArrayList<GeoPoint>();

        //获取全局类实例
        myApp = (MyApplication) getApplication();
        //设置蓝牙连接的消息处理器为当前界面处理器
        myApp.getBluetoothService().setHandler(mNavigationHandler);
        if (D) {
            Log.e(TAG, "+++ setHandler: mNavigationHandler +++");
        }

        //实例化默认偏好设置
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        initViews(); //初始化各个控件

         /* 读取默认偏车辆设置 */
        defaultTractorName = myPreferences.getString(DEFAULT_TRACTOR, null);
        if (!TextUtils.isEmpty(defaultTractorName)) {
            Cursor resultCursor = myApp.getDatabaseManager().queryTractorByName(defaultTractorName);
            Map<String, String> map = DatabaseManager.cursorToMap(resultCursor);
            txtTractorName.setText(defaultTractorName);
            txtLineSpacing.setText(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING) + "m");
            try {
                linespacing = Double.parseDouble(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING));
                minTurning = Double.parseDouble(map.get(TractorInfo.TRACTOR_MIN_TURNING_RADIUS));
            } catch (NumberFormatException e) {
                ToastUtil.showToast("读取作业行间距数字格式错误!", true);
            }
        } else {
            ToastUtil.showToast(getString(R.string.toast_set_default_tractor_tip), true);
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


        // boundListX = new ArrayList<Double>();
        // boundListY = new ArrayList<Double>();


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

        //设置默认地块
        defaultFieldName = myPreferences.getString(DEFAULT_FIELD, null);
        if (!TextUtils.isEmpty(defaultFieldName)) {
            Cursor resultCursor = myApp.getDatabaseManager().queryFieldWithPointsByName(defaultFieldName);
            List<Map<String, String>> resultList = DatabaseManager.cursorToList(resultCursor);

            defaultFieldVertexList.clear();
            for (int i = 0; i < resultList.size(); i++) {
                GeoPoint vertex = new GeoPoint(Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_X_COORDINATE)),
                        Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_Y_COORDINATE)));
                defaultFieldVertexList.add(vertex);
            }
            boolean b = myView.setFieldBoundary(defaultFieldVertexList, true);
            if (b) {
                txtFieldName.setText(defaultFieldName);
            } else {
                ToastUtil.showToast(getString(R.string.toast_set_default_field_tip), true);
            }
            if (D) {
                Log.e(TAG, "set default field: " + defaultFieldName);
            }
        } else {
            ToastUtil.showToast(getString(R.string.toast_set_default_field_tip), true);
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
        myApp.getBluetoothService().setHandler(mNavigationHandler);

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
        // 退出Activity，清除MessageQueue还没处理的消息
        mNavigationHandler.removeCallbacksAndMessages(null);
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
                    /* 将当前选择的地块名称存到默认偏好设置，下次程序自动选择此次选择的地块 */

                    Cursor resultCursor = myApp.getDatabaseManager().queryFieldWithPointsByName(name);
                    List<Map<String, String>> resultList = DatabaseManager.cursorToList(resultCursor);

                    defaultFieldVertexList.clear();
                    for (int i = 0; i < resultList.size(); i++) {
                        GeoPoint vertex = new GeoPoint(Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_X_COORDINATE)),
                                Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_Y_COORDINATE)));
                        defaultFieldVertexList.add(vertex);
                    }

                    boolean b = myView.setFieldBoundary(defaultFieldVertexList, true);
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
                    defaultTractorName = name;
                    txtTractorName.setText(name);

                    /* 将当前选择的地块名称存到默认偏好设置，下次程序自动选择此次选择的地块 */
                    myPreferences.edit().putString(DEFAULT_TRACTOR, name).commit();

                    Cursor resultCursor = myApp.getDatabaseManager().queryTractorByName(name);
                    Map<String, String> map = DatabaseManager.cursorToMap(resultCursor);
                    txtLineSpacing.setText(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING) + " m");
                    try {
                        linespacing = Double.parseDouble(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING));
                        minTurning = Double.parseDouble(map.get(TractorInfo.TRACTOR_MIN_TURNING_RADIUS));
                    } catch (NumberFormatException e) {
                        ToastUtil.showToast("读取作业行间距数字格式错误!", true);
                    }
                }
                break;

            case REQUEST_SELECT_AB_LINE:
                if (resultCode == RESULT_OK) {
                    //将默认AB显示设置为用户选择的历史AB线
                    bundle = data.getExtras();
                    String name = (bundle == null ? null : bundle.getString(ABLine.AB_LINE_NAME_BY_DATE));
                    Cursor cursor = myApp.getDatabaseManager().queryABlineByDate(name);
                    Map<String, String> map = DatabaseManager.cursorToMap(cursor);
                    try {
                        double ax = Double.parseDouble(map.get(ABLine.A_POINT_X_COORDINATE));
                        double ay = Double.parseDouble(map.get(ABLine.A_POINT_Y_COORDINATE));
                        double bx = Double.parseDouble(map.get(ABLine.B_POINT_X_COORDINATE));
                        double by = Double.parseDouble(map.get(ABLine.B_POINT_Y_COORDINATE));
                        Log.e(TAG, "ax: " + ax);
                        Log.e(TAG, "ay: " + ay);
                        Log.e(TAG, "bx: " + bx);
                        Log.e(TAG, "by: " + by);
                        lineAB = new GeoLine(ax, ay, bx, by);
                        myView.drawABline(ax, ay, bx, by, true);
                    } catch (NumberFormatException e) {
                        ToastUtil.showToast("读取AB线历史数据数字格式错误!", true);
                    }
                }
                break;

            case REQUEST_SELECT_HISTORY_PATH:
                if (resultCode == RESULT_OK) {
                    bundle = data.getExtras();
                    Map<String, String> map = (Map<String, String>) bundle.getSerializable("history_entry");
                    String name = (map == null ? null : map.get(HistoryPath.HISTORY_RECORD_FILE_NAME));
                    String field = (map == null ? null : map.get(HistoryPath.FIELD_NAME));
                    Log.e(TAG, "HISTORY FIELD IS " + field);

                    boolean b = getHistoryDataFromFiles(name, historyPathPoints, historyPointValues); //读取历史记录；
                    Log.e(TAG, "Read history successfully? " + b);

                    if (b) {
                        if (!TextUtils.isEmpty(field)) {
                            Cursor resultCursor = myApp.getDatabaseManager().queryFieldWithPointsByName(field);
                            List<Map<String, String>> resultList = DatabaseManager.cursorToList(resultCursor);

                            //获取历史轨迹所在地块；
                            ArrayList<GeoPoint> hField = new ArrayList<>();
                            for (int i = 0; i < resultList.size(); i++) {
                                GeoPoint vertex = new GeoPoint(Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_X_COORDINATE)),
                                        Double.valueOf(resultList.get(i).get(FieldInfo.FIELD_POINT_Y_COORDINATE)));
                                hField.add(vertex);
                            }

                            //设置历史轨迹地块;
                            boolean b2 = myView.setFieldBoundary(hField, true);
                            if (b2) {
                                txtFieldName.setText(field);
                            } else {
                                ToastUtil.showToast("找不到历史数据记录对应的地块信息!", true);
                            }
                        }
                        myView.drawHistoryPath(historyPathPoints);
                        Log.e(TAG, "points number: " + historyPathPoints.size());
                    } else {
                        ToastUtil.showToast("历史记录数据读取错误!", true);
                    }

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
                    isStartNavigation = true;
                    isStopNavigation = false;
                    /*导航过程中设置历史查询按钮为不可点击状态*/
                    checkboxHistory.setClickable(false);
                    checkboxHistory.setChecked(false);
                } else {
                    stopNavigation();
                    stopPlotAndSavePath();
                    isStartNavigation = false;
                    isStopNavigation = true;
                    /*导航结束后设置历史查询按钮为可点击状态*/
                    checkboxHistory.setClickable(true);
                }
                break;

            case R.id.btnPlanningMode:
                AlertDialog planningDialog = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.alert_title_field_tip))
                        .setMessage(getString(R.string.alert_message_navigation_download))
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(R.string.loading_planned_path, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (defaultFieldVertexList != null) {
                                    PlanningPathGenerator planningPathGenerator =
                                            new PlanningPathGenerator(defaultFieldVertexList, lineAB, linespacing, minTurning);
                                    planningPathGenerator.planningField();
                                    List<GeoPoint> headland1 = planningPathGenerator.getHeadLand1();
                                    List<GeoPoint> headland2 = planningPathGenerator.getHeadLand2();
                                    List<GeoLine> plannedPaths = planningPathGenerator.getGeneratedPathList();
                                    myView.drawHeadland1(headland1);
                                    myView.drawHeadland2(headland2);
                                    myView.drawPlannedPath(plannedPaths);
                                }
                            }
                        })
                        .setNeutralButton(R.string.unloading_planned_path, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myView.hidePlannedPath();
                            }
                        })
                        .setNegativeButton(R.string.select_no_operation, null)
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

                    isOnQueryingHistory = true; //切换是否历史记录标志
                    /*田地设置按钮设为不可点击状态*/
                    btnSetField.setClickable(false);
                } else {
                    isOnQueryingHistory = false;
                    myView.hideHistoryPath();
                    myView.setFieldBoundary(defaultFieldVertexList, true);
                     /*田地设置按钮设为可点击状态*/
                    btnSetField.setClickable(true);
                }

                break;

            case R.id.chkboxStatistics:
                if (((CheckBox) v).isChecked()) {
                    if (!isOnQueryingHistory) {
                        lineChart.setPointValues(mPointValues);
                        if (mPointValues != null && mPointValues.size() > 0) {
                            float totalLateral = 0.0f;
                            float maxLateral = Float.MIN_VALUE;
                            for (PointValue mPointValue : mPointValues) {
                                float absLateral = Math.abs(mPointValue.getY());
                                if (absLateral > maxLateral) {
                                    maxLateral = absLateral;
                                }
                                totalLateral += absLateral;

                            }
                            float average = totalLateral / mPointValues.size();
                            txtAverageLateral.setText("横向偏差绝对值平均值为：" + String.valueOf(average) + " m"
                                    + "            最大横向偏差绝对值为：" + maxLateral + " m");
                        }
                    } else {
                        if (historyPointValues.size() != 0) {
                            lineChart.setPointValues(historyPointValues);
                            Log.e(TAG, "HISTORY LINE CHART POINT VALUES SET");
                            if (historyPointValues != null && historyPointValues.size() > 0) {
                                float totalLateral = 0.0f;
                                float maxLateral = Float.MIN_VALUE;
                                for (PointValue mPointValue : historyPointValues) {
                                    float absLateral = Math.abs(mPointValue.getY());
                                    if (absLateral > maxLateral) {
                                        maxLateral = absLateral;
                                    }
                                    totalLateral += absLateral;

                                }
                                float average = totalLateral / historyPointValues.size();
                                txtAverageLateral.setText("横向偏差绝对值平均值为：" + String.valueOf(average) + " m"
                                        + "            最大横向偏差绝对值为：" + maxLateral + " m");
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
                isStopNavigation = true;
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

        checkboxABMode = (CheckBox) findViewById(R.id.chkboxABMode);
        checkboxABMode.setOnClickListener(this);

        btnPlanningMode = (Button) findViewById(R.id.btnPlanningMode);
        btnPlanningMode.setOnClickListener(this);

        checkboxHistory = (CheckBox) findViewById(R.id.chkboxHistory);
        checkboxHistory.setOnClickListener(this);

        checkboxStatistics = (CheckBox) findViewById(R.id.chkboxStatistics);
        checkboxStatistics.setOnClickListener(this);

        checkboxRemoteMode = (CheckBox) findViewById(R.id.chkboxRemoteMode);
        checkboxRemoteMode.setOnClickListener(this);

        layoutABModePane = (LinearLayout) findViewById(R.id.layoutABMode);

        btnHistoryAB = (Button) findViewById(R.id.btnHistoryAB);
        btnHistoryAB.setOnClickListener(this);

        btnSetA = (Button) findViewById(R.id.btnSetA);
        btnSetA.setOnClickListener(this);

        btnSetB = (Button) findViewById(R.id.btnSetB);
        btnSetB.setOnClickListener(this);

        checkboxStartNavigation = (CheckBox) findViewById(R.id.chkboxStartNavi);
        checkboxStartNavigation.setOnClickListener(this);

        layoutRemotePane = (LinearLayout) findViewById(R.id.layoutRemotePane);

        btnAccelerate = (Button) findViewById(R.id.btnAccelerate);
        btnAccelerate.setOnClickListener(this);

        btnTurnLeft = (Button) findViewById(R.id.btnTurnLeft);
        btnTurnLeft.setOnClickListener(this);

        checkboxStartSwitch = (CheckBox) findViewById(R.id.chkboxStartSwitch);
        checkboxStartSwitch.setOnClickListener(this);

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

        /*如果 ","的个数不为12，或者字符串开始和结束字符不是指定字符，则数据无效*/
//        if ((SysUtil.countCharacter(stringBuilder, SEPARATOR) != SEPARATOR_NUMBER)
        if ((SysUtil.countCharacter(stringBuilder, SEPARATOR) != 8)
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
            txtReceivedStringNo.setText(String.valueOf(dataNo));

            if (parseReceivedMessage(dataString)) {
                txtDeviance.setText(String.valueOf(lateral));
                txtSatellite.setText(String.valueOf(satellite));
                switch (gps) {
                    /*根据GPS定位标识设置显示文本*/
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
                txtDirectionAngle.setText(String.valueOf(direction) + "°");
                txtTurningAngle.setText(String.valueOf(turnning));
                txtPrecisionSeeding.setText(String.valueOf(seeding));
                currentState = command;
                txtReceivedString.setText(String.valueOf(command));
                locationX = xx;
                locationY = yy;

                if (!isStopNavigation) { //记录横向偏差
                    long timeMillis = System.currentTimeMillis();
                /*以时间横轴，横向偏差为纵轴，添加偏差数据到集合*/
                    if (Math.abs(lateral) < LATERAL_THRESHOLD
                            && currentState == LINE_NAVIGATION_RESPONSE) {//只统计直线阶段的横向偏差并过滤掉计算错误产生的过大横向偏差
                        mPointValues.add(new PointValue((float) ((timeMillis - startNavigationTime) / 1000.0), (float) lateral));
                    }
                }

                /*绘制当前点*/
                if ((locationX > fieldBoundsLimits[0] - BOUNDS_THRESHOLD
                        && locationX < fieldBoundsLimits[1] + BOUNDS_THRESHOLD
                        && locationY > fieldBoundsLimits[2] - BOUNDS_THRESHOLD
                        && locationY < fieldBoundsLimits[3] + BOUNDS_THRESHOLD)) {
//                    Log.e(TAG, "****" + fieldBoundsLimits[0]);
//                    Log.e(TAG, "****" + fieldBoundsLimits[1]);
//                    Log.e(TAG, "****" + fieldBoundsLimits[2]);
//                    Log.e(TAG, "****" + fieldBoundsLimits[3]);
//                    Log.e(TAG, "****" + locationX);
//                    Log.e(TAG, "****" + locationY);
//                    Log.e(TAG, "myView.setCurrentPoint");
                    myView.setCurrentPoint(dataNo, locationX, locationY);
                }

                /*设置A点*/
                if (isPointASet && currentState == SET_A_RESPONSE) {// 判断此时是否点击设置A点
                    if (preState != SET_A_RESPONSE) {
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
                if (isPointBSet && currentState == SET_B_RESPONSE) {// 判断此时是否点击设置B点
                    if (preState != SET_B_RESPONSE) {
                        bX = locationX;
                        bY = locationY;
                        bLat = lat;
                        bLng = lng;

                        if (aY == bY && aX == bX) {
                            //弹出AB点重合警告
                            ToastUtil.showToast(getString(R.string.ab_overlay_error_warning), true);
                            return;
                        } else {
                            ToastUtil.showToast(getString(R.string.b_point_already_set), true);
                            lineAB = new GeoLine(aX, aY, bX, bY); //更新默认AB线（规划轨迹时用AB线作为基准线）
                            myView.drawABline(aX, aY, bX, bY, true); //在界面上绘制AB线

                            currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                            String fileABPoints = currentTime + "_ab_points.ab";
                            String abLine = new StringBuilder()
                                    .append(aX).append(",").append(aY).append(",").append(aLat).append(",").append(aLng)
                                    .append("\r\n")
                                    .append(bX).append(",").append(bY).append(",").append(bLat).append(",").append(bLng)
                                    .toString();
                            // 保存AB线到外部文件
                            FileUtil.writeDataToExternalStorage(AB_LINE_DIRECTORY, fileABPoints, abLine, true, false);

                            myApp.getDatabaseManager().insertABline(currentTime + "_ab", new ABLine(aX, aY, bX, bY), defaultFieldName);
                        }

                        isPointBSet = false;
                    }
                    // 设置发送指令为确认收到B点坐标命令
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_B_AFFIRM);
                }

                /*启动导航后收到直线行驶响应则切换为默认发送指令*/
                if (isStartNavigation == true && currentState == LINE_NAVIGATION_RESPONSE) {
                    isStartNavigation = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                    ToastUtil.showToast(getString(R.string.line_navigating), true);
                }

                /*停止导航后收到默认响应则切换为默认发送指令*/
                if (isStopNavigation == true && currentState == DEFAULT_STATE_RESPONSE) {
                    isStopNavigation = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                }

                /*右转后收到右转响应则切换为默认发送指令*/
                if (isToTurnRight && currentState == TURNING_RIGHT_RESPONSE) {
                    isToTurnRight = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                    ToastUtil.showToast(getString(R.string.turning_right_successfully), true);

                }

                /*左转后收到左转响应则切换为默认发送指令*/
                if (isToTurnLeft && currentState == TURNING_LEFT_RESPONSE) {
                    isToTurnLeft = false;
                    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                    ToastUtil.showToast(getString(R.string.turning_left_successfully), true);
                }

                /*
                //这一段本来是按照通信协议写的，加上去之后，下位机貌似不稳定（由于计算能力限制？）
                if (!isStopNavigation) {
                    if (currentState == LINE_NAVIGATION_RESPONSE || currentState == HEADLAND_P1_RESPONSE
                            || currentState == HEADLAND_P2_RESPONSE) {
                        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
                    }
                }*/

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
//        if (dataArray.length < 13) {
//            return false;
//        }
        try {

//            lat = Double.parseDouble(dataArray[0]);
//            lng = Double.parseDouble(dataArray[1]);
//            xx = Double.parseDouble(dataArray[2]);
//            yy = Double.parseDouble(dataArray[3]);
//            satellite = Integer.parseInt(dataArray[4]);
//            gps = Integer.parseInt(dataArray[5]);
//            direction = Double.parseDouble(dataArray[6]);
//            velocity = Double.parseDouble(dataArray[7]);
//            command = Integer.parseInt(dataArray[8]);
//            sensor = Double.parseDouble(dataArray[9]);
//            lateral = Double.parseDouble(dataArray[10]);
//            if ("nan".equalsIgnoreCase(dataArray[11])) {
//                turnning = 99999.999;
//            } else {
//                turnning = Double.parseDouble(dataArray[11]);
//            }
//            seeding = Double.parseDouble(dataArray[12]);


            lat = 30.557573;
            lng = 121.169113;

            xx = Double.parseDouble(dataArray[0]);
            yy = Double.parseDouble(dataArray[1]);
            satellite = 17;
            gps = Integer.parseInt(dataArray[2]);
            direction = Double.parseDouble(dataArray[3]);
            velocity = Double.parseDouble(dataArray[4]);
            command = Integer.parseInt(dataArray[5]);
            sensor = Double.parseDouble(dataArray[6]);
            lateral = Double.parseDouble(dataArray[7]);
            turnning = Double.parseDouble(dataArray[8]);
            seeding = 0;

            return true;

        } catch (NumberFormatException e) {
            ToastUtil.showToast(getString(R.string.received_data_type_error), true);
            return false;
        }
    }

    /**
     * 开始导航操作方法
     */
    private void startNavigation() {
        /*发送启动导航命令*/
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_START_NAVI); //蓝牙发送开始导航指令
        startNavigationTime = System.currentTimeMillis(); //记录开始导航系统时间（毫秒）
        mPointValues.clear(); //导航横向偏差数据集合清零
        isStartNavigation = true; //设置导航状态为真
    }

    /**
     * 停止导航操作方法
     */
    private void stopNavigation() {
        /*发送停止导航命令*/
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI); //蓝牙发送停止导航指令
        lineChart.setPointValues(mPointValues); //将导航偏差数据集传到折线图
        Log.e(TAG, "VALUES SIZE() IS :" + mPointValues.size());
        isStopNavigation = true;
        long stopNavigationTime = System.currentTimeMillis();

        /*如果点击停止导航和点击开始导航时间差大于20秒则保存该次轨迹到数据库*/
        if ((stopNavigationTime - startNavigationTime) / 1000 > 20) {
            myApp.getDatabaseManager().insertHistoryEntry(fileNameToSave, defaultFieldName);
        }
    }

    /**
     * 开始绘制轨迹操作方法
     */
    private void startPlotAndSavePath() {
        isDataToSave = true; //设置开始保存数据状态为真
        dataNo = 0; //将数据点编号重置为零
        if (GisAlgorithm.getBoundaryLimits(defaultFieldVertexList) != null) {
            fieldBoundsLimits = GisAlgorithm.getBoundaryLimits(defaultFieldVertexList);
        }
        myView.setOperationPathWidth(linespacing);
        myView.drawPointToPath(1, true); //设置绘制轨迹状态为真
        currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()); //获取当前日期时间字符串
        fileNameToSave = "data_" + currentTime + ".dt"; //设置数据保存文件名
    }

    /**
     * 停止绘制轨迹操作方法
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
     * 判断是否需要拐弯
     * （这方法暂时是不能用的，因为车辆来回两次穿越距离边界一定距离的直线，但是只有一次转弯）
     *
     * @param distance 距离边界实时距离
     * @param limit    拐弯距离限制
     * @return
     */
    public boolean isToTurn(double distance, double limit) {
        return distance > limit && (distance - limit) < 0.1;
    }

    /**
     * @param fileName           历史记录文件名
     * @param historyPathPoints  读取历史文件后保存历史轨迹的缓存列表
     * @param historyPointValues 读取历史文件后保存历史横向偏差记录的缓存列表
     * @return 成功标志
     */
    private boolean getHistoryDataFromFiles(String fileName,
                                            List<GeoPoint> historyPathPoints,
                                            List<PointValue> historyPointValues) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS", Locale.SIMPLIFIED_CHINESE);
        String dataDirectory = FileUtil.getAlbumStorageDir(NavigationActivity.DATA_DIRECTORY).toString();
        String filePath = dataDirectory + File.separator + fileName;
        String[] lines = FileUtil.readFileFromExternalStorage(filePath, null);
        if (lines == null || lines.length == 0) {
            return false;
        }
        Log.e(TAG, "LINES LENGTH IS :" + lines.length);
        historyPathPoints.clear();
        historyPointValues.clear();
        long startTime;
        try {
            startTime = sdf.parse(lines[0].split(",")[9]).getTime();
//            startTime = sdf.parse(lines[0].split(",")[13]).getTime();
            for (int i = 0; i < lines.length; i++) {
                String[] arrays = lines[i].split(",");
//                if (arrays.length == 14) {
                if (arrays.length == 10) {
                    try {
//                        double hX = Double.parseDouble(arrays[2]);
//                        double hY = Double.parseDouble(arrays[3]);
//                        double hLateral = Double.parseDouble(arrays[10]);
//                        long hTime = sdf.parse(arrays[13]).getTime();

                       /* 如果下位机发过来的数据只有9个字段，就用下面这段*/
                        double hX = Double.parseDouble(arrays[0]);
                        double hY = Double.parseDouble(arrays[1]);
                        int hCommand = Integer.parseInt(arrays[5]);
                        double hLateral = Double.parseDouble(arrays[7]);
                        long hTime = sdf.parse(arrays[9]).getTime();
//                        if ((hX > fieldBoundsLimits[0] - BOUNDS_THRESHOLD
//                                && hX < fieldBoundsLimits[1] + BOUNDS_THRESHOLD
//                                && hY > fieldBoundsLimits[2] - BOUNDS_THRESHOLD
//                                && hY < fieldBoundsLimits[3] + BOUNDS_THRESHOLD)) {
//                            historyPathPoints.add(new GeoPoint(hX, hY)); //添加历史轨迹各个点}
//                        }
                        historyPathPoints.add(new GeoPoint(hX, hY)); //添加历史轨迹各个点}
                        if (hCommand == 10010 &&
                                Math.abs(hLateral) < LATERAL_THRESHOLD) {
                            /*忽略过大的横向偏差(接收数据跳动引起的)*/
                            historyPointValues.add(new PointValue((float) ((hTime - startTime) / 1000.0), (float) hLateral));
                        }
                    } catch (NumberFormatException e) {
                        ToastUtil.showToast("历史数据数字格式不对!", true);
                        return false;
                    } catch (java.text.ParseException e) {
                        ToastUtil.showToast("历史数据日期格式解析错误!", true);
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
