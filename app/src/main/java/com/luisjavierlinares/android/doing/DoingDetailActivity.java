package com.luisjavierlinares.android.doing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.luisjavierlinares.android.doing.events.DoingReceivedEvent;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.DoingDetailCallbacks.DOING_LIST_TYPE;

/**
 * Created by Luis on 11/04/2017.
 */

public class DoingDetailActivity extends AppCompatActivity {

    private static final String EXTRA_DOING_ID = "com.luisjavierlinares.android.doing.DoingDetailActivity.extra_doing_id";
    private static final String EXTRA_LIST_TYPE = "com.luisjavierlinares.android.doing.DoingDetailActivity.extra_list_type";

    private static final int REQUEST_DOING_INFO = 0;

    private static final String DIALOG_DOING_INFO = "com.luisjavierlinares.android.Doing.DoingDetailActivity.dialog_doing_info";

    private Toolbar mToolbar;
    private ViewPager mViewPager;

    private DoingController mController;
    private List<Doing> mDoings;

    public static Intent newIntent(Context packageContext, UUID doingId) {
        return newIntent(packageContext, doingId, DOING_LIST_TYPE.ALL);
    }

    public static Intent newIntent(Context packageContext, UUID doingId, DOING_LIST_TYPE List_type) {
        Intent intent = new Intent(packageContext, DoingDetailActivity.class);
        intent.putExtra(EXTRA_DOING_ID, doingId);
        intent.putExtra(EXTRA_LIST_TYPE, List_type);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_doing_detail);

        mToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mController = DoingController.get(this);
        UUID doingId = (UUID) getIntent().getSerializableExtra(EXTRA_DOING_ID);
        DOING_LIST_TYPE list_type = (DOING_LIST_TYPE) getIntent().getSerializableExtra(EXTRA_LIST_TYPE);

        mViewPager = (ViewPager) findViewById(R.id.activity_doing_detail_view_pager);

        if (list_type == DOING_LIST_TYPE.RECENT) {
            mDoings = mController.getRecentDoings();
        } else if (list_type == DOING_LIST_TYPE.USER) {
            User user = mController.getDoing(doingId).getUser();
            mDoings = mController.getAllDoingsFromUser(user);
        } else {
            mDoings = mController.getAllDoingsFromDataBase();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Doing doing = mDoings.get(position);
                return DoingDetailFragment.newInstance(doing.getId());
            }

            @Override
            public int getCount() {
                return mDoings.size();
            }
        });

        for (int i = 0; i < mDoings.size(); i++) {
            if (mDoings.get(i).getId().equals(doingId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        super.onCreateOptionsMenu(menu);
        menuInflater.inflate(R.menu.menu_doing_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_item_doing_detail_info:
                int position = mViewPager.getCurrentItem();
                Doing doing = mDoings.get(position);
                FragmentManager fragmentManager = getSupportFragmentManager();
                DoingInfoFragment doingInfoFragment = DoingInfoFragment.newInstance(doing.getId());
                doingInfoFragment.show(fragmentManager, DIALOG_DOING_INFO);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDoingReceivedEvent(DoingReceivedEvent doingReceivedEvent) {
        mViewPager.getAdapter().notifyDataSetChanged();
    }

}
