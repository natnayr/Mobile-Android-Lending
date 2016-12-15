package com.crowdo.p2pmobile;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.crowdo.p2pmobile.data.LoanDetail;
import com.crowdo.p2pmobile.data.LoanDetailClient;
import com.crowdo.p2pmobile.data.LoanFactSheetClient;
import com.crowdo.p2pmobile.viewholders.DetailsFragmentViewHolder;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment {

    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    private Subscription detailsSubscription;
    private Subscription factsheetSubscription;
    private int initId;

    public DetailsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null && getArguments()
                .getInt(DetailsActivity.BUNDLE_ID_KEY) >= 0 ) {
            this.initId = getArguments()
                    .getInt(DetailsActivity.BUNDLE_ID_KEY); //store
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_details, parent, false);

        final DetailsFragmentViewHolder viewHolder = new DetailsFragmentViewHolder(rootView);

        //Init view first,
        viewHolder.initView(getActivity(), this.initId);

        detailsSubscription = LoanDetailClient.getInstance()
                .getLoanDetails(this.initId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LoanDetail>() {

                    @Override
                    public void onCompleted() {
                        Log.d(LOG_TAG, "TEST: populated LOANDETAILS Rx onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "ERROR: " + e.getMessage());
                    }

                    @Override
                    public void onNext(LoanDetail loanDetail) {
                        Log.d(LOG_TAG, "TEST: populated LoanDetails Rx onNext with :"
                                + loanDetail.loanId + " loanid retreived.");
                        viewHolder.attachView(loanDetail, getActivity());
                    }
                });

        //set file download button here..
        viewHolder.mFactsheetDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(initId >= 0) {
                    factsheetSubscription = LoanFactSheetClient.getInstance()
                        .getLoanFactSheet(initId)
                        .subscribe(new Observer<File>() {
                            @Override
                            public void onCompleted() {
                                Log.d(LOG_TAG, "TEST: mFactSheetDownloadBtn complete");
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(File file) {
                                Log.d(LOG_TAG, "TEST: mFactSheetDownloadBtn onNext => "
                                    + file.getAbsolutePath());

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                Intent chooserIntent = Intent.createChooser(intent, "Open With");
                                try{
                                    startActivity(chooserIntent);
                                }catch (ActivityNotFoundException e){
                                    Log.e(LOG_TAG, "ERROR: " + e.getMessage(), e);
                                }

                            }
                        });
                }
            }
        });

        rootView.setTag(viewHolder);
        return rootView;
    }

}
