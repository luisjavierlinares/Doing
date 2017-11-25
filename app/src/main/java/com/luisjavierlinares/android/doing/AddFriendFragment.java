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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.events.FriendCheckedEvent;
import com.luisjavierlinares.android.doing.events.FriendSearchEvent;
import com.luisjavierlinares.android.doing.events.FriendSuggestionsEvent;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.utils.FriendMessage;
import com.luisjavierlinares.android.doing.utils.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Luis on 30/04/2017.
 */

public class AddFriendFragment extends DialogFragment {

    private static final String ARG_FRIEND_NAME = "com.luisjavierlinares.android.Doing.AddFriendFragment.arg_friend_alias";
    private static final String ARG_CONFIRM_FRIEND = "com.luisjavierlinares.android.Doing.AddFriendFragment.arg_confirm_friend";
    private static final String ARG_OK_TEXT = "com.luisjavierlinares.android.Doing.AddFriendFragment.arg_ok_text";

    private static final String EXTRA_USER_CODE = "com.luisjavierlinares.android.Doing.AddFriendFragment.extra_user_code";
    private static final String EXTRA_USER_NAME = "com.luisjavierlinares.android.Doing.AddFriendFragment.extra_user_name";
    private static final String EXTRA_FRIEND_CODE = "com.luisjavierlinares.android.Doing.AddFriendFragment.extra_friend_code";
    private static final String EXTRA_FRIEND_ALIAS = "com.luisjavierlinares.android.Doing.AddFriendFragment.extra_friend_name";

    private DoingController mController;

    List<User> mFriendSuggestions;
    List<User> mFriendCompletions;

    private AlertDialog mDialog;
    private EditText mFriendNameEditText;
    private String mFriendName;
    private Boolean mIsConfirmingFriend;
    private TextView mAskFriendTextView;
    private String mOkText;
    private TextView mSuggestedFriendsTextView;
    private TextView mAndMoreTextView;
    private RecyclerView mSuggestionsRecyclerView;
    private SuggestionAdapter mSuggestionAdapter;
    private RecyclerView mCompletionsRecyclerView;
    private SuggestionAdapter mCompletionAdapter;

    private EventBus mEventBus;

    public static AddFriendFragment newInstance() {
        Bundle args = new Bundle();

        AddFriendFragment fragment = new AddFriendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AddFriendFragment newInstance(String friendName, String friendCode, String okText) {
        Bundle args = new Bundle();

        args.putString(ARG_FRIEND_NAME, friendName);
        args.putBoolean(ARG_CONFIRM_FRIEND, true);
        args.putString(ARG_OK_TEXT, okText);

        AddFriendFragment fragment = new AddFriendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
        mEventBus = EventBus.getDefault();

        if (getArguments() != null) {
            mFriendName = getArguments().getString(ARG_FRIEND_NAME);
            mIsConfirmingFriend = getArguments().getBoolean(ARG_CONFIRM_FRIEND);
            mOkText = getArguments().getString(ARG_OK_TEXT);
            if (mOkText == null) {
                mOkText = new String();
            }
        } else {
            mIsConfirmingFriend = false;
        }

        mFriendSuggestions = new ArrayList<>();
        mFriendCompletions = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventBus.register(this);
        mEventBus.removeStickyEvent(FriendCheckedEvent.class);
        mEventBus.removeStickyEvent(FriendSearchEvent.class);
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventBus.unregister(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_friend, null);

        mFriendNameEditText = (EditText) view.findViewById(R.id.add_friend_edit_text_name);
        mFriendNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mIsConfirmingFriend) {
                    mController.searchForOnlineUsers(s.toString());
                }
            }
        });

        mAskFriendTextView = (TextView) view.findViewById(R.id.add_friend_ask_your_friend);
        mSuggestedFriendsTextView = (TextView) view.findViewById(R.id.friends_suggestions_title);
        mAndMoreTextView = (TextView) view.findViewById(R.id.add_friend_and_more);

        mSuggestionsRecyclerView = (RecyclerView) view.findViewById(R.id.friends_suggestions_recycler_view);
        mSuggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSuggestionsRecyclerView.setFocusable(false);

        mCompletionsRecyclerView = (RecyclerView) view.findViewById(R.id.friends_completions_recycler_view);
        mCompletionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCompletionsRecyclerView.setFocusable(false);


        if (mFriendName != null) {
            mFriendNameEditText.setText(mFriendName);
        }

        if (mIsConfirmingFriend) {
            mFriendNameEditText.setEnabled(false);
            mFriendNameEditText.setTextColor(getResources().getColor(R.color.inactiveDarkColor));
            mAskFriendTextView.setVisibility(View.GONE);
            mSuggestedFriendsTextView.setVisibility(View.GONE);
            mSuggestionsRecyclerView.setVisibility(View.GONE);
            mCompletionsRecyclerView.setVisibility(View.GONE);
            mAndMoreTextView.setVisibility(View.GONE);
        } else {
            mAskFriendTextView.setVisibility(View.VISIBLE);
            mAskFriendTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FriendMessage friendMessage = new FriendMessage(getActivity());
                    friendMessage.sendFriendshipMessage();
                }
            });
            findFriendSuggestionsOnBackground();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        if (mIsConfirmingFriend) {
            builder.setPositiveButton(mOkText, null);
        } else {
            builder.setPositiveButton(R.string.ok, null);
        }
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mDialog = builder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        boolean inputError = false;
                        String friendName = mFriendNameEditText.getText().toString();
                        String compactFriendName = TextUtils.toNoAccentsNoSpacesLowerCase(friendName);
                        String myCompactName = TextUtils.toNoAccentsNoSpacesLowerCase(mController.getMyUser().getFriendName());

                        if (friendName.trim().isEmpty()) {
                            mFriendNameEditText.setError(getString(R.string.error_message_empty_field));
                            inputError = true;
                        } else if (compactFriendName.equals(myCompactName)) {
                            mFriendNameEditText.setError(getString(R.string.error_message_friend_code_yourself));
                            inputError = true;
                        }

                        if ((!mIsConfirmingFriend) && (!inputError) && mController.existsUserWithCompactName(compactFriendName)) {
                            mFriendNameEditText.setError(getString(R.string.error_message_friend_exists));
                            inputError = true;
                        }

                        if ((!mController.isDeviceOnline()) && (!inputError)) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.no_net_message),Toast.LENGTH_LONG).show();
                            inputError = true;
                        }

                        if (inputError) {
                            return;
                        }

                        mFriendName = friendName;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mController.checkFriend(mFriendName);
                            }
                        }).start();

                    }
                });
            }
        });

        return mDialog;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFriendCheckedEvent(FriendCheckedEvent friendCheckedEvent) {
        mEventBus.removeStickyEvent(friendCheckedEvent);

        if (!friendCheckedEvent.success()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_server_message),Toast.LENGTH_LONG).show();
            return;
        }

        if (!friendCheckedEvent.exists()) {
            mFriendNameEditText.setError(getString(R.string.error_message_friend_dont_exists));
            return;
        }

        String friendCode = friendCheckedEvent.getFriendCode();
        String userCode = friendCheckedEvent.getUserCode();
        String friendName = friendCheckedEvent.getFriendName();

        sendResult(Activity.RESULT_OK, mFriendName, userCode, friendCode, friendName);
        mDialog.dismiss();
    }

    private void sendResult(int resultCode, String friendAlias, String userCode, String friendCode, String userName) {
        if (getTargetFragment() == null) return;

        Intent intent = new Intent();
        intent.putExtra(EXTRA_FRIEND_ALIAS, friendAlias);
        intent.putExtra(EXTRA_USER_CODE, userCode);
        intent.putExtra(EXTRA_FRIEND_CODE, friendCode);
        intent.putExtra(EXTRA_USER_NAME, userName);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    public static String getFriendAlias(Intent result) {
        return result.getStringExtra(EXTRA_FRIEND_ALIAS);
    }

    public static String getUserCode(Intent result) {
        return result.getStringExtra(EXTRA_USER_CODE);
    }

    public static String getFriendCode(Intent result) {
        return result.getStringExtra(EXTRA_FRIEND_CODE);
    }

    public static String getUserName(Intent result) {
        return result.getStringExtra(EXTRA_USER_NAME);
    }

    private void findFriendSuggestionsOnBackground() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                findFriendSuggestions();
                mEventBus.postSticky(new FriendSuggestionsEvent());
            }
        });
        thread.start();
    }

    private void findFriendSuggestions() {
        mFriendSuggestions = mController.getFriendSuggestions(20);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFriendSuggestionsEvent(FriendSuggestionsEvent friendSuggestionsEvent) {
        EventBus.getDefault().removeStickyEvent(friendSuggestionsEvent);
        if (!mFriendSuggestions.isEmpty()) {
            mSuggestedFriendsTextView.setVisibility(View.VISIBLE);
            mSuggestionsRecyclerView.setVisibility(View.VISIBLE);
            mSuggestionAdapter = new SuggestionAdapter(mFriendSuggestions);
            mSuggestionsRecyclerView.setAdapter(mSuggestionAdapter);
        } else {
            mSuggestedFriendsTextView.setVisibility(View.GONE);
            mSuggestionsRecyclerView.setVisibility(View.GONE);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFriendSearchEvent(FriendSearchEvent friendSearchEvent) {
        EventBus.getDefault().removeStickyEvent(friendSearchEvent);
        mFriendCompletions = friendSearchEvent.getFriends();
        if (!mFriendCompletions.isEmpty()) {
            mCompletionsRecyclerView.setVisibility(View.VISIBLE);
            mCompletionAdapter = new SuggestionAdapter(mFriendCompletions);
            mCompletionsRecyclerView.setAdapter(mCompletionAdapter);
            if (friendSearchEvent.thereAreMore()) {
                mAndMoreTextView.setVisibility(View.VISIBLE);
            } else {
                mAndMoreTextView.setVisibility(View.GONE);
            }
        } else {
            mCompletionsRecyclerView.setVisibility(View.GONE);
            mAndMoreTextView.setVisibility(View.GONE);
        }
    }

    private class SuggestionHolder extends RecyclerView.ViewHolder {

        private User mSuggestion;
        private TextView mSuggestionName;
        private ImageView mSuggestionAddButton;

        public SuggestionHolder(View itemView) {
            super(itemView);
            mSuggestionName = (TextView) itemView.findViewById(R.id.suggested_friend_name);
            mSuggestionAddButton = (ImageView) itemView.findViewById(R.id.suggested_friend_button);
        }

        public void bindSuggestion(final User suggestion){
            mSuggestion = suggestion;
            mSuggestionName.setText(DoingUiUtils.getUserNameMeString(getActivity(), mSuggestion));
            mSuggestionName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFriendNameEditText.setText(suggestion.getName());
                }
            });
            mSuggestionAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFriendNameEditText.setText(suggestion.getName());
                }
            });
        }
    }


    private class SuggestionAdapter extends RecyclerView.Adapter<SuggestionHolder> {

        List<User> mSuggestions;

        public SuggestionAdapter(List<User> suggestions) {
            mSuggestions = suggestions;
        }

        @Override
        public SuggestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_friend_suggestion, parent, false);

            return new SuggestionHolder(view);
        }

        @Override
        public void onBindViewHolder(SuggestionHolder holder, int position) {
            User suggestion = mSuggestions.get(position);
            holder.bindSuggestion(suggestion);
        }

        @Override
        public int getItemCount() {
            return mSuggestions.size();
        }
    }
}
