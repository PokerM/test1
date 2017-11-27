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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;
import sjtu.me.tractor.tractorinfo.TractorInfo;

public class TractorResultActivity extends Activity {

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
        Collections.reverse(list);
        //��List��װ��SimpleAdapter
        SimpleAdapter adapter = new SimpleAdapter(TractorResultActivity.this, list, R.layout.tractor_result_list_cell,
                new String[]{TractorInfo.TRACTOR_NAME}, new int[]{R.id.tractorName});
        //���ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> av, View v, int position, long id) {

                Map<String, String> item = list.get(position);
                Intent intent = new Intent();
                intent.putExtra(TractorInfo.TRACTOR_NAME, item.get(TractorInfo.TRACTOR_NAME));
                Log.e(position + " item is clicked", item.get(TractorInfo.TRACTOR_NAME));
                // ���÷���ֵ����������
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

}
