package sjtu.me.tractor.tractorinfo;

import sjtu.me.tractor.R;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


public class Info4MinTurning extends Fragment implements TextWatcher {
	public EditText editTextMinTurning;
	private ImageButton btnHelpMinTurning;
	private ImageButton btnCloseMinTurning;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View view = inflater.inflate(R.layout.tractor_fragment_5_min_turning_radius, container, false);
		editTextMinTurning = (EditText)view.findViewById(R.id.edit5);
		editTextMinTurning.addTextChangedListener(this);
		btnHelpMinTurning = (ImageButton)view.findViewById(R.id.help5);
		
		//Ϊ�ʺ�ͼ�����ü�������������ʾ��Ϣ
		btnHelpMinTurning.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
				ad.show();
				Window window = ad.getWindow();
				window.setContentView(R.layout.window_dialog);
				TextView tv_title = (TextView)window.findViewById(R.id.dialog_title);
				tv_title.setText(getString(R.string.info_fragment5_min_turning));
				TextView tv_message = (TextView)window.findViewById(R.id.dialog_message);
				tv_message.setText(getString(R.string.info_fragment5_tips));
				btnCloseMinTurning = (ImageButton)window.findViewById(R.id.dialog_close);
				
				//�����رռ����ɹر���ʾ�����߽����뿪��ʾ��ʱҲ�ɹر���ʾ��
				btnCloseMinTurning.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						ad.dismiss();
					}
				});
			}
		});
		return view;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}
	
	//���༭�����ݷ����仯ʱ��ͬ�������ض�listView�е�����
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		TractorAddingActivity m = (TractorAddingActivity)getActivity();
		if (m.txtMinTurning != null) {
			String s = editTextMinTurning.getText().toString();
			m.txtMinTurning.setText(s);
			m.tractorAttributeValue[4] = s;
			m.updateItems();
			TractorInfoInputListAdapter ma = (TractorInfoInputListAdapter)m.lstInfoInput.getAdapter();
			ma.updateData(m.listItems);
		}
	}

}
