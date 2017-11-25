package com.luisjavierlinares.android.doing;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 01/09/2017.
 */

public class DoingInfoFragment extends DialogFragment {

    private static final String ARG_DOING_ID = "com.luisjavierlinares.android.Doing.DoingInfoFragment.doing_id";

    private static final String DIALOG_ADD_FRIEND = "com.luisjavierlinares.android.Doing.DoingInfoFragment.dialog_confirm_friend";

    private static final int REQUEST_ADD_FRIEND = 0;

    private DoingController mController;
    private Doing mDoing;

    private ImageButton mUpButton;
    private TextView mSenderName;
    private ImageView mSenderAvatar;

    private RecyclerView mRecyclerView;
    private ReceiverAdapter mReceiverAdapter;

    private List<User> mReceivers;

    public static DoingInfoFragment newInstance(UUID doingId) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_DOING_ID, doingId);

        DoingInfoFragment doingInfoFragment = new DoingInfoFragment();
        doingInfoFragment.setArguments(arguments);
        return doingInfoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_doing_info, null);

        UUID doingId = (UUID) getArguments().getSerializable(ARG_DOING_ID);
        mDoing = mController.getDoing(doingId);

        mSenderName = (TextView) view.findViewById(R.id.friend_name);
        mSenderAvatar = (ImageView) view.findViewById(R.id.user_avatar);

        mSenderName.setText(DoingUiUtils.getUserNameMeString(getActivity(), mDoing.getUser()));
        mSenderAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), mDoing.getUser()));

        mRecyclerView = (RecyclerView) view.findViewById(R.id.doing_info_sent_to_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setFocusable(false);

        updateUI();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();

        mUpButton = (ImageButton) view.findViewById(R.id.doing_info_back_button);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }

    public void updateUI() {
        mReceivers = mController.getReceiversNotSender(mDoing);
        mReceiverAdapter = new ReceiverAdapter(mReceivers);
        mRecyclerView.setAdapter(mReceiverAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_ADD_FRIEND) {
            String friendName = AddFriendFragment.getFriendAlias(data);
            String userCode = AddFriendFragment.getUserCode(data);
            String friendCode = AddFriendFragment.getFriendCode(data);
            String userName = AddFriendFragment.getUserName(data);

            mController.addFriend(friendName, userCode, friendCode, userName);
            updateUI();
        }
    }

    private class ReceiverHolder extends RecyclerView.ViewHolder {

        private User mReceiver;
        private TextView mReceiverName;
        private ImageView mReceiverAvatar;
        private ImageView mReceiverAddButton;

        public ReceiverHolder(View itemView) {
            super(itemView);
            mReceiverName = (TextView) itemView.findViewById(R.id.friend_name);
            mReceiverAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
            mReceiverAddButton = (ImageView) itemView.findViewById(R.id.add_friend_button);
        }

        public void bindReceiver(User receiver){
            mReceiver = receiver;
            mReceiverName.setText(DoingUiUtils.getUserNameMeString(getActivity(), mReceiver));
            mReceiverAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), mReceiver));

            if (receiver.isAFriendOfAFriend() || receiver.isInvitingMe()) {
                mReceiverAddButton.setVisibility(View.VISIBLE);
                mReceiverAddButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String friendName = mReceiver.getFriendName();
                        if (friendName == null) {
                            friendName = DoingUiUtils.getUserUnkownName(getActivity(), mReceiver);
                        }
                        String friendCode = mReceiver.getFriendCode();
                        FragmentManager fragmentManager = getFragmentManager();
                        String okText = getResources().getString(R.string.add_friend);
                        AddFriendFragment addFriendDialog = AddFriendFragment.newInstance(friendName, friendCode, okText);
                        addFriendDialog.setTargetFragment(DoingInfoFragment.this, REQUEST_ADD_FRIEND);
                        addFriendDialog.show(fragmentManager, DIALOG_ADD_FRIEND);
                    }
                });
            } else {
                mReceiverAddButton.setVisibility(View.INVISIBLE);
            }
         }
    }

    private class ReceiverAdapter extends RecyclerView.Adapter<ReceiverHolder> {

        List<User> mReceivers;

        public ReceiverAdapter(List<User> receivers) {
            mReceivers = receivers;
        }

        @Override
        public ReceiverHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_doing_info_friend, parent, false);

            return new ReceiverHolder(view);
        }

        @Override
        public void onBindViewHolder(ReceiverHolder holder, int position) {
            User receiver = mReceivers.get(position);
            holder.bindReceiver(receiver);
        }

        @Override
        public int getItemCount() {
            return mReceivers.size();
        }
    }
}
