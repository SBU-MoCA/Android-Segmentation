package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;

public class startupActivity extends AppCompatActivity {
    private Button letsStartButton;
    private EditText inputPatientId; // TODO: Need to add validation for this.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        letsStartButton = (Button) findViewById(R.id.letsStartButton);
        inputPatientId = (EditText) findViewById(R.id.inputPatientId);
        // onClick for lets get started button
        letsStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        // inputPatientId functions
        // disable the getStarted button at first
        letsStartButton.setEnabled(false);
        inputPatientId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    letsStartButton.setEnabled(false);
                } else {
                    letsStartButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}