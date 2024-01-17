package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import java.io.IOException;


public class TimedActivityAlert extends AppCompatDialogFragment {
    private TimedAlertListener timedAlertListener;
    private String message;
    private String positiveBtnText;
    private String negativeBtnText;

    private Button positiveBtn;
    private Button negativeBtn;
    private TextView alertTextView;
    private String subjectId;
    private String jsonObject;
    private String activityId;
    private String fileLocation;
    private String voiceFileName;
    private String SAMPLE_TEXT = "1. Press 'Next' to move to next activity.\n2. Press 'Restart Activity' to try again.";

    TimedActivityAlert(
            String subjectId,
            String jsonObject,
            String activityId,
            String fileLocation,
            String message,
            String positiveButtonText,
            String negativeBtnText
    ) {
        this.message = (message.length() != 0) ? message : SAMPLE_TEXT;
        if (!positiveButtonText.equals("\0") || positiveButtonText.equals("")) {
            this.positiveBtnText = positiveButtonText;
        }
        if (!negativeBtnText.equals("\0") || positiveButtonText.equals("")) {
            this.negativeBtnText = negativeBtnText;
        }
        this.jsonObject = jsonObject;
        this.subjectId = subjectId;
        this.activityId = activityId;
        this.fileLocation = fileLocation;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptView = layoutInflater.inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setView(promptView);
        builder.setCancelable(false);
        positiveBtn = (Button) promptView.findViewById(R.id.alertPositiveButton);
        negativeBtn = (Button) promptView.findViewById(R.id.alertNegativeButton);
        alertTextView = (TextView) promptView.findViewById(R.id.alertTextMessage);
        alertTextView.setText(this.message);
        builder.setTitle("ACTIVITY COMPLETE!");
        ViewGroup alertParentGroup = (ViewGroup) positiveBtn.getParent();
        alertParentGroup.removeView(positiveBtn);
        alertParentGroup.removeView(negativeBtn);
        if (positiveBtnText.length() != 0) {
            alertParentGroup.addView(positiveBtn);
            positiveBtn.setText(positiveBtnText);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        timedAlertListener.timedAlertButtonPress(true, subjectId, jsonObject, activityId, fileLocation);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        if (negativeBtnText.length() != 0) {
            alertParentGroup.addView(negativeBtn);
            negativeBtn.setText(negativeBtnText);
            negativeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        timedAlertListener.timedAlertButtonPress(false, subjectId, jsonObject, activityId, fileLocation);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }




//        String title = "Have you completed the task?";
//        String message = "If any confusion, kindly restart this activity.\n\nIf no issues, please continue.";
//        builder.setTitle(title)
//                .setMessage(message)
//                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {}
//                });
        return builder.create();
    }

    public interface TimedAlertListener {
        void timedAlertButtonPress(
                Boolean positiveBtnPress,
                String subjectId, String jsonObject, String activityId, String fileLocation) throws IOException;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            timedAlertListener = (TimedAlertListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "not implemented");
        }
    }
}
