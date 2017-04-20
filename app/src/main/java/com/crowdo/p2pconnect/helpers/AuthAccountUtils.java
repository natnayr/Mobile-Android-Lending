package com.crowdo.p2pconnect.helpers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import com.crowdo.p2pconnect.oauth.CrowdoAccountGeneral;
import com.crowdo.p2pconnect.view.activities.LaunchActivity;


/**
 * Created by cwdsg05 on 29/3/17.
 */

public class AuthAccountUtils {

    public static final String LOG_TAG = AuthAccountUtils.class.getSimpleName();

    public static void removeAccounts(final Activity activity){

        AccountManager am = AccountManager.get(activity);
        Account[] accounts = am.getAccountsByType(CrowdoAccountGeneral.ACCOUNT_TYPE);
        if(accounts.length > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                for (Account acc : accounts) {
                    am.clearPassword(acc);
                    am.removeAccountExplicitly(acc);
                }
            } else {
                for (Account acc : accounts) {
                    am.clearPassword(acc);
                    am.removeAccount(acc, null, null);
                }
            }
        }
    }

    public static Account getOneAndOnlyOneAccount(AccountManager accountManager){
        Account[] accounts = accountManager.getAccounts();
        if(accounts.length > 0){
            return accounts[0];
        }
        return null;
    }


    public static void invalidateAuthToken(final Activity activity, final AccountManager accountManager, String authToken){

        //invalidate SharedPref
        SharedPreferencesUtils.setSharePrefString(activity,
                CrowdoAccountGeneral.AUTHTOKEN_SHARED_PREF_KEY, null);

        final Account account = getOneAndOnlyOneAccount(accountManager);
        if(account != null) {
            final AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account,
                    authToken, null, activity, null, null);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bundle bnd = future.getResult();
                        final String authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                        accountManager.invalidateAuthToken(account.type, authToken);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "ERROR: " + e.getMessage(), e);
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void getExisitingAuthToken(final Activity activity, AccountManager accountManager,
                                             final CallBackUtil<String> callback){

        Log.d(LOG_TAG, "APP getExisitingAuthToken()");

        Account account = AuthAccountUtils.getOneAndOnlyOneAccount(accountManager);
        if(account == null) {
            callback.eventCallBack(null); //return back to callback a null string
            return;
        }

        Log.d(LOG_TAG, "APP getExisitingAuthToken() > account.name " + account.name);

        final AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account,
                CrowdoAccountGeneral.AUTHTOKEN_TYPE_ONLINE_ACCESS, null, activity, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Bundle bundle = future.getResult();
                    String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.d(LOG_TAG, "APP getExisitingAuthToken > authToken = " + authToken);
                    callback.eventCallBack(authToken);
                }catch (Exception e){
                    Log.e(LOG_TAG, "ERROR: " + e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void actionLogout(AccountManager accountManager, final Activity activity){
        Log.d(LOG_TAG, "APP actionLogout()");

        //clear cookie cache fro webview
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeSessionCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    Log.d(LOG_TAG, "APP CookieManager.removeSessionCookies onReceiveValue " + value);
                }
            });
            cookieManager.flush();
        }else{
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(activity);
            cookieSyncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }

        //invalidate authKey if avalible
        String authToken = SharedPreferencesUtils.getSharedPrefString(activity,
                CrowdoAccountGeneral.AUTHTOKEN_SHARED_PREF_KEY, null);
        if(authToken != null){
            AuthAccountUtils.invalidateAuthToken(activity, accountManager, authToken);
        }

        //invalidate only account and remove accounts
        AuthAccountUtils.removeAccounts(activity);

        Intent intent = new Intent(activity, LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }
}
