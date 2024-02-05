package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import org.w3c.dom.Text;

import java.io.IOException;

public class CountTimerDialog extends AppCompatDialogFragment {
    private TextView countDownText;
    CountTimerDialog() { }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptView = layoutInflater.inflate(R.layout.countdown_timer_dialog, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        AlertDialog builder = alertBuilder.create();
        builder.setView(promptView);
        WindowManager.LayoutParams windowLayout = builder.getWindow().getAttributes();
        windowLayout.y = 450;
        builder.setCancelable(false);
        TextView titleText = (TextView) promptView.findViewById(R.id.titleText);
        String titleTextString = "Please continue the activity. The app will alert you after:";
        titleText.setText(titleTextString);
        countDownText = (TextView) promptView.findViewById(R.id.countDownText);
        return builder;
    }

    public void setCountDownText(String timeInSeconds) {
        countDownText.setText(timeInSeconds);
    }

}
