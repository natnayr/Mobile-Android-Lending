package com.crowdo.p2pmobile.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.crowdo.p2pmobile.R;
import com.crowdo.p2pmobile.model.LearningCenter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by cwdsg05 on 9/2/17.
 */

public class LearningCenterUtils {

    private static final String LOG_TAG = LearningCenterUtils.class.getSimpleName();

    public void populateData(final Context context, final Realm realm){
        final ProgressDialog progress = new ProgressDialog(context);
        progress.setTitle("Loading");
        progress.setMessage("Please wait while loading..");
        progress.setCancelable(true);
        progress.show();

        //on another thread
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final List<String> csvCategories = new ArrayList<String>();
                csvCategories.add(ConstantVariables.LEARNING_CENTER_DB_CATEGORY_KEY_GENERAL);
                csvCategories.add(ConstantVariables.LEARNING_CENTER_DB_CATEGORY_KEY_INVESTOR);
                csvCategories.add(ConstantVariables.LEARNING_CENTER_DB_CATEGORY_KEY_BORROWER);

                int[] counterEn = {1, 1, 1};
                int[] counterId = {1, 1, 1};

                final String EN = ConstantVariables.LEARNING_CENTER_DB_EN;
                final String ID = ConstantVariables.LEARNING_CENTER_DB_ID;

                try {
                    InputStreamReader isr = new InputStreamReader(context.getAssets()
                            .open(ConstantVariables.LEARNING_CENTER_CSV_FILE_LOCATION));
                    BufferedReader reader = new BufferedReader(isr);
                    CSVParser parser = new CSVParser(reader, CSVFormat.RFC4180);
                    for (final CSVRecord rec : parser) {
                        if (!rec.get(0).equals("") && !rec.get(1).equals("") && !rec.get(2).equals("")
                                && !rec.get(4).equals("") && !rec.get(5).equals("")) {
                            if (csvCategories.contains(rec.get(0))) {
                                int csvIdx = csvCategories.indexOf(rec.get(0));
                                LearningCenter enLearningCenter = realm.createObject(LearningCenter.class);
                                enLearningCenter.setLanguage(EN);
                                enLearningCenter.setCategory(rec.get(0));
                                enLearningCenter.setQuestion(rec.get(1));
                                enLearningCenter.setAnswer(rec.get(2));
                                enLearningCenter.setIndex(Integer.toString(counterEn[csvIdx]) + ". ");
                                counterEn[csvIdx]++;

                                LearningCenter idLearningCenter = realm.createObject(LearningCenter.class);
                                idLearningCenter.setLanguage(ID);
                                idLearningCenter.setCategory(rec.get(0));
                                idLearningCenter.setQuestion(rec.get(4));
                                idLearningCenter.setAnswer(rec.get(5));
                                idLearningCenter.setIndex(Integer.toString(counterId[csvIdx]) + ". ");
                                counterId[csvIdx]++;
                            }
                        }
                    }

                    isr.close();
                } catch (IOException ioe) {
                    Log.e(LOG_TAG, "ERROR: " + ioe.getMessage(), ioe);
                }

            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "APP: Realm database is done processing from CSV");
                SharedPreferencesUtils.setSharePrefBool(context,
                        ConstantVariables.PREF_KEY_LOADED_LEARNINGCENTER_DB, true);

                progress.dismiss();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e(LOG_TAG, "ERROR: " + error.getMessage(), error);
                SharedPreferencesUtils.setSharePrefBool(context,
                        ConstantVariables.PREF_KEY_LOADED_LEARNINGCENTER_DB, false);
                Toast.makeText(context, "Sry, I looks like there was an error loading " +
                        "the learning center information..", Toast.LENGTH_LONG).show();
                progress.dismiss();
            }
        });



    }

}