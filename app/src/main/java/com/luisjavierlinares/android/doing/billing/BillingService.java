package com.luisjavierlinares.android.doing.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.luisjavierlinares.android.doing.R;
import com.luisjavierlinares.android.doing.managers.AdsManager;

import java.util.ArrayList;

/**
 * Created by Luis on 18/09/2017.
 */

public class BillingService {

    private static final String APP_FULL_NAME = "com.luisjavierlinares.android.doing";

    private static final String SKU_REMOVE_ADS = "doing.remove_ads";
//    private static final String SKU_REMOVE_ADS = "android.test.canceled";
    private static final String BASE64_ENCODED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2d/ZmNnlSMRt8p9F02p6rvOjXq05F1XGDop66vxevusdDaqSq4OkMF7iBytiUW1GaOBPE/W88R5lfbiI5ZXuAmkSUmHr7aowwIXzaNWlUsyI1WXozgEoqF7lY8fIob7+QmDaPqdq+klzGdOpNUjlHEPQUKNF4/pKOhNbiVq6PJHM9BFzX//y6kvgd4fJGOOP1YpCpnYbLI+ShRd+zsWsUQmZGzO5ZbgGVmMFkoittb9jSohl/OiF8OwvCAvgmEeQXvmESVdTgyFNDalKVk8CCyhYrixbemp8m1inlRXG4UrfKtb5XrzqUjzaF9qh41wH+C06vEPgDpCMrR0SvCPUUwIDAQAB";
    private static final String DEVELOPER_PAYLOAD = "ZFXijdu8UIHAX9XhuG";

    private static int RESPONSE_ALREADY_OWNED = 7;

    public static final int REQUEST_PURCHASE = 1101;

    private static BillingService sBillingService;

    private Context mContext;
    private IabHelper mHelper;
    private AdsManager mAdsManager;

    public static synchronized BillingService get(Context context) {
        if (sBillingService == null) {
            sBillingService = new BillingService(context);
        }
        return sBillingService;
    }

    private BillingService(Context context) {
        mContext = context;
        mAdsManager = AdsManager.get(mContext);
    }

    public void start() {

        // Create the helper, passing it our context and the public key to
        // verify signatures with
        mHelper = new IabHelper(mContext, BASE64_ENCODED_PUBLIC_KEY);

        // enable debug logging (for a production application, you should set
        // this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
         mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    // complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed off in the meantime? If so, quit.
                if (mHelper == null)
                    return;

                // IAB is fully set up. Now, let's get an inventory of stuff we
                // own.
                mHelper.queryInventoryAsync(mGotInventoryListener);

                checkOwnedItems(mHelper.mService);

            }
        });
    }

    private void checkOwnedItems(IInAppBillingService service) {
        try {
            Bundle ownedItems = service.getPurchases(3, APP_FULL_NAME, "inapp",  null);
            if (ownedItems.getInt("RESPONSE_CODE") == 0) {
                ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                for (int i = 0; i < ownedSkus.size(); ++i) {
                    String sku = ownedSkus.get(i);
                    if (sku.equals(SKU_REMOVE_ADS)) {
                        removeAds();
                    }
                }
            };
        } catch (RemoteException e) { }
    }

    // Listener that's called when we finish querying the items and
    // subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null)
                return;

            // Is it a failure?
            if (result.isFailure()) {
                // complain("Failed to query inventory: " + result);
                return;
            }

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase removeAdsPurchase = inventory.getPurchase(SKU_REMOVE_ADS);
            if (removeAdsPurchase != null && verifyDeveloperPayload(removeAdsPurchase)) {
                removeAds();
            }
        }
    };

    // User clicked the "Remove Ads" button.
    public void purchaseRemoveAds() {

        final Activity activity = (Activity) mContext;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mHelper.launchPurchaseFlow(activity, SKU_REMOVE_ADS,
                            REQUEST_PURCHASE, mPurchaseFinishedListener, DEVELOPER_PAYLOAD);
                } catch (java.lang.IllegalStateException e) {
                    complain(activity.getResources().getString(R.string.store_error));
                }
            }
        });
    }

    public boolean onHandleResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null)
            return true;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            return false;
        } else {
            return true;
        }

    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct.
         * It will be the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase
         * and verifying it here might seem like a good approach, but this will
         * fail in the case where the user purchases an item on one device and
         * then uses your app on a different device, because on the other device
         * you will not have access to the random string you originally
         * generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different
         * between them, so that one user's purchase can't be replayed to
         * another user.
         *
         * 2. The payload must be such that you can verify it even when the app
         * wasn't the one who initiated the purchase flow (so that items
         * purchased by the user on one device work on other devices owned by
         * the user).
         *
         * Using your own server to store and verify developer payloads across
         * app installations is recommended.
         */
        return true;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
                return;

            if (result.isFailure()) {
                if (result.getResponse() == RESPONSE_ALREADY_OWNED) {
                    removeAds();
                    complain(mContext.getResources().getString(R.string.item_remove_ads_already_purchased));
                    return;

                }
                complain(mContext.getResources().getString(R.string.error_purchasing) + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain(mContext.getResources().getString(R.string.error_purchasing_auth));
                return;
            }
            if (purchase.getSku().equals(SKU_REMOVE_ADS)) {
                // bought the premium upgrade!
                removeAds();
            }
        }
    };

    private void removeAds() {
        mAdsManager.disableAdsPermanently();
    }

    public Boolean areAdsDisabled() {
        return mAdsManager.areBannerAdsEnabled();
    }

    // We're being destroyed. It's important to dispose of the helper here!

    public void stop() {
        // very important:
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    void complain(final String message) {
        final Activity activity = (Activity) mContext;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

