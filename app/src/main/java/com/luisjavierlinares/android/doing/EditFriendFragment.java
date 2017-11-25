package com.luisjavierlinares.android.doing;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

/**
 * Created by Luis on 05/05/2017.
 */

public class EditFriendFragment extends DialogFragment {

    private static final String ARG_USER_ID = "com.luisjavierlinares.android.Doing.EditFriendFragment.user_id";

    private DoingController mController;
    private User mUser;

    private TextView mFriendRegisteredName;
    private EditText mFriendName;
    private TextView mFriendStatusTitle;
    private TextView mFriendStatusText;
    private Switch mFriendStatusSwicht;

    private EventBus mEventBus;

    public static EditFriendFragment newInstance(UUID userId) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_USER_ID, userId);

        EditFriendFragment fragment = new EditFriendFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
        UUID userId = (UUID) getArguments().getSerializable(ARG_USER_ID);
        mUser = mController.getUser(userId);
        mEventBus = EventBus.getDefault();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_friend_edit, null);

        mFriendRegisteredName = (TextView) view.findViewById(R.id.edit_friend_registered_name);
        mFriendRegisteredName.setText(mUser.getFriendName());

        mFriendName = (EditText) view.findViewById(R.id.edit_friend_text_name);
        mFriendName.setText(mUser.getName());

        mFriendStatusTitle = (TextView) view.findViewById(R.id.edit_friend_status_title);
        mFriendStatusText  = (TextView) view.findViewById(R.id.edit_friend_status_text);
        mFriendStatusSwicht = (Switch) view.findViewById(R.id.edit_friend_status_switch);

        mFriendStatusSwicht.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFriendStatusSwicht.setText(getResources().getString(R.string.enabled));
                    mFriendStatusText.setText(getResources().getString(R.string.enabled_status_text));
                } else {
                    mFriendStatusSwicht.setText(getResources().getString(R.string.disabled));
                    mFriendStatusText.setText(getResources().getString(R.string.disabled_status_text));
                }
            }
        });

        initializeUI();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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
                        boolean inputError = false;
                        String friendName = mFriendName.getText().toString();

                        if (friendName.trim().isEmpty()) {
                            mFriendName.setError(getString(R.string.error_message_empty_field));
                            inputError = true;
                        }

                        if (inputError) {
                            return;
                        }

                        mUser.setName(mFriendName.getText().toString());

                        if ((mFriendStatusSwicht.isChecked()) && (mUser.isInactive())) {
                            mUser.activate();
                        } else if ((!mFriendStatusSwicht.isChecked()) && (mUser.isActive())){
                            mUser.deactivate();
                        }

                        mController.updateUser(mUser);
                        dialog.dismiss();
                    }
                });
            }
        });

        return dialog;

    }

    private void initializeUI() {

        switch (mUser.getState()) {
            case ACTIVE:
                mFriendStatusTitle.setVisibility(View.VISIBLE);
                mFriendStatusText.setVisibility(View.VISIBLE);
                mFriendStatusSwicht.setVisibility(View.VISIBLE);
                mFriendStatusSwicht.setText(getResources().getString(R.string.enabled));
                mFriendStatusSwicht.setChecked(true);
                mFriendStatusText.setText(getResources().getString(R.string.enabled_status_text));
                break;
            case INACTIVE:
                mFriendStatusTitle.setVisibility(View.VISIBLE);
                mFriendStatusText.setVisibility(View.VISIBLE);
                mFriendStatusSwicht.setVisibility(View.VISIBLE);
                mFriendStatusSwicht.setText(getResources().getString(R.string.disabled));
                mFriendStatusSwicht.setChecked(false);
                mFriendStatusText.setText(getResources().getString(R.string.disabled_status_text));
                break;
            default:
                mFriendStatusTitle.setVisibility(View.GONE);
                mFriendStatusText.setVisibility(View.GONE);
                mFriendStatusSwicht.setVisibility(View.GONE);
        }
    }
}
