package sjtu.me.tractor.field;


import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.BaseBundle;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import sjtu.me.tractor.R;

public class FieldResultActivity extends Activity {

    public static final String EXTRA_FIELD_NAME = "name";
	List<Map<String,String>> list;

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
		SimpleAdapter adapter = new SimpleAdapter(FieldResultActivity.this, list, R.layout.line, 
				new String[] {"fNo", "fName", "fDate", "fPNo"}, 
				new int[] {R.id.fNo, R.id.fName, R.id.fDate, R.id.fPNo});
		//填充ListView
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(fieldListClickListener	);
	}
	
	// 选择设备响应函数 
    private OnItemClickListener fieldListClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> av, View v, int position, long id) {

        	Map<String, String> item = list.get(position);
            Intent intent = new Intent();
            intent.putExtra(EXTRA_FIELD_NAME, item.get("fName"));
            Log.e(position + " item is clicked", item.get("fName"));
            // 设置返回值并结束程序
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

}
