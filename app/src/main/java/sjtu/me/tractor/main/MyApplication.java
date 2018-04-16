package sjtu.me.tractor.main;

import android.app.Application;
import android.content.Context;

import sjtu.me.tractor.connection.BluetoothService;
import sjtu.me.tractor.database.DatabaseManager;

public class MyApplication extends Application{
    
    public static final String ADDRESS_NULL = "empty_address";
    
    private static Context context; //定义全局环境
    public BluetoothService bluetoothService; //全局蓝牙服务对象
    public String bluetoothAddress = ADDRESS_NULL;
    public String currentDeviceName ="";
    public DatabaseManager databaseManager; //全局数据库管理器
    public boolean serviceInitialized = false;
    /**
     * 构造函数
     */
    public MyApplication() {
        super();
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        context = getApplicationContext();
        databaseManager = DatabaseManager.getInstance(context);
    }
    

    /**
     * @return
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * @return
     */
    public static Context getContext() {
        return context;
    }

    /**
     * @return 
     */
    public BluetoothService getBluetoothService() {
        return bluetoothService;
    }

    /**
     * @param bluetooth
     */
    public void setBluetoothService(BluetoothService bluetooth) {
        this.bluetoothService = bluetooth;
        serviceInitialized = true;
    }

    /**
     * @return
     */
    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    /**
     * @param mBluetoothAddress
     */
    public void setBluetoothAddress(String mBluetoothAddress) {
        this.bluetoothAddress = mBluetoothAddress;
    }

    public void setBluetoothName(String name){
        currentDeviceName = name;
    }

    public String getCurrentDeviceName(){
        return currentDeviceName;
    }

    public boolean getServiceState(){
        return serviceInitialized;
    }
}


