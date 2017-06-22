package com.crowdo.p2pconnect.data.client;

import android.content.Context;

import com.crowdo.p2pconnect.data.APIServices;
import com.crowdo.p2pconnect.data.ReceivingCookiesInterceptor;
import com.crowdo.p2pconnect.data.SendingCookiesInterceptor;
import com.crowdo.p2pconnect.helpers.ConstantVariables;
import com.crowdo.p2pconnect.helpers.SharedPreferencesUtils;
import com.crowdo.p2pconnect.model.response.MemberInfoResponse;
import com.crowdo.p2pconnect.oauth.AuthHTTPInterceptor;
import com.crowdo.p2pconnect.oauth.CrowdoAccountGeneral;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by cwdsg05 on 12/6/17.
 */

public class MemberClient {

    public static final String LOG_TAG = MemberClient.class.getSimpleName();

    private Retrofit retrofit;
    private APIServices apiServices;
    private static MemberClient instance;

    public MemberClient(Context context){
        final Gson gson = new GsonBuilder().serializeNulls().create();

        //Http Interceptor
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        //get token
        String authToken = SharedPreferencesUtils.getSharedPrefString(context,
                CrowdoAccountGeneral.AUTHTOKEN_SHARED_PREF_KEY, null);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new SendingCookiesInterceptor(context))
                .addInterceptor(new ReceivingCookiesInterceptor(context))
                .addInterceptor(new AuthHTTPInterceptor(authToken))
                .build();

        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .baseUrl(APIServices.API_LIVE_BASE_URL + APIServices.LIVE_STAGE)
                .build();

        this.apiServices = retrofit.create(APIServices.class);
    }

    public static MemberClient getInstance(Context context){
        if(instance == null)
            instance = new MemberClient(context);
        return instance;
    }

    public Observable<Response<MemberInfoResponse>> getMemberInfo(String deviceId){
        return apiServices.getMemberInfo(deviceId, ConstantVariables.API_SITE_CONFIG_ID);
    }
}