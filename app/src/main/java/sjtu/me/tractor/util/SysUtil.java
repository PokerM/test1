package sjtu.me.tractor.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by billhu on 2017/11/18.
 */

public class SysUtil {
    /**
     * �жϳ����Ƿ���ǰ̨����
     * @param context
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();
        /**
         * ��ȡAndroid�豸�������������е�App
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
     * �����ַ�����ĳ���ַ��ĸ�����
     * �ڱ����м��㡰,���ĸ�����������sb.toString().split(",").length����������������е�ʱ��ӽ�countCharacter��������
     * @param str �������ַ���
     * @param ch ĳ���ַ�
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
