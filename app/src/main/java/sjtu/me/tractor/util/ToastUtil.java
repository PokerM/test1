package sjtu.me.tractor.util;

import android.view.Gravity;
import android.widget.Toast;
import sjtu.me.tractor.main.MyApplication;

public class ToastUtil {
    /**
     * Display a Toast prompt with words specified by the string.
     * 弹出一个Toast提示
     * @param str 提示内容字符串
     * @param isShort 是否短时显示，值为true时持续时间为Toast.LENGTH_SHORT，值为false时持续时间为Toast.LENGTH_LONG
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