package com.luisjavierlinares.android.doing;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.events.CommentaryAddedEvent;
import com.luisjavierlinares.android.doing.model.Commentary;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.UUID;


/**
 * Created by Luis on 26/04/2017.
 */

public class DoingCommentariesFragment extends RefreshingVisibleDialogFragment {

    private static final String ARG_DOING_ID = "com.luisjavierlinares.android.doing.DoingCommetariesFragment.arg_doing";

    private DoingController mController;
    private Doing mDoing;

    private ImageButton mUpButton;
    private EditText mCommentaryEditText;
    private ImageButton mAddCommentaryButton;
    private RecyclerView mRecyclerView;
    private CommentaryAdapter mCommentaryAdapter;

    private String mCommentaryText;

    private EventBus mEventBus;

    public static DoingCommentariesFragment newInstance(UUID doingId) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_DOING_ID, doingId);

        DoingCommentariesFragment doingCommentariesFragment = new DoingCommentariesFragment();
        doingCommentariesFragment.setArguments(arguments);
        return doingCommentariesFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mController = DoingController.get(getActivity());
        UUID doingId = (UUID) getArguments().getSerializable(ARG_DOING_ID);
        mDoing = mController.getDoing(doingId);
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        checkCommentariesAsNotNew();

        mEventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        checkCommentariesAsNotNew();
        mEventBus.unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_doing_commentaries, container, false);

        mCommentaryText = new String();

        mUpButton = (ImageButton) view.findViewById(R.id.doing_commentaries_back_button);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mCommentaryEditText = (EditText) view.findViewById(R.id.doing_commentaries_add_commentary_text);
        mCommentaryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCommentaryText = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //
            }
        });

        mAddCommentaryButton = (ImageButton) view.findViewById(R.id.doing_commentaries_add_commentary_button);
        mAddCommentaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mCommentaryText.isEmpty()) {
                    User sender = mController.getMyUser();
                    mController.addCommentary(sender, mCommentaryText, mDoing);
                    mCommentaryText = "";
                    mCommentaryEditText.setText("");
                }

            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.doing_commentaries_recycler_view);
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
        Integer previouCount = mDoing.getCommentariesCount();
        mController.updateCounts(mDoing);
        Integer currentCount = mDoing.getCommentariesCount();

        // do not update UI if there are not new commentaries
        if (previouCount >= currentCount) {
            return;
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int currentPosition = layoutManager.findLastVisibleItemPosition();
        int commentariesCount = layoutManager.getItemCount();
        int globalPosition = commentariesCount - currentPosition;

        updateUI();
//        mCommentaryAdapter.notifyDataSetChanged();

        // if the user is looking at the last comments scroll to last position to show
        // the new commentary
        if (globalPosition > 5) {
            mRecyclerView.scrollToPosition(currentPosition);
        }
    }

    @Subscribe
    public void onCommentaryAddedEvent(CommentaryAddedEvent commentaryAddedEvent) {
        Commentary commentary = commentaryAddedEvent.getCommentary();

        if (commentary == null) {
            return;
        }

        if (mDoing.getId().equals(commentary.getDoing().getId())) {
            updateUI();
        }
    }

    private void updateUI() {
        List<Commentary> commentaries = mDoing.getCommentaries();

        mDoing.setCommentariesCount(commentaries.size());
        mDoing.setHasNewCommentaries(false);
        mController.updateDoing(mDoing);

        mCommentaryAdapter = new CommentaryAdapter(commentaries);
        mCommentaryAdapter.notifyDataSetChanged();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mCommentaryAdapter);
    }

    private void checkCommentariesAsNotNew() {
        if (mDoing.hasNewCommentaries()) {
            mDoing.setHasNewCommentaries(false);
            mController.updateDoing(mDoing);
        }
    }

    private class CommentaryHolder extends RecyclerView.ViewHolder{

        private Commentary mCommentary;
        private TextView mUserName;
        private TextView mCommentaryText;
        private TextView mAgoText;
        private LinearLayout mCommentaryLayout;

        public CommentaryHolder(View itemView) {
            super(itemView);

            mUserName = (TextView) itemView.findViewById(R.id.item_doing_commentary_user);
            mCommentaryText = (TextView) itemView.findViewById(R.id.item_doing_commentary_text);
            mAgoText = (TextView) itemView.findViewById(R.id.commentary_item_ago_text);
            mCommentaryLayout = (LinearLayout) itemView.findViewById(R.id.item_doing_commentary);
        }

        public void bindCommentary(Commentary commentary) {
            mCommentary = commentary;
            mUserName.setText(DoingUiUtils.getUserNameMeString(getActivity(), mCommentary.getSender()));
            if (mCommentary.getSender().isMe()) {
                mUserName.setGravity(Gravity.RIGHT);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mUserName.getLayoutParams();
                params.gravity = Gravity.RIGHT;
                mUserName.setLayoutParams(params);

                mCommentaryLayout.setBackground(DoingUiUtils.getInvCommentaryImage(getActivity()));
            } else {
                mUserName.setGravity(Gravity.LEFT);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mUserName.getLayoutParams();
                params.gravity = Gravity.LEFT;
                mCommentaryLayout.setBackground(DoingUiUtils.getCommentaryImage(getActivity()));
            }
            mCommentaryText.setText(mCommentary.getText());
            mAgoText.setText(DoingUiUtils.getCommentaryAgoDateString(getActivity(), mCommentary));
        }
    }

    private class CommentaryAdapter extends RecyclerView.Adapter<CommentaryHolder> {

        private List<Commentary> mCommentaries;

        public CommentaryAdapter(List<Commentary> commentaries) {
            mCommentaries = commentaries;
        }

        @Override
        public CommentaryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_doing_commentary, parent, false);

            return new CommentaryHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentaryHolder holder, int position) {
            Commentary commentary = mCommentaries.get(position);
            holder.bindCommentary(commentary);
        }

        @Override
        public int getItemCount() {
            return mCommentaries.size();
        }
    }
}
