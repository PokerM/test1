package sjtu.me.tractor.tractorinfo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import sjtu.me.tractor.R;


public class TractorSetting extends Activity {
	
	float[] settingData=new float[6];
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	        setContentView(R.layout.car_setting);	        
	    }
	    
	    public void onClick(View view) {
	        Intent data = new Intent();

	        //---get the EditText view---
	        EditText txt_bodyLength = (EditText) findViewById(R.id.editText01);
	        EditText txt_antennaHeight = (EditText) findViewById(R.id.editText02);
	        EditText txt_distance2shaft = (EditText) findViewById(R.id.editText03);
	        EditText txt_distance2back = (EditText) findViewById(R.id.editText04);
	        EditText txt_speed = (EditText) findViewById(R.id.editText05);
	        EditText txt_space = (EditText) findViewById(R.id.editText06);

	        //---set the data to pass back---
	        try {
		        settingData[0]=Float.parseFloat(txt_bodyLength.getText().toString());
		        settingData[1]=Float.parseFloat(txt_antennaHeight.getText().toString());
		        settingData[2]=Float.parseFloat(txt_distance2shaft.getText().toString());
		        settingData[3]=Float.parseFloat(txt_distance2back.getText().toString());	        							
		        settingData[4]=Float.parseFloat(txt_speed.getText().toString());
		        settingData[5]=Float.parseFloat(txt_space.getText().toString());
	        } catch(NumberFormatException e) {}
	        
	        Bundle extras=new Bundle();
	        extras.putFloatArray("settingData", settingData);	        
	        data.putExtras(extras);
	        setResult(RESULT_OK, data);

	        //---closes the activity---
	        finish();
	    }

}
