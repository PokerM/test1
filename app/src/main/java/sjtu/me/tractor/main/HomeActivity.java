package sjtu.me.tractor.main;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import sjtu.me.tractor.R;
import sjtu.me.tractor.connection.ConnectionFragment;
import sjtu.me.tractor.database.MyDatabaseHelper;
import sjtu.me.tractor.field.FieldSettingFragment;
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.tractorinfo.TractorSettingFragment;
import sjtu.me.tractor.utils.FileUtil;
import sjtu.me.tractor.utils.SysUtil;
import sjtu.me.tractor.utils.ToastUtil;

/**
 * @author billhu 程序主界面和功能区
 */
public class HomeActivity extends Activity implements OnClickListener {

    private static final String TAG = "HomeActivity";
    private static final boolean D = true; // 日志入口开关

    private boolean isExitApplication = false;  //是否退出应用程序标志

    private FragmentManager mFragmentManager; //Fragment管理器
    private FragmentTransaction mFragmentTransaction;
    private ConnectionFragment mConnectionFragment;
    private TractorSettingFragment mTractorSettingFragment;
    private FieldSettingFragment mFieldSettingFragment;

    private ImageButton btnBack;
    private Button btnConnectionConfig;
    private Button btnTractorSetting;
    private Button btnFieldSetting;
    private Button btnNavigation;
    private Button btnPathPlanning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d(TAG,"onCreate");
        setContentView(R.layout.home_activity);
        initViews();
        mFragmentManager = getFragmentManager();
        selectFragment(0);

//       /* *******************************************测试数据库用*********************************************/
//        for (int i = 0; i < 5; i++) {
//            ((MyApplication) getApplication()).getDatabaseManager().insertABline("20171124_" + i, new ABLine(622518.64095, 3423839.105289,
//                    622518.641109, 3423839.108775), "songjiang001");
//        }
//
//        for (int i = 0; i < 5; i++) {
//            ((MyApplication) getApplication()).getDatabaseManager().insertHistoryEntry("20171124_" + i, "songjiang001");
//        }

//        for (int i = 0; i < 5; i++) {
//            ((MyApplication) getApplication()).getDatabaseManager().deleteABLine("20171124_" + i);
//        }
//
//        for (int i = 0; i < 5; i++) {
//            ((MyApplication) getApplication()).getDatabaseManager().deleteHistoryEntries("20171124_" + i);
//        }

//        ((MyApplication) getApplication()).getDatabaseManager().insertHistoryEntry("data_20171120_145451.txt", "SONGJIANG_S01");
//        ((MyApplication) getApplication()).getDatabaseManager().deleteHistoryEntries("data_20171120_145451.txt");
//        ((MyApplication) getApplication()).getDatabaseManager().insertABline("20171202_161218_ab", new ABLine(622495.894255, 3423831.776486, 622489.26359, 3423831.763582), "SONGJIANG_SO2");
//       /* *******************************************测试数据库用*********************************************/

        if (D) {
            Log.e(TAG, "+++ ON CREATE +++");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (D) {
            Log.d(TAG, "+++ ON START +++");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /*
         * 为了省电和防止后台通信线程长期运行，可以在程序进入后台运行时关闭通信；
         * 在restart中重启通信，这样会降低程序启动速度。
         */
//        MyApplication myApp = (MyApplication) getApplication();
//        if (myApp.getBluetoothService().getState() == BluetoothService.STATE_NONE) {
//            myApp.getBluetoothService().startConnection(myApp.getBluetoothAddress());
//        }

        if (D) {
            Log.d(TAG, "+++ ON RESTART +++");
        }
    }

    @Override
    protected void onStop() {
        /*
        * 为了省电和防止后台通信线程长期运行，可以在程序进入后台运行时关闭通信；
        * 在restart中重启通信，这样会降低程序启动速度。
        */
        if (!SysUtil.isAppOnForeground(this)) {
            Log.d("APP", "ON BACK GROUDN");
//		    MyApplication myApp = (MyApplication) getApplication();
//		    if (myApp.getBluetoothService().getState() != BluetoothService.STATE_NONE) {
//		        myApp.getBluetoothService().stopConnection();
//		    }
        }

        super.onStop();

        if (D) {
            Log.e(TAG, "+++ ON STOP +++");
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (D) {
            Log.d(TAG, "+++ ON RESUME +++");
        }
    }

    @Override
    protected void onPause() {

        super.onPause();

        if (D) {
            Log.d(TAG, "+++ ON PAUSE +++");
        }
    }

    @Override
    protected void onDestroy() {

        /* 拷贝数据库文件到外部目录 */
        new Thread("copy db thread") {
            @Override
            public void run() {
                String dbPath = getApplication().getDatabasePath(MyDatabaseHelper.DB_NAME).toString();
                FileUtil.copyDbFilesToExternalStorage(dbPath);
            }
        }.start();

        if (D) {
            Log.d(TAG, "+++ ON DESTROY +++");
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnectionConfig:
                selectFragment(0);
                break;

            case R.id.btnTractorSetting:
                selectFragment(1);
                break;

            case R.id.btnFieldSetting:
                selectFragment(2);
                break;

            case R.id.btnPathPlanning:
                Intent intent2 = new Intent("sjtu.me.tractor.planning.PathPlanningActivity");
                startActivityForResult(intent2, 1);
                break;

            case R.id.btnNavigation:
                startActivity(new Intent("sjtu.me.tractor.navigation.NavigationActivity"));
                break;

            case R.id.btnBack:
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    //对返回键设置事件，实现弹出“再按一次退出程序”的效果
    @SuppressWarnings("deprecation")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 判断此时的Activity是否为MainActivity，若是则执行退出程序的操作
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            String thisActivity = am.getRunningTasks(1).get(0).topActivity.getClassName();
            if ("sjtu.me.tractor.main.HomeActivity".equals(thisActivity)) {
                if (isExitApplication == false) {
                    isExitApplication = true;
                    ToastUtil.showToast(getString(R.string.touch_again_and_exit), true);
                    new Timer().schedule(new TimerTask() {
                        // 新建时间表，过2000分钟自动将退出标志设置为false
                        @Override
                        public void run() {
                            isExitApplication = false;
                        }
                    }, 2000);
                } else {
                    finish();
                }
            } else {
                this.finish();
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 1:
                break;

            default:
                break;
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        btnConnectionConfig = (Button) findViewById(R.id.btnConnectionConfig);
        btnConnectionConfig.setOnClickListener(this);

        btnTractorSetting = (Button) findViewById(R.id.btnTractorSetting);
        btnTractorSetting.setOnClickListener(this);

        btnFieldSetting = (Button) findViewById(R.id.btnFieldSetting);
        btnFieldSetting.setOnClickListener(this);

        btnNavigation = (Button) findViewById(R.id.btnNavigation);
        btnNavigation.setOnClickListener(this);

        btnPathPlanning = (Button) findViewById(R.id.btnPathPlanning);
        btnPathPlanning.setOnClickListener(this);

    }

    /**
     * 设置选择标签
     *
     * @param index 标签号
     */
    private void selectFragment(int index) {
        resetButtonBackgroud();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        hideAllFragments(mFragmentTransaction);
        switch (index) {
            case 0:
                // 当点击了消息tab时，改变控件的图片和文字颜色
                btnConnectionConfig.setBackgroundResource(R.drawable.connection_pressed);
                if (mConnectionFragment == null) {
                    // 如果MessageFragment为空，则创建一个并添加到界面上
                    mConnectionFragment = new ConnectionFragment();
                    mFragmentTransaction.add(R.id.home_content, mConnectionFragment,mConnectionFragment.getClass().getName());
                } else {
                    // 如果MessageFragment不为空，则直接将它显示出来
                    mFragmentTransaction.show(mConnectionFragment);
                }
                break;

            case 1:
                // 当点击了消息tab时，改变控件的图片和文字颜色
                btnTractorSetting.setBackgroundResource(R.drawable.tractor_setting_pressed);
                if (mTractorSettingFragment == null) {
                    // 如果MessageFragment为空，则创建一个并添加到界面上
                    mTractorSettingFragment = new TractorSettingFragment();
                    mFragmentTransaction.add(R.id.home_content, mTractorSettingFragment, mTractorSettingFragment.getClass().getName());
                } else {
                    // 如果MessageFragment不为空，则直接将它显示出来
                    mFragmentTransaction.show(mTractorSettingFragment);
                }
                break;

            case 2:
                // 当点击了动态tab时，改变控件的图片和文字颜色
                btnFieldSetting.setBackgroundResource(R.drawable.field_setting_pressed);
                if (mFieldSettingFragment == null) {
                    // 如果NewsFragment为空，则创建一个并添加到界面上
                    mFieldSettingFragment = new FieldSettingFragment();
                    mFragmentTransaction.add(R.id.home_content, mFieldSettingFragment, mFieldSettingFragment.getClass().getName());
                } else {
                    // 如果NewsFragment不为空，则直接将它显示出来
                    mFragmentTransaction.show(mFieldSettingFragment);
                }
                break;

            default:
                break;
        }

        // 提交碎片管理事务
        mFragmentTransaction.commit();
    }

    /**
     * 清除掉所有的选中状态。
     */
    private void resetButtonBackgroud() {
        btnConnectionConfig.setBackgroundResource(R.drawable.connection);
        btnTractorSetting.setBackgroundResource(R.drawable.tractor_setting);
        btnFieldSetting.setBackgroundResource(R.drawable.field_setting);
    }

    /**
     * 将所有fragment置为隐藏状态。
     *
     * @param transaction 用于对Fragment执行操作的事务
     */
    private void hideAllFragments(FragmentTransaction transaction) {
        if (mConnectionFragment != null) {
            transaction.hide(mConnectionFragment);
        }

        if (mTractorSettingFragment != null) {
            transaction.hide(mTractorSettingFragment);
        }

        if (mFieldSettingFragment != null) {
            transaction.hide(mFieldSettingFragment);
        }
    }

    /**
     * duan 18.4.9
     *重写该方法解决fragement重影问题
     *
     */
    @Override
    protected void onSaveInstanceState(Bundle outState){

    }
}