package sjtu.me.tractor.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.widget.TextView;

public class AlertDialogUtil {

    /**
     * 改变原生AlertDialog的样式
     * @param dialog
     */
    public static void changeDialogTheme(AlertDialog dialog) {
        
        // 利用AlertDialog提供的方法改变原生AlertDialog中的按钮的颜色和字体
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.RED);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize(20);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(20);

        // 利用反射改变原生AlertDialog中的按钮的颜色和字体
        try {
            java.lang.reflect.Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            java.lang.reflect.Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextSize(20);
            mMessageView.setTextColor(Color.WHITE);
            java.lang.reflect.Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextSize(25);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

}
