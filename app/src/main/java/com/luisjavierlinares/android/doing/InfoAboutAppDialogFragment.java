package com.luisjavierlinares.android.doing;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.managers.DoingAccountManager;

import java.util.List;

/**
 * Created by Luis on 15/09/2017.
 */

public class InfoAboutAppDialogFragment extends DialogFragment {

    private static final String DIALOG_LICENSES = "com.luisjavierlinares.android.Doing.InfoAboutAppDialogFragment.dialog_licenses";

    public static InfoAboutAppDialogFragment newInstance() {
        Bundle arguments = new Bundle();

        InfoAboutAppDialogFragment infoFragment = new InfoAboutAppDialogFragment();
        infoFragment.setArguments(arguments);

        return infoFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.info_about_app, null);

        ImageButton mUpButton = (ImageButton) view.findViewById(R.id.info_about_app_back_button);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        TextView mLicensesText = (TextView) view.findViewById(R.id.info_about_app_licenses);
        mLicensesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_licenses();
            }
        });

        return view;
    }

    public void show_licenses() {
        FragmentManager fragmentManager = getFragmentManager();
        InfoLicensesDialogFragment dialog = InfoLicensesDialogFragment.newInstance();
        dialog.show(fragmentManager, DIALOG_LICENSES);
    }
}
