package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

public class CustomDialog extends AppCompatDialogFragment {
    private CustomDialogListener listener;
    private String activity;
    private String subjectId;
    private String jsonObject;
    private String activityId;

    CustomDialog(String activity, String subjectId, String jsonObject, String activityId) {
        this.activity = activity;
        this.jsonObject = jsonObject;
        this.subjectId = subjectId;
        this.activityId = activityId;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        Boolean restartActivity = (this.activity == "restartActivity") ? true : false;
        String title = restartActivity ? "Do you want to restart this activity?" : "Do you want to restart from beginning?";
        String message = "Press 'Yes' to restart.\nPress 'No' to continue.";
        builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onYesClicked(activity, subjectId, jsonObject, activityId);
                    }
                });
        return builder.create();
    }

    public interface CustomDialogListener {
        void onYesClicked(String activity, String subjectId, String jsonObject, String activityId);
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