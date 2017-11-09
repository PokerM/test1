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
 * ��ӳ��������б���ͼ����������
 */
public class TractorInfoInputListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater listContainer;
	private List<Map<String, String>> list;
	private TractorAddingActivity tractorAddingActivity;
	
	/**
	 * @author billhu
	 *����itemView���������Ԫ�أ�������������ֵ��һ���޸İ�ť����������viewPager�ض�ҳ��
	 */
	public class ListItemView {
		public TextView txtLabelName; //�����ı��ؼ�
		public TextView txtLabelDetail;	//�����ı��ؼ�
		public Button btnEditLabel;	//�༭��ť
	}
	
	/**
	 * @return
	 */
	public Context getContext() {
		return this.context;
	}
	
	/**
	 * ���췽��
	 * @param context �����Ļ���
	 * @param listItem ����
	 */
	public TractorInfoInputListAdapter(Context context, List<Map<String, String>> listItem) {
		this.context = context;
		this.list = listItem;
		this.tractorAddingActivity = (TractorAddingActivity)getContext();
		this.listContainer = LayoutInflater.from(context);
	}
	
	//Ϊ����ʵʩ���²���
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
	 * ΪListViewʵ�ֲ��ֵķ�����������д�ſ�ʵ���Զ��岼��
	 * seclectIdΪ��ö�Ӧ��ť����������������viewPager������ʵ��
	 * ��convertView��Ϊ��ʱ�Ŵ���������᲻��new�˷��ڴ�ռ�
	 * inflateΪ��XMLװ�ز��ֵķ���
	 * ��listItemView�ֱ��������ؼ�������list��Ԫ�ظ�ֵ������
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
	
	//���ݴ������İ�ť���ʵ��listView��ViewPager����
	private void selectDetail(int ClickId) {
		tractorAddingActivity.viewPager.setCurrentItem(ClickId);
	}
}
