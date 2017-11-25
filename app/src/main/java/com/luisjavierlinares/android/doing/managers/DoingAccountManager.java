package com.luisjavierlinares.android.doing.managers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Patterns;

import com.luisjavierlinares.android.doing.DoingSettings;
import com.luisjavierlinares.android.doing.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Luis on 28/09/2017.
 */

public class DoingAccountManager {

    private static final String DOING_EMAIL = "DoingApp.com";
    private static final int PASS_SIZE = 20;

    private static DoingAccountManager sDoingAccountManager;
    private Context mContext;

    public static synchronized DoingAccountManager get(Context context){
        if (sDoingAccountManager == null){
            sDoingAccountManager = new DoingAccountManager(context);
        }
        return sDoingAccountManager;
    }

    private DoingAccountManager(Context context) {
        mContext = context;
    }

    public String getOnlineId() {
        return DoingSettings.getMyOnlineId(mContext);
    }

    public void setOnlineId(String onlineId) {
        DoingSettings.setMyOnlineId(mContext, onlineId);
    }

    public String getOnlineSecret() {
        return DoingSettings.getMyOnlineSecret(mContext);
    }

    public void setOnlineSecret(String onlineSecret) {
        DoingSettings.setMyOnlineSecret(mContext, onlineSecret);
    }

    public String getOrCreateOnlineId() {
        String onlineId;

        // get or create user online id and secret
        if (DoingSettings.getMyOnlineId(mContext) == null) {
            onlineId = generateUserEmail();
            DoingSettings.setMyOnlineId(mContext, onlineId);
        } else {
            onlineId = DoingSettings.getMyOnlineId(mContext);
        }

        return onlineId;
    }

    public String getOrCreateOnlineSecret() {
        String password;

        if (DoingSettings.getMyOnlineSecret(mContext) == null) {
            password = RandomUtils.getRandomPassword(PASS_SIZE);
            DoingSettings.setMyOnlineSecret(mContext, password);
        } else {
            password = DoingSettings.getMyOnlineSecret(mContext);
        }

        return password;
    }

    private String generateUserEmail() {
        String userNameString = "user".concat(RandomUtils.getBase58ReadableRandom(10));
        String fillerString = RandomUtils.getBase58ReadableRandom(20);
        return userNameString.concat("@").concat(fillerString).concat(DOING_EMAIL);
    }

    public String getRecoveryCode() {
        String onlineId = getOnlineId();
        String onlineSecret = getOnlineSecret();

        if ((onlineId == null) || (onlineSecret == null)){return "";};

        String userRandom = onlineId.substring(4, 14);
        String filler = onlineId.substring(15, 35);

        String recoveryCode = userRandom.concat(filler).concat(onlineSecret);

        return recoveryCode;
    }

    public String getOnlineIdFromRecoveryCode(String recoveryCode) {
        String userRandom = recoveryCode.substring(0, 10);
        String filler = recoveryCode.substring(10,30);

        String onlineId = "user".concat(userRandom).concat("@").concat(filler).concat(DOING_EMAIL);
        return onlineId;
    }

    public String getOnlineSecretFromRecoveryCode(String recoveryCode) {
        String onlineSecret = recoveryCode.substring(30);
        return onlineSecret;
    }

    public void setAccountFromRecoveryCode(String recoveryCode) {
        String onlineId = getOnlineIdFromRecoveryCode(recoveryCode);
        String onlineSecret = getOnlineSecretFromRecoveryCode(recoveryCode);

        setOnlineId(onlineId);
        setOnlineSecret(onlineSecret);
    }

}
