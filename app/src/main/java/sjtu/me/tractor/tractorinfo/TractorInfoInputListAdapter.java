package sjtu.me.tractor.tractorinfo;

import java.util.List;
import java.util.Map;

import sjtu.me.tractor.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author billhu
 * 添加车辆界面列表视图的适配器类
 */
public class TractorInfoInputListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater listContainer;
	private List<Map<String, String>> list;
	private TractorAddingActivity tractorAddingActivity;
	
	/**
	 * @author billhu
	 *构建itemView所需的三个元素：变量名，变量值和一个修改按钮，用于引出viewPager特定页码
	 */
	public class ListItemView {
		public TextView txtLabelName; //名称文本控件
		public TextView txtLabelDetail;	//内容文本控件
		public Button btnEditLabel;	//编辑按钮
	}
	
	/**
	 * @return
	 */
	public Context getContext() {
		return this.context;
	}
	
	/**
	 * 构造方法
	 * @param context 上下文环境
	 * @param listItem 数据
	 */
	public TractorInfoInputListAdapter(Context context, List<Map<String, String>> listItem) {
		this.context = context;
		this.list = listItem;
		this.tractorAddingActivity = (TractorAddingActivity)getContext();
		this.listContainer = LayoutInflater.from(context);
	}
	
	//为数据实施更新操作
	public void updateData(List<Map<String, String>> list) {
		this.list = list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	/*
	 * 为ListView实现布局的方法，必须重写才可实现自定义布局
	 * seclectId为获得对应按钮的列数，用于最后和viewPager交互的实现
	 * 当convertView不为空时才创建，否则会不断new浪费内存空间
	 * inflate为从XML装载布局的方法
	 * 将listItemView分别赋予三个控件，并把list的元素赋值到其中
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int selectId = position;
		ListItemView listItemView;
		if (convertView == null) {
			listItemView = new ListItemView();
			convertView = listContainer.inflate(R.layout.tractor_info_list_item, null);
			listItemView.txtLabelName = (TextView) convertView.findViewById(R.id.txtLabelName);
			listItemView.txtLabelDetail = (TextView) convertView.findViewById(R.id.txtLabelDetail);
			listItemView.btnEditLabel = (Button) convertView.findViewById(R.id.btnEditLabel);
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView) convertView.getTag();
		}
		listItemView.txtLabelName.setText(list.get(position).get("name"));
		listItemView.txtLabelDetail.setText(list.get(position).get("detail"));
		listItemView.btnEditLabel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectDetail(selectId);
			}
		});
		return convertView;
	}
	
	//根据传回来的按钮编号实现listView与ViewPager联动
	private void selectDetail(int ClickId) {
		tractorAddingActivity.viewPager.setCurrentItem(ClickId);
	}
}
