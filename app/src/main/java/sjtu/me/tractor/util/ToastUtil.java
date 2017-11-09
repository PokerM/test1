package sjtu.me.tractor.util;

import android.view.Gravity;
import android.widget.Toast;
import sjtu.me.tractor.main.MyApplication;

public class ToastUtil {
    /**
     * Display a Toast prompt with words specified by the string.
     * ����һ��Toast��ʾ
     * @param str ��ʾ�����ַ���
     * @param isShort �Ƿ��ʱ��ʾ��ֵΪtrueʱ����ʱ��ΪToast.LENGTH_SHORT��ֵΪfalseʱ����ʱ��ΪToast.LENGTH_LONG
     */
    public static void showToast(String str, boolean isShort) {
        Toast toast;
        if (!isShort) {
            toast = Toast.makeText(MyApplication.getContext(), str, Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(MyApplication.getContext(), str, Toast.LENGTH_SHORT);
        }
        toast.setGravity(Gravity.BOTTOM, 0, 220);
        toast.show();
    }
}