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

import org.json.JSONObject;

import java.io.IOException;

public class StartAlert extends AppCompatDialogFragment {
    private StartAlertListener listener;

    private Button positiveBtn;
    private Button negativeBtn;
    private TextView alertTextView;

    private Button restartActivityButton, startActivitybutton, playButton, activityCompleteButton;
    private TextView timedActivityTextView;
    private String subjectId, activityId, fileLocation;
    private JSONObject newJSONTransferData;

    StartAlert(Button restartActivityButton, Button startActivitybutton, Button playButton,  Button activityCompleteButton,
               TextView timedActivityTextView,
               String subjectId, JSONObject newJSONTransferData, String activityId, String fileLocation) {
        this.restartActivityButton = restartActivityButton;
        this.startActivitybutton = startActivitybutton;
        this.playButton = playButton;
        this.activityCompleteButton = activityCompleteButton;
        this.timedActivityTextView = timedActivityTextView;
        this.subjectId = subjectId;
        this.newJSONTransferData = newJSONTransferData;
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
        String title = "Are you ready to start activity?";
        alertTextView.setText(title);
        /* Positive and negative buttons are negated for this action */
        positiveBtn.setText("START ACTIVITY");
        negativeBtn.setText("SHOW INSTRUCTIONS");

        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listener.onBackToInstructions();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    builder.dismiss();
                }
            }
        });

        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listener.onStartActivity(restartActivityButton, startActivitybutton, playButton, activityCompleteButton, timedActivityTextView, subjectId,
                            newJSONTransferData, activityId, fileLocation);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    builder.dismiss();
                }
            }
        });
        return builder;
    }

    public interface StartAlertListener {
        void onStartActivity(Button restartActivityButton, Button startActivitybutton, Button playButton, Button activityCompleteButton,
                             TextView timedActivityTextView,
                             String subjectId, JSONObject newJSONTransferData, String activityId, String fileLocation) throws IOException;
        void onBackToInstructions() throws IOException;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (StartAlertListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "not implemented");
        }
    }
}
