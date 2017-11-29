package sjtu.me.tractor.planning;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;
import sjtu.me.tractor.database.DatabaseManager;
import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.gis.GeoLine;
import sjtu.me.tractor.gis.GeoPoint;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.surfaceview.MySurfaceView;
import sjtu.me.tractor.tractorinfo.TractorInfo;
import sjtu.me.tractor.util.ToastUtil;


public class PathPlanningActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "PathPlanningActivity";
    private static final boolean D = true;

    private MyApplication myApp; // 程序全局变量
    private ArrayList<GeoPoint> planningFieldVertices = new ArrayList<>(); // 定义地块顶点数组
    private GeoLine lineAB;
    private double minTurning;
    private double linespacing;

    private MySurfaceView myView;
    private static final int SURFACE_VIEW_WIDTH = 710;
    private static final int SURFACE_VIEW_HEIGHT = 700;
    private String planningFieldName;
    private String planningTractorName;
    private boolean isFieldSet = false;
    private boolean isTractorSet = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.path_planning_activity);

        myApp = (MyApplication) getApplication();
        myView = (MySurfaceView) findViewById(R.id.myView);
        myView.setCanvasSize(SURFACE_VIEW_WIDTH * 2, SURFACE_VIEW_HEIGHT * 2);

        final List<String> fieldList = new ArrayList<>();
        fieldList.add(getString(R.string.spinner_tip));
        List<Map<String, String>> fieldMapList = DatabaseManager.cursorToList(myApp.getDatabaseManager().getFieldsNameSet());
        Collections.reverse(fieldMapList);
        for (Map<String, String> map : fieldMapList) {
            fieldList.add(map.get(FieldInfo.FIELD_NAME));
        }

        final List<String> tractorList = new ArrayList<>();
        tractorList.add(getString(R.string.spinner_tip));
        List<Map<String, String>> tractorMapList = DatabaseManager.cursorToList(myApp.getDatabaseManager().getTractorsNameSet());
        Collections.reverse(tractorMapList);
        for (Map<String, String> map : tractorMapList) {
            tractorList.add(map.get(TractorInfo.TRACTOR_NAME));
        }

        final List<String> abList = new ArrayList<>();
        abList.add(getString(R.string.spinner_tip));
        List<Map<String, String>> abMapList = DatabaseManager.cursorToList(myApp.getDatabaseManager().getAllABlines());
        Collections.reverse(abMapList);
        for (Map<String, String> map : abMapList) {
            abList.add(map.get(ABLine.AB_LINE_NAME_BY_DATE));
        }

        Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        Button btnSwitch = (Button) findViewById(R.id.btnSwitch);
        btnSwitch.setOnClickListener(this);

        myView = (MySurfaceView) findViewById(R.id.myView);

        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fieldList);
        fieldAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        Spinner spField = (Spinner) findViewById(R.id.spField);
        spField.setAdapter(fieldAdapter);
        spField.setSelection(0, true);
        spField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    ToastUtil.showToast("已选择" + fieldList.get(i), true);
                    planningFieldName = fieldList.get(i);
//                    SharedPreferences.Editor editor = myPreferences.edit();
//                    editor.putString(DEFAULT_PLANNING_FIELD, planningFieldName);
//                    editor.commit();

                    Cursor resultCursor = myApp.getDatabaseManager().queryFieldWithPointsByName(planningFieldName);
                    List<Map<String, String>> resultList = DatabaseManager.cursorToList(resultCursor);

                    planningFieldVertices.clear();
                    for (int n = 0; n < resultList.size(); n++) {
                        GeoPoint vertex = new GeoPoint(Double.valueOf(resultList.get(n).get(FieldInfo.FIELD_POINT_X_COORDINATE)),
                                Double.valueOf(resultList.get(n).get(FieldInfo.FIELD_POINT_Y_COORDINATE)));
                        planningFieldVertices.add(vertex);
                    }
                    isFieldSet = myView.setFieldBoundary(planningFieldVertices, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayAdapter<String> TractorAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, tractorList);
        TractorAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        Spinner spTractor = (Spinner) findViewById(R.id.spTractor);
        spTractor.setAdapter(TractorAdapter);
        spTractor.setSelection(0, true);
        spTractor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    ToastUtil.showToast("已选择" + tractorList.get(i), true);
                    planningTractorName = tractorList.get(i);
                    Cursor cursor = myApp.getDatabaseManager().queryTractorByName(planningTractorName);
                    Map<String, String> map = DatabaseManager.cursorToMap(cursor);
                    try {
                        linespacing = Double.parseDouble(map.get(TractorInfo.TRACTOR_OPERATION_LINESPACING));
                        minTurning = Double.parseDouble(map.get(TractorInfo.TRACTOR_MIN_TURNING_RADIUS));
                        isTractorSet = true;
                        Log.e(TAG, "linespace: " + linespacing);
                        Log.e(TAG, "min_turning " + minTurning);
                    } catch (NumberFormatException e) {
                        ToastUtil.showToast("读取作业行间距数字格式错误!", true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> abAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, abList);
        abAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        Spinner spABLine = (Spinner) findViewById(R.id.spABLine);
        spABLine.setAdapter(abAdapter);
        spABLine.setSelection(0, true);
        spABLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    ToastUtil.showToast("已选择" + abList.get(i), true);
                    Cursor cursor = myApp.getDatabaseManager().queryABlineByDate(abList.get(i));
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
                        ToastUtil.showToast("读取作业行间距数字格式错误!", true);
                    }

                }
                if (i == 0) {
                    lineAB = null;
                    myView.drawABline(0, 0, 0, 0, false);
                }
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
                if (isFieldSet && isTractorSet) {
                    PlanningPathGenerator planningPathGenerator =
                            new PlanningPathGenerator(planningFieldVertices, lineAB, linespacing, minTurning);
                    planningPathGenerator.planningField();
                    List<GeoPoint> headland1 = planningPathGenerator.getHeadLand1();
                    List<GeoPoint> headland2 = planningPathGenerator.getHeadLand2();
                    List<GeoLine> plannedPaths = planningPathGenerator.getGeneratedPathList();
                    myView.drawHeadland1(headland1);
                    myView.drawHeadland2(headland2);
                    myView.drawPlannedPath(plannedPaths);
                    Log.e(TAG, "head1.size = : " + headland1.size());
                    Log.e(TAG, "head2.size = : " + headland2.size());
                    Log.e(TAG, "lines.size = : " + plannedPaths.size());


                   /*
                   //测试路径规划功能
                    List<GeoPoint> l = new ArrayList<>();
                    l.add(new GeoPoint(0, 0));
                    l.add(new GeoPoint(0, 100));
                    l.add(new GeoPoint(100, 100));
                    l.add(new GeoPoint(100, 0));
                    GeoPoint p = new GeoPoint(55, 55);
                    GeoPoint p1 = new GeoPoint(0, 55);
                    GeoPoint p2 = new GeoPoint(-5, 55);
                    GeoPoint p3 = new GeoPoint(105, 55);

                    Log.e(TAG, "" + GisAlgorithm.getBoundaryLimits(l)[0]);
                    Log.e(TAG, "" + GisAlgorithm.getBoundaryLimits(l)[1]);
                    Log.e(TAG, "" + GisAlgorithm.getBoundaryLimits(l)[2]);
                    Log.e(TAG, "" + GisAlgorithm.getBoundaryLimits(l)[3]);

                    Log.e(TAG, "in?" + GisAlgorithm.pointInPolygon(l, p));
                    Log.e(TAG, "in?" + GisAlgorithm.pointInPolygon(l, p1));
                    Log.e(TAG, "in?" + GisAlgorithm.pointInPolygon(l, p2));
                    Log.e(TAG, "in?" + GisAlgorithm.pointInPolygon(l, p3));

                    GeoLine ll = new GeoLine(0,0,100,100);
                    GeoLine pl = GisAlgorithm.parallelLine(ll, 141.4);
                    Log.e(TAG, pl.getP1().getX() + "," + pl.getP1().getY());
                    Log.e(TAG, pl.getP2().getX() + "," + pl.getP2().getY());

                    GeoLine l1 = new GeoLine(planningFieldVertices.get(0), planningFieldVertices.get(1));
                    double xx1 = planningFieldVertices.get(0).getX() /2 + planningFieldVertices.get(3).getX()/2;
                    double yy1 = planningFieldVertices.get(0).getY()/2 + planningFieldVertices.get(3).getY()/2;
                    double xx2 = planningFieldVertices.get(2).getX() /2 + planningFieldVertices.get(1).getX()/2;
                    double yy2 = planningFieldVertices.get(2).getY()/2 + planningFieldVertices.get(1).getY()/2;
                    GeoLine l2 = new GeoLine(xx1, yy1, xx2, yy2);

                    double xx3 = xx1 /2 + planningFieldVertices.get(3).getX()/2;
                    double yy3 = yy1/2 + planningFieldVertices.get(3).getY()/2;
                    double xx4 = xx2 /2 + planningFieldVertices.get(2).getX()/2;
                    double yy4 = yy2/2 + planningFieldVertices.get(2).getY()/2;
                    GeoLine l3 = new GeoLine(xx3, yy3, xx4, yy4);
                    GeoLine l4 = new GeoLine(planningFieldVertices.get(3), planningFieldVertices.get(2));
                    List<GeoLine> lines = new ArrayList<>();
                    lines.add(l1);
                    lines.add(l2);
                    lines.add(l3);
                    lines.add(l4);
                    myView.drawPlannedPath(lines);
                    */


                }
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

