package sjtu.me.tractor.main;

import java.lang.String;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.UUID;

import sjtu.me.tractor.R;
import sjtu.me.tractor.bluetooth.DeviceListActivity;
import sjtu.me.tractor.gis.GisAlgorithm;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import sjtu.me.tractor.surfaceview.MySurfaceView;
import sjtu.me.tractor.util.FileUtil;;

@SuppressLint("UseSparseArrays")
public class NaviActivityOldVersion extends Activity implements android.view.View.OnClickListener {

	final int handlerState = 0;
	private static final String TAG = "TRACTOR";
	private static final boolean D = true;
	private static final char END = '*'; // ����ͨ���ַ���������־
	private static final char START = '#'; // ����ͨ���ַ�����ʼ��־
	private static final char SEPARATOR = ','; // �ָ���
	
	private static final int REQUEST_ENABLE_BLUETOOTH = 0;   // ��ת��������������ҳ��ı�־
	private static final int REQUEST_CAR_SETTING = 1; // ��ת����������ҳ��ı�־
	private static final int REQUEST_CONNECT_DEVICE = 2; // ��ת����������ҳ��ı�־
	

	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // ������UUID��
	private static final String FILE_NAME = "data.txt";
	private static final String ALBUM_NAME = "myfiles";
	private static final Double POSITION_OFFSET = 0.01;
	
	// ��������
	
/*	��һ��getNavigationState()��setNavigationState(Int state);*/
	
	public static final String STATE_IDLE = "99999";    
    public static final String SET_A_REQUEST = "40003";
    public static final String SET_A_RESPONSE = "40103";
    public static final String SET_A_RESULT = "40013";
    public static final String SET_B_REQUEST = "40004";
    public static final String SET_B_RESPONSE = "40104";
    public static final String SET_B_RESULT = "40014";
    public static final String HEARTBEAT = "11111";
    

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothDevice mDevice = null;
	private BluetoothSocket btSocket = null;
	private DataRecevingThread mDataReceivingThread;
	private DataSendingThread mDataSendingThread;
	private OutputStream outStream = null;
	private File rootPath = null;

	// the following are some global variables
	private int sendCommand;
	private String readMessage;
	Bundle bundle;
	private int dataNo = 0;
	private int isStop = 0;
	private boolean isDataReceiving = false;
	private double pointX = 0;
	private double pointY = 0;
	private double nextPointX = 0;
	private double nextPointY = 0;
	private double originX, originY, APointX, APointY, BPointX, BPointY;
	private float lineSpace = 0;
	private String fileNameToSave;
	private String currentTime;
	
	private boolean exitCommThread = false;
	private boolean isDataToSave = false;
	private boolean isPlotting = false;
	private boolean isOriginSet = false;
	private boolean isPointASet = false;
	private boolean isPointBSet = false;
	private boolean isBoundP1Set = false;
	private boolean isBoundP2Set = false;
	private boolean isBoundP3Set = false;
	private boolean isBoundP4Set = false;
	private boolean isOnNavigate = false; //����������־
	private boolean isOffNavigate = false; //����������־
	boolean isToTurnRight = false;
	boolean isToTurnLeft = false;

	ImageButton imgbtnF, imgbtnL, imgbtnR, imgbtnB, imgbtnSTOP;
	Button btnConnect, btnStart, btnSetting, btnInitialize, btnDataReceival, btnSetField, btnUseOldField, btnSetOrigin, btnSetA,
			btnSetB, btnDrawAB, btnPlot;
	TextView txtString, txtStringLength, xCoordinateView, yCoordinateView, directionAngleView, carVelocityView, gpsView,
			stopBitView, northAngleView, traceDistanceView, phiAngleView, txtBodyLength, txtAntennaHeight,
			txtDistance2Shaft, txtDistance2Back, txtSpeed, txtSpace, txtDistance2Bound1, txtDistance2Bound2;
	EditText txt_fieldwidth, txt_fieldlength;
	MySurfaceView myView;
	Handler bluetoothIn;
	ArrayList<Double> pathListX, pathListY;
	// ArrayList<Double> boundListX,boundListY;
	HashMap<Integer, Double> boundMapX, boundMapY;
	
	HashMap<Integer, Double> boundTempMapX, boundTempMapY;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navi_activity_old);
		
		pathListX = new ArrayList<Double>();
		pathListY = new ArrayList<Double>();
		// boundListX = new ArrayList<Double>();
		// boundListY = new ArrayList<Double>();
		boundMapX = new HashMap<Integer, Double>();
		boundMapY = new HashMap<Integer, Double>();
		
		boundTempMapX = new HashMap<Integer, Double>();
		boundTempMapY = new HashMap<Integer, Double>();
		

		txtString = (TextView) findViewById(R.id.txtString);
		txtStringLength = (TextView) findViewById(R.id.txtStringLength);
		xCoordinateView = (TextView) findViewById(R.id.xCoordinate);
		yCoordinateView = (TextView) findViewById(R.id.yCoordinate);
		directionAngleView = (TextView) findViewById(R.id.directionAngle);
		carVelocityView = (TextView) findViewById(R.id.carVelocity);
		gpsView = (TextView) findViewById(R.id.gps);
		stopBitView = (TextView) findViewById(R.id.stopBit);
		northAngleView = (TextView) findViewById(R.id.northAngle);
		traceDistanceView = (TextView) findViewById(R.id.traceDistance);
		phiAngleView = (TextView) findViewById(R.id.phiAngle);
		txtBodyLength = (TextView) findViewById(R.id.txtBodyLength);
		txtAntennaHeight = (TextView) findViewById(R.id.txtAntennaHeight);
		txtDistance2Shaft = (TextView) findViewById(R.id.txtDistance2Shaft);
		txtDistance2Back = (TextView) findViewById(R.id.txtDistance2Back);
		txtSpeed = (TextView) findViewById(R.id.txtSpeed);
		txtSpace = (TextView) findViewById(R.id.txtSpace);
		txt_fieldwidth = (EditText) findViewById(R.id.editTextFieldWidth);
		txt_fieldlength = (EditText) findViewById(R.id.editTextFieldLength);
		
		txtDistance2Bound1 = (TextView) findViewById(R.id.txtDistance2Bound1);
		txtDistance2Bound2 = (TextView) findViewById(R.id.txtDistance2Bound2);
		
		myView = (MySurfaceView) findViewById(R.id.mySurfaceView);
		myView.setCanvasSize(1200, 1200);
		
		// ---CheckBox---
		CheckBox checkBox1 = (CheckBox) findViewById(R.id.starTopLeft);
		checkBox1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((CheckBox) v).isChecked())
					isBoundP1Set = true;
			}
		});

		CheckBox checkBox2 = (CheckBox) findViewById(R.id.starTopRight);
		checkBox2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((CheckBox) v).isChecked())
					isBoundP2Set = true;
				writeDataToOutputStream("#0,50001,*\n");

			}
		});

		CheckBox checkBox3 = (CheckBox) findViewById(R.id.starBottomRight);
		checkBox3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((CheckBox) v).isChecked())
					isBoundP3Set = true;
				writeDataToOutputStream("#0,50002,*\n");

			}
		});

		CheckBox checkBox4 = (CheckBox) findViewById(R.id.starBottomLeft);
		checkBox4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((CheckBox) v).isChecked())
					isBoundP4Set = true;
			}
		});

		// receive the datum from the Bluetooth ReceivingThread
		bluetoothIn = new Handler() {
			StringBuilder recDataString = new StringBuilder();
			double formerDidstance = 0;

			public void handleMessage(android.os.Message msg) {
				if (msg.what == handlerState) { // if message is what we want
					readMessage = (String) msg.obj; // msg.arg1 = bytes from
													// receiving thread
					recDataString.append(readMessage);

					// ��� ","�ĸ�����Ϊ8�������ַ�����ʼ�ͽ����ַ�����ָ���ģ���������Ч
					if (!((countCharacter(recDataString, SEPARATOR) == 8) && (recDataString.charAt(0) == START)
							&& (recDataString.charAt(recDataString.length() - 1) == END)))
						recDataString.delete(0, recDataString.length());
					else
						dataNo++;

					int endOfLineIndex = recDataString.indexOf(String.valueOf(END)); // �ҵ�������ʶ��
					if (endOfLineIndex > 0) {
						String dataInPrint = recDataString.substring(1, endOfLineIndex); // ��ȡ�ַ���

						// ���յ������ݱ��浽�ⲿ�洢��
						if (isDataToSave) {
							FileUtil.writeDataToExternalStorage(ALBUM_NAME, fileNameToSave, dataInPrint, true,
									true);
						}

						// set the TextView with the received data
						txtString.setText("�������ݣ�" + dataInPrint);
						int dataLength = dataInPrint.length(); // get length of
																// data received
						txtStringLength.setText("���ݳ��ȣ� " + String.valueOf(dataLength) + "  ������ţ�" + dataNo);

						String[] dataArray = dataInPrint.split(",", 9);
						try {
							double xCoordinate = Double.parseDouble(dataArray[0]);  // x����
							double yCoordinate = Double.parseDouble(dataArray[1]);  // y����																				// again...
							int GPS = Integer.parseInt(dataArray[2]); 
							double northAngle = Double.parseDouble(dataArray[3]);
							double carVelocity = Double.parseDouble(dataArray[4]);
							int stopBit = Integer.parseInt(dataArray[5]);
							double directionAngle = Double.parseDouble(dataArray[6]);
							double traceDistance = Double.parseDouble(dataArray[7]);
							double phiAngle;
							if ("nan".equalsIgnoreCase(dataArray[8])) {
								phiAngle = 99999.999;
							} else {
								phiAngle = Double.parseDouble(dataArray[8]);
							}

							xCoordinateView.setText(" X����:" + xCoordinate);
							yCoordinateView.setText(" Y����:" + yCoordinate);
							gpsView.setText("GPS:" + GPS);
							directionAngleView.setText("�����:" + directionAngle);
							carVelocityView.setText("�ٶ�:" + carVelocity + " km/h");
							stopBitView.setText("ֹͣλ:" + stopBit);
							northAngleView.setText("�汱����:" + northAngle);
							traceDistanceView.setText("����:" + traceDistance);
							phiAngleView.setText("�н�:" + phiAngle);

							// ���Ƶ�ǰ��
							pointX = xCoordinate;
							pointY = yCoordinate;
							if ((pointX - nextPointX) * (pointX - nextPointX)
									+ (pointY - nextPointY) * (pointY - nextPointY) >= POSITION_OFFSET
											* POSITION_OFFSET) {
								myView.setCurentPoint(dataNo, pointX, pointY);
							}
							// Log.e(TAG, "handler������");
							
							double distance1 = GisAlgorithm.distanceFromPointToLine(622420.828635, 3423930.259849,
							        622422.2437, 3423929.17782, pointX, pointY);
							double distance2 = GisAlgorithm.distanceFromPointToLine(622423.89173, 3424003.981122,
							        622446.48542, 3424005.692487, pointX, pointY);
							txtDistance2Bound1.setText("����1��" + distance1);
							txtDistance2Bound2.setText("����2��" + distance2);
							
							if (dataNo % 20 == 0) formerDidstance = distance1;
							
							
							if (stopBit == 10020) {
							    isToTurnRight = false;
	                            sendCommand = 20;
							}
                                
							
							 if (stopBit == 10030) {
							     isToTurnLeft = false;
							     sendCommand = 20;
							 }
							 
							
//							if (isOnNavigate == true && stopBit != 10020
//							        && distance1 - 10 <= 0.25 && distance1 < formerDidstance) {
//								isToTurnRight = true;
//								sendCommand = 7;
//							}
//							
//							if (isOnNavigate == true && stopBit != 10030
//							        && distance2 - 10 <= 0.25 && distance1 > formerDidstance) {
//							    isToTurnLeft = true;
//							    sendCommand = 8;
//							}

							// �жϴ�ʱ�Ƿ�������A��
//							if (isPointASet == true && stopBit == 40103) {
							if (stopBit == 40103) {
								if (isStop != 40103) {
									APointX = pointX;
									APointY = pointY;
									displayToast("A��������!");
									isPointASet = false;
								}
								sendCommand = 5;
//								writeDataToOutputStream("#0,40013,*\n");
								Log.e(TAG, "A received");
							}

							//�жϴ�ʱ�Ƿ�������B��
//							if (isPointBSet == true && stopBit == 40104) {
							if (stopBit == 40104) {
								if (isStop != 40014) {
									BPointX = pointX;
									BPointY = pointY;
									displayToast("B��������!");
									isPointBSet = false;
								}
								sendCommand = 6;
//								writeDataToOutputStream("#0,40014,*\n");
								Log.e(TAG, "B received");
							}

							isStop = stopBit;

							// �ж��Ƿ����ñ߽��B1
							if (isBoundP1Set) {
								boundMapX.put(1, pointX);
								boundMapY.put(1, pointY);
								Log.e("P1", boundMapX.get(1) + "," + boundMapY.get(1)); // ���P1���귽�����
								displayToast("P1��������!");
								isBoundP1Set = false;
							}

							// �ж��Ƿ����ñ߽��B2
							if (isBoundP2Set) {
								boundMapX.put(2, pointX);
								boundMapY.put(2, pointY);
								Log.e("P2", boundMapX.get(2) + "," + boundMapY.get(2)); // ���P2���귽�����
								displayToast("P2��������!");
								isBoundP2Set = false;
							}

							// �ж��Ƿ����ñ߽��B3
							if (isBoundP3Set) {
								boundMapX.put(3, pointX);
								boundMapY.put(3, pointY);
								Log.e("P3", boundMapX.get(3) + "," + boundMapY.get(3)); // ���P3���귽�����
								displayToast("P3��������!");
								isBoundP3Set = false;
							}

							// �ж��Ƿ����ñ߽��B4
							if (isBoundP4Set) {
								boundMapX.put(4, pointX);
								boundMapY.put(4, pointY);
								Log.e("P4", boundMapX.get(4) + "," + boundMapY.get(4)); // ���P4���귽�����
								displayToast("P4��������!");
								isBoundP4Set = false;
							}

						} catch (NumberFormatException e) {
							displayToast("�����������ʹ���!");
						}

						// clear all string data
						recDataString.delete(0, recDataString.length());
					}
				}
			}
		};

		// forward
		imgbtnF = (ImageButton) findViewById(R.id.imgbtnF);
		imgbtnB = (ImageButton) findViewById(R.id.imgbtnB);
		imgbtnL = (ImageButton) findViewById(R.id.imgbtnL);
		imgbtnR = (ImageButton) findViewById(R.id.imgbtnR);
		btnDataReceival = (Button) findViewById(R.id.btnDataReceival);
		btnStart = (Button) findViewById(R.id.btnStart);
		imgbtnSTOP = (ImageButton) findViewById(R.id.imgbtnSTOP);
		btnSetting = (Button) findViewById(R.id.btnSetting);
		btnInitialize = (Button) findViewById(R.id.btnInitialize);
		btnSetField = (Button) findViewById(R.id.btnSetField);
		btnUseOldField = (Button) findViewById(R.id.btnUseOldField);
		btnSetOrigin = (Button) findViewById(R.id.btnSetOrigin);
		btnSetA = (Button) findViewById(R.id.btnSetA);
		btnSetB = (Button) findViewById(R.id.btnSetB);
		btnDrawAB = (Button) findViewById(R.id.btnDrawAB);
		btnPlot = (Button) findViewById(R.id.btnPlotPath);

		imgbtnF.setOnClickListener(this);
		imgbtnB.setOnClickListener(this);
		imgbtnL.setOnClickListener(this);
		imgbtnR.setOnClickListener(this);
		btnDataReceival.setOnClickListener(this);
		btnStart.setOnClickListener(this);
		imgbtnSTOP.setOnClickListener(this);
		btnSetting.setOnClickListener(this);
		btnInitialize.setOnClickListener(this);
		btnSetField.setOnClickListener(this);
		btnUseOldField.setOnClickListener(this);
		btnSetOrigin.setOnClickListener(this);
		btnSetA.setOnClickListener(this);
		btnSetB.setOnClickListener(this);
		btnDrawAB.setOnClickListener(this);
		btnPlot.setOnClickListener(this);

		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// ��ȡ�����豸������
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// ����豸�Ƿ���������豸
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "This device does not support Bluetooth!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// �����豸����
		new Thread() {
			public void run() {
				if (mBluetoothAdapter.isEnabled() == false) {
					mBluetoothAdapter.enable();
		            displayToast("���ڴ�����...");
				}
			}
		}.start();

		if (D) {
			Log.e(TAG, "+++ DONE IN ON CREATE, GOT LOCAL BT ADAPTER +++");
		}
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.imgbtnF:
			// writeDataToOutputStream("#0,20001,*\n");
			break;

		case R.id.imgbtnB:
			// writeDataToOutputStream("#0,20002,*\n");
			break;

		case R.id.imgbtnL:
		    isToTurnLeft = true;
//			writeDataToOutputStream("#0,30002,*\n");
		    sendCommand = 8;
			break;

		case R.id.imgbtnR:
		    isToTurnRight = true;
//			writeDataToOutputStream("#0,30001,*\n");
		    sendCommand = 7;
			break;

		case R.id.btnStart:
			isOnNavigate = true;
			isOffNavigate = false;
			Log.e(TAG, "start button");
//			writeDataToOutputStream("#0,10000,*\n");
			sendCommand = 1;
			break;

		case R.id.imgbtnSTOP:
		    isOffNavigate = true;
		    isOnNavigate = false;
//			writeDataToOutputStream("#0,20000,*\n");
		    sendCommand = 2;
			break;

		case R.id.btnSetting:
			Intent intent2 = new Intent("sjtu.me.tractor.car.setting.CarSetting");
			startActivityForResult(intent2, 1);
			break;

		case R.id.btnInitialize:
			if (bundle != null) {
				float[] settingDataArray = bundle.getFloatArray("settingData");
				for (int i = 0; i < settingDataArray.length; i++) {
					String mMessage = "#3" + i + String.format("%04.1f", settingDataArray[i]) + "*\r\n";
					writeDataToOutputStream(mMessage);
				}
			}
			break;

		case R.id.btnDataReceival:
			if (isDataReceiving == false) {
				isDataReceiving = true;
				writeDataToOutputStream("#0,10001,*\n");
				btnDataReceival.setText("��ͣ����");
			} else {
				isDataReceiving = false;
				writeDataToOutputStream("#0,20001*\n");
				btnDataReceival.setText("��������");
			}
			break;

		case R.id.btnSetOrigin:
			// isOriginSet = true;
			break;

		case R.id.btnSetField:
			// if (txt_fieldwidth!=null&&txt_fieldlength!=null)
			// myView.setMyViewSize(Float.parseFloat(txt_fieldwidth.getText().toString()),
			// Float.parseFloat(txt_fieldlength.getText().toString()));
			if (boundMapX.size() == 4 && boundMapY.size() == 4) {
				myView.setViewFieldBoundary(boundMapX, boundMapY, true);
				currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
				String fileField = currentTime + "field.txt";
				// ������ض������굽�ⲿ�ļ�
				FileUtil.writeDataToExternalStorage(ALBUM_NAME, fileField,
						"field:\r\n" + boundMapX.get(1) + "," + boundMapY.get(1) + "\r\n" + boundMapX.get(2) + ","
								+ boundMapY.get(2) + "\r\n" + boundMapX.get(3) + "," + boundMapY.get(3) + "\r\n"
								+ boundMapX.get(4) + "," + boundMapY.get(4) + "\r\n",
						true, false);
			}
			break;
			
		case R.id.btnUseOldField:
			boundTempMapX.put(1, 622442.2437);
    		boundTempMapX.put(2, 622420.828635);
    		boundTempMapX.put(3, 622423.89173);
    		boundTempMapX.put(4, 622446.48542);
    		boundTempMapY.put(1, 3423929.17782);
    		boundTempMapY.put(2, 3423930.259849);
    		boundTempMapY.put(3, 3424003.981122);
    		boundTempMapY.put(4, 3424005.692487);
			myView.setViewFieldBoundary(boundTempMapX, boundTempMapY, true);
			break;

		case R.id.btnSetA:
			//writeDataToOutputStream("#0,40003,*\n");
			isPointASet = true;
			isPointBSet = false;
			sendCommand = 3;
			Log.e(TAG, "A button");
			break;

		case R.id.btnSetB:
			writeDataToOutputStream("#0,40004,*\n");
			isPointBSet = true;
			isPointASet = false;
			sendCommand = 4;
			Log.e(TAG, "B button");
			break;

		case R.id.btnDrawAB:
			if (APointY == BPointY && APointX == BPointX) {
				displayToast("B�㲻�ܺ�A���غ�!����������B��!");
				return;
			} else
				myView.drawABline(APointX, APointY, BPointX, BPointY, true);
				currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
				String fileABPoints = currentTime + "_AB_Points.txt";
				// ������ض������굽�ⲿ�ļ�
				FileUtil.writeDataToExternalStorage(ALBUM_NAME, fileABPoints,
						"ABpoints:\r\n" + APointX + "," + APointY + "\r\n" + BPointX + "," + BPointX, true, false);
			break;

		case R.id.btnPlotPath:
			btnPlot = (Button) findViewById(R.id.btnPlotPath);
			if (isPlotting == false) {
				isPlotting = true;
				dataNo = 0;
				myView.drawPath(1, true);
				currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
				fileNameToSave = currentTime + "_" + FILE_NAME;
				
				isDataToSave = true;
				btnPlot.setText("ֹͣ����");
			} else {
				isPlotting = false;
				myView.drawPath(1, false);
				isDataToSave = false;
				btnPlot.setText("���ƹ켣");
			}

			break;

		default:
			break;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BLUETOOTH:
		    if (resultCode == RESULT_OK) {
		        displayToast("�豸�����Ѿ��򿪣�");
		    } else {
		        displayToast("����δ�򿪣��޷����ӣ�");
		    }
		    break;
		
		case REQUEST_CAR_SETTING:
			if (resultCode == RESULT_OK) {
				bundle = data.getExtras();
				float[] settingDataArray = bundle.getFloatArray("settingData");
				if (settingDataArray.length == 6) {
					txtBodyLength.setText("������:" + settingDataArray[0] + "mm");
					txtAntennaHeight.setText("���߰�װ�߶�" + settingDataArray[1] + "mm");
					txtDistance2Shaft.setText("���ߵ��������:" + settingDataArray[2] + "mm");
					txtDistance2Back.setText("���ߵ����־���:" + settingDataArray[3] + "mm");
					txtSpeed.setText("�����н��ٶ�:" + settingDataArray[4] + "km/h");
					txtSpace.setText("�м��:" + settingDataArray[5] + "m");
					
					lineSpace = (float) settingDataArray[5];
				} else
					displayToast("��������в������룡");
			}
			break;
			
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == RESULT_OK) {
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				if (address != null) {
					mDevice = mBluetoothAdapter.getRemoteDevice(address);
					BluetoothSocket tmp = null;
					try {
						tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
					} catch (IOException e) {
						displayToast("�޷�����RfcommSocket!");
					}
					btSocket = tmp;
					if (btSocket != null) {
						displayToast("�ɹ�����RfcommSocket!");
					}

					btnConnect = (Button) findViewById(R.id.btnConnect);
					try {
						btSocket.connect();
						displayToast(mDevice.getName() + "�Ѿ��ɹ�����!���Դ������ݣ�");
						btnConnect.setText("�Ͽ�����");
						btnConnect.setBackgroundResource(R.drawable.disconnect);
					} catch (IOException connectException) {
						displayToast(mDevice.getName() + "δ���ӳɹ�!���ܴ�������!");
						try {
							btSocket.close();
							btSocket = null;
						} catch (IOException closeException) {
							displayToast("�����޷��ر�!");
						}
						return;
					}

					mDataReceivingThread = new DataRecevingThread(btSocket);
					mDataReceivingThread.start();

					mDataSendingThread = new DataSendingThread(btSocket);
					mDataSendingThread.start();

				} else {
					displayToast("������ѡ��һ�������豸�������ӣ�");
				}
			}

			break;
		default:
			break;
		}
	}

	public void onConnectClicked(View v) {
		if (mBluetoothAdapter.isEnabled() == false) {
		    new Thread() {
	            public void run() {
	                if (mBluetoothAdapter.isEnabled() == false) {
	                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	                    startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
	                }
	            }
	        }.start();
		} else {
		    btnConnect = (Button) findViewById(R.id.btnConnect);
	        if (btSocket == null) {
	            exitCommThread = false; // make sure that the connected thread can
	                                    // keep running
	            Intent serverIntent = new Intent(this, DeviceListActivity.class);
	            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	        } else {
	            try {
	                btSocket.close();
	                btSocket = null;
	                btnConnect.setText("������");
	                btnConnect.setBackgroundResource(R.drawable.connect);
	                exitCommThread = true; // ֹͣ�����߳�
	            } catch (IOException e) {
	            }
	        }
		}
		
		return;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) {
			Log.e(TAG, "++ ON START ++");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (D) {
			Log.e(TAG, "+ ON RESUME +");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				Log.e(TAG, "ON PAUSE: Couldn't flush output stream.", e);
			}
		}
		if (D) {
			Log.e(TAG, "- ON PAUSE -");
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D) {
			Log.e(TAG, "-- ON STOP --");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// �ر�socket
		if (btSocket != null)
			try {
				btSocket.close();
				btSocket = null;
				btnConnect.setText("������");
			} catch (IOException e) {
			}
//		// �ر�����
//		mBluetoothAdapter.disable();
		if (D) {
			Log.e(TAG, "--- ON DESTROY ---");
		}
	}

	/**
	 * Write data to outputStream via bluetooth socket
	 * @param str
	 */
	public void writeDataToOutputStream(String str) {
		String message;
		byte[] msgBuffer;
		if (btSocket != null) {
			try {
				outStream = btSocket.getOutputStream();

			} catch (IOException e) {
				Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
			}
			message = str;
			msgBuffer = message.getBytes();
			try {
				outStream.write(msgBuffer);
			} catch (IOException e) {
				Log.e(TAG, "ON RESUME: Exception during write.", e);
			}
		}
	}

	/* Count the specified Character in a string */
	public int countCharacter(StringBuilder str, char ch) {
		int count = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ch)
				count++;
		}
		return count;
	}

	/* Display a Toast prompt with words specified by the string */
	public void displayToast(String str) {
		Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, 220);
		toast.show();
	}

	/* thread for receiving data */
	private class DataRecevingThread extends Thread {
		private final InputStream mmInStream; // �������������ֽ���

		public DataRecevingThread(BluetoothSocket socket) {
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				// create I/O streams for receiving data
				tmpIn = socket.getInputStream();
			} catch (IOException e) {
				Log.e(TAG, "ReceivingThread cannot get the InputStream!");
			}
			mmInStream = tmpIn;
		}

		public void run() {

			Log.e(TAG, "DataRecevingData is started!");

			byte[] readBuffer = new byte[1024];
			int bytes = 0;
			StringBuilder readMsg = new StringBuilder();

			// keep looping to listen for received message
			while (!exitCommThread) {
				try {
					/*
					 *��������ģ�鷢������������ѭ����i��Buffer��������
					 *USB����ģ�����ʿ죬���԰Ѳ���Ҫѭ��
					 */
					for (int i = 0; i < 1; i++) {
						/*
						 * Read bytes from input buffer.
						 * �������������ж�ȡ�ֽ�������
						 */
						bytes = mmInStream.read(readBuffer); 
						String readMessage = new String(readBuffer, 0, bytes);
						
						/*
						 * �����ݵķְ�Э��
						 * ��������������� ���ݷֶΣ�����ճ������ʧ��ʼ������ʧ����������
						 * ��һ�����ѻ����ж����ķֶε�����ƴ����һ��
						 * �����յ��Ŀ����ǡ�#012���͡�3456789*�����Σ���Ҫƴ����һ��
						 */
						readMsg = readMsg.append(readMessage);
					}
					
//					Log.e("received_data", readMsg.toString());
					
					/*
					 *��֤�����ַ����ԡ�#����ͷ�����������ɾ����#��ǰ���ַ�
					 *���硰9*#01����Ϊ��#012��
					 */
					if (readMsg.indexOf("#") > 0) {
						readMsg.delete(0, readMsg.indexOf("#"));
					}
					
					/*
					 *ɾ����*���ڡ�#��֮ǰ���ַ����еĶ����ַ�����
					 *������ʧ��ʼ�������ݣ�
					 *���硰6789*#0123456789*����Ϊ��#0123456789*��
					 */
					if (readMsg.indexOf("*") >= 0 && readMsg.indexOf("*") < readMsg.indexOf("#")) {
						readMsg.delete(0, readMsg.indexOf("*") + 1);
					}
					
					/*
					 *ȡ�������ַ�����
					 *ѭ����ȡ�ַ�������ȡ����#���͡�*��֮�������
					 */
					while (readMsg.indexOf("#") >= 0 && readMsg.indexOf("*") > 0 
							&& readMsg.indexOf("#") < readMsg.indexOf("*")) {
						bluetoothIn.obtainMessage(handlerState, bytes, -1,
										readMsg.substring(readMsg.indexOf("#"), readMsg.indexOf("*") + 1)).sendToTarget();
						readMsg.delete(readMsg.indexOf("#"), readMsg.indexOf("*") + 1);
					}
					
					/*
					 *��֤�����ʣ���ַ����ԡ�#����ͷ�����������ɾ����#��ǰ���ַ�
					 *���硰9*#01����Ϊ��#012��
					 *ɾ�����һ����#���ַ�ǰ�����ַ�
					 *���硰#12345#12345#12345����Ϊ��#12345��
					 *��֤������ʣ���ַ���ֻ��һ����#�������ⶪʧ��*���ŵ�����һֱ������readMsg��
					 */
					if (readMsg.indexOf("#") >= 0) {
						readMsg.delete(0, readMsg.lastIndexOf("#"));
					}
					
				} catch (IOException e) {
					Log.e(TAG, "Reading Exception!");
				}
			}
		}
	}

	/* Create a thread to keep sending data to the lowwer computer. */
	private class DataSendingThread extends Thread {
		private final OutputStream mmOutStream;

		public DataSendingThread(BluetoothSocket socket) {
			OutputStream tmpOut = null;
			try {
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "SendingThread cannot get the OuputStream!");
			}
			mmOutStream = tmpOut;
		}

		public void run() {

			Log.e(TAG, "DataSendingThread is started!");
			
            String Command = "11111";
			String sendMessage;
			byte[] writeBuffer;

			while (!exitCommThread) {
			    
			    switch (sendCommand) {
			    case 1:
			        Command = "10000";
			        break;
			    case 2:
			        Command = "20000";
			        break;
			    case 3:
			        Command = "40003";
			        break;
			    case 4:
			        Command = "40004";
			        break;
			    case 5:
			        Command = "40013";
			        break;
			    case 6:
			        Command = "40014";
			        break;
			    case 7:
			        Command = "30001";
			        break;
			    case 8:
			        Command = "30002";
			        break;
			    case 20:
			        Command = "11111";
			        break;
                default:
                    Command = "11111";
                    break;
			    }
				
				sendMessage = "#0," + Command + ",*\n";
				writeBuffer = sendMessage.getBytes();
				
				long start = System.currentTimeMillis();

				try {
					mmOutStream.flush();
					mmOutStream.write(writeBuffer);
					mmOutStream.flush();
				} catch (IOException e) {
					Log.e(TAG, "Writing Exception!");
				}

				long end = System.currentTimeMillis();

				if (end - start < 250) {
					try {
						Thread.sleep(250 - end + start);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
}
