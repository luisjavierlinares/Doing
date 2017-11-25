package com.luisjavierlinares.android.doing;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.utils.RandomUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.luisjavierlinares.android.doing.model.Doing.DoingAction;

/**
 * Created by Luis on 27/03/2017.
 */

public class AddDoingFragment extends DialogFragment {

    private static final String KEY_DOING_ACTION = "doing_action";
    private static final String KEY_DOING_IS_ACTION_SELECTED = "doing_is_action_selected";
    private static final String KEY_DOING_TEXT = "doing_text";
    private static final String KEY_DOING_RECEIVERS = "doing_receivers";

    private static final String EXTRA_DOING_USER = "com.luisjavierlinares.android.Doing.AddDoingFragment.extra_doing_user";
    private static final String EXTRA_DOING_ACTION = "com.luisjavierlinares.android.Doing.AddDoingFragment.extra_doing_action";
    private static final String EXTRA_DOING_TEXT = "com.luisjavierlinares.android.Doing.AddDoingFragment.extra_doing_text";
    private static final String EXTRA_DOING_RECEIVERS = "com.luisjavierlinares.android.Doing.AddDoingFragment.extra_doing_receivers";

    private static final int REQUEST_SELECTED_FRIENDS = 0;

    private static final String DIALOG_SELECT_FRIENDS = "com.luisjavierlinares.android.Doing.AddDoingFragment.dialog_select_friends";

    private DoingController mController;

    private ImageView mUserAvatar;
    private EditText mDoingEditText;
    private ImageButton mDoingActionButton;
    private TextView mSelectFriendsButton;
    private DoingAction mDoingActionSelected;
    private Boolean mIsActionSelected;
    private List<User> mReceiverFriends;

    public static AddDoingFragment newInstance() {
        Bundle args = new Bundle();

        AddDoingFragment fragment = new AddDoingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
        mIsActionSelected = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_DOING_ACTION, mDoingActionSelected.toString());
        outState.putString(KEY_DOING_TEXT, mDoingEditText.getText().toString());
        outState.putBoolean(KEY_DOING_IS_ACTION_SELECTED, mIsActionSelected);

        ArrayList<String> receiversUserCode = new ArrayList<>();
        for (User receiverFriend : mReceiverFriends) {
            receiversUserCode.add(receiverFriend.getUserCode());
        }
        outState.putStringArrayList(KEY_DOING_RECEIVERS, receiversUserCode);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_doing, null);

        mUserAvatar = (ImageView) view.findViewById(R.id.user_avatar);
        mUserAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), mController.getMyUser()));

        DoingAction doingAction = null;
        String doingText = null;
        if (savedInstanceState != null) {
            doingAction = DoingAction.valueOf(savedInstanceState.getString(KEY_DOING_ACTION));
            doingText = savedInstanceState.getString(KEY_DOING_TEXT);
            mIsActionSelected = savedInstanceState.getBoolean(KEY_DOING_IS_ACTION_SELECTED);

            mReceiverFriends = new ArrayList<>();
            ArrayList<String> receiversUserCode = savedInstanceState.getStringArrayList(KEY_DOING_RECEIVERS);
            for (String receiverUserCode : receiversUserCode) {
                mReceiverFriends.add(mController.getUserWithCode(receiverUserCode));
            }
        } else {
            mReceiverFriends = mController.getAllActiveFriends();
        }

        mDoingEditText = (EditText) view.findViewById(R.id.add_doing_edit_text);
        if (doingText != null) {
            mDoingEditText.setText(doingText);
        }

        mDoingActionButton = (ImageButton) view.findViewById(R.id.add_doing_action_button);
        mDoingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(getActivity(), mDoingActionButton);
                popupMenu.getMenuInflater().inflate(R.menu.menu_doing_actions, popupMenu.getMenu());

                giveColorToActionIcons(popupMenu.getMenu());
                mIsActionSelected = true;
                setDoing(mDoingActionSelected);

                setForceShowIcon(popupMenu);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Drawable icon = (Drawable) item.getIcon();
                        mDoingActionButton.setImageDrawable(icon);
                        setHint(item);
                        setDoingAction(item);
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        mSelectFriendsButton = (TextView) view.findViewById(R.id.add_doing_select_friends_button);
        if (mReceiverFriends.size() == 0) {
            mSelectFriendsButton.setText(R.string.add_doing_only_for_me);
        } else if (mReceiverFriends.size() == mController.getAllActiveFriends().size()) {
            mSelectFriendsButton.setText(R.string.add_doing_all_my_friends);
        } else {
            mSelectFriendsButton.setText(R.string.add_doing_some_of_my_friends);
        }
        mSelectFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                SelectFriendsFragment selectFriendsFragment = SelectFriendsFragment.newInstance(mReceiverFriends);
                selectFriendsFragment.setTargetFragment(AddDoingFragment.this, REQUEST_SELECTED_FRIENDS);
                selectFriendsFragment.show(fragmentManager, DIALOG_SELECT_FRIENDS);
            }
        });

        if (doingAction != null) {
            setDoing(doingAction);
            if (!mIsActionSelected) {
                setInactiveButtomColor();
            }
        } else {
            setDefaultRandomDoing();
        }

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
                        if (mDoingEditText.getText().toString().trim().isEmpty()) {
                            mDoingEditText.setError(getString(R.string.error_message_empty_action));
                        } else {
                            User user = mController.getMyUser();
                            DoingAction action = mDoingActionSelected;
                            String text = mDoingEditText.getText().toString();

                            sendResult(Activity.RESULT_OK, user, action, text);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        return dialog;
    }

    private void updateUI() {
        if (mReceiverFriends.size() == 0) {
            mSelectFriendsButton.setText(R.string.add_doing_only_for_me);
        } else if (mReceiverFriends.size() == mController.getAllActiveFriends().size()) {
            mSelectFriendsButton.setText(R.string.add_doing_all_my_friends);
        } else {
            mSelectFriendsButton.setText(R.string.add_doing_some_of_my_friends);
        }
    }

    private void sendResult(int resultCode, User user, DoingAction doingAction, String doingText) {
        if (getTargetFragment() == null) return;

        Intent intent = new Intent();

        intent.putExtra(EXTRA_DOING_ACTION,doingAction);
        intent.putExtra(EXTRA_DOING_TEXT, doingText);
        intent.putExtra(EXTRA_DOING_USER, user);

        ArrayList<String> receiversUserCode = new ArrayList<>();
        for (User receiverFriend : mReceiverFriends) {
            receiversUserCode.add(receiverFriend.getUserCode());
        }
        intent.putExtra(EXTRA_DOING_RECEIVERS, receiversUserCode);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    public static User getDoingUser(Intent result) {
        return (User) result.getSerializableExtra(EXTRA_DOING_USER);
    }

    public static DoingAction getDoingAction(Intent result) {
        return (DoingAction) result.getSerializableExtra(EXTRA_DOING_ACTION);
    }

    public static String getDoingText(Intent result) {
        return result.getStringExtra(EXTRA_DOING_TEXT);
    }

    public static List<String> getDoingReceivers(Intent result) {
        return result.getStringArrayListExtra(EXTRA_DOING_RECEIVERS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_SELECTED_FRIENDS) {
            mReceiverFriends = new ArrayList<>();
            List<String> doingReceiversUserCode = SelectFriendsFragment.getSelectedFriends(data);

            for (String doingReceiverUserCode : doingReceiversUserCode) {
                mReceiverFriends.add(mController.getUserWithCode(doingReceiverUserCode));
            }

            updateUI();
        }
    }

    private void setDoing(DoingAction action) {
        mDoingActionSelected = action;

        switch (action) {
            case LISTENING:
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorListening), PorterDuff.Mode.SRC_IN);
                mDoingActionButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.action_listening));
                mDoingEditText.setHint(getString(R.string.this_artist_or_group));
                break;
            case PLAYING:
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPlaying), PorterDuff.Mode.SRC_IN);
                mDoingActionButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.action_playing));
                mDoingEditText.setHint(getString(R.string.this_game));
                break;
            case READING:
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorReading), PorterDuff.Mode.SRC_IN);
                mDoingActionButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.action_reading));
                mDoingEditText.setHint(R.string.this_book);
                break;
            case WATCHING:
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorWatching), PorterDuff.Mode.SRC_IN);
                mDoingActionButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.action_watching));
                mDoingEditText.setHint(R.string.this_movie_show);
                break;
            case ENJOYING:
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorEnjoying), PorterDuff.Mode.SRC_IN);
                mDoingActionButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.action_enjoying));
                mDoingEditText.setHint(getString(R.string.doing_this));
                break;
        }
    }

    private void setInactiveButtomColor() {
        mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorInactiveButton), PorterDuff.Mode.SRC_IN);
    }

    private void setDefaultRandomDoing() {
        Drawable actionIcon = mDoingActionButton.getDrawable();
        String hintText = null;

        int randomInt = RandomUtils.getRandomNumberBetween(1, 5);
        switch (randomInt) {
            case 1:
                mDoingActionSelected = DoingAction.PLAYING;
                actionIcon = ContextCompat.getDrawable(getActivity(), R.drawable.action_playing);
                hintText = getString(R.string.this_game);
                break;
            case 2:
                mDoingActionSelected = DoingAction.WATCHING;
                actionIcon = ContextCompat.getDrawable(getActivity(), R.drawable.action_watching);
                hintText = getString(R.string.this_movie_show);
                break;
            case 3:
                mDoingActionSelected = DoingAction.READING;
                actionIcon = ContextCompat.getDrawable(getActivity(), R.drawable.action_reading);
                hintText = getString(R.string.this_book);
                break;
            case 4:
                mDoingActionSelected = DoingAction.LISTENING;
                actionIcon = ContextCompat.getDrawable(getActivity(), R.drawable.action_listening);
                hintText = getString(R.string.this_artist_or_group);
                break;
            case 5:
                mDoingActionSelected = DoingAction.ENJOYING;
                actionIcon = ContextCompat.getDrawable(getActivity(), R.drawable.action_enjoying);
                hintText = getString(R.string.doing_this);
                break;
        }
        actionIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorInactiveButton), PorterDuff.Mode.SRC_IN);
        mDoingActionButton.setImageDrawable(actionIcon);
        mDoingEditText.setHint(hintText);
    }

    private void giveColorToActionIcons(Menu menu) {

        MenuItem menuItem = menu.findItem(R.id.menu_item_listening);
        Drawable coloredIcon = (Drawable) menuItem.getIcon();
        coloredIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorListening), PorterDuff.Mode.SRC_IN);
        menuItem.setIcon(coloredIcon);

        menuItem = menu.findItem(R.id.menu_item_playing);
        coloredIcon = (Drawable) menuItem.getIcon();
        coloredIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPlaying), PorterDuff.Mode.SRC_IN);
        menuItem.setIcon(coloredIcon);

        menuItem = menu.findItem(R.id.menu_item_reading);
        coloredIcon = (Drawable) menuItem.getIcon();
        coloredIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorReading), PorterDuff.Mode.SRC_IN);
        menuItem.setIcon(coloredIcon);

        menuItem = menu.findItem(R.id.menu_item_watching);
        coloredIcon = (Drawable) menuItem.getIcon();
        coloredIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorWatching), PorterDuff.Mode.SRC_IN);
        menuItem.setIcon(coloredIcon);

        menuItem = menu.findItem(R.id.menu_item_enjoying);
        coloredIcon = (Drawable) menuItem.getIcon();
        coloredIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorEnjoying), PorterDuff.Mode.SRC_IN);
        menuItem.setIcon(coloredIcon);
    }

    private void setHint(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_item_listening:
                mDoingEditText.setHint(R.string.this_artist_or_group);
                break;
            case R.id.menu_item_playing:
                mDoingEditText.setHint(R.string.this_game);
                break;
            case R.id.menu_item_reading:
                mDoingEditText.setHint(R.string.this_book);
                break;
            case R.id.menu_item_watching:
                mDoingEditText.setHint(R.string.this_movie_show);
                break;
            case R.id.menu_item_enjoying:
                mDoingEditText.setHint(R.string.doing_this);
                break;
        }
    }

    private void setDoingAction(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_item_listening:
                mDoingActionSelected = DoingAction.LISTENING;
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorListening), PorterDuff.Mode.SRC_IN);
                break;
            case R.id.menu_item_playing:
                mDoingActionSelected = DoingAction.PLAYING;
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPlaying), PorterDuff.Mode.SRC_IN);
                break;
            case R.id.menu_item_reading:
                mDoingActionSelected = DoingAction.READING;
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorReading), PorterDuff.Mode.SRC_IN);
                break;
            case R.id.menu_item_watching:
                mDoingActionSelected = DoingAction.WATCHING;
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorWatching), PorterDuff.Mode.SRC_IN);
                break;
            case R.id.menu_item_enjoying:
                mDoingActionSelected = DoingAction.ENJOYING;
                mDoingActionButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorEnjoying), PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    private static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
