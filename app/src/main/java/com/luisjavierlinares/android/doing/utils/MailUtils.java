package com.luisjavierlinares.android.doing.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.luisjavierlinares.android.doing.R;

/**
 * Created by Luis on 28/09/2017.
 */

public class MailUtils {

    public static void sendMail(Context context, String subject, String text) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/html");
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_by_email)));
    }
}
