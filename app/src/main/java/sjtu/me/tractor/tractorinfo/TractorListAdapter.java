package sjtu.me.tractor.tractorinfo;

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

public class TractorListAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, String>> list;
    
    /**
     * 构造方法：需要上下文环境和装载数据的列表
     * @param context 上下文环境
     * @param list 数据列表
     */
    public TractorListAdapter(Context context, List<Map<String, String>> list) {
        this.context = context;
        this.list = list;
    }
    
    /**
     * @return
     */
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
        LinearLayout currentView;
        if (convertView != null) {
            currentView = (LinearLayout) convertView;
        } else {
            currentView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.tractor_list_cell, null);
        }
        
        TextView tName = (TextView) currentView.findViewById(R.id.cellTextTractorName);
        TextView tType = (TextView) currentView.findViewById(R.id.cellTextTractorType);
        TextView tMade = (TextView) currentView.findViewById(R.id.cellTextTractorMade);
        TextView tTypeNumber = (TextView) currentView.findViewById(R.id.cellTextTractorTypeNumber);

        tName.setText(list.get(position).get(TractorInfo.TRACTOR_NAME));
        tType.setText(list.get(position).get(TractorInfo.TRACTOR_TYPE));
        tMade.setText(list.get(position).get(TractorInfo.TRACTOR_MADE));
        tTypeNumber.setText(list.get(position).get(TractorInfo.TRACTOR_TYPE_NUMBER));

        return currentView;
    }

}
