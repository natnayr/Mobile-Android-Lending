package com.crowdo.p2pconnect.view.activities;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.ArraySet;
import android.util.Log;

import com.andretietz.retroauth.AuthenticationActivity;
import com.crowdo.p2pconnect.R;
import com.crowdo.p2pconnect.helpers.ConstantVariables;
import com.crowdo.p2pconnect.support.NetworkConnectionChecks;
import com.crowdo.p2pconnect.helpers.LocaleHelper;
import com.crowdo.p2pconnect.view.fragments.LoginFragment;
import com.crowdo.p2pconnect.view.fragments.RegisterFragment;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;


/**
 * Created by cwdsg05 on 10/3/17.
 */

public class AuthActivity extends AuthenticationActivity {

    private final static String LOG_TAG = AuthActivity.class.getSimpleName();

    public final static String AUTH_MEMBER_EMAIL = "AUTH_MEMBER_EMAIL";
    public final static String AUTH_MEMBER_NAME = "AUTH_MEMBER_NAME";
    public final static String AUTH_MEMBER_TOKEN = "AUTH_MEMBER_TOKEN";
    public final static String AUTH_MEMBER_LOCALE = "AUTH_MEMBER_LOCALE";

    public final static int REQUEST_FRAGMENT_RESULT = 123;
    public final static String FRAGMENT_CLASS_TAG_CALL = "AUTH_ACTIVITY_FRAGMENT_CLASS_TAG_CALL";

    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_auth);

        Log.d(LOG_TAG, "APP AuthActivity onCreate");

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList(ConstantVariables.AUTH_FACEBOOK_READ_PERMISSIONS));

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                submitFB(loginResult);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e(LOG_TAG, "ERROR " + error.getMessage(), error);
            }
        });

        Bundle extras = getIntent().getExtras();

        Fragment fragment = null;
        String fragmentTag = extras.getString(FRAGMENT_CLASS_TAG_CALL);
        if(fragmentTag != null) {
            if(fragmentTag.equals(LoginFragment.LOGIN_FRAGMENT_TAG)){
                fragment = new LoginFragment();
            }else if(fragmentTag.equals(RegisterFragment.REGISTER_FRAGMENT_TAG)){
                fragment = new RegisterFragment();
            }
        }else{
            fragment = new LoginFragment(); //default
        }
        //fragment should be either Login or Register
        getSupportFragmentManager().beginTransaction()
                .add(R.id.auth_content, fragment)
                .commit();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    public void finishAuth(Bundle userData){

        Log.d(LOG_TAG, "APP finishAuth");

        final String accountUserName = userData.getString(AuthActivity.AUTH_MEMBER_NAME);
        final String accountUserEmail = userData.getString(AuthActivity.AUTH_MEMBER_EMAIL);
        final String accountAuthToken = userData.getString(AuthActivity.AUTH_MEMBER_TOKEN);
        final String accountUserLocale = userData.getString(AuthActivity.AUTH_MEMBER_LOCALE);

        if(accountAuthToken != null) {
            final Account userAccount = createOrGetAccount(accountUserName);
            storeToken(userAccount, getRequestedTokenType(), accountAuthToken);
            storeUserData(userAccount, getString(R.string.authentication_EMAIL), accountUserEmail);
            storeUserData(userAccount, getString(R.string.authentication_LOCALE), accountUserLocale);
            finalizeAuthentication(userAccount);
        }
    }

    @Override
    public void onBackPressed() {
        Intent launchIntent = new Intent(AuthActivity.this, LaunchActivity.class);
        startActivityForResult(launchIntent, REQUEST_FRAGMENT_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "APP onActivityResult " + requestCode);

        if(requestCode == REQUEST_FRAGMENT_RESULT) {
            Fragment fragment = null;
            if(resultCode == LaunchActivity.RESULT_CODE_REGISTER){
                fragment = new RegisterFragment();
            }else{
                fragment = new LoginFragment();
            }
            //fragment should be either Login or Register
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.auth_content, fragment)
                    .commitAllowingStateLoss();
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //check network and dun show loggout
        NetworkConnectionChecks.isOnline(this);
    }

    private void submitFB(LoginResult loginResult){
        AccessToken accessToken = loginResult.getAccessToken();

        Log.d(LOG_TAG, "APP Login FB AccessToken: " + loginResult.getAccessToken().getToken());

        Set<String> grantedPermissions = loginResult.getRecentlyGrantedPermissions();

        Log.d(LOG_TAG, "APP Login FB grantedPermissions: " + StringUtils.join(grantedPermissions, ","));

        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(LOG_TAG, "APP Login FB" + response.getRawResponse());
                    }
                }
        );


        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,education,location,work");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
