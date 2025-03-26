package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class StartupActivity extends AppCompatActivity {
    public StartupActivity() throws IOException {}
    private final JavaAppendFileWriter mAppendFileWriter = new JavaAppendFileWriter();
    private final String fileLocation = mAppendFileWriter.getFileName();
    private Button letsStartButton;
    private EditText inputPatientId; // TODO: Need to add validation for this.

    private Boolean alertToStart = true;
    private Boolean turnInBed = true;

    private Boolean takeOffShoes = true;
    private Boolean realFood = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_startup);


        letsStartButton = (Button) findViewById(R.id.letsStartButton);
        inputPatientId = (EditText) findViewById(R.id.inputPatientId);
        SwitchMaterial alertToStartToggle = (SwitchMaterial) findViewById(R.id.alertToStartToggle);
        SwitchMaterial turnInBedToggle = (SwitchMaterial) findViewById(R.id.turnInBedToggle);
        SwitchMaterial takeOffShoesToggle = (SwitchMaterial) findViewById(R.id.takeOutShoesToggle);
        SwitchMaterial realFoodToggle = (SwitchMaterial) findViewById(R.id.foodActivityToggle);

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

        // Loading the JSON Instructions File.
        InputStream inputStream = null;
        JSONObject jsonObject = null;
        String jsonString = null;
        try {
            inputStream = getAssets().open("activities_list.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            System.out.println("ERROR: while fetching activities file: " + e);
            e.printStackTrace();
        }
        // Parsing the JSON String
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            System.out.println("ERROR: while parsing JSON data: " + e);
            e.printStackTrace();
        }

        // onClick for lets get started button
        final JSONObject passingJSONObject = jsonObject; // declaring as final for passing to inner class.

        // check toggle status:
        alertToStartToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) alertToStart = true;
                else alertToStart = false;
            }
        });
        turnInBedToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) turnInBed = true;
                else turnInBed = false;
            }
        });
        takeOffShoesToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) takeOffShoes = true;
                else takeOffShoes = false;
            }
        });
        realFoodToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) realFood = true;
                else realFood = false;
            }
        });

        letsStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInstructionActivities(passingJSONObject);
            }
        });
    }

    public void openInstructionActivities(JSONObject jsonObject) {
        Intent intent = new Intent(this, Overview.class);
        intent.putExtra("subjectId", inputPatientId.getText().toString());
        intent.putExtra("jsonData", jsonObject.toString());
        intent.putExtra("activityId", "1");
        intent.putExtra("fileLocation", fileLocation);
        intent.putExtra("alertToStart", alertToStart);
        intent.putExtra("turnInBed", turnInBed);
        intent.putExtra("takeOffShoes", takeOffShoes);
        intent.putExtra("realFood", realFood);
        startActivity(intent);
    }
}