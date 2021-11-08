package com.tkpark86.runch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Picasso;
import com.tkpark86.runch.common.HackyViewPager;

import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;


public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = "Goodruns";
    private static final String ISLOCKED_ARG = "isLocked";

    private HackyViewPager mViewPager;
    private MenuItem menuLockItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);


        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        //setContentView(mViewPager);

        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }

        // received data
        Bundle bundle = getIntent().getExtras();
        String position = bundle.getString("position");

        int cnt = bundle.getInt("cnt");

        List<String> list = new ArrayList<>();
        for(int i = 0; i<cnt; i++) {
            String url = "";
            if(i == 0) {
                url = bundle.getString("url_1");
            } else if(i == 1) {
                url = bundle.getString("url_2");
            } else if(i == 2) {
                url = bundle.getString("url_3");
            } else if(i == 3) {
                url = bundle.getString("url_4");
            } else if(i == 4) {
                url = bundle.getString("url_5");
            }

            list.add(url);
        }

        mViewPager.setAdapter(new SamplePagerAdapter(this, list));
        mViewPager.setCurrentItem(Integer.parseInt(position));

        Log.d(TAG, "onCreate: position="+position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewpager_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuLockItem = menu.findItem(R.id.menu_lock);
        toggleLockBtnTitle();
        menuLockItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                toggleViewPagerScrolling();
                toggleLockBtnTitle();
                return true;
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private void toggleViewPagerScrolling() {
        if (isViewPagerActive()) {
            ((HackyViewPager) mViewPager).toggleLock();
        }
    }

    private void toggleLockBtnTitle() {
        boolean isLocked = false;
        if (isViewPagerActive()) {
            isLocked = ((HackyViewPager) mViewPager).isLocked();
        }
        String title = (isLocked) ? getString(R.string.menu_unlock) : getString(R.string.menu_lock);
        if (menuLockItem != null) {
            menuLockItem.setTitle(title);
        }
    }

    private boolean isViewPagerActive() {
        return (mViewPager != null && mViewPager instanceof HackyViewPager);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (isViewPagerActive()) {
            outState.putBoolean(ISLOCKED_ARG, ((HackyViewPager) mViewPager).isLocked());
        }

        super.onSaveInstanceState(outState);
    }



    static class SamplePagerAdapter extends PagerAdapter {
        private Context context;
        private List<String> list;

        public SamplePagerAdapter(Context _context, List<String> _list) {
            context = _context;
            list = _list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());

            Picasso.with(context)
                    .load(list.get(position))
                    .error(R.drawable.ic_error)
                    .into(photoView);

            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
