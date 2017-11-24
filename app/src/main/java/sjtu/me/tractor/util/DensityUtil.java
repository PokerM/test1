package sjtu.me.tractor.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by billhu on 2017/11/18.
 */

public class DensityUtil {
    private static final String TAG = "DensityUtil";

    /**
     * �����ֻ��ķֱ��ʵĵ�λ�� dp ת��Ϊ px(����)
     * @param context
     * @param dip
     * @return
     */
    public static int dipToPixel(Context context, float dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * �����ֻ��ķֱ��ʵĵ�λ�� px(����) ת��Ϊ dp
     * @param context
     * @param pxValue
     * @return
     */
    public static int pixelToDip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * ��ȡ�ֻ��ķֱ���px������һ�����飬Ϊ��Ⱥͳ��ȡ�
     */
    public static int[] getDevicePixels(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int heigth = metrics.heightPixels;
        return new int[]{width, heigth};
    }
}
