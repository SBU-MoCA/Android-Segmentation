package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import java.io.IOException;

public class CustomDialog extends AppCompatDialogFragment {
    private CustomDialogListener listener;
    private String activity;
    private String subjectId;
    private String jsonObject;
    private String activityId;
    private String fileLocation;
    private Button positiveBtn;
    private Button negativeBtn;
    private TextView alertTextView;

    CustomDialog(String activity, String subjectId, String jsonObject, String activityId, String fileLocation) {
        this.activity = activity;
        this.jsonObject = jsonObject;
        this.subjectId = subjectId;
        this.activityId = activityId;
        this.fileLocation = fileLocation;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptView = layoutInflater.inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        AlertDialog builder = alertBuilder.create();
        builder.setView(promptView);

        positiveBtn = (Button) promptView.findViewById(R.id.alertPositiveButton);
        negativeBtn = (Button) promptView.findViewById(R.id.alertNegativeButton);
        alertTextView = (TextView) promptView.findViewById(R.id.alertTextMessage);
        Boolean restartActivity = (this.activity == "restartActivity") ? true : false;
        String title = restartActivity ? "Do you want to restart this activity?" : "Do you want to restart from beginning?";
        String buttonTxt = restartActivity ? "Restart Activity" : "Start Over";
//        String message = "Press 'No' to continue.\n\nPress 'Yes' to restart.";
        alertTextView.setText(title);
        /* Positive and negative buttons are negated for this action */
        positiveBtn.setText("Continue Activity");

        negativeBtn.setText(buttonTxt);


        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               builder.dismiss();
            }
        });

        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listener.onYesClicked(activity, subjectId, jsonObject, activityId, fileLocation);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return builder;
    }

    public interface CustomDialogListener {
        void onYesClicked(String activity, String subjectId, String jsonObject, String activityId, String fileLocation) throws IOException;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (CustomDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "not implemented");
        }
    }
}
