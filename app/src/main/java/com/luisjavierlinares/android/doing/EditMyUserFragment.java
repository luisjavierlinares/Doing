package com.luisjavierlinares.android.doing;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.utils.ImageUtils;
import com.soundcloud.android.crop.Crop;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * Created by Luis on 02/05/2017.
 */

public class EditMyUserFragment extends DialogFragment {

    private static final String KEY_AVATAR_BITMAP = "avatar_bitmap";
    private static final String KEY_UPDATE_AVATAR = "update_avatar";
    private static final String KEY_MY_FRIEND_NAME = "mi_friend_name";
    private static final int PERM_REQUEST_EXTERNAL_STORAGE = 0;

    private DoingController mController;

    private EditText mMyFriendName;
    private ImageView mMyUserAvatar;
    private View mMyPictureView;
    private Bitmap mAvatarBitmap;
    private boolean mUpdateAvatar;

    private EventBus mEventBus;

    public static EditMyUserFragment newInstance() {
        Bundle arguments = new Bundle();

        EditMyUserFragment myUserDetailFragment = new EditMyUserFragment();
        myUserDetailFragment.setArguments(arguments);
        return myUserDetailFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = DoingController.get(getActivity());
        mEventBus = EventBus.getDefault();
        mUpdateAvatar = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mUpdateAvatar) {
            outState.putParcelable(KEY_AVATAR_BITMAP, mAvatarBitmap);
        }
        outState.putBoolean(KEY_UPDATE_AVATAR, mUpdateAvatar);
        outState.putString(KEY_MY_FRIEND_NAME, mMyFriendName.getText().toString());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_my_user_edit, null);

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

        if (savedInstanceState != null) {
            mUpdateAvatar = savedInstanceState.getBoolean(KEY_UPDATE_AVATAR);
            mMyFriendName.setText(savedInstanceState.getString(KEY_MY_FRIEND_NAME));
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

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        User myUser = mController.getMyUser();
        mMyFriendName.setText(myUser.getFriendName());
        mMyFriendName.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_500));
        mMyFriendName.setEnabled(false);
        mMyFriendName.setTextColor(getResources().getColor(R.color.inactiveDarkColor));
        setCancelable(true);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (mUpdateAvatar) {
                            User myUser = mController.getMyUser();
                            mController.updateAvatar(myUser, mAvatarBitmap);
                        }

                        dialog.dismiss();
                    }
                });
            }
        });

        return dialog;
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

            if (uri == null) {
                return;
            }

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
