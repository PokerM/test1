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
import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;
import sjtu.me.tractor.field.FieldInfo;
import sjtu.me.tractor.main.MyApplication;
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.tractorinfo.TractorInfo;
import sjtu.me.tractor.util.AlertDialogUtil;

public class HistoryPathResultActivity extends Activity {

    List<Map<String, String>> list;
    private static MyApplication myApp;

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

        //��ȡ��intent��Я��������
        Bundle data = intent.getExtras();

        //��Bundle���ݰ���ȡ������
        list = (List<Map<String, String>>) data.getSerializable("data");
        //��List��װ��SimpleAdapter
        SimpleAdapter adapter = new SimpleAdapter(HistoryPathResultActivity.this, list, R.layout.history_result_list_cell,
                new String[]{HistoryPath.HISTORY_RECORD_FILE_NAME, HistoryPath.FIELD_NAME}, new int[]{R.id.historyFileName, R.id.fieldName});
        //���ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> av, View v, int position, long id) {

                Map<String, String> item = list.get(position);
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("history_entry", (Serializable) item);
                intent.putExtras(bundle);
                Log.e(position + " item is clicked", item.get(HistoryPath.HISTORY_RECORD_FILE_NAME));
                // ���÷���ֵ����������
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    //����ɾ�����ݼ���������д��������
    View.OnCreateContextMenuListener longPressListener = new View.OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int position = info.position;
            Map<String, String> map = list.get(position);
            final String name = map.get(HistoryPath.HISTORY_RECORD_FILE_NAME);
            AlertDialog longPressAlertBuilder = new AlertDialog.Builder(getApplicationContext())
                    .setTitle(getString(R.string.alert_title))
                    .setMessage(getString(R.string.delete_entries))
                    .setIcon(R.drawable.alert)
                    .setPositiveButton(getString(R.string.affirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myApp.getDatabaseManager().deleteHistoryEntries(name);
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

}
