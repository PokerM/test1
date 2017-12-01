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


public class Info1AntennaLateral extends Fragment implements TextWatcher {
	public EditText editTextAntennaLateral;
	private ImageButton btnHelpAntennaLateral;
	private ImageButton btnCloseAntennaLateral;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View view = inflater.inflate(R.layout.tractor_fragment_1_antenna_lateral_deviation, container, false);
		editTextAntennaLateral = (EditText)view.findViewById(R.id.editTractorAntennaLateralDeviation);
		editTextAntennaLateral.addTextChangedListener(this);
		btnHelpAntennaLateral = (ImageButton)view.findViewById(R.id.help_tractor_antenna_lateral_deviation);
		
		//为问号图标设置监听器，弹出提示信息
		btnHelpAntennaLateral.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
				alertDialog.show();
				Window window = alertDialog.getWindow();
				window.setContentView(R.layout.window_dialog);
				TextView tv_title = (TextView)window.findViewById(R.id.dialog_title);
				tv_title.setText(getString(R.string.info_fragment2_antenna_lateral));
				TextView tv_message = (TextView)window.findViewById(R.id.dialog_message);
				tv_message.setText(getString(R.string.info_fragment2_tips));
				btnCloseAntennaLateral = (ImageButton)window.findViewById(R.id.dialog_close);
				
				//按到关闭键即可关闭提示（或者焦点离开提示框时也可关闭提示）
				btnCloseAntennaLateral.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						alertDialog.dismiss();
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
	
	//当编辑框数据发生变化时，同步更新特定listView中的数据
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		TractorAddingActivity m = (TractorAddingActivity)getActivity();
		if (m.txtAntennaLateral != null) {
			String s = editTextAntennaLateral.getText().toString();
			m.txtAntennaLateral.setText(s);
			m.tractorAttributeValue[1] = s;
			m.updateItems();
			TractorInfoInputListAdapter ma = (TractorInfoInputListAdapter)m.lstInfoInput.getAdapter();
			ma.updateData(m.listItems);
		}
	}

}
