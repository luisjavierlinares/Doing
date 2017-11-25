package com.luisjavierlinares.android.doing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.luisjavierlinares.android.doing.events.CommentaryAddedEvent;
import com.luisjavierlinares.android.doing.events.DisableAdsPermanentlyEvent;
import com.luisjavierlinares.android.doing.events.DoingAddedEvent;
import com.luisjavierlinares.android.doing.events.GoTopOfListEvent;
import com.luisjavierlinares.android.doing.events.InTheDeepOfAListEvent;
import com.luisjavierlinares.android.doing.events.LikeAddedEvent;
import com.luisjavierlinares.android.doing.events.UserUpdatedEvent;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.managers.AdsManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.DoingDetailCallbacks.DOING_LIST_TYPE;

/**
 * Created by Luis on 23/03/2017.
 */

public class UserHistoryFragment extends RefreshingVisibleFragment {

    private static final String ARG_USER_ID = "com.luisjavierlinares.android.Doing.UserHistoryFragment.user_id";

    private static final int REQUEST_DOING_ID = 0;
    private static final int DEEP_IN_THE_LIST_POSITON = 25;

    private DoingController mController;
    private AdsManager mAdsManager;
//    private BillingService mBillingService;

    private User mUser;
    private Doing.DoingAction mDoingAction;

    private View mView;
    private RecyclerView mRecyclerView;
    private DoingAdapter mDoingAdapter;
    private ImageView mNoDoingsImageView;
    private TextView mNoDoingsTextView;
    private Parcelable mRecyclerViewState;

    private ImageView mAllFilterButton;
    private ImageView mListenFilterButton;
    private ImageView mPlayFilterButton;
    private ImageView mReadFilterButton;
    private ImageView mWatchFilterButton;
    private ImageView mEnjoyFilterButton;

    private AdView mAdView;
    private View mRemoveAdsButton;
    private View mRemoveAdsPermaButton;

    private boolean mIsInTheDeepOfAList;

    private EventBus mEventBus;

    private DoingDetailCallbacks mCallbacks;

    public static UserHistoryFragment newInstance(UUID userId) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_USER_ID, userId);

        UserHistoryFragment fragment = new UserHistoryFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mController = DoingController.get(getActivity());
        UUID userId = (UUID) getArguments().getSerializable(ARG_USER_ID);
        mUser = mController.getUser(userId);
        mDoingAction = null;
        mEventBus = EventBus.getDefault();
        mAdsManager = AdsManager.get(getActivity());
//        mBillingService = BillingService.get(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventBus.register(this);
        if (mController != null) {mController.start();}
//        if (mBillingService != null) {mBillingService.start();}
        restoreState();
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveState();
        mEventBus.unregister(this);
        if (mController != null) {mController.stop();}
//        if (mBillingService != null) {mBillingService.stop();}
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof DoingDetailCallbacks) {
            mCallbacks = (DoingDetailCallbacks) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement DongDetailCallbacks");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_user_history, container, false);

        mAdView = (AdView) mView.findViewById(R.id.adView);

        mRemoveAdsButton = (View) mView.findViewById(R.id.remove_ads_button);
        mRemoveAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdsManager.disableBannerAds();
                disableAds();
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

        if (mAdsManager.areBannerAdsEnabled()) {
            MobileAds.initialize(getActivity(), mAdsManager.getAdmobAppId());
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("BDD91807971547591A3B72B538EB15D1")
                    .build();
            mAdView.loadAd(adRequest);
        }
        updateAdsVisibility();

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.history_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                postInTheDeepOfAList();
            }

        });

        mNoDoingsImageView = (ImageView) mView.findViewById(R.id.history_no_doings_image);
        mNoDoingsTextView = (TextView) mView.findViewById(R.id.history_no_doings_text);

        mAllFilterButton = (ImageView) mView.findViewById(R.id.filter_select_all);
        mAllFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoingAction = null;
                updateUI();
            }
        });

        mListenFilterButton = (ImageView) mView.findViewById(R.id.filter_listen);
        mListenFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoingAction = Doing.DoingAction.LISTENING;
                updateUI();
            }
        });

        mPlayFilterButton = (ImageView) mView.findViewById(R.id.filter_play);
        mPlayFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoingAction = Doing.DoingAction.PLAYING;
                updateUI();
            }
        });

        mReadFilterButton = (ImageView) mView.findViewById(R.id.filter_read);
        mReadFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoingAction = Doing.DoingAction.READING;
                updateUI();
            }
        });

        mWatchFilterButton = (ImageView) mView.findViewById(R.id.filter_watch);
        mWatchFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoingAction = Doing.DoingAction.WATCHING;
                updateUI();
            }
        });

        mEnjoyFilterButton = (ImageView) mView.findViewById(R.id.filter_enjoy);
        mEnjoyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoingAction = Doing.DoingAction.ENJOYING;
                updateUI();
            }
        });

        updateAdsVisibility();
        updateUI();

        String userName = DoingUiUtils.getUserNameMeString(getActivity(), mUser);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(userName);

        return mView;
    }

    @Override
    public void update() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
//        if (requestCode == mBillingService.REQUEST_PURCHASE) {
//            mBillingService.onHandleResult(requestCode, resultCode, data);
//        }
    }

    @Subscribe
    public void onDoingAddedEvent(DoingAddedEvent doingAddedEvent) {
        updateUI();
    }

    @Subscribe
    public void onLikeAddedEvent(LikeAddedEvent likeAddedEvent) {
        mDoingAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onCommentaryAddedEvent(CommentaryAddedEvent commentaryAddedEvent) {
        mDoingAdapter.notifyDataSetChanged();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUserUpdatedEvent(UserUpdatedEvent userUpdatedEvent) {
        mDoingAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onGoTopOfListEvent(GoTopOfListEvent goTopOfListEvent){
        if (mRecyclerView == null) {
            return;
        }

        mRecyclerView.scrollToPosition(0);
    }

    @Subscribe
    public void onInTheDeepOfAListEvent(InTheDeepOfAListEvent inTheDeepOfAListEvent) {
        mIsInTheDeepOfAList = inTheDeepOfAListEvent.isTrue();
        postInTheDeepOfAList();
    }

    @Subscribe
    public void onDisableAdsPermanentyl(DisableAdsPermanentlyEvent disableAdsPermanentlyEvent) {
        disableAdsPermanently();
    }

    private void updateUI() {
        List<Doing> doings = new ArrayList<>();
        if (mDoingAction == null) {
            doings = mController.getAllDoingsFromUser(mUser);
        } else {
            doings = mController.getAllDoingsFromUser(mUser, mDoingAction);
        }

        mDoingAdapter = new DoingAdapter(doings);
        mDoingAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mDoingAdapter);

        setNoDoingImage();
        if (doings.size() == 0) {
            mNoDoingsImageView.setVisibility(View.VISIBLE);
            mNoDoingsTextView.setVisibility(View.VISIBLE);
        } else {
            mNoDoingsImageView.setVisibility(View.INVISIBLE);
            mNoDoingsTextView.setVisibility(View.INVISIBLE);
        }

        postInTheDeepOfAList();

        updateAdsVisibility();
    }

    private void setNoDoingImage() {
        Drawable drawable;

        if (mDoingAction == null) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.no_action_history);
        } else if (mDoingAction == Doing.DoingAction.LISTENING) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.action_listening);
        }  else if (mDoingAction == Doing.DoingAction.PLAYING) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.action_playing);
        } else if (mDoingAction == Doing.DoingAction.READING) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.action_reading);
        } else if (mDoingAction == Doing.DoingAction.WATCHING) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.action_watching);
        } else if (mDoingAction == Doing.DoingAction.ENJOYING) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.action_enjoying);
        } else {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.no_action_history);
        }

        drawable.clearColorFilter();
        mNoDoingsImageView.setImageDrawable(drawable);
    }

    public void postInTheDeepOfAList() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int currentFirstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        if ((currentFirstVisibleItem > DEEP_IN_THE_LIST_POSITON) && !mIsInTheDeepOfAList) {
            mIsInTheDeepOfAList = true;
            mEventBus.post(new InTheDeepOfAListEvent(mIsInTheDeepOfAList));
        } else if ((currentFirstVisibleItem <= DEEP_IN_THE_LIST_POSITON) && mIsInTheDeepOfAList) {
            mIsInTheDeepOfAList = false;
            mEventBus.post(new InTheDeepOfAListEvent(mIsInTheDeepOfAList));
        }
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

    private void saveState() {
        mRecyclerViewState = mRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    private void restoreState() {
        mRecyclerView.getLayoutManager().onRestoreInstanceState(mRecyclerViewState);
    }

    private class DoingHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Doing mDoing;
        private TextView mUserName;
        private ImageView mUserAvatar;
        private TextView mDoingVerb;
        private TextView mDoingText;
        private TextView mAgoText;
        private ImageView mActionImage;
        private ImageView mLikeImage;
        private TextView mTotalNormalLikes;
        private TextView mTotalCommentaries;
        private View mLikesAndCommentaries;
        private View mItemView;

        public DoingHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mItemView = itemView;
            mUserName = (TextView) itemView.findViewById(R.id.history_item_user_name);
            mUserAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
            mDoingVerb = (TextView) itemView.findViewById(R.id.history_item_verb);
            mDoingText = (TextView) itemView.findViewById(R.id.history_item_doing_text);
            mAgoText = (TextView) itemView.findViewById(R.id.history_item_ago_text);
            mActionImage = (ImageView) itemView.findViewById(R.id.history_item_action_image);
            mLikeImage = (ImageView) itemView.findViewById(R.id.history_item_like_image);
            mTotalNormalLikes = (TextView) itemView.findViewById(R.id.item_status_normal_heart_number);
            mTotalCommentaries = (TextView) itemView.findViewById(R.id.item_status_commentaries_number);
            mLikesAndCommentaries = (View) itemView.findViewById(R.id.history_likes_and_commentaries);
        }

        public void bindDoing(final Doing doing) {

            if (doing == null) {
                mItemView.setVisibility(View.GONE);
                return;
            } else {
                mItemView.setVisibility(View.VISIBLE);
            }

            mDoing = doing;

            mUserName.setText(DoingUiUtils.getDoingUserNameString(getActivity(), mDoing));

            mUserAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), mDoing.getUser()));

            mDoingVerb.setText(DoingUiUtils.getDoingHistoryVerbString(getActivity(), mDoing));

            User myUser = mController.getMyUser();
            if (myUser.isMine(mDoing)) {
                mLikeImage.setVisibility(View.INVISIBLE);
            } else {
                mLikeImage.setVisibility(View.VISIBLE);
            }

            mDoingText.setText(doing.getText());

            mActionImage.setImageDrawable(DoingUiUtils.getDoingActionDrawable(getActivity(), doing));
            mActionImage.setColorFilter(DoingUiUtils.getDoingActionColor(getActivity(), doing));

            mLikeImage.setColorFilter(DoingUiUtils.getNormalLikeColor(getActivity()));
            if (mDoing.isLikedByMe()){
                mLikeImage.setImageDrawable(DoingUiUtils.getNormalFilledLikeDrawable(getActivity()));
            } else {
                mLikeImage.setImageDrawable(DoingUiUtils.getNormalEmptyLikeDrawable(getActivity()));
            }
            mLikeImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User myUser = mController.getMyUser();
                    mController.addLike(myUser, mDoing);
                }
            });

            mAgoText.setText(DoingUiUtils.getDoingAgoDateString(getActivity(), doing));

            mTotalNormalLikes.setText(doing.getLikesCount().toString());
            mTotalCommentaries.setText(doing.getCommentariesCount().toString());

            mLikesAndCommentaries.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onDoingSelected(mDoing, DOING_LIST_TYPE.USER);
        }
    }

    private class DoingAdapter extends RecyclerView.Adapter<DoingHolder> {

        private List<Doing> mDoings;

        public DoingAdapter(List<Doing> doings) {
            mDoings = doings;
        }

        @Override
        public UserHistoryFragment.DoingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_doing_history, parent, false);

            return new UserHistoryFragment.DoingHolder(view);
        }

        @Override
        public void onBindViewHolder(DoingHolder holder, int position) {
            Doing doing = mDoings.get(position);
            holder.bindDoing(doing);
        }

        @Override
        public int getItemCount() {
            return mDoings.size();
        }
    }
}


