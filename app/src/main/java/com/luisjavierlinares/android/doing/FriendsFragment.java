package com.luisjavierlinares.android.doing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.events.MyUserUpdatedEvent;
import com.luisjavierlinares.android.doing.events.UserAddedEvent;
import com.luisjavierlinares.android.doing.events.UserUpdatedEvent;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.managers.DoingAccountManager;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.utils.MailUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Luis on 23/03/2017.
 */

public class FriendsFragment extends RefreshingVisibleFragment {

    private static final String DIALOG_VIEW_USER = "com.luisjavierlinares.android.doing.FriendsFragment.dialog_view_user";
    private static final String DIALOG_EDIT_MY_USER = "com.luisjavierlinares.android.doing.FriendsFragment.dialog_edit_my_user";
    private static final String DIALOG_EDIT_USER = "com.luisjavierlinares.android.doing.FriendsFragment.dialog_edit_user";
    private static final String DIALOG_CONFIRM_FRIEND = "com.luisjavierlinares.android.doing.FriendsFragment.dialog_confirm_friend";
    private static final String DIALOG_INFO_RECOVERY_CODE = "com.luisjavierlinares.android.doing.FriendsFragment.help_recovery_code";

    private static final int REQUEST_CONFIRMED_FRIEND = 0;

    private DoingController mController;

    private RecyclerView mRecyclerView;
    private FriendAdapter mFriendAdapter;

    private View mMyUserView;
    private ImageView mMyAvatar;
    private TextView mMyFriendName;
    private TextView mMyRegisteredName;
    private ImageButton mMyInfoButton;
    private ImageButton mMyHistoryButton;
    private ImageButton mMyEditButton;
    private View mNoFriendsView;
    private View mFriendCountView;
    private TextView mFriendCountText;
    private TextView mFriendCountWordText;
    private View mRecoveryCodeView;
    private TextView mRecoveryCodeSendTextView;
    private TextView mRecoveryCodeHelpTextView;

    private EventBus mEventBus;

    private UserHistoryCallbacks mUserHistoryCallbacks;

    public static FriendsFragment newInstance() {
        Bundle args = new Bundle();

        FriendsFragment fragment = new FriendsFragment();
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
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventBus.unregister(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof UserHistoryCallbacks) {
            mUserHistoryCallbacks = (UserHistoryCallbacks) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement UserHistoryCallbacks");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mUserHistoryCallbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        mMyUserView = (View) view.findViewById(R.id.item_user_my_user);
        mMyUserView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User myUser = mController.getMyUser();
                mUserHistoryCallbacks.onUserHistory(myUser);
            }
        });

        mMyAvatar = (ImageView) view.findViewById(R.id.user_avatar);

        final User myUser = mController.getMyUser();

        mMyFriendName = (TextView) view.findViewById(R.id.friends_item_my_name);
        mMyFriendName.setText(DoingUiUtils.getUserNameMeString(getActivity(), myUser));

        mMyRegisteredName = (TextView) view.findViewById(R.id.friends_item_my_registered_name);
        mMyRegisteredName.setText(myUser.getFriendName());

        mNoFriendsView = (View) view.findViewById(R.id.no_friends_yet_view);

        mFriendCountView = (View) view.findViewById(R.id.friends_friend_count_view);
        mFriendCountText = (TextView) view.findViewById(R.id.friends_friend_count);
        mFriendCountWordText = (TextView) view.findViewById(R.id.friends_friend_count_word);

        mRecoveryCodeView = (View) view.findViewById(R.id.recovery_code_view);

        mRecoveryCodeSendTextView = (TextView) view.findViewById(R.id.friends_recovery_code_send_yourself);
        mRecoveryCodeSendTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = getString(R.string.recovery_code_subject);
                String recoveryCode = DoingAccountManager.get(getActivity()).getRecoveryCode();
                String text = getString(R.string.recovery_code_mail_user_title) + " " + myUser.getFriendName() + "\n\n" + getString(R.string.recovery_code_mail_code_title) +
                        "\n" + recoveryCode;

                MailUtils.sendMail(getActivity(), subject, text);
            }
        });

        mRecoveryCodeHelpTextView = (TextView) view.findViewById(R.id.friends_recovery_code_help);
        mRecoveryCodeHelpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                InfoRecoveryCodeDialogFragment dialog = InfoRecoveryCodeDialogFragment.newInstance();
                dialog.show(fragmentManager, DIALOG_INFO_RECOVERY_CODE);
            }
        });

        mMyInfoButton = (ImageButton) view.findViewById(R.id.friends_item_my_info_button);
        mMyInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User myUser = mController.getMyUser();
                FragmentManager fragmentManager = getFragmentManager();
                UserDetailFragment dialog = UserDetailFragment.newInstance(myUser.getId());
                dialog.show(fragmentManager, DIALOG_VIEW_USER);
            }
        });

        mMyHistoryButton = (ImageButton) view.findViewById(R.id.friends_item_my_history_button);
        mMyHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User myUser = mController.getMyUser();
                mUserHistoryCallbacks.onUserHistory(myUser);
            }
        });

        mMyEditButton = (ImageButton) view.findViewById(R.id.friends_item_my_edit_user_button);
        mMyEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                EditMyUserFragment dialog = EditMyUserFragment.newInstance();
                dialog.show(fragmentManager, DIALOG_EDIT_MY_USER);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.friends_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setNestedScrollingEnabled(false);

        updateUI();

        return view;
    }

    @Override
    public void update() {

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUserAddedEvent(UserAddedEvent userAddedEvent) {
        updateUI();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUserUpdatedEvent(UserUpdatedEvent userUpdatedEvent) {
        updateUI();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void myUserUpdatedEvent(MyUserUpdatedEvent myUserUpdatedEvent) {
        updateUI();
    }

    private void updateUI() {
         List<User> friends = mController.getAllOtherUsersOrderByState();

        if (mFriendAdapter != null) {
            mRecyclerView.clearFocus();
        }

        mFriendAdapter = new FriendAdapter(friends);
        mFriendAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mFriendAdapter);

        User myUser = mController.getMyUser();
        mMyAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), myUser));
        mMyRegisteredName.setText(myUser.getFriendName());

        int activeFriendsCount = 0;
        for (User friend:friends) {
            if (friend.isActive()) {
                activeFriendsCount++;
            }
        }

        mFriendCountText.setText(String.valueOf(activeFriendsCount));

        if (activeFriendsCount == 1) {
            mFriendCountWordText.setText(getResources().getString(R.string.friend_count_2_s));
        } else {
            mFriendCountWordText.setText(getResources().getString(R.string.friend_count_2_p));
        }

        if (friends.isEmpty()) {
            mFriendCountView.setVisibility(View.GONE);
            mNoFriendsView.setVisibility(View.VISIBLE);
            mRecoveryCodeView.setVisibility(View.GONE);

        } else {
            mFriendCountView.setVisibility(View.VISIBLE);
            mNoFriendsView.setVisibility(View.GONE);
            mRecoveryCodeView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_CONFIRMED_FRIEND) {
            String friendName = AddFriendFragment.getFriendAlias(data);
            String userCode = AddFriendFragment.getUserCode(data);
            String friendCode = AddFriendFragment.getFriendCode(data);
            String userName = AddFriendFragment.getUserName(data);

            mController.addFriend(friendName, userCode, friendCode, userName);
        }
    }

    private class FriendHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener{

        private User mFriend;
        private TextView mFriendName;
        private ImageView mFriendAvatar;
        private ImageButton mFriendInfoButton;
        private ImageButton mFriendHistoryButton;
        private ImageButton mFriendEditButton;
        private View mFriendshipView;
        private ImageView mFriendshipImageView;
        private TextView mFriendshipTextView;
        private View mPendingFriendshipButtons;
        private Button mConfirmFriendshipButton;
        private Button mIgnoreFriendshipButton;
        private View mIgnoredFrienshipButtons;
        private Button mReconsiderFriendshipButton;

        public FriendHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mFriendName = (TextView) itemView.findViewById(R.id.friends_item_user_name);
            mFriendAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
            mFriendInfoButton = (ImageButton) itemView.findViewById(R.id.friends_item_info_button);
            mFriendHistoryButton = (ImageButton) itemView.findViewById(R.id.friends_item_history_button);
            mFriendEditButton = (ImageButton) itemView.findViewById(R.id.friends_item_edit_user_button);
            mFriendshipView = (View) itemView.findViewById(R.id.friends_item_friendship_view);
            mFriendshipImageView = (ImageView) itemView.findViewById(R.id.friends_item_friendship_icon);
            mFriendshipTextView = (TextView) itemView.findViewById(R.id.friends_item_friendship_text);
            mPendingFriendshipButtons = (View) itemView.findViewById(R.id.friends_item_pending_friendship_buttons);
            mConfirmFriendshipButton = (Button) itemView.findViewById(R.id.friends_item_confirm_friendship);
            mIgnoreFriendshipButton = (Button) itemView.findViewById(R.id.friends_item_ignore_friendship);
            mIgnoredFrienshipButtons = (View) itemView.findViewById(R.id.friends_item_ignore_friendship_buttons);
            mReconsiderFriendshipButton = (Button) itemView.findViewById(R.id.friends_item_reconsider_friendship);
        }

        public void bindFriend(User friend) {
            mFriend = friend;

            mFriendAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), friend));

            mFriendName.setText(DoingUiUtils.getUserNameString(getActivity(), friend));

            mFriendshipView.setVisibility(View.VISIBLE);
            mFriendshipImageView.setImageDrawable(DoingUiUtils.getFriendshipIcon(getActivity(), friend));
            mFriendshipImageView.setColorFilter(DoingUiUtils.getFriendshipIconColor(getActivity(), friend));
            mFriendshipTextView.setText(DoingUiUtils.getFriendshipString(getActivity(), friend));

            if (friend.isInvitingMe()) {
                mPendingFriendshipButtons.setVisibility(View.VISIBLE);
                mIgnoredFrienshipButtons.setVisibility(View.GONE);
            } else if (friend.isIgnoredByMe()) {
                mPendingFriendshipButtons.setVisibility(View.GONE);
                mIgnoredFrienshipButtons.setVisibility(View.VISIBLE);
            } else {
                mPendingFriendshipButtons.setVisibility(View.GONE);
                mIgnoredFrienshipButtons.setVisibility(View.GONE);
            }

            mConfirmFriendshipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String friendName = mFriend.getName();

                    if (friendName == null) {
                        friendName = mFriend.getFriendName();
                    }

                    if (friendName == null) {
                        friendName = DoingUiUtils.getUserUnkownName(getActivity(), mFriend);
                    }
                    String friendCode = mFriend.getFriendCode();
                    FragmentManager fragmentManager = getFragmentManager();
                    String okText = getResources().getString(R.string.confirm);
                    AddFriendFragment addFriendDialog = AddFriendFragment.newInstance(friendName, friendCode, okText);
                    addFriendDialog.setTargetFragment(FriendsFragment.this, REQUEST_CONFIRMED_FRIEND);
                    addFriendDialog.show(fragmentManager, DIALOG_CONFIRM_FRIEND);
                }
            });

            mIgnoreFriendshipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mController.ignoreFriend(mFriend);
                }
            });

            mReconsiderFriendshipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mController.reconsiderIgnoredFriend(mFriend);
                }
            });

            mFriendInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getFragmentManager();
                    UserDetailFragment dialog = UserDetailFragment.newInstance(mFriend.getId());
                    dialog.show(fragmentManager, DIALOG_VIEW_USER);
                }
            });

            mFriendHistoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUserHistoryCallbacks.onUserHistory(mFriend);
                }
            });

            mFriendEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager = getFragmentManager();
                    EditFriendFragment dialog = EditFriendFragment.newInstance(mFriend.getId());
                    dialog.show(fragmentManager, DIALOG_EDIT_USER);
                }
            });

        }

        @Override
        public void onClick(View v) {
            mUserHistoryCallbacks.onUserHistory(mFriend);
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
            View view = layoutInflater.inflate(R.layout.item_friends_friend, parent, false);

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
