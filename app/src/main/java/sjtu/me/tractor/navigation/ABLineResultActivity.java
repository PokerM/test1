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
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.utils.AlertDialogUtil;

public class ABLineResultActivity extends Activity {

    private List<Map<String, String>> list;
    private MyApplication myApp;
    private SimpleAdapter adapter;

    View.OnCreateContextMenuListener listener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
            final int position = info.position;
            Map<String, String> map = list.get(position);
            Collections.reverse(list);
            final String name = map.get(ABLine.AB_LINE_NAME_BY_DATE);
            AlertDialog longPressAlertBuilder = new AlertDialog.Builder(ABLineResultActivity.this)
                    .setTitle(getString(R.string.please_select_an_operation))
                    .setIcon(R.drawable.alert)
                    .setPositiveButton(getString(R.string.affirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Map<String, String> item = list.get(position);
                            Intent intent = new Intent();
                            intent.putExtra(ABLine.AB_LINE_NAME_BY_DATE, item.get(ABLine.AB_LINE_NAME_BY_DATE));
                            Log.e(position + " item is clicked", item.get(ABLine.AB_LINE_NAME_BY_DATE));
                            // 设置返回值并结束程序
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    })
                    .setNeutralButton(R.string.delete_this_entry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myApp.getDatabaseManager().deleteABLine(name);
                            list.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();
            longPressAlertBuilder.show();
            AlertDialogUtil.changeDialogTheme(longPressAlertBuilder);
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        myApp = (MyApplication) getApplication();

        ListView listView = (ListView) findViewById(R.id.show);
        listView.setOnCreateContextMenuListener(listener);
        Intent intent = getIntent();

        //获取该intent所携带的数据
        Bundle data = intent.getExtras();

        //从Bundle数据包中取出数据
        list = (List<Map<String, String>>) data.getSerializable("data");
        //将List封装成SimpleAdapter
        adapter = new SimpleAdapter(ABLineResultActivity.this, list, R.layout.ab_line_result_list_cell,
                new String[]{ABLine.AB_LINE_NAME_BY_DATE, ABLine.FIELD_NAME}, new int[]{R.id.nameBydate, R.id.field});
        //填充ListView
        listView.setAdapter(adapter);
    }

}
