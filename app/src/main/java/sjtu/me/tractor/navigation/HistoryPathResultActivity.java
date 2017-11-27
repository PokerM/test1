package sjtu.me.tractor.navigation;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;
import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.tractorinfo.TractorInfo;
import sjtu.me.tractor.util.AlertDialogUtil;

public class HistoryPathResultActivity extends Activity {

    private List<Map<String, String>> list;
    private SimpleAdapter adapter;
    private MyApplication myApp;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        myApp = (MyApplication) getApplication();

        ListView listView = (ListView) findViewById(R.id.show);
        listView.setOnCreateContextMenuListener(longPressListener);
        Intent intent = getIntent();

        //获取该intent所携带的数据
        Bundle data = intent.getExtras();

        //从Bundle数据包中取出数据
        list = (List<Map<String, String>>) data.getSerializable("data");
        Collections.reverse(list);
        //将List封装成SimpleAdapter
        adapter = new SimpleAdapter(HistoryPathResultActivity.this, list, R.layout.history_result_list_cell,
                new String[]{HistoryPath.HISTORY_RECORD_FILE_NAME, HistoryPath.FIELD_NAME}, new int[]{R.id.historyFileName, R.id.fieldName});
        //填充ListView
        listView.setAdapter(adapter);
    }

    //长按删除数据监听器，重写监听方法
    View.OnCreateContextMenuListener longPressListener = new View.OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            final int position = info.position;
            Map<String, String> map = list.get(position);
            final String name = map.get(HistoryPath.HISTORY_RECORD_FILE_NAME);
            AlertDialog longPressAlertBuilder = new AlertDialog.Builder(HistoryPathResultActivity.this)
                    .setTitle(R.string.please_select_an_operation)
                    .setIcon(R.drawable.alert)
                    .setPositiveButton(R.string.select_the_entry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Map<String, String> item = list.get(position);
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("history_entry", (Serializable) item);
                            intent.putExtras(bundle);
                            Log.e(position + " item is clicked", item.get(HistoryPath.HISTORY_RECORD_FILE_NAME));
                            // 设置返回值并结束程序
                            setResult(Activity.RESULT_OK, intent);
                            finish();


                        }
                    })
                    .setNeutralButton(R.string.delete_this_entry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myApp.getDatabaseManager().deleteHistoryEntries(name);
                            list.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(R.string.no_operation, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();

            longPressAlertBuilder.show();
            AlertDialogUtil.changeDialogTheme(longPressAlertBuilder);
        }

    };

}
