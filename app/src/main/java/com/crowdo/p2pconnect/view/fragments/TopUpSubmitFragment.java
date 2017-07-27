package com.crowdo.p2pconnect.view.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.crowdo.p2pconnect.R;
import com.crowdo.p2pconnect.commons.DefinitionsRetrieval;
import com.crowdo.p2pconnect.commons.MemberInfoRetrieval;
import com.crowdo.p2pconnect.helpers.CallBackUtil;
import com.crowdo.p2pconnect.helpers.SoftInputHelper;
import com.crowdo.p2pconnect.model.response.DefinitionBankInfoResponse;
import com.crowdo.p2pconnect.model.response.MemberInfoResponse;
import com.crowdo.p2pconnect.view.activities.TopUpActivity;
import com.crowdo.p2pconnect.viewholders.TopUpSubmitViewHolder;
import com.github.angads25.filepicker.controller.DialogSelectionListener;

import java.io.File;

/**
 * Created by cwdsg05 on 13/7/17.
 */

public class TopUpSubmitFragment extends Fragment{

    private TopUpSubmitViewHolder viewHolder;
    private File chosenFile;

    public TopUpSubmitFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_up_submit, container, false);

        viewHolder = new TopUpSubmitViewHolder(rootView, getActivity());
        viewHolder.initView();

        SoftInputHelper.setupUI(rootView, getActivity(), new EditText[]{viewHolder.mSubmitPaymentReferenceEditText});
        viewHolder.mSubmitPaymentReferenceEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int key, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (key) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_NUMPAD_ENTER:
                            SoftInputHelper.hideSoftKeyboardOfView(view,view);
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        viewHolder.mSubmitPaymentReferenceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    ((TopUpActivity) getActivity()).mTopUpBalanceExpandableLayout.collapse();
                }
            }
        });

        viewHolder.pickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if(files.length == 1){
                    //force single file only
                    chosenFile = new File(files[0]);

                }
            }
        });

        MemberInfoRetrieval memberRetrieval = new MemberInfoRetrieval();
        memberRetrieval.retrieveInfo(getActivity(), new CallBackUtil<MemberInfoResponse>() {
            @Override
            public void eventCallBack(MemberInfoResponse memberInfoResponse) {
                viewHolder.fillMemberInfo(memberInfoResponse);
            }
        });

        DefinitionsRetrieval definitionsRetrieval = new DefinitionsRetrieval();
        definitionsRetrieval.retreiveInfo(getActivity(), new CallBackUtil<DefinitionBankInfoResponse>() {
            @Override
            public void eventCallBack(DefinitionBankInfoResponse definitionBankInfoResponse) {
                viewHolder.fillDefinitionsBankInfo(definitionBankInfoResponse);
            }
        });

        return rootView;
    }

}
