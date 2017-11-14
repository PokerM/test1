package sjtu.me.tractor.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import sjtu.me.tractor.bluetooth.BluetoothService;
import sjtu.me.tractor.database.DatabaseManager;

public class MyApplication extends Application{
    
    public static final String ADDRESS_NULL = "empty_address";
    
    private static Context context; //定义全局环境
    public BluetoothService bluetoothService; //全局蓝牙服务对象
    public String bluetoothAddress = ADDRESS_NULL;
    public DatabaseManager databaseManager; //全局数据库管理器

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
        databaseManager = new DatabaseManager(context);
    }
    

    /**
     * @return
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * @param mDatabaseManager
     */
    public void setDatabaseManager(DatabaseManager mDatabaseManager) {
        this.databaseManager = mDatabaseManager;
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
    
    /**
     * 判断程序是否在前台运行
     * @param context
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();
        /**
         * 获取Android设备中所有正在运行的App
         */
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }
    
    /**
	 * Count the specified Character in a string.
	 * 计算字符串中某个字符的个数。
	 * 在本例中计算“,”的个数还可以用sb.toString().split(",").length，但是这个方法运行的时间接近countCharacter的三倍；
	 * @param str 待计数字符串
	 * @param ch 某个字符
	 * @return
	 */
	public static int countCharacter(StringBuilder str, char ch) {
        char[] chs = str.toString().toCharArray();
		int count = 0;
		for (int i = 0; i < chs.length; i++) {
			if (chs[i] == ch) {
                count++;
            }
		}
		return count;
	}
    
	/**
	 * 保存数据库文件到外部存储空间
	 * @param dbPath 数据库文件路径
	 * @return
	 */
	@SuppressLint("SdCardPath")
    public static boolean copyDbFilesToExternalStorage(String dbPath) {
//	    String dbPath = "/data/data/com.example.fielddatabase/databases/" +"auto_tractor";
        String dir = "dbFiles";
        File newPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), dir);
        if (!newPath.exists()) {
            newPath.mkdirs();
        }
        return copyFile(dbPath, newPath + "/" + "auto_tractor.db");
    }

    public static boolean copyFile(String source, String dest) {
        try {
            File f1 = new File(source);
            File f2 = new File(dest);
            InputStream in = new FileInputStream(f1);
            OutputStream out = new FileOutputStream(f2);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            in.close();
            out.close();
            return true;
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
	
	
}


