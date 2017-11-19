package sjtu.me.tractor.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by billhu on 2017/11/18.
 */

public class SysUtil {
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
}
