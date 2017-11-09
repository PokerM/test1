package sjtu.me.tractor.main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

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
import sjtu.me.tractor.R;
import sjtu.me.tractor.connection.ConnectionFragment;
import sjtu.me.tractor.field.FieldSettingFragment;
import sjtu.me.tractor.tractorinfo.TractorSettingFragment;
import sjtu.me.tractor.util.ToastUtil;

/**
 * @author billhu ����������͹�����
 */
public class HomeActivity extends Activity implements OnClickListener {

    private String TAG = "HomeActivity";
    private static final boolean D = false; // ��־��ڿ���
    
    private boolean isExitApplication = false;  //�Ƿ��˳�Ӧ�ó����־

    private FragmentManager mFragmentManager; //Fragment������
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
        setContentView(R.layout.home_activity);
        initViews();
        mFragmentManager = getFragmentManager();
        selectFragment(0);

        
        if (D)
            Log.e(TAG, "+++ ON CREATE +++");
    }

    @Override
    protected void onStart() {
      
        super.onStart();
        
        if (D)    Log.e(TAG, "+++ ON START +++");
    }

    @Override
    protected void onRestart() {
      
        super.onRestart();
        
//        //Ϊ��ʡ��ͷ�ֹ��̨ͨ���̳߳������У������ڳ�������̨����ʱ�ر�ͨ�ţ�
//        //��restart������ͨ�ţ������ή�ͳ��������ٶȡ�
//        MyApplication myApp = (MyApplication) getApplication();
//        if (myApp.getBluetoothService().getState() == BluetoothService.STATE_NONE) {
//            myApp.getBluetoothService().startConnection(myApp.getBluetoothAddress());
//        }
        
        if (D)   Log.e(TAG, "+++ ON RESTART +++");
    }

    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		//Ϊ��ʡ��ͷ�ֹ��̨ͨ���̳߳������У������ڳ�������̨����ʱ�ر�ͨ�ţ�
        //��restart������ͨ�ţ������ή�ͳ��������ٶȡ�
		if (!MyApplication.isAppOnForeground(this)) {
		    Log.e("APP", "ON BACK GROUDN");
//		    MyApplication myApp = (MyApplication) getApplication();
//		    if (myApp.getBluetoothService().getState() != BluetoothService.STATE_NONE) {
//		        myApp.getBluetoothService().stopConnection();
//		    }
		}
		 if (D)   Log.e(TAG, "+++ ON STOP +++");
	}

	@Override
    protected void onResume() {
      
        super.onResume();
        
        if (D)   Log.e(TAG, "+++ ON RESUME +++");
    }

    @Override
    protected void onPause() {
      
        super.onPause();
        
        if (D)   Log.e(TAG, "+++ ON PAUSE +++");
    }

    @Override
    protected void onDestroy() {
      
        super.onDestroy();
        
        if (D)   Log.e(TAG, "+++ ON DESTROY +++");
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
		
		case R.id.btnNavigation:
			startActivity(new Intent("sjtu.me.tractor.main.NaviActivity" ));
			break;
			
        case R.id.btnPathPlanning:
            Intent intent2 = new Intent("sjtu.me.tractor.field.FieldAddingActivity2");
            startActivityForResult(intent2, 1);
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

  //�Է��ؼ������¼���ʵ�ֵ������ٰ�һ���˳����򡱵�Ч��
    @SuppressWarnings("deprecation")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // �жϴ�ʱ��Activity�Ƿ�ΪMainActivity��������ִ���˳�����Ĳ���
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            String thisActivity = am.getRunningTasks(1).get(0).topActivity.getClassName();
            if ("sjtu.me.tractor.main.HomeActivity".equals(thisActivity)) {
                if (isExitApplication == false) {
                    isExitApplication = true;
                    ToastUtil.showToast(getString(R.string.touch_again_and_exit), true);
                    new Timer().schedule(new TimerTask() {
                        // �½�ʱ�����2000�����Զ����˳���־����Ϊfalse
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
     * ��ʼ����ͼ
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
     * ����ѡ���ǩ
     * @param index ��ǩ��
     */
    private void selectFragment(int index) {
        resetButtonBackgroud();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        hideAllFragments(mFragmentTransaction);
        switch (index) {
        case 0:
            // ���������Ϣtabʱ���ı�ؼ���ͼƬ��������ɫ
            btnConnectionConfig.setBackgroundResource(R.drawable.connection_pressed);
            if (mConnectionFragment == null) {
                // ���MessageFragmentΪ�գ��򴴽�һ������ӵ�������
                mConnectionFragment = new ConnectionFragment();
                mFragmentTransaction.add(R.id.home_content, mConnectionFragment);
            } else {
                // ���MessageFragment��Ϊ�գ���ֱ�ӽ�����ʾ����
                mFragmentTransaction.show(mConnectionFragment);
            }
            break;

        case 1:
            // ���������Ϣtabʱ���ı�ؼ���ͼƬ��������ɫ
            btnTractorSetting.setBackgroundResource(R.drawable.tractor_setting_pressed);
            if (mTractorSettingFragment == null) {
                // ���MessageFragmentΪ�գ��򴴽�һ������ӵ�������
                mTractorSettingFragment = new TractorSettingFragment();
                mFragmentTransaction.add(R.id.home_content, mTractorSettingFragment);
            } else {
                // ���MessageFragment��Ϊ�գ���ֱ�ӽ�����ʾ����
                mFragmentTransaction.show(mTractorSettingFragment);
            }
            break;

        case 2:
            // ������˶�̬tabʱ���ı�ؼ���ͼƬ��������ɫ
            btnFieldSetting.setBackgroundResource(R.drawable.field_setting_pressed);
            if (mFieldSettingFragment == null) {
                // ���NewsFragmentΪ�գ��򴴽�һ������ӵ�������
                mFieldSettingFragment = new FieldSettingFragment();
                mFragmentTransaction.add(R.id.home_content, mFieldSettingFragment);
            } else {
                // ���NewsFragment��Ϊ�գ���ֱ�ӽ�����ʾ����
                mFragmentTransaction.show(mFieldSettingFragment);
            }
            break;

        default:
            break;
        }

        // �ύ��Ƭ��������
        mFragmentTransaction.commit();
    }
    
    /** 
     * ��������е�ѡ��״̬�� 
     */  
    private void resetButtonBackgroud()  
    {  
        btnConnectionConfig.setBackgroundResource(R.drawable.connection);  
        btnTractorSetting.setBackgroundResource(R.drawable.tractor_setting);
        btnFieldSetting.setBackgroundResource(R.drawable.field_setting);
    }  
    
    /**
     * ������fragment��Ϊ����״̬��
     * @param transaction ���ڶ�Fragmentִ�в���������
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
    
    
}