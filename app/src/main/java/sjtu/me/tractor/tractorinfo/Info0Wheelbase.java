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


public class Info0Wheelbase extends Fragment implements TextWatcher {
    
	public EditText editTextTractorWheelbase;
	private ImageButton btnHelpTractorWheelbase;
	private ImageButton btnCloseWheelbase;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View view = inflater.inflate(R.layout.tractor_fragment_0_wheelbase, container, false);
		editTextTractorWheelbase = (EditText)view.findViewById(R.id.editTractorWheelbase);
		editTextTractorWheelbase.addTextChangedListener(this);
		btnHelpTractorWheelbase = (ImageButton)view.findViewById(R.id.help_tractor_info_wheelbase);
		
		//为问号图标设置监听器，弹出提示信息
		btnHelpTractorWheelbase.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
				alertDialog.show();
				Window window = alertDialog.getWindow();
				window.setContentView(R.layout.window_dialog);
				
				TextView txtDialogTitle = (TextView)window.findViewById(R.id.dialog_title);
				txtDialogTitle.setText(getString(R.string.info_fragment1_wheelbase));
				
				TextView txtDialogMessage = (TextView)window.findViewById(R.id.dialog_message);
				txtDialogMessage.setText(getString(R.string.info_fragment1_tips));
				
				btnCloseWheelbase = (ImageButton)window.findViewById(R.id.dialog_close);
				btnCloseWheelbase.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
					  //按到关闭键即可关闭提示（或者焦点离开提示框时也可关闭提示）
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
		TractorAddingActivity m = (TractorAddingActivity) getActivity();
		if (m.txtWheelbase != null) {
			String s = editTextTractorWheelbase.getText().toString();
			m.txtWheelbase.setText(s);
			m.tractorAttributeValue[0] = s;
			m.updateItems();
			TractorInfoInputListAdapter ma = (TractorInfoInputListAdapter)m.lstInfoInput.getAdapter();
			ma.updateData(m.listItems);
		}
	}

}
