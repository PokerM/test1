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
import sjtu.me.tractor.planning.ABLine;
import sjtu.me.tractor.tractorinfo.TractorInfo;

public class ABLineResultActivity extends Activity {

    List<Map<String, String>> list;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        ListView listView = (ListView) findViewById(R.id.show);
        Intent intent = getIntent();

        //获取该intent所携带的数据
        Bundle data = intent.getExtras();

        //从Bundle数据包中取出数据
        list = (List<Map<String, String>>) data.getSerializable("data");
        //将List封装成SimpleAdapter
        SimpleAdapter adapter = new SimpleAdapter(ABLineResultActivity.this, list, R.layout.ab_line_result_list_cell,
                new String[]{ABLine.AB_LINE_NAME_BY_DATE, ABLine.FIELD_NAME}, new int[]{R.id.nameBydate, R.id.field});
        //填充ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> av, View v, int position, long id) {

                Map<String, String> item = list.get(position);
                Intent intent = new Intent();
                intent.putExtra(ABLine.AB_LINE_NAME_BY_DATE, item.get(ABLine.AB_LINE_NAME_BY_DATE));
                Log.e(position + " item is clicked", item.get(ABLine.AB_LINE_NAME_BY_DATE));
                // 设置返回值并结束程序
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

}
