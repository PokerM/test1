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


public class Info2AntennaRear extends Fragment implements TextWatcher {
	public EditText editTextAntennaRear;
	private ImageButton btnHelpAntennaRear;
	private ImageButton btnCloseAntennaRear;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View view = inflater.inflate(R.layout.tractor_fragment_2_antenna_rear_axle, container, false);
		editTextAntennaRear = (EditText)view.findViewById(R.id.edit3);
		editTextAntennaRear.addTextChangedListener(this);
		btnHelpAntennaRear = (ImageButton)view.findViewById(R.id.help3);
		
		//Ϊ�ʺ�ͼ�����ü�������������ʾ��Ϣ
		btnHelpAntennaRear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
				ad.show();
				Window window = ad.getWindow();
				window.setContentView(R.layout.window_dialog);
				TextView tv_title = (TextView)window.findViewById(R.id.dialog_title);
				tv_title.setText(getString(R.string.info_fragment3_antenna_rear));
				TextView tv_message = (TextView)window.findViewById(R.id.dialog_message);
				tv_message.setText(getString(R.string.info_fragment3_tips));
				btnCloseAntennaRear = (ImageButton)window.findViewById(R.id.dialog_close);
				
				//�����رռ����ɹر���ʾ�����߽����뿪��ʾ��ʱҲ�ɹر���ʾ��
				btnCloseAntennaRear.setOnClickListener(new OnClickListener() {
					
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
		if (m.txtAntennaRear != null) {
			String s = editTextAntennaRear.getText().toString();
			m.txtAntennaRear.setText(s);
			m.tractorAttributeValue[2] = s;
			m.updateItems();
			TractorInfoInputListAdapter ma = (TractorInfoInputListAdapter)m.lstInfoInput.getAdapter();
			ma.updateData(m.listItems);
		}
	}

}
