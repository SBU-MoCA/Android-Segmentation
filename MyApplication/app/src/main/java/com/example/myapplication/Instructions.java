package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Instructions extends AppCompatActivity {
    final String ACTIVITY = "WALK_KITCHEN";
    public Instructions() throws IOException {}
    private JavaAppendFileWriter mAppendFileWriter = new JavaAppendFileWriter();
    private String fileName = mAppendFileWriter.getFileName();
    Context context = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        Intent intent = getIntent();
        Button playButton = (Button) findViewById(R.id.play_audio);
        TextView instructionsTextView = (TextView) findViewById(R.id.instructions_text);

        // getting subjectId passed from previous activity
        String subjectId = intent.getStringExtra("subjectId");
        // adding subjectId as title of the page.
        getSupportActionBar().setTitle("Patient Id: " + subjectId);

        // getting jsonString passed from previous activity
        JSONObject jsonObject = null;
        JSONObject activityDetails = null;
        JSONArray activityInstructions = null;
        String audioFilename = null;
        Boolean recordTime = null;
        try {
           jsonObject = new JSONObject(intent.getStringExtra("jsonData"));
        } catch (JSONException e) {
            System.out.println("CODE ERROR: while parsing JSON data in instructions: " + e);
            e.printStackTrace();
        }
        try {
            activityDetails = jsonObject.getJSONObject(ACTIVITY);
            activityInstructions = activityDetails.getJSONArray("instructions");
            audioFilename = activityDetails.getString("audioFileName");
            recordTime = activityDetails.getBoolean("recordTime");
        } catch (JSONException e) {
            System.out.println("CODE ERROR: while getting activity JSON data: " + e);
            e.printStackTrace();
        }

        // getting the audioFileName
        Resources res = context.getResources();

        int soundId = res.getIdentifier(audioFilename, "raw", context.getPackageName());
        System.out.println(soundId);
        // TODO: Need fixing.
//        MediaPlayer mp = new MediaPlayer();
//        mp.create(Instructions.this, soundId);
//        mp.start(); // start the audio once when page opens
//
//        // attaching play/pause feature to the button
//        playButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mp.isPlaying()){
//                    mp.pause();
//                } else {
//                    mp.start();
//                }
//            }
//        });
        String instructionSet = "";
        // attaching instructions text
        for (int i = 0; i < activityInstructions.length(); i += 1) {
            try {
                instructionSet += ( i + 1) + ". " + activityInstructions.getString(i);
                instructionSet += "\n\n";
            } catch (JSONException e) {
                System.out.println("CODE ERROR: Failed to parse instructions: " + e);
                throw new RuntimeException(e);
            }
        }
        System.out.println("test: " + instructionSet);
        instructionsTextView.setText(instructionSet);



    }
}