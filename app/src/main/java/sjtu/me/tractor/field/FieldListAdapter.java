package sjtu.me.tractor.field;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import sjtu.me.tractor.R;

public class FieldListAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, String>> list;

    /**
     * 构造方法：需要上下文环境和装载数据的列表
     *
     * @param context 上下文环境
     * @param list    数据列表
     */
    FieldListAdapter(Context context, List<Map<String, String>> list) {
        this.context = context;
        this.list = list;
    }

    public Context getContext() {
        return this.context;
    }

    @Override
    public int getCount() {
        return this.list == null ? 0 : this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout ll;
        if (convertView != null) {
            ll = (LinearLayout) convertView;
        } else {
            ll = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.field_list_cell, null);
        }

        TextView listNumber = (TextView) ll.findViewById(R.id.txtListNumber);
        TextView fName = (TextView) ll.findViewById(R.id.txtFieldName);
        TextView fNo = (TextView) ll.findViewById(R.id.txtFieldNumber);
        TextView fDate = (TextView) ll.findViewById(R.id.txtFieldDate);
        TextView fPNo = (TextView) ll.findViewById(R.id.txtFieldVertexNumber);

        listNumber.setText(list.get(position).get("listNumber"));
        fName.setText(list.get(position).get(FieldInfo.FIELD_NAME));
        fNo.setText(list.get(position).get(FieldInfo.FIELD_ID));
        fDate.setText(list.get(position).get(FieldInfo.FIELD_DATE));
        fPNo.setText(list.get(position).get(FieldInfo.FIELD_POINT_NO));
        return ll;
    }

}
