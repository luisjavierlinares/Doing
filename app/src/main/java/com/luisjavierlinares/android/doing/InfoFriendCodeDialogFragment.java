package com.luisjavierlinares.android.doing;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.R;
import com.luisjavierlinares.android.doing.utils.FriendMessage;

/**
 * Created by Luis on 15/09/2017.
 */

public class InfoFriendCodeDialogFragment extends DialogFragment {

    public static InfoFriendCodeDialogFragment newInstance() {
        Bundle arguments = new Bundle();

        InfoFriendCodeDialogFragment infoFragment = new InfoFriendCodeDialogFragment();
        infoFragment.setArguments(arguments);

        return infoFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.info_help_friend_code, null);

        ImageButton mUpButton = (ImageButton) view.findViewById(R.id.help_friend_code_back_button);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        TextView askFriendButton = (TextView) view.findViewById(R.id.friend_code_help_invite_your_friend);
        askFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendMessage friendMessage = new FriendMessage(getActivity());
                friendMessage.sendFriendshipMessage();
            }
        });

        String text = "<html><body style=\"text-align:justify\"><p style=\"font-size:14px; color:#616161\";> %s </p> </body></Html>";
        String data = getResources().getString(R.string.help_friend_code_text_1)
                .concat("<br><br>")
                .concat(getResources().getString(R.string.help_friend_code_text_2))
                .concat("<br><br>")
                .concat(getResources().getString(R.string.help_friend_code_text_3));

        WebView webView = (WebView) view.findViewById(R.id.help_friend_code_web_view);
        webView.loadDataWithBaseURL(null, String.format(text, data), "text/html", "utf-8", null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        return dialog;
    }

}