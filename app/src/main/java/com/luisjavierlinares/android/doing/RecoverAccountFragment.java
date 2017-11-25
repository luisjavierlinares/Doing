package com.luisjavierlinares.android.doing;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.events.AccountRecoveredEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by Luis on 29/09/2017.
 */

public class RecoverAccountFragment extends DialogFragment {

    private static int RECOVERY_CODE_LENGTH = 50;

    private static final String KEY_ARE_ELEMENTS_ENABLED = "are_elements_enabled";

    private DoingController mController;

    private AlertDialog mDialog;
    private ImageView mPasteCodeButton;
    private EditText mRecoveryCodeText;
    private Button mOkButton;
    private Button mCancelButton;

    private Boolean mAreElementsEnabled;

    private EventBus mEventBus;

    public static RecoverAccountFragment newInstance() {
        Bundle arguments = new Bundle();

        RecoverAccountFragment fragment = new RecoverAccountFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_ARE_ELEMENTS_ENABLED, mAreElementsEnabled);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
        mEventBus = EventBus.getDefault();
        mAreElementsEnabled = true;
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_recover_account, null);

        if (savedInstanceState != null) {
            mAreElementsEnabled =  savedInstanceState.getBoolean(KEY_ARE_ELEMENTS_ENABLED);
        }

        mPasteCodeButton = (ImageView) view.findViewById(R.id.recovery_code_paste_button);
        mPasteCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData myClip;
                ClipboardManager myClipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);

                if (myClipboard.hasPrimaryClip()) {
                    myClip = myClipboard.getPrimaryClip();
                } else {
                    return;
                }

                if (myClip.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    ClipData.Item item = myClip.getItemAt(0);
                    String recoveryCode = item.getText().toString();
                    mRecoveryCodeText.setText(recoveryCode);
                } else {
                    return;
                }
            }
        });

        mRecoveryCodeText = (EditText) view.findViewById(R.id.recovery_code_input_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mDialog = builder.create();
        mCancelButton = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        mOkButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                mCancelButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                mOkButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                if (mAreElementsEnabled) {
                    enableUiElements();
                } else {
                    disableUiElements();
                }

                mOkButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        boolean inputError = false;
                        String recoveryCode = mRecoveryCodeText.getText().toString();

                        if (recoveryCode.trim().isEmpty()) {
                            mRecoveryCodeText.setError(getString(R.string.no_recovery_code_error));
                            inputError = true;
                        } else if (recoveryCode.trim().length() < RECOVERY_CODE_LENGTH) {
                            mRecoveryCodeText.setError(getString(R.string.short_recovery_code_error));
                            inputError = true;
                        } else if (!mController.isDeviceOnline()) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.no_net_message),Toast.LENGTH_LONG).show();
                            inputError = true;
                        }

                        if (inputError) {
                            return;
                        }

                        disableUiElements();
                        mController.recoverMyUser(recoveryCode);
                    }
                });
            }
        });

        return mDialog;

    }

    private void disableUiElements() {
        setCancelable(false);
        mOkButton.setEnabled(false);
        mCancelButton.setEnabled(false);
        mPasteCodeButton.setEnabled(false);
        mRecoveryCodeText.setEnabled(false);
        mAreElementsEnabled = false;
    }

    private void enableUiElements() {
        setCancelable(true);
        mOkButton.setEnabled(true);
        mCancelButton.setEnabled(true);
        mPasteCodeButton.setEnabled(true);
        mRecoveryCodeText.setEnabled(true);
        mAreElementsEnabled = true;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAccountRecoveredEvent(AccountRecoveredEvent accountRecoveredEvent) {
        EventBus.getDefault().removeStickyEvent(accountRecoveredEvent);
        if (accountRecoveredEvent.success()) {
            mDialog.dismiss();
        } else {
            enableUiElements();
            Toast.makeText(getActivity(), getResources().getString(R.string.account_recovery_error),Toast.LENGTH_LONG).show();
        }
    }

}
