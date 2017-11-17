package sjtu.me.tractor.start;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import sjtu.me.tractor.R;

public class StartActivity extends Activity {
	private ImageView welcome;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = View.inflate(this, R.layout.start_activity, null);
		setContentView(view);
		
		welcome = (ImageView)view.findViewById(R.id.welcome);
		
		//����͸���ȶ���
		Animation anim = new AlphaAnimation(0.1f, 0.9f);
		
		//���ö���ʱ��
		anim.setDuration(2000);
		
		//��������
		welcome.startAnimation(anim);
		
		//���ö�������
		anim.setAnimationListener(new ImageAnimation());
	
	}
	
	private class ImageAnimation implements AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {
			welcome.setBackgroundResource(R.drawable.welcome_page);
		}
	
		@Override
		public void onAnimationEnd(Animation animation) {
			//����0.9-1.0�ĵڶ��㶯�����������û����屳��ͼ
			Animation anim_2 = new AlphaAnimation(0.9f, 1.0f);
			anim_2.setDuration(800);
			welcome.startAnimation(anim_2);
			anim_2.setAnimationListener(new ImageAnimation2());
		}
	
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		//�����ж���
		public class ImageAnimation2 implements AnimationListener {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				redirectTo();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		}
		
	}
	
	/**
	 * ������תActivity
	 */
	private void redirectTo() {
//		startActivity(new Intent().setClass(this, NaviActivity.class));
		startActivity(new Intent("sjtu.me.tractor.main.HomeActivity"));
		overridePendingTransition(R.anim.alpha_out, R.anim.alpha_in);
		this.finish();
	}
	
	//�Է��ؼ������¼��������˳�����
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        return false;
    }
}
