package com.luisjavierlinares.android.doing;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.utils.TextUtils;

import java.io.InputStream;

/**
 * Created by Luis on 15/09/2017.
 */

public class InfoLicensesDialogFragment extends DialogFragment {

    public static InfoLicensesDialogFragment newInstance() {
        Bundle arguments = new Bundle();

        InfoLicensesDialogFragment infoFragment = new InfoLicensesDialogFragment();
        infoFragment.setArguments(arguments);

        return infoFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.info_licenses, null);

        InputStream licensesIs = getResources().openRawResource(R.raw.licenses);
        String licensesText = TextUtils.convertStreamToString(licensesIs);

        TextView licensesTextView = (TextView) view.findViewById(R.id.info_licenses_text);
        licensesTextView.setText(licensesText);

        ImageButton mUpButton = (ImageButton) view.findViewById(R.id.info_licenses_back_button);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        return dialog;
    }
}
