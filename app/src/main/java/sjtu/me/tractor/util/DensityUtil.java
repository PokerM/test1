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
     * 根据手机的分辨率的单位从 dp 转成为 px(像素)
     * @param context
     * @param dip
     * @return
     */
    public static int dipToPixel(Context context, float dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率的单位从 px(像素) 转成为 dp
     * @param context
     * @param pxValue
     * @return
     */
    public static int pixelToDip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取手机的分辨率px，返回一个数组，为宽度和长度。
     */
    public static int[] getDevicePixels(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int heigth = metrics.heightPixels;
        return new int[]{width, heigth};
    }
}
