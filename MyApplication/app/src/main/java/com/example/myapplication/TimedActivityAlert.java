package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;


public class TimedActivityAlert extends AppCompatDialogFragment {
    private CustomDialog.CustomDialogListener listener;
    private String message;
    private String positiveBtnText;
    private String negativeBtnText;

    private Button positiveBtn;
    private Button negativeBtn;
    private TextView alertTextView;

    TimedActivityAlert(String message, String positiveButtonText, String negativeBtnText) {
        this.message = message;
        if (!positiveButtonText.equals("\0") || positiveButtonText.equals("")) {
            this.positiveBtnText = positiveButtonText;
        }
        if (!negativeBtnText.equals("\0") || positiveButtonText.equals("")) {
            this.negativeBtnText = negativeBtnText;
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptView = layoutInflater.inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setView(promptView);
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
        }
        if (negativeBtnText.length() != 0) {
            alertParentGroup.addView(negativeBtn);
            negativeBtn.setText(negativeBtnText);
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
}
