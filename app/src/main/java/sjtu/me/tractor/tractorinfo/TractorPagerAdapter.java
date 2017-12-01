package sjtu.me.tractor.tractorinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import sjtu.me.tractor.R;

//继承listener以获得页数
public class TractorPagerAdapter extends PagerAdapter implements OnPageChangeListener {
    private List<Fragment> fragments;
    private FragmentManager fm;
    private ViewPager viewPager;
    private int pageNum = 0;
    private TractorAddingActivity tractorAddingActivity;
    private ImageView[] imageView = new ImageView[10]; //装载10个圆点

    //初始化所需变量：用于联系上下文和viewPager
    public TractorPagerAdapter(FragmentManager fm, List<Fragment> fragments, ViewPager viewPager, Context context) {
        this.fm = fm;
        this.fragments = fragments;
        this.viewPager = viewPager;
        this.viewPager.setAdapter(this);
        this.tractorAddingActivity = (TractorAddingActivity) context;
        this.viewPager.setOnPageChangeListener(this);
        /*先得到10个圆点代表10个页面，然后将即将被翻过的页面对应的圆点变白，翻到的圆点变蓝*/
            imageView[0] = (ImageView) tractorAddingActivity.findViewById(R.id.page1);
            imageView[1] = (ImageView) tractorAddingActivity.findViewById(R.id.page2);
            imageView[2] = (ImageView) tractorAddingActivity.findViewById(R.id.page3);
            imageView[3] = (ImageView) tractorAddingActivity.findViewById(R.id.page4);
            imageView[4] = (ImageView) tractorAddingActivity.findViewById(R.id.page5);
            imageView[5] = (ImageView) tractorAddingActivity.findViewById(R.id.page6);
            imageView[6] = (ImageView) tractorAddingActivity.findViewById(R.id.page7);
            imageView[7] = (ImageView) tractorAddingActivity.findViewById(R.id.page8);
            imageView[8] = (ImageView) tractorAddingActivity.findViewById(R.id.page9);
            imageView[9] = (ImageView) tractorAddingActivity.findViewById(R.id.page10);
    }

    //复写移除布局的方法（把布局从容器中移除）
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(fragments.get(position).getView());
    }

    //初始化界面（最重要），判断每一页是否已经被创建，若已被创建则将其加入容器中，否则直接返回每个fragment的布局
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = fragments.get(position);
        if (!fragment.isAdded()) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(fragment, fragment.getClass().getSimpleName());
            ft.commit();
            fm.executePendingTransactions();
        }

        if (fragment.getView().getParent() == null) {
            container.addView(fragment.getView());
        }

        return fragment.getView();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
    }

    @SuppressLint("NewApi")
    @Override
    public void onPageSelected(int i) {

        Drawable bulletBlue = tractorAddingActivity.getResources().getDrawable(R.drawable.bullet_blue, null);
        Drawable bulletWhite = tractorAddingActivity.getResources().getDrawable(R.drawable.bullet_white, null);
        imageView[i].setImageDrawable(bulletBlue);
        imageView[pageNum].setImageDrawable(bulletWhite);

        fragments.get(pageNum).onPause();
        if (fragments.get(i).isAdded()) {
            fragments.get(i).onResume();
        }
        pageNum = i;

    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    public int getPageNum() {
        return pageNum;
    }
}
