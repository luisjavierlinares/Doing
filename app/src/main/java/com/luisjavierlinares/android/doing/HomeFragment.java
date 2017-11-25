package com.luisjavierlinares.android.doing;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.events.CommentaryAddedEvent;
import com.luisjavierlinares.android.doing.events.DoingAddedEvent;
import com.luisjavierlinares.android.doing.events.DoingReceivedEvent;
import com.luisjavierlinares.android.doing.events.LikeAddedEvent;
import com.luisjavierlinares.android.doing.events.UserUpdatedEvent;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.luisjavierlinares.android.doing.DoingDetailCallbacks.DOING_LIST_TYPE;

/**
 * Created by Luis on 23/03/2017.
 */

public class HomeFragment extends RefreshingVisibleFragment {

    private static final int REQUEST_DOING_ID = 0;

    private DoingController mController;
    List<Doing> mDoings;

    private RecyclerView mRecyclerView;
    private DoingAdapter mDoingAdapter;
    private ImageView mNoDoingsImageView;
    private TextView mNoDoingsTextView;
    private Parcelable mRecyclerViewState;

    private EventBus mEventBus;

    private DoingDetailCallbacks mCallbacks;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventBus.register(this);
        restoreState();
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveState();
        mEventBus.unregister(this);
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
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.home_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    cancelUpdateTimer();
                } else {
                    startUpdateTimer();
                }
            }
        });
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        mNoDoingsImageView = (ImageView) view.findViewById(R.id.home_no_doings_image);
        mNoDoingsTextView = (TextView) view.findViewById(R.id.home_no_doings_text);

        updateUI();

        return view;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDoingAddedEvent(DoingAddedEvent doingAddedEvent) {
        updateUI();
        if (this.isVisible()) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDoingReceivedEvent(DoingReceivedEvent doingReceivedEvent) {
        updateUI();
        Doing doing = doingReceivedEvent.getDoing();
        if (mController.exists(doing)) {
            mController.fixDatabaseCounts(doing);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLikeAddedEvent(LikeAddedEvent likeAddedEvent) {
        mDoingAdapter.notifyDataSetChanged();
        update();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCommentaryAddedEvent(CommentaryAddedEvent commentaryAddedEvent) {
        mDoingAdapter.notifyDataSetChanged();
        update();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUserUpdatedEvent(UserUpdatedEvent userUpdatedEvent) {
        updateUI();
    }

    private void updateUI() {
        mDoings = mController.getRecentDoings();

        mDoingAdapter = new DoingAdapter(mDoings);
        mRecyclerView.setAdapter(mDoingAdapter);
//        mDoingAdapter.notifyDataSetChanged();

        if (mDoings.size() == 0) {
            mNoDoingsImageView.setVisibility(View.VISIBLE);
            mNoDoingsTextView.setVisibility(View.VISIBLE);
        } else {
            mNoDoingsImageView.setVisibility(View.INVISIBLE);
            mNoDoingsTextView.setVisibility(View.INVISIBLE);
        }

        update();

    }

    @Override
    public void update() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
        int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        int lastItemPosition = layoutManager.findLastVisibleItemPosition();
        int count = (lastItemPosition - firstItemPosition) + 1;
        updateCounts(firstItemPosition, lastItemPosition);
        mDoingAdapter.notifyItemRangeChanged(firstItemPosition, count);
    }

    private void updateCounts(int first, int last) {
        for (int i = first - 10; i <= last + 10; i++) {
            if (i < 0) {
                continue;
            }
            if (i >= mDoings.size()) {
                return;
            }
            Doing doing = mDoings.get(i);
            mController.updateCounts(doing);
        }
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
        private ImageView mActionImage;
        private ImageView mLikeImage;
        private TextView mLikesCount;
        private TextView mCommentariesCount;

        public DoingHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mUserName = (TextView) itemView.findViewById(R.id.home_item_user_name);
            mUserAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
            mDoingVerb = (TextView) itemView.findViewById(R.id.home_item_verb);
            mDoingText = (TextView) itemView.findViewById(R.id.home_item_doing_text);
            mActionImage = (ImageView) itemView.findViewById(R.id.home_item_action_image);
            mLikeImage = (ImageView) itemView.findViewById(R.id.home_item_like_image);
            mLikesCount = (TextView) itemView.findViewById(R.id.item_status_normal_heart_number);
            mCommentariesCount = (TextView) itemView.findViewById(R.id.item_status_commentaries_number);
        }

        public void bindDoing(Doing doing) {
            mDoing = doing;

            mUserName.setText(DoingUiUtils.getDoingUserNameString(getActivity(), mDoing));

            mUserAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), mDoing.getUser()));

            mDoingVerb.setText(DoingUiUtils.getDoingVerbString(getActivity(), mDoing));

            User myUser = mController.getMyUser();
            if (myUser.isMine(doing)) {
                mLikeImage.setVisibility(View.INVISIBLE);
            } else {
                mLikeImage.setVisibility(View.VISIBLE);
            }

            mDoingText.setText(doing.getText());

            mActionImage.setImageDrawable(DoingUiUtils.getDoingActionDrawable(getActivity(), doing));
            mActionImage.setColorFilter(DoingUiUtils.getDoingActionColor(getActivity(), doing));

            mLikeImage.setColorFilter(DoingUiUtils.getNormalLikeColor(getActivity()));
            if (mDoing.isLikedByMe()) {
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

            mLikesCount.setText(DoingUiUtils.getLikesCountKString(getActivity(), doing));
            mLikesCount.setTypeface(null, DoingUiUtils.getLikesCountStyle(getActivity(), doing));
            mCommentariesCount.setText(DoingUiUtils.getCommentariesCountKString(getActivity(), doing));
            mCommentariesCount.setTypeface(null, DoingUiUtils.getCommentariesCountStyle(getActivity(), doing));
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onDoingSelected(mDoing, DOING_LIST_TYPE.RECENT);
        }
    }

    private class DoingAdapter extends RecyclerView.Adapter<DoingHolder> {

        private List<Doing> mDoings;

        public DoingAdapter(List<Doing> doings) {
            mDoings = doings;
        }

        @Override
        public DoingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_doing_home, parent, false);

            return new DoingHolder(view);
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
