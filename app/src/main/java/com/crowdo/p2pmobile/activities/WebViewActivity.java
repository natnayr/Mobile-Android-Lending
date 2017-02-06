package com.crowdo.p2pmobile.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crowdo.p2pmobile.R;
import com.crowdo.p2pmobile.helpers.PermissionsUtil;
import com.crowdo.p2pmobile.helpers.SnackBarUtil;
import com.esafirm.rxdownloader.RxDownloader;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.delight.android.webview.AdvancedWebView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by cwdsg05 on 4/1/17.
 */

public class WebViewActivity extends AppCompatActivity implements AdvancedWebView.Listener{

    public static final String LOG_TAG = WebViewActivity.class.getSimpleName();

    @BindView(R.id.webview) AdvancedWebView mWebView;
    @BindView(R.id.toolbar_webview) Toolbar mToolbar;
    @BindView(R.id.webview_swipe_container) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.webview_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.webview_root) CoordinatorLayout rootView;
    @BindColor(R.color.color_icons_text) int colorIconText;
    @InjectExtra public int id;
    @InjectExtra public String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        ButterKnife.bind(WebViewActivity.this);

        //mToolbar view
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //inject intent settings
        Dart.inject(WebViewActivity.this);

        mWebView.setListener(WebViewActivity.this, WebViewActivity.this);
        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mWebView.loadUrl(url);

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setProgress(newProgress);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                Log.d(LOG_TAG, "APP: Permission Requested");
                request.grant(request.getResources());
            }
        });


        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(R.color.color_primary_light,
                R.color.color_primary, R.color.color_primary_dark);

    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }


    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager()
                .getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            toBackStackOrParent();
        }
    }


    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {

        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDownloadRequested(final String url, final String fileName,
                                    final String downloadMimeType, long contentLength,
                                    String contentDisposition, String userAgent) {

        //check permissions
        if(!PermissionsUtil.checkPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            final Snackbar snackBar = SnackBarUtil
                    .snackBarCreate(rootView, "Unable to write to external Drive",
                            colorIconText, Snackbar.LENGTH_LONG);
            snackBar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackBar.dismiss();
                }
            }).show();
            return;
        }

        String mimeType = null;
        final String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if(extension != null) {
            mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension);
        }

        Toast.makeText(this, "Downloading...",
                Toast.LENGTH_SHORT).show();

        final String usageMimeType = mimeType;

        RxDownloader.getInstance(this)
                .download(url, fileName, mimeType)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(LOG_TAG, "APP: Completed WebView Download");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, "ERROR: onError " + e.getMessage(), e);
                    }

                    @Override
                    public void onNext(final String s) {
                        Log.d(LOG_TAG, "APP: file is now in " + s);

                        final Snackbar snackbar = SnackBarUtil
                                .snackBarCreate(rootView, "Downloaded to " + s,
                                colorIconText, Snackbar.LENGTH_LONG);


                        snackbar.setAction("OPEN", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    File downloadFile = new File(new URI(s));

                                    intent.setDataAndType(Uri.fromFile(downloadFile), usageMimeType);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                            | Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Intent chooserIntent = Intent.createChooser(intent, "Open With");


                                    startActivity(chooserIntent);
                                }catch(URISyntaxException ue){
                                    Log.e(LOG_TAG, "ERROR: " + ue.getMessage(), ue);
                                    final Snackbar snackbar = SnackBarUtil.snackBarCreate(rootView,
                                            "Error, reading file",
                                            colorIconText);

                                    snackbar.setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snackbar.dismiss();
                                        }
                                    });
                                }catch (ActivityNotFoundException anfe){
                                    Log.e(LOG_TAG, "ERROR: " + anfe.getMessage(), anfe);
                                    final Snackbar snackbar = SnackBarUtil.snackBarCreate(rootView,
                                            "Error, you do not seem to have a PDF Reader installed, " +
                                                    "moving file to downloads",
                                            colorIconText);

                                    snackbar.setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snackbar.dismiss();
                                        }
                                    });
                                }
                            }
                        });
                        snackbar.show();
                    }
                });
    }

    @Override
    public void onExternalPageRequest(String url) { }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean toBackStackOrParent(){
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            //If no backstack then navigate to logical main list view
            NavUtils.navigateUpTo(this, upIntent);
        }
        return true;
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context){
            mContext = context;
        }

        @JavascriptInterface
        public void goBack(){
            Log.d(LOG_TAG, "APP: JavaScriptInterface goBack() called");
            finish();
        }
    }
}
