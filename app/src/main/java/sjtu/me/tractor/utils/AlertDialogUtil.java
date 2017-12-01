package sjtu.me.tractor.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.widget.TextView;

public class AlertDialogUtil {

    /**
     * �ı�ԭ��AlertDialog����ʽ
     * @param dialog
     */
    public static void changeDialogTheme(AlertDialog dialog) {
        
        // ����AlertDialog�ṩ�ķ����ı�ԭ��AlertDialog�еİ�ť����ɫ������
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.RED);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize(20);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(20);

        // ���÷���ı�ԭ��AlertDialog�еİ�ť����ɫ������
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
