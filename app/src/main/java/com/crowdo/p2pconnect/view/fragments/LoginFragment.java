package com.crowdo.p2pconnect.view.fragments;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.crowdo.p2pconnect.R;
import com.crowdo.p2pconnect.data.client.AuthClient;
import com.crowdo.p2pconnect.data.response.AuthResponse;
import com.crowdo.p2pconnect.helpers.ConstantVariables;
import com.crowdo.p2pconnect.helpers.HashingUtils;
import com.crowdo.p2pconnect.helpers.RegexValidationUtil;
import com.crowdo.p2pconnect.helpers.SnackBarUtil;
import com.crowdo.p2pconnect.helpers.HTTPStatusCodeUtil;
import com.crowdo.p2pconnect.model.Member;
import com.crowdo.p2pconnect.oauth.AccountGeneral;
import com.crowdo.p2pconnect.view.activities.AuthActivity;
import com.crowdo.p2pconnect.viewholders.LoginViewHolder;
import com.f2prateek.dart.Dart;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit2.Response;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cwdsg05 on 8/3/17.
 */

public class LoginFragment extends Fragment{

    @BindString(R.string.auth_http_error_message) String mHttpErrorServerMessage;
    @BindString(R.string.auth_http_handling_message) String mHttpErrorHandlingMessage;
    @BindColor(R.color.color_accent) int mColorAccent;
    @BindColor(R.color.color_icons_text) int mColorIconText;
    @BindColor(R.color.color_primary_700) int mColorPrimaryDark;
    private static final String LOG_TAG = LoginFragment.class.getSimpleName();
    public static final String LOGIN_FRAGMENT_TAG = "LOGIN_FRAGMENT_TAG";

    private String initAccountType;
    private String initAccountName;
    private LoginViewHolder viewHolder;
    private Subscription loginSubscription;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initAccountType = Dart.get(getArguments(), AuthActivity.ARG_ACCOUNT_TYPE);
        this.initAccountName = Dart.get(getArguments(), AuthActivity.ARG_ACCOUNT_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, rootView);

        viewHolder = new LoginViewHolder(rootView, getActivity());
        viewHolder.initView();

        viewHolder.mExitImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        viewHolder.mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if no view has focus:
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                submit();
            }
        });

        return rootView;
    }

    private void submit(){
        final String inputEmail = viewHolder.mEmailEditText.getText().toString().toLowerCase().trim();
        final String inputPassword = viewHolder.mPasswdEditText.getText().toString();

        //local incorrect email check
        if(!RegexValidationUtil.isValidEmailID(inputEmail)){
            final Snackbar snack = SnackBarUtil.snackBarForAuthCreate(getView(),
                    "Email in Incorrect Format", Snackbar.LENGTH_SHORT,
                    mColorIconText, mColorPrimaryDark);
            snack.show();
            return;
        }

        //do just http
        loginSubscription = AuthClient.getInstance()
                .loginUser(inputEmail, inputPassword,
                        ConstantVariables.getUniqueAndroidID(getActivity()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<AuthResponse>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(LOG_TAG, "APP: onCompleted reached for AuthClient.loginUser()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "ERROR: " + e.getMessage(), e);
                    }

                    @Override
                    public void onNext(Response<AuthResponse> response) {
                        Log.d(LOG_TAG, "APP: http-message:" + response.message()
                                + " http-code:" + response.code()
                                + ", http-body: {" + response.body().toString() + "}");

                        handleResult(response);
                    }
                });


    }

    private void handleResult(Response<AuthResponse> response){
        final String passwordToHashKeep = viewHolder.mPasswdEditText.getText().toString();
        final Bundle data = new Bundle();
        final Intent res = new Intent();

        //check response
        if(!response.isSuccessful()){
            final Snackbar snack = SnackBarUtil.snackBarForAuthCreate(getView(),
                    "Error: " + response.errorBody(),
                    Snackbar.LENGTH_SHORT,
                    mColorIconText, mColorPrimaryDark);
            snack.show();
            //TODO: do more error handling in future..
            return;
        }

        AuthResponse authResponse = response.body();

        //failed login response from server
        if(HTTPStatusCodeUtil.check4xxClientError(authResponse.getStatus())){
            SnackBarUtil.snackBarForAuthCreate(getView(),
                    authResponse.getMessage(),
                    Snackbar.LENGTH_SHORT,
                    mColorIconText, mColorAccent).show();
            return;
        }

        //success login
        if(HTTPStatusCodeUtil.check2xxSuccess(authResponse.getStatus())){
            //show success
            SnackBarUtil.snackBarForAuthCreate(getView(),
                    authResponse.getMessage(),
                    Snackbar.LENGTH_SHORT,
                    mColorIconText, mColorAccent).show();

            //Encapsulate results
            Member memberInfo = authResponse.getMember();
            try {
                data.putString(AccountManager.KEY_ACCOUNT_NAME, memberInfo.getName());
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, initAccountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, authResponse.getAuthToken());
                data.putString(AccountManager.KEY_PASSWORD, HashingUtils.hashSHA256(passwordToHashKeep));

            }catch(Exception e){
                SnackBarUtil.snackBarForAuthCreate(getView(),
                        mHttpErrorHandlingMessage + "\n" + e.getMessage(),
                        Snackbar.LENGTH_SHORT,
                        mColorIconText, mColorPrimaryDark).show();
                e.printStackTrace();
                Log.e(LOG_TAG, "ERROR: " + e.getMessage(), e);
                return;
            }

            res.putExtras(data);

            //finalise auth
            ((AuthActivity) getActivity()).finishAuth(res);
        }
    }
}
