package sjtu.me.tractor.planning;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.baidu.mapapi.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.gis.GeoPoint;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.surfaceview.MySurfaceView;
import sjtu.me.tractor.tractorinfo.TractorInfo;
import sjtu.me.tractor.util.ToastUtil;


public class PathPlanningActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "PathPlanningActivity";
    private static final boolean D = true;

    private static final String DEFAULT_PLANNING_FIELD = "default_planning_field";
    private static final String QUERY_ALL = "%%";

    private MyApplication myApp; // 程序全局变量
    private SharedPreferences myPreferences; //默认偏好参数存储实例
    private ArrayList<GeoPoint> fieldVertices = new ArrayList<>(); // 定义地块顶点数组
    private HashMap<Integer, GeoPoint> fieldMap = new HashMap<>();
    private List<LatLng> points = new ArrayList<>();//多边形的点,点的记录
    private List<LatLng> pointsTemporary = new ArrayList<>();//暂时存放的点

    private Button btnBack;//返回按钮
    private Button btnSwitch;//开始停止切换按钮
    private Spinner spField;
    private Spinner spTractor;
    private Spinner spABLine;
    private MySurfaceView myView;
    private static final int SURFACE_VIEW_WIDTH = 710;
    private static final int SURFACE_VIEW_HEIGHT = 700;
    private String planningFieldName;
    private String planningTractorName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.path_planning_activity);
        myApp = (MyApplication) getApplication();
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final List<String> fieldList = new ArrayList<>();
        fieldList.add(getString(R.string.spinner_tip));
        List<Map<String, String>> fieldMapList = DatabaseManager.cursorToList(myApp.getDatabaseManager().getFieldsNameSet());
        for (Map<String, String> map : fieldMapList) {
            fieldList.add(map.get(FieldInfo.FIELD_NAME));
        }

        final List<String> tractorList = new ArrayList<>();
        tractorList.add(getString(R.string.spinner_tip));
        List<Map<String, String>> tractorMapList = DatabaseManager.cursorToList(myApp.getDatabaseManager().getTractorsNameSet());
        for (Map<String, String> map : tractorMapList) {
            tractorList.add(map.get(TractorInfo.TRACTOR_NAME));
        }

        final List<String> abList = new ArrayList<>();
        abList.add(getString(R.string.spinner_tip));
        List<Map<String, String>> abMapList = DatabaseManager.cursorToList(myApp.getDatabaseManager().getAllABlines());
        for (Map<String, String> map : abMapList) {
            abList.add(map.get(ABLine.AB_LINE_NAME_BY_DATE));
        }

        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        btnSwitch = (Button) findViewById(R.id.btnSwitch);
        btnSwitch.setOnClickListener(this);

        myView = (MySurfaceView) findViewById(R.id.myView);

        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fieldList);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spField = (Spinner) findViewById(R.id.spField);
        spField.setAdapter(fieldAdapter);
        spField.setSelection(0, true);
        spField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ToastUtil.showToast(fieldList.get(i), true);
                SharedPreferences.Editor editor = myPreferences.edit();
                editor.putString(DEFAULT_PLANNING_FIELD, planningFieldName);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayAdapter<String> TractorAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, tractorList);
        TractorAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spTractor = (Spinner) findViewById(R.id.spTractor);
        spTractor.setAdapter(TractorAdapter);
        spTractor.setSelection(0, true);
        spTractor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ToastUtil.showToast(tractorList.get(i), true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> abAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, abList);
        abAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spABLine = (Spinner) findViewById(R.id.spABLine);
        spABLine.setAdapter(abAdapter);
        spABLine.setSelection(0, true);
        spABLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ToastUtil.showToast(abList.get(i), true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (D) {
            Log.e(TAG, "*** ON CREATE ***");
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
                    e.printStackTrace();
                }
                break;

            case R.id.btnSwitch:
                break;

            default:
                break;
        }
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "*** ON RESUME ***");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (D) {
            Log.e(TAG, "*** ON PAUSE ***");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (D) {
            Log.e(TAG, "*** ON STOP ***");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出Activity，清除MessageQueue还没处理的消息
    }
}

