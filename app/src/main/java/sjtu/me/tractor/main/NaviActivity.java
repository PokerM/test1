package sjtu.me.tractor.main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import sjtu.me.tractor.R;
import sjtu.me.tractor.bluetooth.BluetoothService;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.field.FieldResultActivity;
import sjtu.me.tractor.gis.GeoPoint;
import sjtu.me.tractor.gis.GisAlgorithm;
import sjtu.me.tractor.hellochart.LineChart;
import sjtu.me.tractor.surfaceview.MySurfaceView;
import sjtu.me.tractor.util.FileUtil;
import sjtu.me.tractor.util.ToastUtil;

/**
 * @author billhu
 *
 */
@SuppressLint("UseSparseArrays")
public class NaviActivity extends Activity implements OnClickListener{
	
	private static final String TAG = "NaviActivity";		//调试日志标签
	private static final boolean D = true;
	
	private static final char END = '*'; // 串口通信字符串结束标志
	private static final char START = '#'; // 串口通信字符串开始标志
	private static final char SEPARATOR = ','; // 分隔符
	
	private static final int REQUEST_SELECT_FIELD = 3;   // 跳转到开启蓝牙连接页面的标志

    public static final int CAR_STATE_IDLE = 99999;    
    public static final int CAR_SET_A_RESPONSE = 40103;
    public static final int CAR_SET_B_RESPONSE = 40104;
    public static final int CAR_LINE_NAVI = 10010;
    public static final int CAR_TURNING_RIGHT = 10020;
    public static final int CAR_TURNING_LEFT = 10030;
    
    public static final int HEADLAND_P1_RESPONSE = 50101;
    public static final int HEADLAND_P2_RESPONSE = 50102;
    
	private static final String FILE_NAME = "data.txt";
	private static final String ALBUM_NAME = "AutoTractorData";
	
	ImageButton imgbtnBack;  //返回按钮
	ImageButton imgbtnForwards;	 //前进按钮
	ImageButton imgbtnBackwards;	//后退按钮
	ImageButton imgbtnTurnLeft;	 //左转按钮
	ImageButton imgbtnTurnRight;		//右转按钮
	ImageButton imgbtnStop;	//停止按钮
	
	Button btnSetField;  //设置田地按钮
	Button btnStartNavi;  //启动导航按钮
    Button btnTurnLeft;  //左转按钮
    Button btnTurnRight;  //右转按钮
    Button btnSetA;  //设置A点按钮
    Button btnSetB;  //设置B点按钮
    Button btnDrawAB;  //画AB线按钮
    Button btnHistoryAB;  //历史AB线按钮
    Button btnPlotPath;  //绘制轨迹按钮
    Button btnSavePath;  //保存路径按钮
    Button btnHistoryPath;  //历史按钮
    
    TextView txtDistance2Bound1;    //到边界距离文本
    TextView txtDistance2Bound2;    //到边界距离文本
    TextView txtGpsState;  //GPS状态文本
    TextView txtCarState;  //导航控制状态文本
    TextView txtNorthAngle;    //真北方向角文本
    TextView txtLocationX;     //定位X坐标文本
    TextView txtLocationY;     //定位Y坐标文本
    TextView txtDirectionAngle;    //航向角文本
    TextView txtVelocity;  //速度文本
    TextView txtDeviance;  //横向偏差文本
    TextView txtTurningAngle;  //横向偏差文本
    TextView txtCommunicationState;    //通信状态文本
    TextView txtReceivedString;    //接收数据文本
    TextView txtReceivedStringLength;  //接收数据长度文本
    TextView txtReceivedStringNo;  //接收数据编号文本
    TextView txtSentString;    //发送数据文本
    
	private String readMessage;
	Bundle bundle;
	private int dataNo = 0;
	private int currentState = 0;
	private int pastState = 0;
	
	private double pointX = 0;
	private double pointY = 0;
	private double lateralDeviation = 0;
	private double APointX, APointY, BPointX, BPointY;
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
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navi_activity);
        if (D)   Log.e(TAG, "+++ ON CREATE +++");
        
        //实例化地块顶点列表对象
        fieldVertex = new ArrayList<GeoPoint>();
        
        //获取全局类实例
        myApp = (MyApplication) getApplication();
        //设置蓝牙连接的消息处理器为当前界面处理器
        myApp.getBluetoothService().setHandler(mNaviHandler);
        if (D) Log.e(TAG, "+++ setHandler: mNaviHandler +++");
        
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
                if (((CheckBox) v).isChecked())
                    isBoundP3Set = true;
            }
        });

        CheckBox checkBox3 = (CheckBox) findViewById(R.id.starBottomLeft2);
        checkBox3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    isBoundP4Set = true;
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
        if (D) Log.e(TAG, "+++ ON START +++");
        
        if (myApp.getBluetoothService().getState() == BluetoothService.STATE_NONE) {
            txtCommunicationState.setText(R.string.comm_off);
        } else {
            txtCommunicationState.setText(R.string.comm_on);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+++ ON RESUME +++");
    }

    @Override
    protected void onPause() {
      
        super.onPause();
        if (D) Log.e(TAG, "+++ ON PAUSE +++");
    }

    @Override
    protected void onRestart() {
      
        super.onRestart();
        if (D) Log.e(TAG, "+++ ON RESTART +++");
        
        // 重新进入界面时设置消息处理器为当前处理器
        myApp.getBluetoothService().setHandler(mNaviHandler);
        
        if (myApp.getBluetoothService().getState() == BluetoothService.STATE_NONE) {
            txtCommunicationState.setText(R.string.comm_off);
        } else {
            txtCommunicationState.setText(R.string.comm_on);
        }
    }

    @Override
	protected void onStop() {
		
		super.onStop();
		 if (D)   Log.e(TAG, "+++ ON STOP +++");
	}

	@Override
    protected void onDestroy() {
      
        super.onDestroy();
        if (D) Log.e(TAG, "+++ ON DESTROY +++");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_SELECT_FIELD:
            if (resultCode == RESULT_OK) {
                bundle = data.getExtras();
                String name = bundle.getString(FieldResultActivity.EXTRA_FIELD_NAME);
                Log.e("selected item", name);
                Cursor resultCursor = myApp.getDatabaseManager().queryFieldWithPointsByName(name);
                List<Map<String, String>> resultList = DatabaseManager.cursorToList(resultCursor);
                
                for (int i = 0; i < resultList.size(); i++) {
                    GeoPoint vertex = new GeoPoint(Double.valueOf(resultList.get(i).get("fPX")), Double.valueOf(resultList.get(i).get("fPY")));
                    fieldVertex.add(vertex);
                }
                
                Log.e("fieldVertex", fieldVertex.get(0).getXCoordinate() + "," + fieldVertex.get(0).getYCoordinate());
                Log.e("fieldVertex", fieldVertex.get(1).getXCoordinate() + "," + fieldVertex.get(1).getYCoordinate());
                Log.e("fieldVertex", fieldVertex.get(2).getXCoordinate() + "," + fieldVertex.get(2).getYCoordinate());
                Log.e("fieldVertex", fieldVertex.get(3).getXCoordinate() + "," + fieldVertex.get(3).getYCoordinate());
                
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
        	
        case R.id.btnSetField:
            Cursor cursor = myApp.getDatabaseManager().queryFieldByName("");
            Bundle data = new Bundle();
            data.putSerializable("data", DatabaseManager.cursorToList(cursor));
            Intent intent = new Intent("sjtu.me.tractor.fieldsetting.FieldResultActivity");
            intent.putExtras(data);
            startActivityForResult(intent, REQUEST_SELECT_FIELD);
            break;
            
        case R.id.btnStartNavi:
            if (!isStartNavi) {
                startNavi();
            } else {
                stopNavi();
            }
            break;
            
        case R.id.btnTurnLeft:
        	isToTurnLeft = true;
        	myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_TURN_LEFT);
        	break;
        	
        case R.id.btnTurnRight:
        	isToTurnRight = true;
        	myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_TURN_RIGHT);
        	break;
        	
        case R.id.btnSetA:
        	isPointASet = true;
        	myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_A_REQUEST);
        	break;
        	
        case R.id.btnSetB:
        	isPointBSet = true;
        	myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_B_REQUEST);
        	break;
        	
        case R.id.btnDrawAB:
            if (APointY == BPointY && APointX == BPointX) {
                //弹出AB点重合警告
                ToastUtil.showToast(getString(R.string.ab_overlay_error_warning), true);
                return;
            } else
                myView.drawABline(APointX, APointY, BPointX, BPointY, true);
                currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String fileABPoints = currentTime + "_AB_Points.txt";
                // 保存田地顶点坐标到外部文件
                FileUtil.writeDataToExternalStorage(ALBUM_NAME, fileABPoints,
                        "ABpoints:\r\n" + APointX + "," + APointY + "\r\n" + BPointX + "," + BPointX, true, false);
        	break;
        	
        case R.id.btnHistoryAB:
        	break;
        	
        case R.id.btnPlotPath:
			if (isPlotting == false) {
				startPlotPath();
			} else {
			    stopPlotPath();
			}
        	break;
        	
        case R.id.btnSavePath:
//            boundTempMapX = new HashMap<Integer, Double>();
//            boundTempMapY = new HashMap<Integer, Double>();
//
//            boundTempMapX.put(1, 622442.2437);
//            boundTempMapX.put(2, 622420.828635);
//            boundTempMapX.put(3, 622423.89173);
//            boundTempMapX.put(4, 622446.48542);
//            boundTempMapY.put(1, 3423929.17782);
//            boundTempMapY.put(2, 3423930.259849);
//            boundTempMapY.put(3, 3424003.981122);
//            boundTempMapY.put(4, 3424005.692487);
//
//            myView.setViewFieldBoundary(boundTempMapX, boundTempMapY, true);
            break;
        	
        case R.id.btnHistoryPath:
            String dbPath = getApplication().getDatabasePath("auto_tractor").toString();
//            MyApplication.copyDbFilesToExternalStorage(dbPath);
//            Log.e("hhh", "COPY FILES SUCCESSFULLY? " + MyApplication.copyDbFilesToExternalStorage(dbPath));
        	break;
        	
        case R.id.imgbtnForwards:
			break;

		case R.id.imgbtnBackwards:
			break;

		case R.id.imgbtnTurnLeft:
		    isToTurnLeft = true;
		    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_TURN_LEFT);
			break;

		case R.id.imgbtnTurnRight:
		    isToTurnRight = true;
		    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_TURN_RIGHT);
			break;


		case R.id.imgbtnStop:
			myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI);
			break;
			
		case R.id.btnShowLineChart:
		    if (((CheckBox) v).isChecked()) {
		        lineChart.showChartView(true);
		    } else {
		        lineChart.showChartView(false);
		    }
		    break;
        
        default:
            break;
        }
    }
    
    private void initViews() 	{
    	myView = (MySurfaceView) findViewById(R.id.mySurfaceView);
        myView.setCanvasSize(1400, 1330);
        
        txtDistance2Bound1 = (TextView) findViewById(R.id.txtDistance2Bound1);
        txtDistance2Bound2 = (TextView) findViewById(R.id.txtDistance2Bound2);
        txtGpsState = (TextView) findViewById(R.id.txtGpsState);
        txtCarState = (TextView) findViewById(R.id.txtCarState);
        txtNorthAngle = (TextView) findViewById(R.id.txtNorthAngle);
        txtLocationX = (TextView) findViewById(R.id.txtLocationX);
        txtLocationY = (TextView) findViewById(R.id.txtLocationY);
        txtDirectionAngle = (TextView) findViewById(R.id.txtDirectionAngle);
        txtVelocity = (TextView) findViewById(R.id.txtVelocity);
        txtDeviance = (TextView) findViewById(R.id.txtDeviance);
        txtTurningAngle = (TextView) findViewById(R.id.txtTurningAngle);
        txtCommunicationState = (TextView) findViewById(R.id.txtCommunicationState);
        txtReceivedString = (TextView) findViewById(R.id.txtReceivedString);
        txtReceivedStringLength = (TextView) findViewById(R.id.txtReceivedStringLength);
        txtReceivedStringNo = (TextView) findViewById(R.id.txtReceivedStringNo);
        txtSentString = (TextView) findViewById(R.id.txtSentString);
        
        imgbtnBack = (ImageButton) findViewById(R.id.imgbtnBack);
        imgbtnBack.setOnClickListener(this);
        
        imgbtnForwards = (ImageButton) findViewById(R.id.imgbtnForwards);
        imgbtnForwards.setOnClickListener(this);
        
        imgbtnBackwards = (ImageButton) findViewById(R.id.imgbtnBackwards);
        imgbtnBackwards.setOnClickListener(this);
        
        imgbtnTurnRight = (ImageButton) findViewById(R.id.imgbtnTurnRight);
        imgbtnTurnRight.setOnClickListener(this);
        
        imgbtnTurnLeft = (ImageButton) findViewById(R.id.imgbtnTurnLeft);
        imgbtnTurnLeft.setOnClickListener(this);
        
        imgbtnStop = (ImageButton) findViewById(R.id.imgbtnStop);
        imgbtnStop.setOnClickListener(this);
        
        btnSetField = (Button) findViewById(R.id.btnSetField);
        btnSetField.setOnClickListener(this);
        
        btnStartNavi = (Button) findViewById(R.id.btnStartNavi);
        btnStartNavi.setOnClickListener(this);
        
        btnTurnLeft = (Button) findViewById(R.id.btnTurnLeft);
        btnTurnLeft.setOnClickListener(this);
        
        btnTurnRight = (Button) findViewById(R.id.btnTurnRight);
        btnTurnRight.setOnClickListener(this);
        
        btnSetA = (Button) findViewById(R.id.btnSetA);
        btnSetA.setOnClickListener(this);
        
        btnSetB = (Button) findViewById(R.id.btnSetB);
        btnSetB.setOnClickListener(this);
        
        btnDrawAB = (Button) findViewById(R.id.btnDrawAB);
        btnDrawAB.setOnClickListener(this);
        
        btnHistoryAB = (Button) findViewById(R.id.btnHistoryAB);
        btnHistoryAB.setOnClickListener(this);
        
        btnPlotPath = (Button) findViewById(R.id.btnPlotPath);
        btnPlotPath.setOnClickListener(this);
        
        btnSavePath = (Button) findViewById(R.id.btnSavePath);
        btnSavePath.setOnClickListener(this);
        
        btnHistoryPath = (Button) findViewById(R.id.btnHistoryPath);
        btnHistoryPath.setOnClickListener(this);
        
        findViewById(R.id.btnShowLineChart).setOnClickListener(this);
    }
    
    /**
     * 创建消息处理器子类对象；
     * 在handlerMessage()中写消息处理代码
     */
    public final Handler mNaviHandler = new Handler() {
		StringBuilder readMessageSB = new StringBuilder();
		double formerDidstance = 0;

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case BluetoothService.MESSAGE_RECEIVED:
				// 处理接收到的数据
				String dataString = "";
				readMessage = (String) msg.obj;
				readMessageSB.append(readMessage);

				// 如果 ","的个数不为8，或者字符串开始和结束字符不是指定的，则数据无效
				if (!((MyApplication.countCharacter(readMessageSB, SEPARATOR) == 8) && (readMessageSB.charAt(0) == START)
						&& (readMessageSB.charAt(readMessageSB.length() - 1) == END)))
					readMessageSB.delete(0, readMessageSB.length());
				else {
					dataNo++;
					dataString = readMessageSB.substring(1, readMessageSB.length() - 1); // 提取字符串
					// 将收到的数据保存到外部存储器
					if (isDataToSave) {
						FileUtil.writeDataToExternalStorage(ALBUM_NAME, fileNameToSave, dataString, true, true);
					}

					// set the TextView with the received data
					txtReceivedString.setText(dataString);
					int dataLength = dataString.length();
					txtReceivedStringLength.setText(getString(R.string.length) + dataLength);
					txtReceivedStringNo.setText(getString(R.string.number) + dataNo);

					String[] dataArray = dataString.split(",", 9);
					try {
						double xCoordinate = Double.parseDouble(dataArray[0]); // x坐标
						double yCoordinate = Double.parseDouble(dataArray[1]); // y坐标
						int GPS = Integer.parseInt(dataArray[2]);
						double northAngle = Double.parseDouble(dataArray[3]);
						double carVelocity = Double.parseDouble(dataArray[4]);
						int stopBit = Integer.parseInt(dataArray[5]);
						double directionAngle = Double.parseDouble(dataArray[6]);
						double lateralDeviation = Double.parseDouble(dataArray[7]);
						double turningAngle;
						if ("nan".equalsIgnoreCase(dataArray[8])) {
							turningAngle = 99999.999;
						} else {
							turningAngle = Double.parseDouble(dataArray[8]);
						}

						txtLocationX.setText(getString(R.string.x_coordinate) + xCoordinate);
						txtLocationY.setText(getString(R.string.y_coordinate) + yCoordinate);
						txtGpsState.setText(getString(R.string.gps_sign) + GPS);
						txtDirectionAngle.setText(getString(R.string.direction_angle) + directionAngle);
						txtVelocity.setText(getString(R.string.speed) + carVelocity + getString(R.string.speed_unit));
						txtCarState.setText(getString(R.string.stop_bit) + stopBit);
						txtNorthAngle.setText(getString(R.string.north_angle) + northAngle);
						txtDeviance.setText(getString(R.string.lateral_deviation) + lateralDeviation);
						txtTurningAngle.setText(getString(R.string.turning_angle) + turningAngle);

						currentState = stopBit;
						pointX = xCoordinate;
						pointY = yCoordinate;
						
						//保存横向偏差数据到集合
						NaviActivity.this.lateralDeviation = lateralDeviation;
						long timeMillis = System.currentTimeMillis();
				        mPointValues.add(new PointValue((float) ((timeMillis - startTimeMillis) / 1000.0), (float) NaviActivity.this.lateralDeviation));
				            
						// draw the path
						// 绘制当前点
						myView.setCurentPoint(dataNo, pointX, pointY);
//						Log.e(TAG, "setCurrentPoint");

						double distance1 = GisAlgorithm.distanceFromPointToLine(622420.828635, 3423930.259849,
						        622422.2437, 3423929.17782, pointX, pointY);
						double distance2 = GisAlgorithm.distanceFromPointToLine(622423.89173, 3424003.981122,
						        622446.48542, 3424005.692487, pointX, pointY);
						txtDistance2Bound1.setText(getString(R.string.border_distance_1) + distance1);
						txtDistance2Bound2.setText(getString(R.string.border_distance_2) + distance2);
						
						if (dataNo % 20 == 0) formerDidstance = distance1;
						
//						if (isStartNavi == true && stopBit != 10020
//						        && distance1 - 10 <= 0.25 && distance1 < formerDidstance) {
//							isToTurnRight = true;
//							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_TURN_RIGHT);
//							if (currentState == 10020) {
//								isToTurnRight = false;
//								// 设置发送指令为默认命令
//								myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
//							}
//						}
//						
//						if (isStartNavi == true && stopBit != 10030
//						        && distance2 - 10 <= 0.25 && distance1 > formerDidstance) {
//						    isToTurnLeft = true;
//						    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_TURN_LEFT);
//						    if (currentState == 10030) {
//								isToTurnRight = false;
//								// 设置发送指令为默认命令
//								myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
//							}
//						}

						// 判断此时是否点击设置A点
						if (isPointASet && currentState == CAR_SET_A_RESPONSE) {
							if (pastState != CAR_SET_A_RESPONSE) {
								APointX = pointX;
								APointY = pointY;
								ToastUtil.showToast(getString(R.string.a_point_already_set), true);
								isPointASet = false;
							}
							// 设置发送指令为确认收到A点坐标命令
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_A_AFFIRM);
						}

						// 判断此时是否点击设置B点
						if (isPointBSet && currentState == CAR_SET_B_RESPONSE) {
							if (pastState != CAR_SET_B_RESPONSE) {
								BPointX = pointX;
								BPointY = pointY;
								ToastUtil.showToast(getString(R.string.b_point_already_set), true);
								isPointBSet = false;
							}
							// 设置发送指令为确认收到B点坐标命令
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_B_AFFIRM);
						}

						if (currentState == CAR_LINE_NAVI) {
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
						}
						
//						if (currentState == CAR_LINE_NAVI || currentState == HEADLAND_P1_RESPONSE
//						        || currentState == HEADLAND_P2_RESPONSE) {
//						    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
//						}

						// 判断此时是否右转
						if (isToTurnRight && currentState == CAR_TURNING_RIGHT) {
							isToTurnRight = false;
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
						}

						// 判断此时是否左转
						if (isToTurnLeft && currentState == CAR_TURNING_LEFT) {
							isToTurnLeft = false;
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
						}

						

						// 保存当前状态
						pastState = currentState;

					} catch (NumberFormatException e) {
						ToastUtil.showToast(getString(R.string.received_data_type_error), true);
					}

					// clear all string data
					readMessageSB.delete(0, readMessageSB.length());
				}
				break;

			case BluetoothService.MESSAGE_SENT:
				String writeMessage = msg.obj.toString();
				txtSentString.setText(writeMessage);
				break;
				
            case BluetoothService.MESSAGE_CONNECT_RESULT:
                ToastUtil.showToast(msg.obj.toString(), true);
                break;

			default:
				break;
			}
		}
	};
	
	/**
	 *  开始导航操作方法
	 */
	private void startNavi() {
	    isStartNavi = true; //设置导航状态为真
        startTimeMillis = System.currentTimeMillis(); //记录开始导航系统时间（毫秒）
        mPointValues.clear(); //导航横向偏差数据集合清零
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_START_NAVI); //蓝牙发送开始导航指令
        btnStartNavi.setText(R.string.stop_navi);
        findViewById(R.id.btnShowLineChart).setVisibility(View.INVISIBLE);
	}
	
	/**
	 * 停止导航操作方法
	 */
	private void stopNavi() {
	    isStartNavi = false; // 
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI); //蓝牙发送停止导航指令
        lineChart = new LineChart((LineChartView) findViewById(R.id.lineChartView), mPointValues); //将导航偏差数据集传到折线图
        btnStartNavi.setText(getString(R.string.start_navi));
        findViewById(R.id.btnShowLineChart).setVisibility(View.VISIBLE);
	}
    
	/**
	 *  开始绘制轨迹操作方法
	 */
	private void startPlotPath() {
	    isPlotting = true; //设置开始绘制轨迹状态为真
	    isDataToSave = true; //设置开始保存数据状态为真
        dataNo = 0; //将数据点编号重置为零
        myView.drawPath(1, true); //设置绘制轨迹状态为真
        currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()); //获取当前日期时间字符串
        fileNameToSave = currentTime + "_" + FILE_NAME; //设置数据保存文件名
        btnPlotPath.setText(R.string.stop_plot_path);
	}
	
	/**
	 * 停止绘制轨迹操作方法
	 */
	private void stopPlotPath() {
	    isPlotting = false;
        myView.drawPath(1, false);
        isDataToSave = false;
        btnPlotPath.setText(getString(R.string.plot_path));
	}
	
	/**
	 * 判断是否需要拐弯
	 * （这算法暂时是错误的，因为车辆来回两次穿越距离边界一定距离的直线，但是只有一次转弯）
	 * @param distance 距离边界实时距离
	 * @param limit 拐弯距离限制
	 * @return
	 */
	public boolean isToTurn(double distance, double limit) {
		return distance > limit && (distance - limit) < 0.1;
	}

}
