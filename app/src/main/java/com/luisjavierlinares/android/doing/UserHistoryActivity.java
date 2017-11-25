package com.luisjavierlinares.android.doing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.luisjavierlinares.android.doing.model.Doing;

import java.util.UUID;

/**
 * Created by Luis on 03/05/2017.
 */

public class UserHistoryActivity extends AppCompatActivity implements DoingDetailCallbacks {

    private static final String EXTRA_USER_ID = "com.luisjavierlinares.android.doing.UserHistoryActivity.extra_user_id";

    private Toolbar mToolbar;

    public static Intent newIntent(Context packageContext, UUID userId) {
        Intent intent = new Intent(packageContext, UserHistoryActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstaceState){
        super.onCreate(savedInstaceState);
        setContentView(R.layout.activity_user_history);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setSubtitle(R.string.my_user_title);
        }

        UUID userId = (UUID) getIntent().getSerializableExtra(EXTRA_USER_ID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment == null){
            fragment = UserHistoryFragment.newInstance(userId);
            fragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDoingSelected(Doing doing, DOING_LIST_TYPE List_type) {
        Intent intent = DoingDetailActivity.newIntent(this, doing.getId(), List_type);
        startActivity(intent);
    }

    @Override
    public void onDoingSelected(Doing doing) {
        Intent intent = DoingDetailActivity.newIntent(this, doing.getId());
        startActivity(intent);
    }

}
