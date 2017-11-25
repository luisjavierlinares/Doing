package com.luisjavierlinares.android.doing;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.events.LikeAddedEvent;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.Like;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.utils.WebSearcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by Luis on 11/04/2017.
 */

public class DoingDetailFragment extends RefreshingVisibleFragment {

    private static final String ARG_DOING_ID = "com.luisjavierlinares.android.Doing.DoingDetailFragment.doing_id";
    private static final String DIALOG_DOING_LIKES = "com.luisjavierlinares.android.doing.DoingLikesFragment.dialog_doing_likes";
    private static final String DIALOG_DOING_COMMENTARIES = "com.luisjavierlinares.android.doing.DoingLikesFragment.dialog_doing_commentaries";

    private DoingController mController;
    private Doing mDoing;

    private View mView;
    private Toolbar mToolbar;
    private DoingCommentariesFragment mCommentariesDialog;
    private DoingLikesFragment mLikesDialog;
    private TextView mUserName;
    private ImageView mUserAvatar;
    private TextView mDoingVerb;
    private ImageView mActionImage;
    private TextView mDoingText;
    private ImageView mSearchWebButton;
    private TextView mAgoText;
    private ViewGroup mMyLikeView;
    private ImageView mILikeImage;
    private ViewGroup mNoLikesView;
    private ViewGroup mLikesView;
    private ViewGroup mLike1View;
    private ViewGroup mLike2View;
    private ViewGroup mLike3View;
    private TextView mUserLike1Name;
    private TextView mUserLike2Name;
    private TextView mUserLike3Name;
    private ViewGroup mMoreLikesView;
    private TextView mMoreLikesText;
    private ViewGroup mNoCommentariesView;
    private ViewGroup mCommentariesView;
    private TextView mCommentariesCount;

    private EventBus mEventBus;

    public static DoingDetailFragment newInstance(UUID doingId) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_DOING_ID, doingId);

        DoingDetailFragment doingDetailFragment = new DoingDetailFragment();
        doingDetailFragment.setArguments(arguments);
        return doingDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mController = DoingController.get(getActivity());
        UUID doingId = (UUID) getArguments().getSerializable(ARG_DOING_ID);
        mDoing = mController.getDoing(doingId);
        applyFix();
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkLikesAsNotNew();
    }

    @Override
    public void onStop() {
        super.onStop();
        checkLikesAsNotNew();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventBus.register(this);
        if (mController != null) {mController.start();}
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventBus.unregister(this);
        if (mController != null) {
            mController.stop();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.fragment_doing_detail, container, false);

        mUserName = (TextView) mView.findViewById(R.id.detail_item_user_name);
        mUserName.setText(DoingUiUtils.getDoingUserNameString(getActivity(), mDoing));

        mUserAvatar = (ImageView) mView.findViewById(R.id.user_avatar);
        mUserAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), mDoing.getUser()));

        mDoingVerb = (TextView) mView.findViewById(R.id.detail_item_verb);
        mDoingVerb.setText(DoingUiUtils.getDoingVerbString(getActivity(), mDoing));

        mActionImage = (ImageView) mView.findViewById(R.id.detail_item_action_image);
        mActionImage.setImageDrawable(DoingUiUtils.getDoingActionDrawable(getActivity(), mDoing));
        mActionImage.setColorFilter(DoingUiUtils.getDoingActionColor(getActivity(), mDoing));

        mDoingText = (TextView) mView.findViewById(R.id.detail_item_doing_text);
        mDoingText.setText(mDoing.getText());
        mDoingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebSearcher webSearcher = new WebSearcher(getActivity(), WebSearcher.SearchEngine.GOOGLE);
                try {
                    webSearcher.search(mDoingText.getText().toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        mSearchWebButton = (ImageView) mView.findViewById(R.id.detail_item_search_web);
        mSearchWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebSearcher webSearcher = new WebSearcher(getActivity(), WebSearcher.SearchEngine.GOOGLE);
                try {
                    webSearcher.search(mDoingText.getText().toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        mAgoText = (TextView) mView.findViewById(R.id.details_item_ago_text);
        mAgoText.setText(DoingUiUtils.getDoingAgoDateString(getActivity(), mDoing));

        User myUser = mController.getMyUser();

        mMyLikeView = (ViewGroup) mView.findViewById(R.id.details_item_i_like);

        mILikeImage = (ImageView) mView.findViewById(R.id.details_item_i_like_image);
        mILikeImage.setColorFilter(DoingUiUtils.getNormalLikeColor(getActivity()));
        mILikeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User myUser = mController.getMyUser();
                mController.addLike(myUser, mDoing);
            }
        });

        if (myUser.isMine(mDoing)) {
            mMyLikeView.setVisibility(View.GONE);
        } else {
            mMyLikeView.setVisibility(View.VISIBLE);
            if (mDoing.isLikedByMe()) {
                mILikeImage.setImageDrawable(DoingUiUtils.getNormalFilledLikeDrawable(getActivity()));
            } else {
                mILikeImage.setImageDrawable(DoingUiUtils.getNormalEmptyLikeDrawable(getActivity()));
            }
        }

        mNoLikesView = (ViewGroup) mView.findViewById(R.id.details_item_no_likes);
        mLikesView = (ViewGroup) mView.findViewById(R.id.details_item_likes);
        mLike1View = (ViewGroup) mView.findViewById(R.id.details_item_like_1);
        mLike2View = (ViewGroup) mView.findViewById(R.id.details_item_like_2);
        mLike3View = (ViewGroup) mView.findViewById(R.id.details_item_like_3);
        mMoreLikesView = (ViewGroup) mView.findViewById(R.id.details_item_more_likes);
        mUserLike1Name = (TextView) mView.findViewById(R.id.details_item_like_user_1);
        mUserLike2Name = (TextView) mView.findViewById(R.id.details_item_like_user_2);
        mUserLike3Name = (TextView) mView.findViewById(R.id.details_item_like_user_3);
        mMoreLikesText = (TextView) mView.findViewById(R.id.details_item_more_likes_i);

        mNoCommentariesView = (ViewGroup) mView.findViewById(R.id.details_item_no_commentaries);
        mCommentariesView = (ViewGroup) mView.findViewById(R.id.details_item_commentaries);
        mCommentariesCount = (TextView) mView.findViewById(R.id.details_item_commentaries_total);

        updateUI();

        return mView;
    }

    @Override
    public void update() {

        // do not update when the likes or commentaries dialog is visible
        if ((mLikesDialog != null) && (mLikesDialog.isVisible())) {
            return;
        }

        if ((mCommentariesDialog != null) && (mCommentariesDialog.isVisible())) {
            return;
        }

        if (!getUserVisibleHint()) {
            return;
        }

        updateUI();
    };

    private void updateLikesPreview() {
        User myUser = mController.getMyUser();

        List<Like> allLikes = mDoing.getLikes();
        List<Like> likes = new ArrayList<>();

//      Find likes that have not been sent by the the user and belong to known users
        for (int i = 0; i < allLikes.size(); i++) {
            User sender = allLikes.get(i).getSender();
            if ((!mController.isMe(sender)) && (!sender.isUnknown())) {
                likes.add(allLikes.get(i));
            }
        }

//      Find likes that have not been sent by the the user and belong to unknown users
        for (int i = 0; i < allLikes.size(); i++) {
            User sender = allLikes.get(i).getSender();
            if ((!mController.isMe(sender)) && (sender.isUnknown())) {
                likes.add(allLikes.get(i));
            }
        }

        if (likes.size() > 0) {
            mNoLikesView.setVisibility(View.GONE);
        } else if (!myUser.isMine(mDoing)){
            mNoLikesView.setVisibility(View.GONE);
        } else {
            mNoLikesView.setVisibility(View.VISIBLE);
            ImageView noLikeImage = (ImageView) mNoLikesView.findViewById(R.id.details_item_no_likes_image);
            noLikeImage.setColorFilter(DoingUiUtils.getNormalLikeColor(getActivity()));
        }

        switch (likes.size()) {
            case 0:
                mLikesView.setVisibility(View.GONE);
                mMoreLikesView.setVisibility(View.GONE);
                break;
            case 1:
                mLikesView.setVisibility(View.VISIBLE);
                mLike1View.setVisibility(View.VISIBLE);
                mUserLike1Name.setText(DoingUiUtils.getUserNameString(getActivity(), likes.get(0).getSender()));
                mLike2View.setVisibility(View.GONE);
                mLike3View.setVisibility(View.GONE);
                mMoreLikesView.setVisibility(View.GONE);
                break;
            case 2:
                mLikesView.setVisibility(View.VISIBLE);
                mLike1View.setVisibility(View.VISIBLE);
                mUserLike1Name.setText(DoingUiUtils.getUserNameString(getActivity(), likes.get(0).getSender()));
                mLike2View.setVisibility(View.VISIBLE);
                mUserLike2Name.setText(DoingUiUtils.getUserNameString(getActivity(), likes.get(1).getSender()));
                mLike3View.setVisibility(View.GONE);
                mMoreLikesView.setVisibility(View.GONE);
                break;
            case 3:
                mLikesView.setVisibility(View.VISIBLE);
                mLike1View.setVisibility(View.VISIBLE);
                mUserLike1Name.setText(DoingUiUtils.getUserNameString(getActivity(), likes.get(0).getSender()));
                mLike2View.setVisibility(View.VISIBLE);
                mUserLike2Name.setText(DoingUiUtils.getUserNameString(getActivity(), likes.get(1).getSender()));
                mLike3View.setVisibility(View.VISIBLE);
                mUserLike3Name.setText(DoingUiUtils.getUserNameString(getActivity(), likes.get(2).getSender()));
                mMoreLikesView.setVisibility(View.GONE);
                break;
            default:
                mLikesView.setVisibility(View.VISIBLE);
                mLike1View.setVisibility(View.VISIBLE);
                mUserLike1Name.setText(DoingUiUtils.getUserNameString(getActivity(), likes.get(0).getSender()));
                mLike2View.setVisibility(View.VISIBLE);
                mUserLike2Name.setText(DoingUiUtils.getUserNameString(getActivity(), likes.get(1).getSender()));
                mLike3View.setVisibility(View.VISIBLE);
                mUserLike3Name.setText(DoingUiUtils.getUserNameString(getActivity(), likes.get(2).getSender()));
                mMoreLikesView.setVisibility(View.VISIBLE);
                Integer moreLikes = likes.size() - 3;
                mMoreLikesText.setText(moreLikes.toString());
                break;
        }

        if (likes.size()>0) {
            mLikesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getFragmentManager();
                    mLikesDialog = DoingLikesFragment.newInstance(mDoing.getId());
                    mLikesDialog.show(fragmentManager, DIALOG_DOING_LIKES);
                }
            });
        } else {
            mNoLikesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getFragmentManager();
                    mLikesDialog = DoingLikesFragment.newInstance(mDoing.getId());
                    mLikesDialog.show(fragmentManager, DIALOG_DOING_LIKES);
                }
            });
        }

    }

    private void updateCommentariesPreview() {

        if (mDoing.getCommentariesCount() > 0) {
            mNoCommentariesView.setVisibility(View.GONE);
            mCommentariesView.setVisibility(View.VISIBLE);
            mCommentariesCount.setText(String.valueOf(mDoing.getCommentariesCount()));
            mCommentariesCount.setTypeface(null, DoingUiUtils.getCommentariesCountStyle(getActivity(), mDoing));
        } else {
            mNoCommentariesView.setVisibility(View.VISIBLE);
            mCommentariesView.setVisibility(View.GONE);
        }

        mNoCommentariesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                mCommentariesDialog = DoingCommentariesFragment.newInstance(mDoing.getId());
                mCommentariesDialog.show(fragmentManager, DIALOG_DOING_COMMENTARIES);
             }
        });

        mCommentariesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                mCommentariesDialog = DoingCommentariesFragment.newInstance(mDoing.getId());
                mCommentariesDialog.show(fragmentManager, DIALOG_DOING_COMMENTARIES);
            }
        });

    }

    private void updateUI() {
        mController.updateCounts(mDoing);
        mController.updateHasNew(mDoing);
        updateLikesPreview();
        updateCommentariesPreview();
    }

    private void checkLikesAsNotNew() {
        int otherLikesCount = mDoing.getLikesCount();
        if (mDoing.isLikedByMe()) {
            otherLikesCount = otherLikesCount - 1;
        }
        if ((mDoing.hasNewLikes()) && (otherLikesCount < 4)) {
            mDoing.setHasNewLikes(false);
            mController.updateDoing(mDoing);
        }
    }

    private void applyFix() {
        mController.fixDatabaseCounts(mDoing);
    }

    @Subscribe
    public void onLikeAddedEvent(LikeAddedEvent likeAddedEvent) {
        Like like = likeAddedEvent.getLike();

        if (like == null) {
            return;
        }

        if (mDoing.getId().equals(like.getDoing().getId()) && (mController.isMe(like.getSender()))) {
            mILikeImage.setImageDrawable(DoingUiUtils.getNormalFilledLikeDrawable(getActivity()));
        }
    }
}
