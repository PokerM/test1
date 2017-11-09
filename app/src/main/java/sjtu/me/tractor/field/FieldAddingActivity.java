package sjtu.me.tractor.field;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import sjtu.me.tractor.R;
import sjtu.me.tractor.bluetooth.BluetoothService;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.database.MyDatabaseHelper;
import sjtu.me.tractor.gis.GeoPoint;
import sjtu.me.tractor.gis.GisAlgorithm;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.util.FileUtil;
import sjtu.me.tractor.util.ToastUtil;

public class FieldAddingActivity extends Activity implements OnClickListener {

    private static final String TAG = "FieldAddingActivity";
    private static final boolean D = true;

    private static final char END = '*'; // 串口通信字符串结束标志
    private static final char START = '#'; // 串口通信字符串开始标志
    private static final char SEPARATOR = ','; // 分隔符
    private static final String ALBUM_NAME = "AutoTractorData";

    private MyApplication myApp; // 程序全局变量
    private ArrayList<GeoPoint> fieldVertex; // 定义地块顶点数组
    private HashMap<Integer, GeoPoint> fieldMap;
    private GeoPoint currentLocation; // 定义当前位置点
    private int fieldNumber = 100;  //田地编号
    private int fieldVertexNumber = 0;

    private boolean isBoundP1Set = false;
    private boolean isBoundP2Set = false;
    private boolean isBoundP3Set = false;
    private boolean isBoundP4Set = false;

    ImageButton btnExitFieldAdding;  //返回按钮
    private Button btnRectangleField;
    private Button btnPolylineField;
    private Button btnFreeBoundryField;
    private Button btnEnter;
    private Button btnCancel;
    private CheckBox chkBoxFieldTopLeft;
    private CheckBox chkBoxFieldTopRight;
    private CheckBox chkBoxFieldBottomLeft;
    private CheckBox chkBoxFieldBottomRight;
    private TextView txtCoordinateX;
    private TextView txtCoordinateY;
    private TextView txtHint;
    private EditText inputName;

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (D) {
            Log.e(TAG, "*** ON CREATE ***");
        }

        setContentView(R.layout.field_adding_activity);

        fieldVertex = new ArrayList<GeoPoint>();
        fieldMap = new HashMap<Integer, GeoPoint>();

        currentLocation = new GeoPoint();
        
        // 初始化全局变量
        myApp = (MyApplication) getApplication();
        // 设置蓝牙连接的消息处理器为当前界面处理器
        myApp.getBluetoothService().setHandler(mFieldHandler);
        if (D) {
            Log.e(TAG, "+++ setHandler: mFieldHandler +++");
        }

        initViews();

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (D) {
            Log.e(TAG, "*** ON START ***");
        }
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        if (D) {
            Log.e(TAG, "*** ON RESTART ***");
        }

        // 重新进入界面时设置消息处理器为当前处理器
        myApp.getBluetoothService().setHandler(mFieldHandler);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (D) {
            Log.e(TAG, "*** ON RESUME ***");
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (D) {
            Log.e(TAG, "*** ON PAUSE ***");
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (D) {
            Log.e(TAG, "*** ON STOP ***");
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (D) {
            Log.e(TAG, "*** ON DESTROY ***");
        }

        myApp.getDatabaseManager().releaseDataBase();
    }

    private void initViews() {
        btnExitFieldAdding = (ImageButton) findViewById(R.id.btnExitFieldAdding);
        btnExitFieldAdding.setOnClickListener(this);
        
        btnRectangleField = (Button) findViewById(R.id.btnRectangleField);
        btnRectangleField.setOnClickListener(this);

        btnPolylineField = (Button) findViewById(R.id.btnPolylineField);
        btnPolylineField.setOnClickListener(this);

        btnFreeBoundryField = (Button) findViewById(R.id.btnFreeBoundryField);
        btnFreeBoundryField.setOnClickListener(this);

        btnEnter = (Button) findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(this);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        chkBoxFieldTopLeft = (CheckBox) findViewById(R.id.checkBoxFieldTopLeft);
//        chkBoxFieldTopLeft.setOnClickListener(this);
        chkBoxFieldTopLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    isBoundP1Set = true;
                }
            }
        });

        chkBoxFieldTopRight = (CheckBox) findViewById(R.id.checkBoxFieldTopRight);
//        chkBoxFieldTopRight.setOnClickListener(this);
        chkBoxFieldTopRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    isBoundP2Set = true;
                }
            }
        });

        chkBoxFieldBottomRight = (CheckBox) findViewById(R.id.checkBoxFieldBottomRight);
//        chkBoxFieldBottomRight.setOnClickListener(this);
        chkBoxFieldBottomRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    isBoundP3Set = true;
                }
            }
        });

        chkBoxFieldBottomLeft = (CheckBox) findViewById(R.id.checkBoxFieldBottomLeft);
//        chkBoxFieldBottomLeft.setOnClickListener(this);
        chkBoxFieldBottomLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    isBoundP4Set = true;
                }
                Log.e(TAG, "CHECK BOX 4 IS CHECKED");
            }
        });

        txtCoordinateX = (TextView) findViewById(R.id.txtCoordinateX);
        txtCoordinateY = (TextView) findViewById(R.id.txtCoordinateY);
        txtHint = (TextView) findViewById(R.id.txtHint);
        inputName = (EditText) findViewById(R.id.inputName);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.btnExitFieldAdding:
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        
        case R.id.btnEnter:
            String fieldName = inputName.getText().toString();
            if (!TextUtils.isEmpty(fieldName)) {
                if (fieldMap.size() == 4) {
                    fieldNumber++;
                    
                    Log.e(TAG, "P IS SET: " + isBoundP1Set + "," + isBoundP2Set + "," + isBoundP3Set + "," + isBoundP4Set);
                    Log.e(TAG, "FIELD MAP: " + fieldMap);

                    // 自动按时间生成文件名
                    String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                    String fileField = currentTime + "field.txt";
                    // 保存田地顶点坐标到外部文件
                    FileUtil.writeDataToExternalStorage(ALBUM_NAME, fileField,
                            "field:\r\n" + fieldMap.get(1).getXCoordinate() + "," + fieldMap.get(1).getYCoordinate()
                                    + "\r\n" + fieldMap.get(2).getXCoordinate() + "," + fieldMap.get(2).getYCoordinate()
                                    + "\r\n" + fieldMap.get(3).getXCoordinate() + "," + fieldMap.get(3).getYCoordinate()
                                    + "\r\n" + fieldMap.get(4).getXCoordinate() + "," + fieldMap.get(4).getYCoordinate()
                                    + "\r\n",
                            true, false);

                    // 插入地块记录
                    for (int i = 0; i < fieldMap.size(); i++) {
                        // String fNo = Integer.toString(fieldNumber);
                        String fNo = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + fieldNumber;
                        String fName = fieldName;
                        String fDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                        String fPNo = Integer.toString(i + 1);
                        String fPX = Double.toString(fieldMap.get(i + 1).getXCoordinate());
                        String fPY = Double.toString(fieldMap.get(i + 1).getYCoordinate());
                    }

                    // 地块坐标列表清空
                    fieldMap.clear();

                    // 显示提示信息
                    ToastUtil.showToast("Successfully add a new field.", true);
                } else {
                    new AlertDialog.Builder(FieldAddingActivity.this).setTitle("警告").setMessage("请按顺序完成点击四个顶点！")
                            .setIcon(R.drawable.alert).setPositiveButton("确定", null).show();
                }

            } else {
                new AlertDialog.Builder(FieldAddingActivity.this).setTitle("警告").setMessage("请输入地块名字拼音!")
                        .setIcon(R.drawable.alert).setPositiveButton("确定", null).show();
            }
            break;

        case R.id.btnCancel:
            Cursor cursor = myApp.getDatabaseManager().queryFieldByName("");
            Bundle data = new Bundle();
            data.putSerializable("data", DatabaseManager.cursorToList(cursor));
            Intent intent = new Intent("sjtu.me.tractor.fieldsetting.FieldResultActivity");
            intent.putExtras(data);
            startActivity(intent);
            break;

        case R.id.btnPolylineField:
            break;

        case R.id.btnFreeBoundryField:
            break;

//        case R.id.checkBoxFieldTopLeft:
//            if (((CheckBox) v).isChecked()) {
//                isBoundP1Set = true;
//            }
//            break;
//
//        case R.id.checkBoxFieldTopRight:
//            if (((CheckBox) v).isChecked()) {
//                isBoundP2Set = true;
//            }
//            break;
//
//        case R.id.checkBoxFieldBottomRight:
//            if (((CheckBox) v).isChecked()) {
//                isBoundP3Set = true;
//            }
//            break;
//
//        case R.id.checkBoxFieldBottomLeft:
//            if (((CheckBox) v).isChecked()) {
//                isBoundP4Set = true;
//            }
//            break;

        default:
            break;
        }
    }

    public final Handler mFieldHandler = new Handler() {
        StringBuilder readMessageSB = new StringBuilder();

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case BluetoothService.MESSAGE_RECEIVED:
                // 处理接收到的数据
                String dataString = "";
                String message = (String) msg.obj;
                readMessageSB.append(message);

                // 如果 ","的个数不为8，或者字符串开始和结束字符不是指定的，则数据无效
                if (!((MyApplication.countCharacter(readMessageSB, SEPARATOR) == 8)
                        && (readMessageSB.charAt(0) == START)
                        && (readMessageSB.charAt(readMessageSB.length() - 1) == END))) {
                    readMessageSB.delete(0, readMessageSB.length());
                } else {
                    dataString = readMessageSB.substring(1, readMessageSB.length() - 1); // 提取字符串
                    String[] dataArray = dataString.split(",", 9);
                    try {
                        double xCoordinate = Double.parseDouble(dataArray[0]); // x坐标
                        double yCoordinate = Double.parseDouble(dataArray[1]); // y坐标

                        txtCoordinateX.setText(" X坐标:" + xCoordinate);
                        txtCoordinateY.setText(" Y坐标:" + yCoordinate);

                        currentLocation.setXCoordinate(xCoordinate);
                        currentLocation.setYCoordinate(yCoordinate);

                        // 判断是否设置边界点B1
                        if (isBoundP1Set) {
                            fieldMap.put(1, new GeoPoint(currentLocation));
                            ToastUtil.showToast("P1点已设置!", true);
//                            Log.e(TAG, "P1 SET" + isBoundP1Set + fieldMap.get(1).toString());
                            isBoundP1Set = false;
//                            Log.e(TAG, "FIELD MAP: " + fieldMap);
                        }
                        
                        // 判断是否设置边界点B2
                        if (isBoundP2Set) {
                            fieldMap.put(2, new GeoPoint(currentLocation));
//                            Log.e(TAG, "P2 SET " + isBoundP2Set + fieldMap.get(2).toString());
                            ToastUtil.showToast("P2点已设置!", true);
                            isBoundP2Set = false;
//                            Log.e(TAG, "FIELD MAP: " + fieldMap);
                        }
                        
                        // 判断是否设置边界点B3
                        if (isBoundP3Set) {
                            fieldMap.put(3,  new GeoPoint(currentLocation));
//                            Log.e(TAG, "P3 SET " + isBoundP3Set + fieldMap.get(3).toString());
                            ToastUtil.showToast("P3点已设置!", true);
                            isBoundP3Set = false;
//                            Log.e(TAG, "FIELD MAP: " + fieldMap);
                        }

                        // 判断是否设置边界点B4
                        if (isBoundP4Set) {
                            fieldMap.put(4, new GeoPoint(currentLocation));
//                            Log.e(TAG, "P4 SET " + isBoundP4Set + fieldMap.get(4).toString());
                            ToastUtil.showToast("P4点已设置!", true);
                            isBoundP4Set = false;
//                            Log.e(TAG, "FIELD MAP: " + fieldMap);
                        }

                    } catch (NumberFormatException e) {
                        ToastUtil.showToast("传入数据类型错误!", true);
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

}
