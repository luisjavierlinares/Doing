package com.luisjavierlinares.android.doing;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.events.MyUserCreatedEvent;
import com.luisjavierlinares.android.doing.events.UserNameExistsEvent;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.utils.ImageUtils;
import com.luisjavierlinares.android.doing.utils.TextUtils;
import com.soundcloud.android.crop.Crop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

/**
 * Created by Luis on 02/05/2017.
 */

public class CreateMyUserFragment extends DialogFragment {

    private static final int AKEYCODE_BACK = 4;
    private static final int USER_NAME_MIN_LENGHT = 3;

    private static final String KEY_AVATAR_BITMAP = "avatar_bitmap";
    private static final String KEY_UPDATE_AVATAR = "update_avatar";
    private static final String KEY_MY_FRIEND_NAME = "mi_friend_name";
    private static final String KEY_ELEMENTS_ENABLED = "elements_enabled";
    private static final int PERM_REQUEST_EXTERNAL_STORAGE = 0;

    private static final String DIALOG_RECOVER_ACCOUNT = "com.luisjavierlinares.android.doing.EditMyUserFragment.recover_account";

    private DoingController mController;

    private AlertDialog mDialog;
    private EditText mMyFriendName;
    private ImageView mMyUserAvatar;
    private View mMyPictureView;
    private Bitmap mAvatarBitmap;
    private boolean mUpdateAvatar;
    private Button mOkButton;
    private TextView mRecoverAccountButton;

    private Boolean mAreElementsEnabled;

    private EventBus mEventBus;

    public static CreateMyUserFragment newInstance() {
        Bundle arguments = new Bundle();

        CreateMyUserFragment myUserDetailFragment = new CreateMyUserFragment();
        myUserDetailFragment.setArguments(arguments);
        return myUserDetailFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
        mEventBus = EventBus.getDefault();
        mUpdateAvatar = false;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mUpdateAvatar) {
            outState.putParcelable(KEY_AVATAR_BITMAP, mAvatarBitmap);
        }
        outState.putBoolean(KEY_UPDATE_AVATAR, mUpdateAvatar);
        outState.putString(KEY_MY_FRIEND_NAME, mMyFriendName.getText().toString());
        outState.putBoolean(KEY_ELEMENTS_ENABLED, mAreElementsEnabled);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_my_user_create, null);

        mMyPictureView = (View) view.findViewById(R.id.item_user_my_user_image);
        mMyPictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                request_external_storage_perm();

                if (has_external_storage_perm()) {
                    requestExternalImage();
                }
            }
        });

        mMyUserAvatar = (ImageView) view.findViewById(R.id.user_avatar);
        mMyFriendName = (EditText) view.findViewById(R.id.insert_my_text_name);
        mRecoverAccountButton = (TextView) view.findViewById(R.id.recover_account_button);
        mRecoverAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                RecoverAccountFragment dialog = RecoverAccountFragment.newInstance();
                dialog.show(fragmentManager, DIALOG_RECOVER_ACCOUNT);
            }
        });

        if (savedInstanceState != null) {
            mUpdateAvatar = savedInstanceState.getBoolean(KEY_UPDATE_AVATAR);
            mMyFriendName.setText(savedInstanceState.getString(KEY_MY_FRIEND_NAME));
            mAreElementsEnabled = savedInstanceState.getBoolean(KEY_ELEMENTS_ENABLED);
        }

        if (mUpdateAvatar) {
            mAvatarBitmap = savedInstanceState.getParcelable(KEY_AVATAR_BITMAP);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), mAvatarBitmap);
            roundedBitmapDrawable.setCircular(true);
            mMyUserAvatar.setImageDrawable(roundedBitmapDrawable);
        } else {
            mMyUserAvatar.setImageDrawable(DoingUiUtils.getUserAvatar(getActivity(), mController.getMyUser()));
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, null);

        mMyFriendName.setEnabled(true);
        setCancelable(false);

        mDialog = builder.create();
        mOkButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                mOkButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                if (mAreElementsEnabled) {
                    enableUielements();
                } else {
                    disableUiElements();
                }

                mOkButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        boolean inputError = false;
                        String friendName = mMyFriendName.getText().toString();

                        if (friendName.trim().isEmpty()) {
                            mMyFriendName.setError(getString(R.string.insert_my_name_error));
                            inputError = true;
                        } else if (TextUtils.countWords(friendName) < 2) {
                            mMyFriendName.setError(getString(R.string.insert_my_name_error_last_name));
                            inputError = true;
                        } else if (friendName.trim().length() < USER_NAME_MIN_LENGHT) {
                            mMyFriendName.setError(getString(R.string.insert_my_name_error_short));
                            inputError = true;
                        }

                        if ((!mController.isDeviceOnline()) && (!inputError)) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.no_net_message),Toast.LENGTH_LONG).show();
                            inputError = true;
                        }

                        if (inputError) {
                            return;
                        }

                        disableUiElements();
                        mController.checkIfNameExists(mMyFriendName.getText().toString());

                    }
                });
            }
        });

        // Back button on user registration exits the app
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == AKEYCODE_BACK) {
//                    dialog.dismiss();
                    getActivity().finish();
                }
                return false;
            }
        });

        return mDialog;
    }

    private void disableUiElements() {
        mOkButton.setEnabled(false);
        mMyFriendName.setEnabled(false);
        mAreElementsEnabled = false;
    }

    private void enableUielements() {
        mOkButton.setEnabled(true);
        mMyFriendName.setEnabled(true);
        mAreElementsEnabled = true;
    }

    private void createMyUser() {
        User myUser = mController.getMyUser();
        myUser.setFriendName(mMyFriendName.getText().toString());
        mController.updateUser(myUser);

        if (mUpdateAvatar) {
            myUser = mController.getMyUser();
            mController.updateAvatar(myUser, mAvatarBitmap);
        }

        mDialog.dismiss();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUserNameExistsEvent(UserNameExistsEvent userNameExistsEvent) {
        EventBus.getDefault().removeStickyEvent(userNameExistsEvent);
        if (userNameExistsEvent.success()) {
            if (!userNameExistsEvent.exists()) {
                createMyUser();
                mDialog.dismiss();
            } else {
                enableUielements();
                mMyFriendName.setError(getString(R.string.name_already_exists));
            }
        } else {
            enableUielements();
            Toast.makeText(getActivity(), getResources().getString(R.string.name_exists_error),Toast.LENGTH_LONG).show();
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMyUserCreatedEvent(MyUserCreatedEvent myUserCreatedEvent) {
        EventBus.getDefault().removeStickyEvent(myUserCreatedEvent);
        mDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Crop.REQUEST_PICK && resultCode == Activity.RESULT_OK) {
            Uri uriDestination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
            Uri uriSource = data.getData();
            Crop.of(uriSource, uriDestination).asSquare().start(getActivity(), this);
        }

        if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            Uri uri = Crop.getOutput(data);

            if (uri == null) {return;}

            String path = uri.getPath();
            mAvatarBitmap = ImageUtils.getScaledBitmap(path, DoingUiUtils.getAvatarWidth(getActivity()), DoingUiUtils.getAvatarHeight(getActivity()));
            mUpdateAvatar = true;
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), mAvatarBitmap);
            roundedBitmapDrawable.setCircular(true);
            mMyUserAvatar.setImageDrawable(roundedBitmapDrawable);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERM_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestExternalImage();
                } else {
                    String ErrorMessage = getResources().getString(R.string.external_storage_permission_denied);
                    Snackbar.make(this.getView(), ErrorMessage, Snackbar.LENGTH_LONG)
                            .show();
                }
                return;
            }
        }
    }

    private void requestExternalImage() {
        Crop.pickImage(getActivity(), this);
    }

    private void request_external_storage_perm() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERM_REQUEST_EXTERNAL_STORAGE);
        }
    }

    private boolean has_external_storage_perm() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }



}
