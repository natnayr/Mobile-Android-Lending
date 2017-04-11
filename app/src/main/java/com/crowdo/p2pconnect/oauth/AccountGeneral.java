package com.crowdo.p2pconnect.oauth;

import android.content.Context;

/**
 * Created by cwdsg05 on 24/3/17.
 */

public class AccountGeneral {

    public static final String getACCOUNT_TYPE(Context context){
        return context.getPackageName();
    };

    public static final String AUTHTOKEN_TYPE_ONLINE_ACCESS = "OAUTH_ACCESS_ONLINE";
    public static final String AUTHTOKEN_TYPE_ONLINE_ACCESS_LABEL = "Access to CROWDO online account";

    public static final String AUTHTOKEN_SHARED_PREF_KEY = "AUTH_TOKEN_KEY_FOR_SHARED_PREF";
}
