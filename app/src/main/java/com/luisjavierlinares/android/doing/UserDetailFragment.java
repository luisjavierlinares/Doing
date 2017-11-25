package com.luisjavierlinares.android.doing;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.events.UserUpdatedEvent;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.managers.DoingAccountManager;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.utils.MailUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.UUID;

/**
 * Created by Luis on 02/05/2017.
 */

public class UserDetailFragment extends DialogFragment {

    private static final String DIALOG_EDIT_MY_USER = "com.luisjavierlinares.android.doing.FriendsFragment.dialog_edit_my_user";
    private static final String DIALOG_INFO_RECOVERY_CODE = "com.luisjavierlinares.android.doing.UserDetailFragment.help_recovery_code";

    private static final String ARG_USER_ID = "com.luisjavierlinares.android.Doing.UserDetailFragment.user_id";

    private DoingController mController;
    private User mUser;

    private ImageButton mUpButton;
    private TextView mNameTextView;
    private ImageView mUserAvatar;
    private TextView mChangePictureTextView;
    private Button mHistoryButton;
    private ImageView mPictureImageView;
    private TextView mUserNameTextView;
    private TextView mAliasTitleTextView;
    private TextView mAliasTextView;
    private ImageView mFriendshipImageView;
    private TextView mFriendshipTextView;
    private View mRecoveryCodeLayout;
    private TextView mRecoveryCodeSendTextView;
    private TextView mRecoveryCodeHelpTextView;


    private EventBus mEventBus;

    private UserHistoryCallbacks mCallbacks;

    public static UserDetailFragment newInstance(UUID userId) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_USER_ID, userId);

        UserDetailFragment myUserDetailFragment = new UserDetailFragment();
        myUserDetailFragment.setArguments(arguments);
        return myUserDetailFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
        UUID userId = (UUID) getArguments().getSerializable(ARG_USER_ID);
        mUser = mController.getUser(userId);
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void onStart() {
        super.onStart();
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventBus.register(this);
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
            mCallbacks = (UserHistoryCallbacks) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement UserHistoryCallbacks");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_detail, container, false);

        mUpButton = (ImageButton) view.findViewById(R.id.user_detail_back_button);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mNameTextView = (TextView) view.findViewById(R.id.user_detail_user_name);
        mUserAvatar = (ImageView) view.findViewById(R.id.user_avatar);
        mPictureImageView = (ImageView) view.findViewById(R.id.user_avatar);
        mUserNameTextView = (TextView) view.findViewById(R.id.user_detail_registered_as);
        mAliasTitleTextView = (TextView) view.findViewById(R.id.user_details_alias_title);
        mAliasTextView = (TextView) view.findViewById(R.id.user_detail_alias);

        mChangePictureTextView = (TextView) view.findViewById(R.id.change_my_profile_picture);
        mChangePictureTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                EditMyUserFragment dialog = EditMyUserFragment.newInstance();
                dialog.show(fragmentManager, DIALOG_EDIT_MY_USER);
            }
        });

        mHistoryButton = (Button) view.findViewById(R.id.user_detail_history_button);
        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.onUserHistory(mUser);
            }
        });

        mRecoveryCodeLayout = (View) view.findViewById(R.id.recovery_code_layout);
        mRecoveryCodeSendTextView = (TextView) view.findViewById(R.id.recovery_code_send_yourself);
        mRecoveryCodeSendTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = getString(R.string.recovery_code_subject);
                String recoveryCode = DoingAccountManager.get(getActivity()).getRecoveryCode();
                String text = getString(R.string.recovery_code_mail_user_title) + " " + mUser.getFriendName() + "\n\n" + getString(R.string.recovery_code_mail_code_title) +
                        "\n" + recoveryCode;

                MailUtils.sendMail(getActivity(), subject, text);
            }
        });

        mRecoveryCodeHelpTextView = (TextView) view.findViewById(R.id.recovery_code_friend_code_help);
        mRecoveryCodeHelpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                InfoRecoveryCodeDialogFragment dialog = InfoRecoveryCodeDialogFragment.newInstance();
                dialog.show(fragmentManager, DIALOG_INFO_RECOVERY_CODE);
            }
        });

        mFriendshipImageView = (ImageView) view.findViewById(R.id.friends_item_friendship_icon);
        mFriendshipTextView = (TextView) view.findViewById(R.id.friends_item_friendship_text);

        updateUI();

        return view;
    }

    @Subscribe
    public void onUserUpdatedEvent(UserUpdatedEvent userUpdatedEvent) {
        updateUI();
    }

    private void updateUI() {
        mNameTextView.setText(DoingUiUtils.getUserNameMeString(getActivity(), mUser));
        mUserAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), mUser));
        mUserNameTextView.setText(mUser.getFriendName());

        if (mController.isMe(mUser)) {
            mAliasTitleTextView.setVisibility(View.GONE);
            mAliasTextView.setVisibility(View.GONE);
            mChangePictureTextView.setVisibility(View.VISIBLE);
            mRecoveryCodeLayout.setVisibility(View.VISIBLE);
            mFriendshipImageView.setVisibility(View.GONE);
            mFriendshipTextView.setVisibility(View.GONE);
        } else {
            mAliasTitleTextView.setVisibility(View.VISIBLE);
            mAliasTextView.setVisibility(View.VISIBLE);
            mChangePictureTextView.setVisibility(View.GONE);
            mRecoveryCodeLayout.setVisibility(View.GONE);

            if ((mUser.getName() == null) || (mUser.getName().isEmpty())) {
                mAliasTextView.setText(getResources().getString(R.string.unassigned));
            } else {
                mAliasTextView.setText(mUser.getName());
            }

            mFriendshipImageView.setImageDrawable(DoingUiUtils.getFriendshipIcon(getActivity(), mUser));
            mFriendshipImageView.setColorFilter(DoingUiUtils.getFriendshipIconColor(getActivity(), mUser));
            mFriendshipTextView.setText(DoingUiUtils.getFriendshipString(getActivity(), mUser));
        }
    }

}
