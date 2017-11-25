package com.luisjavierlinares.android.doing.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.luisjavierlinares.android.doing.R;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.managers.StorageManager;

import java.io.File;

/**
 * Created by Luis on 09/06/2017.
 */

public class FriendMessage {

    private Context mContext;

    public FriendMessage(Context context) {
        mContext = context;
    }

    public void sendFriendshipMessage() {
        String subject = mContext.getString(R.string.invitation_message_subject);
        String text = generateFriendShipMessageText();
        sendMessage(subject, text);
    }

    public void sendCodeAskingMessage() {
        String subject = mContext.getString(R.string.code_asking_message_subject);
        String text = generateCodeAskingMessageText();
        sendMessage(subject, text);
    }

    private void sendMessage(String messageSubject, String messageText) {
        String chooserTitle = mContext.getString(R.string.send_message_friend_chooser);

        BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(mContext, R.drawable.ic_doing_round_nodpi);
        Bitmap bitmap = drawable.getBitmap();
        String imagePath = StorageManager.get(mContext).saveExternalImage(bitmap, "doing_icon");
        File file = new File(imagePath);
        Uri uri = null;

        if (imagePath != null) {
            uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName()
                    + ".provider", file);
        }

        Intent shareIntent = ShareCompat.IntentBuilder.from((Activity) mContext)
                .setType("text/plain")
                .setSubject(messageSubject)
                .setText(messageText)
                .addStream(uri)
                .setChooserTitle(chooserTitle)
                .getIntent();

        if (shareIntent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(shareIntent);
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.no_app_available_send_message), Toast.LENGTH_LONG);
        }
    }

    private String generateFriendShipMessageText() {
        String myFriendName = DoingController.get(mContext).getMyUser().getFriendName();

        if (myFriendName == null) {
            myFriendName = new String();
        }

        String content1 = mContext.getString(R.string.invitation_message_content_1);
        String content2 = mContext.getString(R.string.invitation_message_content_2);
        String content3 = mContext.getString(R.string.invitation_message_content_3);
        String content4 = mContext.getString(R.string.invitation_message_content_4);
        String marketUrl = mContext.getString(R.string.market_app_url);
        String br = "\n";

        String content = content1 + br + content2 + br + marketUrl + br + br + content3 + " " +
                         myFriendName + "." + br + content4;

        return content;
    }

    private String generateCodeAskingMessageText() {
        String myFriendCode = DoingController.get(mContext).getMyUser().getFriendCode();

        if (myFriendCode == null) {
            myFriendCode = new String();
        }

        String content1 = mContext.getString(R.string.code_asking_message_content_1);
        String content2 = mContext.getString(R.string.code_asking_message_content_2);
        String content3 = mContext.getString(R.string.code_asking_message_content_3);
        String content4 = mContext.getString(R.string.code_asking_message_content_4);
        String content5 = mContext.getString(R.string.code_asking_message_content_5);
        String content6 = mContext.getString(R.string.code_asking_message_content_6);
        String marketUrl = mContext.getString(R.string.market_app_url);
        String br = "\n";

        String content = content1 + br + br + content2 + br + content3 + " " + myFriendCode + content4 +
                         br + content5 + br + marketUrl + content6;

        return content;
    }
}
