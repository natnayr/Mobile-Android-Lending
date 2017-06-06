package com.crowdo.p2pconnect.view.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.crowdo.p2pconnect.R;
import com.crowdo.p2pconnect.view.fragments.CheckoutSummaryFragment;

/**
 * Created by cwdsg05 on 22/5/17.
 */

public class CheckoutActivity extends AppCompatActivity {

    private static final String LOG_TAG = CheckoutActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_summary);

        CheckoutSummaryFragment checkoutSummaryFragment = new CheckoutSummaryFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.checkout_summary_content, checkoutSummaryFragment)
                .commit();
    }


}
