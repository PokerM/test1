package sjtu.me.tractor.navigation;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;
import sjtu.me.tractor.tractorinfo.TractorInfo;

public class HistoryPathResultActivity extends Activity {

    List<Map<String, String>> list;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        ListView listView = (ListView) findViewById(R.id.show);
        Intent intent = getIntent();

        //��ȡ��intent��Я��������
        Bundle data = intent.getExtras();

        //��Bundle���ݰ���ȡ������
        list = (List<Map<String, String>>) data.getSerializable("data");
        //��List��װ��SimpleAdapter
        SimpleAdapter adapter = new SimpleAdapter(HistoryPathResultActivity.this, list, R.layout.history_result_list_cell   ,
                new String[]{HistoryPath.HISTORY_RECORD_FILE_NAME, HistoryPath.FIELD_NAME}, new int[]{R.id.historyFileName, R.id.fieldName});
        //���ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> av, View v, int position, long id) {

                Map<String, String> item = list.get(position);
                Intent intent = new Intent();
                intent.putExtra(HistoryPath.HISTORY_RECORD_FILE_NAME, item.get(HistoryPath.HISTORY_RECORD_FILE_NAME));
                Log.e(position + " item is clicked", item.get(HistoryPath.HISTORY_RECORD_FILE_NAME));
                // ���÷���ֵ����������
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

}
