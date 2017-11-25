package com.luisjavierlinares.android.doing;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;

import com.luisjavierlinares.android.doing.events.DisableAdsPermanentlyEvent;
import com.luisjavierlinares.android.doing.events.GoTopOfListEvent;
import com.luisjavierlinares.android.doing.events.InTheDeepOfAListEvent;
import com.luisjavierlinares.android.doing.events.UserUpdatedEvent;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.managers.AdsManager;
import com.luisjavierlinares.android.doing.utils.UriUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luis on 22/03/2017.
 */

public class DoingFragment extends Fragment {

    private static final String APP_FULL_NAME = "com.luisjavierlinares.android.doing";

    private static final int TOTAL_TABS = 3;

    private static final int HOME_TAB_POS = 0;
    private static final int HISTORY_TAB_POS = 1;
    private static final int FRIENDS_TAB_POS = 2;

    private static final int REQUEST_VIEW_DOING = 0;
    private static final int REQUEST_ADDED_DOING = 1;
    private static final int REQUEST_ADDED_FRIEND = 2;
    private static final int REQUEST_INVITE = 3;

    private static final long CHECK_ONLINE_COUNTDOWN_MILIS = 1 * 1 * 1000;
    private static final long CHECK_USER_REGISTERED_COUNTDOWN_MILIS = 1 * 1 * 1000;
    private static final long ADD_DOING_COUNTDOWN_MILIS = 1 * 1 * 1000;

    private static final String DIALOG_CREATE_MY_USER = "com.luisjavierlinares.android.Doing.dialog_create_my_user";
    private static final String DIALOG_CREATE_DOING = "com.luisjavierlinares.android.Doing.dialog_add_doing";
    private static final String DIALOG_CREATE_FRIEND = "com.luisjavierlinares.android.Doing.dialog_add_friend";
    private static final String DIALOG_ABOUT_APP = "com.luisjavierlinares.android.Doing.dialog_about_app";
    private static final String DIALOG_VIEW_USER = "com.luisjavierlinares.android.doing.dialog_view_user";


    private DoingController mController;
    private AdsManager mAdsManager;
//    private BillingService mBillingService;

    private View mView;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SectionsPageAdapter mAdapter;
    private FloatingActionButton mAddButton;
    private FloatingActionButton mGoUpButton;
    private ProgressBar mProgressBar;
    private DoingUiUtils.AddFabAnimator mFabAnimator;
    private CreateMyUserFragment mCreateMyUserFragment;
    private AdView mAdView;
    private View mRemoveAdsButton;
    private View mRemoveAdsPermaButton;

    private CountDownTimer mChekcOnlineTimer;
    private boolean mIsOnline;
    private CountDownTimer mAddDoingTimer;
    private boolean mCanAddDoing;
    private CountDownTimer mChekcMyUserRegistered;

    private EventBus mEventBus;

    public static DoingFragment newInstance() {
        Bundle args = new Bundle();

        DoingFragment fragment = new DoingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mEventBus = EventBus.getDefault();
        mCanAddDoing = true;
        mIsOnline = true;
        mAdsManager = AdsManager.get(getActivity());
//        mBillingService = BillingService.get(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventBus.register(this);

        if (mController != null) {mController.start();}
//        if (mBillingService != null) {mBillingService.start();}
        updateAdsVisibility();
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventBus.removeAllStickyEvents();
        mEventBus.unregister(this);
        if (mController != null) {mController.stop();}
//        if (mBillingService != null) {mBillingService.stop();}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_doing, container, false);

        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        setHasOptionsMenu(true);

        mAdView = (AdView) mView.findViewById(R.id.adView);

        mRemoveAdsButton = (View) mView.findViewById(R.id.remove_ads_button);
        mRemoveAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAds();
                mAdsManager.disableBannerAds();
            }
        });

        mRemoveAdsPermaButton = (View) mView.findViewById(R.id.remove_ads_perma_button);
        mRemoveAdsPermaButton.setVisibility(View.GONE);
        mRemoveAdsPermaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseDisableAdsPermanently();
            }
        });

        mAddButton = (FloatingActionButton) mView.findViewById(R.id.add_floating_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = mTabLayout.getSelectedTabPosition();

                switch (position) {
                    case HOME_TAB_POS:
                        if (!mCanAddDoing) {
                            Snackbar.make(view, getString(R.string.wait_for_another_doing), Snackbar.LENGTH_LONG)
                                    .show();
                        } else if (!mIsOnline) {
                            Snackbar.make(view, getString(R.string.no_net_message), Snackbar.LENGTH_LONG)
                                    .show();
                        } else {
                            addDoing();
                        }
                        break;
                    case HISTORY_TAB_POS:
                        if (!mCanAddDoing) {
                            Snackbar.make(view, getString(R.string.wait_for_another_doing), Snackbar.LENGTH_LONG)
                                    .show();
                        } else if (!mIsOnline) {
                            Snackbar.make(view, getString(R.string.no_net_message), Snackbar.LENGTH_LONG)
                                    .show();
                        } else {
                            addDoing();
                        }
                        break;
                    case FRIENDS_TAB_POS:
                        if (!mIsOnline) {
                            Snackbar.make(view, getString(R.string.no_net_message), Snackbar.LENGTH_LONG)
                                    .show();
                        } else {
                            addFriend();
                        }
                        break;
                }
            }
        });

        mGoUpButton = (FloatingActionButton) mView.findViewById(R.id.go_up_floating_button);
        mGoUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventBus.post(new GoTopOfListEvent());
            }
        });

        mTabLayout = (TabLayout) mView.findViewById(R.id.tabs);

        mViewPager = (ViewPager) mView.findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(TOTAL_TABS - 1);

        mProgressBar = (ProgressBar) mView.findViewById(R.id.progress_bar);

        updateAdsVisibility();
        loadInitialAppData();

        return mView;
    }

    public void updateOnlineStatus() {
        if (mController.isDeviceOnline()) {
            mIsOnline = true;
        } else {
            mIsOnline = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_ADDED_DOING) {
            User user = AddDoingFragment.getDoingUser(data);
            Doing.DoingAction action = AddDoingFragment.getDoingAction(data);
            String text = AddDoingFragment.getDoingText(data);

            List<User> receivers = new ArrayList<>();
            List<String> doingReceiversUserCode = AddDoingFragment.getDoingReceivers(data);

            for (String doingReceiverUserCode : doingReceiversUserCode) {
                receivers.add(mController.getUserWithCode(doingReceiverUserCode));
            }

            mController.addDoing(user, action, text, receivers);

            initializeAddDoingCountDown();
        }
        if (requestCode == REQUEST_ADDED_FRIEND) {
            String friendName = AddFriendFragment.getFriendAlias(data);
            String userCode = AddFriendFragment.getUserCode(data);
            String friendCode = AddFriendFragment.getFriendCode(data);
            String userName = AddFriendFragment.getUserName(data);

            mController.addFriend(friendName, userCode, friendCode, userName);
        }
//        if (requestCode == mBillingService.REQUEST_PURCHASE) {
//            mBillingService.onHandleResult(requestCode, resultCode, data);
//        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem toogleItem = menu.findItem(R.id.menu_item_toggle_notifications);
        if (DoingSettings.areNotificationsOn(getActivity())) {
            toogleItem.setTitle(R.string.deactivate_notifications);
            toogleItem.setIcon(getResources().getDrawable(R.drawable.ic_notifications_on_white));
            DoingSettings.setNotificationOn(getActivity(), true);
        } else {
            toogleItem.setTitle(R.string.activate_notifications);
            toogleItem.setIcon(getResources().getDrawable(R.drawable.ic_notifications_off_white));
            DoingSettings.setNotificationOn(getActivity(), false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_toggle_notifications:
                toggleNotifications();
                return true;
            case R.id.menu_item_my_profile:
                showMyProfile();
                return true;
            case R.id.menu_item_rate_app:
                showRateApp();
                return true;
            case R.id.menu_item_about:
                showAboutApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void toggleNotifications() {
        boolean shouldNotify = !DoingSettings.areNotificationsOn(getActivity());
        DoingSettings.setNotificationOn(getActivity(), shouldNotify);
        getActivity().invalidateOptionsMenu();
    }

    public void showRateApp() {
        Intent intentApp = new Intent(Intent.ACTION_VIEW, UriUtils.getGooglePlayAppUri(APP_FULL_NAME));
        intentApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        Intent intentHttps = new Intent(Intent.ACTION_VIEW, UriUtils.getGooglePlayAppHttpsUri(APP_FULL_NAME));

        if (intentApp.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intentApp);
        } else if (intentHttps.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intentHttps);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_app_available_for_action),Toast.LENGTH_LONG).show();
        }
    }

    public void showAboutApp() {
        FragmentManager fragmentManager = getFragmentManager();
        InfoAboutAppDialogFragment dialog = InfoAboutAppDialogFragment.newInstance();
        dialog.show(fragmentManager, DIALOG_ABOUT_APP);
    }

    public void showMyProfile() {
        User myUser = mController.getMyUser();
        FragmentManager fragmentManager = getFragmentManager();
        UserDetailFragment dialog = UserDetailFragment.newInstance(myUser.getId());
        dialog.show(fragmentManager, DIALOG_VIEW_USER);
    }

    @Subscribe
    public void onInTheDeepOfAListEvent(InTheDeepOfAListEvent inTheDeepOfAListEvent) {
        //  If the user is in the deep of a list the app shows the Go Up Buttom
        //  Go Up Buttom is only showed in History tab
        if (mViewPager.getCurrentItem() != HISTORY_TAB_POS) {
            return;
        }

        // We show the Go Up buttom if we are deep down the history list
        if (inTheDeepOfAListEvent.isTrue()) {
            mGoUpButton.setVisibility(View.VISIBLE);
        } else {
            mGoUpButton.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe
    public void onDisableAdsPermanently(DisableAdsPermanentlyEvent disableAdsPermanentlyEvent) {
        disableAdsPermanently();
    }

    private void updateUI() {

        mViewPager.setAdapter(mAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case HISTORY_TAB_POS:
                        mGoUpButton.setVisibility(View.INVISIBLE);
                        mEventBus.post(new InTheDeepOfAListEvent(false));
                        break;
                    default:
                        mGoUpButton.setVisibility(View.INVISIBLE);
                        break;
                }
                mFabAnimator.onChangeTabAnimation(tab.getPosition(), isAddButtonActive());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (mViewPager == null) {
            mFabAnimator = new DoingUiUtils.AddFabAnimator(getActivity(), mAddButton);
        } else {
            mFabAnimator = new DoingUiUtils.AddFabAnimator(getActivity(), mAddButton, mViewPager.getCurrentItem());
        }

        updateFriendTab();

        updateAddButtonColor();
    }

    private void initializeUI(ViewPager viewPager) {
        mAdapter = new SectionsPageAdapter(getFragmentManager());
        mAdapter.addFragment(HomeFragment.newInstance(), getString(R.string.home_tab_text));
        mAdapter.addFragment(HistoryFragment.newInstance(), getString(R.string.history_tab_text));
        mAdapter.addFragment(FriendsFragment.newInstance(), getString(R.string.friends_tab_text));

        if (mAdsManager.areBannerAdsEnabled()) {
            MobileAds.initialize(getActivity(), mAdsManager.getAdmobAppId());
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("BDD91807971547591A3B72B538EB15D1")
                    .build();
            mAdView.loadAd(adRequest);
        }
        updateAdsVisibility();
    }

    private void initializeAddDoingCountDown() {
        mCanAddDoing = false;
        updateAddButtonColor();
        mAddDoingTimer = new CountDownTimer(ADD_DOING_COUNTDOWN_MILIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mCanAddDoing = true;
                updateAddButtonColor();
            }
        }.start();
    }

    private void initializeOnlineCheckCountDown() {
        mChekcOnlineTimer = new CountDownTimer(CHECK_ONLINE_COUNTDOWN_MILIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                updateOnlineStatus();
                updateAddButtonColor();
                mChekcOnlineTimer.start();
            }
        }.start();
    }

    private void initializeCheckMyUserRegisteredCountDown() {

        if (mController.isMyUserRegistered()) {
            return;
        }

        mChekcMyUserRegistered = new CountDownTimer(CHECK_USER_REGISTERED_COUNTDOWN_MILIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (mIsOnline) {
                    mController.registerMyUser();
                }

                if (!mController.isMyUserRegistered()) {
                    mChekcMyUserRegistered.start();
                }
            }
        }.start();
    }

    private void askNameFirstTime() {

        User myUser = mController.getMyUser();

        // the first time the app is launched we ask the name of the user
        if ((myUser.getFriendName() == null) && (mCreateMyUserFragment == null)) {
            FragmentManager fragmentManager = getFragmentManager();
            mCreateMyUserFragment = CreateMyUserFragment.newInstance();
            mCreateMyUserFragment.show(fragmentManager, DIALOG_CREATE_MY_USER);
        }
    }

    private Boolean isAddButtonActive() {
        if (((mCanAddDoing) && (mIsOnline)) || ((mTabLayout.getSelectedTabPosition() == FRIENDS_TAB_POS) && (mIsOnline))) {
            return true;
        } else {
            return false;
        }
    }

    private void updateAddButtonColor() {
        if (isAddButtonActive()) {
            mAddButton.setAlpha(DoingUiUtils.getActiveButtonAlpha(getActivity()));
        } else {
            mAddButton.setAlpha(DoingUiUtils.getInactiveButtonAlpha(getActivity()));
        }
    }

    public void updateFriendTab() {
        LinearLayout friendTabLayout;
        TabLayout.Tab friendTab = mTabLayout.getTabAt(FRIENDS_TAB_POS);
        if (friendTab.getCustomView() == null) {
            friendTabLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.tab_friends, null);
            friendTab.setCustomView(friendTabLayout);
         } else {
            friendTabLayout = (LinearLayout) friendTab.getCustomView();
        }

        TextView friendTabText = (TextView) friendTabLayout.findViewById(R.id.friend_tab_text);
//        friendTabText.setTextColor(getResources().getColorStateList(R.color.text_tab_indicator));

        ImageView friendTabIcon = (ImageView) friendTabLayout.findViewById(R.id.friend_tab_icon);
        if (mController.hasPendingInvitations()) {
            friendTabIcon.setVisibility(View.VISIBLE);
        } else {
            friendTabIcon.setVisibility(View.GONE);
        }

        ViewGroup.MarginLayoutParams layoutParams = ((ViewGroup.MarginLayoutParams) friendTabIcon.getLayoutParams());
        layoutParams.bottomMargin = 0;
        friendTabIcon.requestLayout();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUserUpdatedEvent(UserUpdatedEvent userUpdatedEvent) {
        updateFriendTab();
    }

    public class SectionsPageAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public SectionsPageAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    private void loadInitialAppData() {
        new InitialLoadTask().execute();
    }

    private void addDoing() {
        FragmentManager fragmentManager = getFragmentManager();
        AddDoingFragment addDoingDialog = AddDoingFragment.newInstance();
        addDoingDialog.setTargetFragment(DoingFragment.this, REQUEST_ADDED_DOING);
        addDoingDialog.show(fragmentManager, DIALOG_CREATE_DOING);
    }

    private void addFriend() {
        FragmentManager fragmentManager = getFragmentManager();
        AddFriendFragment addFriendDialog = AddFriendFragment.newInstance();
        addFriendDialog.setTargetFragment(DoingFragment.this, REQUEST_ADDED_FRIEND);
        addFriendDialog.show(fragmentManager, DIALOG_CREATE_FRIEND);
    }


    private void purchaseDisableAdsPermanently() {
//        mBillingService.purchaseRemoveAds();
    }

    private void updateAdsVisibility() {
        if (mAdsManager.areBannerAdsEnabled()) {
            enableAds();
        } else if (mAdsManager.areAdsPermanentlyDisabled()) {
            disableAdsPermanently();
        } else if (!mAdsManager.areBannerAdsEnabled()) {
            disableAds();
        }
    }

    private void enableAds() {
        mAdView.setVisibility(View.VISIBLE);
        mRemoveAdsButton.setVisibility(View.VISIBLE);
    }

    private void disableAds() {
        ViewGroup parent = (ViewGroup) mAdView.getParent();
        if (parent != null) {
            parent.removeView(mAdView);
        }

        parent = (ViewGroup) mRemoveAdsButton.getParent();
        if (parent != null) {
            parent.removeView(mRemoveAdsButton);
        }

//        mRemoveAdsPermaButton.setVisibility(View.VISIBLE);
    }

    private void disableAdsPermanently() {
        disableAds();
        mRemoveAdsPermaButton.setVisibility(View.GONE);
    }

    private class InitialLoadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mController = DoingController.get(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressBar.setVisibility(View.INVISIBLE);
            initializeUI(mViewPager);
            updateUI();
            askNameFirstTime();
            initializeOnlineCheckCountDown();
            initializeCheckMyUserRegisteredCountDown();
        }

    }

}
