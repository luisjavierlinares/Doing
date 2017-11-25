package com.luisjavierlinares.android.doing;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.Like;
import com.luisjavierlinares.android.doing.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 24/04/2017.
 */

public class DoingLikesFragment extends RefreshingVisibleDialogFragment {

    private static final String ARG_DOING_ID = "com.luisjavierlinares.android.doing.DoingLikesFragment.arg_doing";

    private DoingController mController;
    private Doing mDoing;

    private ImageButton mUpButton;
    private View mNoLikesView;
    private RecyclerView mRecyclerView;
    private LikeAdapter mLikeAdapter;

    public static DoingLikesFragment newInstance(UUID doingId) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_DOING_ID, doingId);

        DoingLikesFragment doingLikesFragment = new DoingLikesFragment();
        doingLikesFragment.setArguments(arguments);
        return doingLikesFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        checkLikesAsNotNew();
    }

    @Override
    public void onStop() {
        super.onStop();
        checkLikesAsNotNew();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
        UUID doingId = (UUID) getArguments().getSerializable(ARG_DOING_ID);
        mDoing = mController.getDoing(doingId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_doing_likes, container, false);

        mUpButton = (ImageButton) view.findViewById(R.id.doing_likes_back_button);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mNoLikesView = (View) view.findViewById(R.id.doing_likes_no_likes);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.doing_likes_recycler_view);
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

        updateUI();

        return view;
    }

    @Override
    public void update() {
        Integer previousCount = mDoing.getLikesCount();
        mController.updateCounts(mDoing);
        Integer currentCount = mDoing.getLikesCount();

        // do not update UI if there are not new likes
        if (previousCount >= currentCount) {
            return;
        }

        updateUI();
    }

    private void updateUI() {
        List<Like> allLikes = mDoing.getLikes();
        List<Like> likes = new ArrayList<>();

//      Fix. Update doing likes counter if necessary
//        mDoing.setLikesCount(allLikes.size());
//        mController.updateDoing(mDoing);

//      Find likes that belong to known users
        for (int i = 0; i < allLikes.size(); i++) {
            User sender = allLikes.get(i).getSender();
            if (mController.isMe(sender)) {
                likes.add(0, allLikes.get(i));
            } else if (!sender.isUnknown()) {
                likes.add(allLikes.get(i));
            }
        }

//      Find likes that belong to unknown users
        for (int i = 0; i < allLikes.size(); i++) {
            User sender = allLikes.get(i).getSender();
            if (sender.isUnknown()) {
                likes.add(allLikes.get(i));
            }
        }

        if (likes.size()>0) {
            mNoLikesView.setVisibility(View.GONE);
            mLikeAdapter = new LikeAdapter(likes);
            mLikeAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(mLikeAdapter);
        } else {
            mNoLikesView.setVisibility(View.VISIBLE);
        }
    }

    private void checkLikesAsNotNew() {
        if (mDoing.hasNewLikes()) {
            mDoing.setHasNewLikes(false);
            mController.updateDoing(mDoing);
        }
    }

    private class LikeHolder extends RecyclerView.ViewHolder{

        Like mLike;
        TextView mUserName;

        public LikeHolder(View itemView) {
            super(itemView);

            mUserName = (TextView) itemView.findViewById(R.id.item_doing_like_user);
        }

        public void bindLike(Like like) {
            mLike = like;
            mUserName.setText(DoingUiUtils.getUserNameMeString(getActivity(), mLike.getSender()));
        }
    }

    private class LikeAdapter extends RecyclerView.Adapter<LikeHolder> {

        private List<Like> mLikes;

        public LikeAdapter(List<Like> likes) {
            mLikes = likes;
        }

        @Override
        public LikeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_doing_like, parent, false);

            return new LikeHolder(view);
        }

        @Override
        public void onBindViewHolder(LikeHolder holder, int position) {
            Like like = mLikes.get(position);
            holder.bindLike(like);
        }

        @Override
        public int getItemCount() {
            return mLikes.size();
        }
    }

}
