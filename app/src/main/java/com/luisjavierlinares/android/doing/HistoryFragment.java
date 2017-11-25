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
import com.luisjavierlinares.android.doing.events.GoTopOfListEvent;
import com.luisjavierlinares.android.doing.events.InTheDeepOfAListEvent;
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

public class HistoryFragment extends RefreshingVisibleFragment {

    private static final int REQUEST_DOING_ID = 0;
    private static final int DEEP_IN_THE_LIST_POSITON = 10;

    private DoingController mController;
    private List<Doing> mDoings;

    private RecyclerView mRecyclerView;
    private DoingAdapter mDoingAdapter;
    private ImageView mNoDoingsImageView;
    private TextView mNoDoingsTextView;
    private Parcelable mRecyclerViewState;

    private boolean mIsInTheDeepOfAList;

    private EventBus mEventBus;

    private DoingDetailCallbacks mCallbacks;

    public static HistoryFragment newInstance() {
        Bundle args = new Bundle();

        HistoryFragment fragment = new HistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.history_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                postInTheDeepOfAList();
            }
        });
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

        mNoDoingsImageView = (ImageView) view.findViewById(R.id.history_no_doings_image);
        mNoDoingsTextView = (TextView) view.findViewById(R.id.history_no_doings_text);

        updateUI();

        return view;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDoingAddedEvent(DoingAddedEvent doingAddedEvent) {
        updateUIForDoingEvent();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDoingReceivedEvent(DoingReceivedEvent doingReceivedEvent) {
        updateUIForDoingEvent();
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

    @Subscribe
    public void onGoTopOfListEvent(GoTopOfListEvent goTopOfListEvent) {
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

    private void updateUI() {
        mDoings = mController.getAllDoings();

        mDoingAdapter = new DoingAdapter(mDoings);
        mRecyclerView.setAdapter(mDoingAdapter);
        mDoingAdapter.notifyDataSetChanged();

        if (mDoings.size() == 0) {
            mNoDoingsImageView.setVisibility(View.VISIBLE);
            mNoDoingsTextView.setVisibility(View.VISIBLE);
        } else {
            mNoDoingsImageView.setVisibility(View.INVISIBLE);
            mNoDoingsTextView.setVisibility(View.INVISIBLE);
        }

        postInTheDeepOfAList();
        update();
    }

    private void updateUIForDoingEvent() {
        mDoingAdapter.notifyDataSetChanged();
        mDoingAdapter.getItemCount();

        if (mDoingAdapter.getItemCount() == 0) {
            mNoDoingsImageView.setVisibility(View.VISIBLE);
            mNoDoingsTextView.setVisibility(View.VISIBLE);
        } else {
            mNoDoingsImageView.setVisibility(View.INVISIBLE);
            mNoDoingsTextView.setVisibility(View.INVISIBLE);
        }

        postInTheDeepOfAList();
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
        int offset = 10;
        for (int i = first - offset; i <= last + offset; i++) {
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
        private TextView mAgoText;
        private ImageView mActionImage;
        private ImageView mLikeImage;
        private TextView mLikesCount;
        private TextView mCommentariesCount;
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
            mLikesCount = (TextView) itemView.findViewById(R.id.item_status_normal_heart_number);
            mCommentariesCount = (TextView) itemView.findViewById(R.id.item_status_commentaries_number);
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

            mAgoText.setText(DoingUiUtils.getDoingAgoDateString(getActivity(), doing));

            mLikesCount.setText(DoingUiUtils.getLikesCountKString(getActivity(), doing));
            mLikesCount.setTypeface(null, DoingUiUtils.getLikesCountStyle(getActivity(), doing));
            mCommentariesCount.setText(DoingUiUtils.getCommentariesCountKString(getActivity(), doing));
            mCommentariesCount.setTypeface(null, DoingUiUtils.getCommentariesCountStyle(getActivity(), doing));
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onDoingSelected(mDoing, DOING_LIST_TYPE.ALL);
        }
    }

    private class DoingAdapter extends RecyclerView.Adapter<DoingHolder> {

        private List<Doing> mDoings;

        public DoingAdapter(List<Doing> doings) {
            mDoings = doings;
        }

        @Override
        public HistoryFragment.DoingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_doing_history, parent, false);

            return new HistoryFragment.DoingHolder(view);
        }

        @Override
        public void onBindViewHolder(DoingHolder holder, int position) {
            Doing doing = mDoings.get(position);
            holder.bindDoing(doing);
            if (doing == null) {
                mDoings.remove(position);
            }
        }

        @Override
        public int getItemCount() {
            return mDoings.size();
        }

    }
}

