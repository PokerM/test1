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
	
	private static final String TAG = "NaviActivity";		//������־��ǩ
	private static final boolean D = true;
	
	private static final char END = '*'; // ����ͨ���ַ���������־
	private static final char START = '#'; // ����ͨ���ַ�����ʼ��־
	private static final char SEPARATOR = ','; // �ָ���
	
	private static final int REQUEST_SELECT_FIELD = 3;   // ��ת��������������ҳ��ı�־

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
	
	ImageButton imgbtnBack;  //���ذ�ť
	ImageButton imgbtnForwards;	 //ǰ����ť
	ImageButton imgbtnBackwards;	//���˰�ť
	ImageButton imgbtnTurnLeft;	 //��ת��ť
	ImageButton imgbtnTurnRight;		//��ת��ť
	ImageButton imgbtnStop;	//ֹͣ��ť
	
	Button btnSetField;  //������ذ�ť
	Button btnStartNavi;  //����������ť
    Button btnTurnLeft;  //��ת��ť
    Button btnTurnRight;  //��ת��ť
    Button btnSetA;  //����A�㰴ť
    Button btnSetB;  //����B�㰴ť
    Button btnDrawAB;  //��AB�߰�ť
    Button btnHistoryAB;  //��ʷAB�߰�ť
    Button btnPlotPath;  //���ƹ켣��ť
    Button btnSavePath;  //����·����ť
    Button btnHistoryPath;  //��ʷ��ť
    
    TextView txtDistance2Bound1;    //���߽�����ı�
    TextView txtDistance2Bound2;    //���߽�����ı�
    TextView txtGpsState;  //GPS״̬�ı�
    TextView txtCarState;  //��������״̬�ı�
    TextView txtNorthAngle;    //�汱������ı�
    TextView txtLocationX;     //��λX�����ı�
    TextView txtLocationY;     //��λY�����ı�
    TextView txtDirectionAngle;    //������ı�
    TextView txtVelocity;  //�ٶ��ı�
    TextView txtDeviance;  //����ƫ���ı�
    TextView txtTurningAngle;  //����ƫ���ı�
    TextView txtCommunicationState;    //ͨ��״̬�ı�
    TextView txtReceivedString;    //���������ı�
    TextView txtReceivedStringLength;  //�������ݳ����ı�
    TextView txtReceivedStringNo;  //�������ݱ���ı�
    TextView txtSentString;    //���������ı�
    
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
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navi_activity);
        if (D)   Log.e(TAG, "+++ ON CREATE +++");
        
        //ʵ�����ؿ鶥���б����
        fieldVertex = new ArrayList<GeoPoint>();
        
        //��ȡȫ����ʵ��
        myApp = (MyApplication) getApplication();
        //�����������ӵ���Ϣ������Ϊ��ǰ���洦����
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
        
        // ���½������ʱ������Ϣ������Ϊ��ǰ������
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
                //����AB���غϾ���
                ToastUtil.showToast(getString(R.string.ab_overlay_error_warning), true);
                return;
            } else
                myView.drawABline(APointX, APointY, BPointX, BPointY, true);
                currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String fileABPoints = currentTime + "_AB_Points.txt";
                // ������ض������굽�ⲿ�ļ�
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
     * ������Ϣ�������������
     * ��handlerMessage()��д��Ϣ�������
     */
    public final Handler mNaviHandler = new Handler() {
		StringBuilder readMessageSB = new StringBuilder();
		double formerDidstance = 0;

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case BluetoothService.MESSAGE_RECEIVED:
				// ������յ�������
				String dataString = "";
				readMessage = (String) msg.obj;
				readMessageSB.append(readMessage);

				// ��� ","�ĸ�����Ϊ8�������ַ�����ʼ�ͽ����ַ�����ָ���ģ���������Ч
				if (!((MyApplication.countCharacter(readMessageSB, SEPARATOR) == 8) && (readMessageSB.charAt(0) == START)
						&& (readMessageSB.charAt(readMessageSB.length() - 1) == END)))
					readMessageSB.delete(0, readMessageSB.length());
				else {
					dataNo++;
					dataString = readMessageSB.substring(1, readMessageSB.length() - 1); // ��ȡ�ַ���
					// ���յ������ݱ��浽�ⲿ�洢��
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
						double xCoordinate = Double.parseDouble(dataArray[0]); // x����
						double yCoordinate = Double.parseDouble(dataArray[1]); // y����
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
						
						//�������ƫ�����ݵ�����
						NaviActivity.this.lateralDeviation = lateralDeviation;
						long timeMillis = System.currentTimeMillis();
				        mPointValues.add(new PointValue((float) ((timeMillis - startTimeMillis) / 1000.0), (float) NaviActivity.this.lateralDeviation));
				            
						// draw the path
						// ���Ƶ�ǰ��
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
//								// ���÷���ָ��ΪĬ������
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
//								// ���÷���ָ��ΪĬ������
//								myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
//							}
//						}

						// �жϴ�ʱ�Ƿ�������A��
						if (isPointASet && currentState == CAR_SET_A_RESPONSE) {
							if (pastState != CAR_SET_A_RESPONSE) {
								APointX = pointX;
								APointY = pointY;
								ToastUtil.showToast(getString(R.string.a_point_already_set), true);
								isPointASet = false;
							}
							// ���÷���ָ��Ϊȷ���յ�A����������
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_A_AFFIRM);
						}

						// �жϴ�ʱ�Ƿ�������B��
						if (isPointBSet && currentState == CAR_SET_B_RESPONSE) {
							if (pastState != CAR_SET_B_RESPONSE) {
								BPointX = pointX;
								BPointY = pointY;
								ToastUtil.showToast(getString(R.string.b_point_already_set), true);
								isPointBSet = false;
							}
							// ���÷���ָ��Ϊȷ���յ�B����������
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_SET_B_AFFIRM);
						}

						if (currentState == CAR_LINE_NAVI) {
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
						}
						
//						if (currentState == CAR_LINE_NAVI || currentState == HEADLAND_P1_RESPONSE
//						        || currentState == HEADLAND_P2_RESPONSE) {
//						    myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
//						}

						// �жϴ�ʱ�Ƿ���ת
						if (isToTurnRight && currentState == CAR_TURNING_RIGHT) {
							isToTurnRight = false;
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
						}

						// �жϴ�ʱ�Ƿ���ת
						if (isToTurnLeft && currentState == CAR_TURNING_LEFT) {
							isToTurnLeft = false;
							myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_DEFAULT);
						}

						

						// ���浱ǰ״̬
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
	 *  ��ʼ������������
	 */
	private void startNavi() {
	    isStartNavi = true; //���õ���״̬Ϊ��
        startTimeMillis = System.currentTimeMillis(); //��¼��ʼ����ϵͳʱ�䣨���룩
        mPointValues.clear(); //��������ƫ�����ݼ�������
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_START_NAVI); //�������Ϳ�ʼ����ָ��
        btnStartNavi.setText(R.string.stop_navi);
        findViewById(R.id.btnShowLineChart).setVisibility(View.INVISIBLE);
	}
	
	/**
	 * ֹͣ������������
	 */
	private void stopNavi() {
	    isStartNavi = false; // 
        myApp.getBluetoothService().setCommandType(BluetoothService.COMMAND_STOP_NAVI); //��������ֹͣ����ָ��
        lineChart = new LineChart((LineChartView) findViewById(R.id.lineChartView), mPointValues); //������ƫ�����ݼ���������ͼ
        btnStartNavi.setText(getString(R.string.start_navi));
        findViewById(R.id.btnShowLineChart).setVisibility(View.VISIBLE);
	}
    
	/**
	 *  ��ʼ���ƹ켣��������
	 */
	private void startPlotPath() {
	    isPlotting = true; //���ÿ�ʼ���ƹ켣״̬Ϊ��
	    isDataToSave = true; //���ÿ�ʼ��������״̬Ϊ��
        dataNo = 0; //�����ݵ�������Ϊ��
        myView.drawPath(1, true); //���û��ƹ켣״̬Ϊ��
        currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()); //��ȡ��ǰ����ʱ���ַ���
        fileNameToSave = currentTime + "_" + FILE_NAME; //�������ݱ����ļ���
        btnPlotPath.setText(R.string.stop_plot_path);
	}
	
	/**
	 * ֹͣ���ƹ켣��������
	 */
	private void stopPlotPath() {
	    isPlotting = false;
        myView.drawPath(1, false);
        isDataToSave = false;
        btnPlotPath.setText(getString(R.string.plot_path));
	}
	
	/**
	 * �ж��Ƿ���Ҫ����
	 * �����㷨��ʱ�Ǵ���ģ���Ϊ�����������δ�Խ����߽�һ�������ֱ�ߣ�����ֻ��һ��ת�䣩
	 * @param distance ����߽�ʵʱ����
	 * @param limit �����������
	 * @return
	 */
	public boolean isToTurn(double distance, double limit) {
		return distance > limit && (distance - limit) < 0.1;
	}

}
