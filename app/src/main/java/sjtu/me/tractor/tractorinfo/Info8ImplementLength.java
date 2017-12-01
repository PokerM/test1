package sjtu.me.tractor.tractorinfo;

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

import sjtu.me.tractor.R;


public class Info8ImplementLength extends Fragment implements TextWatcher {
	public EditText editTextImplementLength;
	private ImageButton btnHelpImplementLength;
	private ImageButton btnCloseImplementLength;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View view = inflater.inflate(R.layout.tractor_fragment_8_implement_length, container, false);
		editTextImplementLength = (EditText)view.findViewById(R.id.editTractorImplementLength);
		editTextImplementLength.addTextChangedListener(this);
		btnHelpImplementLength = (ImageButton)view.findViewById(R.id.helpImplementLength);
		
		//为问号图标设置监听器，弹出提示信息
		btnHelpImplementLength.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
				ad.show();
				Window window = ad.getWindow();
				window.setContentView(R.layout.window_dialog);
				TextView tv_title = (TextView)window.findViewById(R.id.dialog_title);
				tv_title.setText(getString(R.string.info_fragment9_implement_length));
				TextView tv_message = (TextView)window.findViewById(R.id.dialog_message);
				tv_message.setText(getString(R.string.info_fragment9_tips));
				btnCloseImplementLength = (ImageButton)window.findViewById(R.id.dialog_close);
				
				//按到关闭键即可关闭提示（或者焦点离开提示框时也可关闭提示）
				btnCloseImplementLength.setOnClickListener(new OnClickListener() {
					
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
	
	//当编辑框数据发生变化时，同步更新特定listView中的数据
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		TractorAddingActivity m = (TractorAddingActivity) getActivity();
		if (m.txtImplementLength != null) {
			String s = editTextImplementLength.getText().toString();
			m.txtImplementLength.setText(s);
			m.tractorAttributeValue[8] = s;
			m.updateItems();
			TractorInfoInputListAdapter ma = (TractorInfoInputListAdapter)m.lstInfoInput.getAdapter();
			ma.updateData(m.listItems);
		}
	}

}
