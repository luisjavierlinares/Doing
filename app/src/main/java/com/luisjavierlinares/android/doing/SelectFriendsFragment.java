package com.luisjavierlinares.android.doing;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luis on 05/07/2017.
 */

public class SelectFriendsFragment extends DialogFragment {

    private static final String ARG_SELECTED_FRIENDS = "com.luisjavierlinares.android.Doing.SelectFriendsFragment.arg_selected_friends";

    private static final String KEY_SELECTED_FRIENDS = "com.luisjavierlinares.android.Doing.SelectFriendsFragment.key_selected_friends";

    private static final String EXTRA_SELECTED_FRIENDS = "com.luisjavierlinares.android.Doing.SelectFriendsFragment.extra_selected_friends";

    private DoingController mController;

    private RecyclerView mRecyclerView;
    private FriendAdapter mFriendAdapter;

    private View mSelectAllView;
    private View mSelectNoneView;

    private List<User> mFriends;
    private List<User> mSelectedFriends;


    public static SelectFriendsFragment newInstance(List<User> selectedFriends) {
        Bundle arguments = new Bundle();

        ArrayList<String> selectedFriendsUserCode = new ArrayList<>();
        for (User selectedFriend : selectedFriends) {
            selectedFriendsUserCode.add(selectedFriend.getUserCode());
        }
        arguments.putStringArrayList(ARG_SELECTED_FRIENDS, selectedFriendsUserCode);

        SelectFriendsFragment selectFriendsFragment = new SelectFriendsFragment();
        selectFriendsFragment.setArguments(arguments);
        return selectFriendsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<String> selectedUserCodes = new ArrayList<>();
        for (User selectedFriend : mSelectedFriends) {
            selectedUserCodes.add(selectedFriend.getUserCode());
        }
        outState.putStringArrayList(KEY_SELECTED_FRIENDS, selectedUserCodes);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_selectable_friends, null);

        mFriends = mController.getAllActiveFriends();
        mSelectedFriends = new ArrayList<>();

        mSelectAllView = (View) view.findViewById(R.id.select_friends_select_all);
        mSelectAllView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedFriends = new ArrayList<User>();
                for (User friend:mFriends) {
                    mSelectedFriends.add(friend);
                    mFriendAdapter.notifyDataSetChanged();
                }
            }
        });

        mSelectNoneView = (View) view.findViewById(R.id.select_friends_select_none);
        mSelectNoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedFriends = new ArrayList<User>();
                mFriendAdapter.notifyDataSetChanged();
            }
        });

        if (savedInstanceState != null) {
            ArrayList<String> selectedUserCodes = savedInstanceState.getStringArrayList(KEY_SELECTED_FRIENDS);
            for (String selectedUserCode : selectedUserCodes) {
                mSelectedFriends.add(mController.getUserWithCode(selectedUserCode));
            }
        }  else if (!getArguments().isEmpty()){
            ArrayList<String> selectedUserCodes = getArguments().getStringArrayList(ARG_SELECTED_FRIENDS);
            if (selectedUserCodes == null) {
                selectedUserCodes = new ArrayList<>();
            }
            for (String selectedUserCode : selectedUserCodes) {
                mSelectedFriends.add(mController.getUserWithCode(selectedUserCode));
            }
        } else {
            for(User friend : mFriends) {
                mSelectedFriends.add(friend);
            }
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.select_friends_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                    sendResult(Activity.RESULT_OK);
                    dialog.dismiss();
                    }
                });
            }
        });

        return dialog;
    }

    private void updateUI() {
        mFriendAdapter = new FriendAdapter(mFriends);
        mRecyclerView.setAdapter(mFriendAdapter);
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) return;

        Intent intent = new Intent();

        ArrayList<String> selectedUserCodes = new ArrayList<>();
        for (User selectedFriend : mSelectedFriends) {
            selectedUserCodes.add(selectedFriend.getUserCode());
        }

        intent.putExtra(EXTRA_SELECTED_FRIENDS, selectedUserCodes);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    public static List<String> getSelectedFriends(Intent result) {
        return result.getStringArrayListExtra(EXTRA_SELECTED_FRIENDS);
    }

    private Boolean isSelected(User friend) {
        for(User selectedFriend : mSelectedFriends) {
            if (selectedFriend.getUserCode().equals(friend.getUserCode())) {
                return true;
            }
        }
        return false;
    }

    private void selectFriend(User friend) {
        mSelectedFriends.add(friend);
    }

    private void unSelectFriend(User friend) {
        for(User selectedFriend : mSelectedFriends) {
            if (selectedFriend.getUserCode().equals(friend.getUserCode())) {
                mSelectedFriends.remove(selectedFriend);
                return;
            }
        }
    }

    private void toogleSelected(User friend) {
        if (isSelected(friend)) {
            unSelectFriend(friend);
        } else {
            selectFriend(friend);
        }
    }

    private class FriendHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        private User mFriend;
        private TextView mFriendName;
        private ImageView mFriendAvatar;
        private ImageView mFriendCheck;

        public FriendHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mFriendName = (TextView) itemView.findViewById(R.id.selectable_friend_name);
            mFriendAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
            mFriendCheck = (ImageView) itemView.findViewById(R.id.selectable_friend_check);
        }

        public void bindFriend(User friend) {
            mFriend = friend;

            mFriendAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), friend));
            mFriendName.setText(DoingUiUtils.getUserNameString(getActivity(), friend));
            if (isSelected(friend)) {
                mFriendCheck.setVisibility(View.VISIBLE);
            } else {
                mFriendCheck.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            toogleSelected(mFriend);
            if (isSelected(mFriend)) {
                mFriendCheck.setVisibility(View.VISIBLE);
            } else {
                mFriendCheck.setVisibility(View.INVISIBLE);
            }
        }
    }


    private class FriendAdapter extends RecyclerView.Adapter<FriendHolder> {

        private List<User> mFriends;

        public FriendAdapter(List<User> friends) {
            mFriends = friends;
        }

        @Override
        public FriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_selectable_friend, parent, false);

            return new FriendHolder(view);
        }

        @Override
        public void onBindViewHolder(FriendHolder holder, int position) {
            User friend = mFriends.get(position);
            holder.bindFriend(friend);
        }

        @Override
        public int getItemCount() {
            return mFriends.size();
        }
    }

}
