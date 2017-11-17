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
		
		//设置透明度动画
		Animation anim = new AlphaAnimation(0.1f, 0.9f);
		
		//设置动画时间
		anim.setDuration(2000);
		
		//启动动画
		welcome.startAnimation(anim);
		
		//设置动画监听
		anim.setAnimationListener(new ImageAnimation());
	
	}
	
	private class ImageAnimation implements AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {
			welcome.setBackgroundResource(R.drawable.welcome_page);
		}
	
		@Override
		public void onAnimationEnd(Animation animation) {
			//设置0.9-1.0的第二层动画，便于让用户看清背景图
			Animation anim_2 = new AlphaAnimation(0.9f, 1.0f);
			anim_2.setDuration(800);
			welcome.startAnimation(anim_2);
			anim_2.setAnimationListener(new ImageAnimation2());
		}
	
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		//动画中动画
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
	 * 设置跳转Activity
	 */
	private void redirectTo() {
//		startActivity(new Intent().setClass(this, NaviActivity.class));
		startActivity(new Intent("sjtu.me.tractor.main.HomeActivity"));
		overridePendingTransition(R.anim.alpha_out, R.anim.alpha_in);
		this.finish();
	}
	
	//对返回键设置事件，不让退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        return false;
    }
}
