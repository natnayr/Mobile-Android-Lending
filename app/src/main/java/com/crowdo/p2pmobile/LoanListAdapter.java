package com.crowdo.p2pmobile;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.crowdo.p2pmobile.data.LoanListItem;
import com.crowdo.p2pmobile.helper.CurrencyNumberFormatter;
import com.crowdo.p2pmobile.helper.FontManager;


import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by cwdsg05 on 15/11/16.
 */

public class LoanListAdapter extends BaseAdapter {

    private final String LOG_TAG = LoanListAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<LoanListItem> mLoanList = new ArrayList<LoanListItem>();

    private static final String IN_FREQUENCY_MONTH_VALUE = "Monthly";
    private static final String OUT_FREQUENCY_MONTH_VALUE = "Months";

    private static final String IN_SEC_COLLATERAL = "Collateral";
    private static final String OUT_SEC_COLLATERAL = "";
    private static final String IN_SEC_UNCOLLATERALIZED = "Uncollateralized";
    private static final String OUT_SEC_UNCOLLATERALIZED = "No Collateral";
    private static final String IN_SEC_INVOICE_OR_CHEQUE = "Working Order/Invoice";
    private static final String OUT_SEC_INVOICE_OR_CHEQUE = "Working Order/\nInvoice";

    private static final String DATE_TIME_REGION = "Asia/Singapore";


    public LoanListAdapter(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mLoanList.size();
    }

    @Override
    public LoanListItem getItem(int position) {
        if(position < 0 || position >= mLoanList.size()){
            return null;
        }else{
            return mLoanList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View contextView, ViewGroup parent) {
        final View view = (contextView != null ? contextView : createView(parent));
        final LoanViewHolder viewHolder = (LoanViewHolder) view.getTag();
        viewHolder.attachLoanItem(getItem(position), mContext);
        return view;
    }

    private View createView(ViewGroup parent){
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.list_item_loan, parent, false);
        final LoanViewHolder loanViewHolder = new LoanViewHolder(view);
        view.setTag(loanViewHolder);
        return view;
    }

    public void setLoans(@Nullable List<LoanListItem> retreivedLoans){
        if(retreivedLoans == null){
            return;
        }
        mLoanList.clear();
        mLoanList.addAll(retreivedLoans);
        notifyDataSetChanged();
        Log.d(LOG_TAG, "TEST: setmLoanList clear and addAll");
    }

    // LoanViewHolder Pattern for ButterKnife
    static class LoanViewHolder {
        @BindView(R.id.loan_item_iden_no) TextView mLoanId;
        @BindView(R.id.loan_item_credit_grade_text) TextView mLoanGrade;
        @BindView(R.id.loan_item_days_left_and_percentage_funded) TextView mDaysLeftAndPercentage;
        @BindView(R.id.loan_item_percentage_return) TextView mPercentageReturn;
        @BindView(R.id.loan_item_term_amount) TextView mTermAmount;
        @BindView(R.id.loan_item_term_description) TextView mTermDescription;
        @BindView(R.id.loan_item_collateral_description) TextView mSecurityDescription;
        @BindView(R.id.loan_item_amount) TextView mLoanAmount;

        @BindView(R.id.loan_item_collateral_icon_container) TextView mSecurityIcon;

        @BindView(R.id.loan_item_credit_grade_layout) View mLoanGradeDrawable;
        @BindView(R.id.loan_item_amount_icon_container) TextView mLoanAmountIcon;

        @BindColor(R.color.grade_color_A_plus) int colorAPlus;
        @BindColor(R.color.grade_color_A) int colorA;
        @BindColor(R.color.grade_color_B_plus) int colorBPlus;
        @BindColor(R.color.grade_color_E) int colorB;
        @BindColor(R.color.grade_colorC) int colorC;
        @BindColor(R.color.grade_color_D) int colorD;
        @BindColor(R.color.grade_color_E) int colorE;

        @BindColor(R.color.fa_icon_shield) int shieldColor;
        @BindColor(R.color.fa_icon_file_text) int fileColor;
        @BindColor(R.color.fa_icon_unlock_alt) int unlockAltColor;

        public LoanViewHolder(View view){
            ButterKnife.bind(this, view);
        }

        public void attachLoanItem(LoanListItem item, Context context){
            mLoanId.setText(item.loanIdOut);
            mLoanGrade.setText(item.grade);

            GradientDrawable mGradeShape = (GradientDrawable) mLoanGradeDrawable.getBackground();
            switch (item.grade) {
                case "A+": mGradeShape.setColor(colorAPlus);
                    break;
                case "A": mGradeShape.setColor(colorA);
                    break;
                case "B+": mGradeShape.setColor(colorBPlus);
                    break;
                case "B": mGradeShape.setColor(colorB);
                    break;
                case "C": mGradeShape.setColor(colorC);
                    break;
                case "D": mGradeShape.setColor(colorD);
                    break;
                case "E": mGradeShape.setColor(colorE);
                    break;
            }

            DateTime sgNow = new DateTime(DateTimeZone.forID(DATE_TIME_REGION)); // set SG time
            DateTime endDate = new DateTime(item.fundingEndDate);
            int daysLeft = Days.daysBetween(sgNow.toLocalDate(), endDate.toLocalDate()).getDays();

            if(daysLeft<0){
                mDaysLeftAndPercentage.setText("Closed - " + item.fundedPercentageCache + "% Funded");
            }else{
                mDaysLeftAndPercentage.setText(daysLeft + " Days Left - " + item.fundedPercentageCache + "% Funded");
            }

            mPercentageReturn.setText(Double.toString(item.interestRateOut));
            mTermAmount.setText(Integer.toString(item.tenureOut));

            //check if monthly is set differently
            String termDescription = OUT_FREQUENCY_MONTH_VALUE;
            if(!item.frequencyOut.equals(IN_FREQUENCY_MONTH_VALUE))
                termDescription = item.frequencyOut;
            mTermDescription.setText(termDescription);

            switch(item.security){
                case IN_SEC_COLLATERAL:
                    mSecurityIcon.setText(R.string.fa_shield);
                    mSecurityIcon.setTextColor(shieldColor);
                    mSecurityDescription.setText(WordUtils.wrap(
                        WordUtils.capitalize(item.collateralOut.replaceAll("_", " ")), 15));
                    break;
                case IN_SEC_UNCOLLATERALIZED:
                    mSecurityIcon.setText(R.string.fa_unlock_alt);
                    mSecurityIcon.setTextColor(unlockAltColor);
                    mSecurityDescription.setText(OUT_SEC_UNCOLLATERALIZED);
                    break;
                case IN_SEC_INVOICE_OR_CHEQUE:
                    mSecurityIcon.setText(R.string.fa_file_text);
                    mSecurityIcon.setTextColor(fileColor);
                    mSecurityDescription.setText(OUT_SEC_INVOICE_OR_CHEQUE);
                    break;
            }

            mLoanAmount.setText(CurrencyNumberFormatter.formatCurrency(item.currencyOut,
                    item.currencyOut+" ", item.targetAmountOut, false));

            Typeface iconFont = FontManager.getTypeface(context, FontManager.FONTAWESOME);
            FontManager.markAsIconContainer(mSecurityIcon, iconFont);
            FontManager.markAsIconContainer(mLoanAmountIcon, iconFont);
        }
    }
}
