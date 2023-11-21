package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.ImageDecoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class Instructions extends AppCompatActivity implements CustomDialog.CustomDialogListener {
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
        Resources res = context.getResources();
        Button playButton = (Button) findViewById(R.id.play_audio);
        Button activityCompleteButton = (Button) findViewById(R.id.activity_complete);
        Button restartActivityButton = (Button) findViewById(R.id.restart_activity);
        Button startOverButton = (Button) findViewById(R.id.start_over);
        TextView instructionsTextView = (TextView) findViewById(R.id.instructions_text);
        GifImageView gifImageView = (GifImageView) findViewById(R.id.instruction_gif);

        // getting information passed from previous activity
        String subjectId = intent.getStringExtra("subjectId");
        String activityId = intent.getStringExtra("activityId");

        // adding subjectId as title of the page.
        getSupportActionBar().setTitle("Patient: " + subjectId);

        // getting jsonString passed from previous activity
        JSONObject jsonObject = null;
        JSONObject activityDetails = null;
        JSONArray activityInstructions = null;
        String audioFilename = null;
        String gifImageName = null;
        try {
           jsonObject = new JSONObject(intent.getStringExtra("jsonData"));
        } catch (JSONException e) {
            System.out.println("CODE ERROR: while parsing JSON data in instructions: " + e);
            e.printStackTrace();
        }
        try {
            activityDetails = jsonObject.getJSONObject(activityId);
            activityInstructions = activityDetails.getJSONArray("instructions");
            audioFilename = activityDetails.getString("audioFileName");
            gifImageName = activityDetails.getString("gifFileName");
        } catch (JSONException e) {
            System.out.println("CODE ERROR: while getting activity JSON data: " + e);
            e.printStackTrace();
        }

        // getting the audioFileName
//        int soundId = res.getIdentifier(audioFilename, "raw", context.getPackageName());
//        System.out.println(soundId);
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

        // attaching the gifImage with each instruction
        int drawableGifId = res.getIdentifier(gifImageName, "drawable", getPackageName());
        GifDrawable gifFromResource = null;
        try {
            gifFromResource = new GifDrawable( getResources(), drawableGifId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        gifImageView.setImageDrawable(gifFromResource);


        // attaching instructions text
        String instructionSet = "";
        for (int i = 0; i < activityInstructions.length(); i += 1) {
            try {
                instructionSet += ( i + 1) + ". " + activityInstructions.getString(i);
                instructionSet += "\n\n";
            } catch (JSONException e) {
                System.out.println("CODE ERROR: Failed to parse instructions: " + e);
                throw new RuntimeException(e);
            }
        }
        instructionsTextView.setText(instructionSet);

        // attaching action for activity complete button
        final JSONObject newJSONTransferData = jsonObject;
        activityCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextInstructionActivity(subjectId, newJSONTransferData.toString(), activityId);
            }
        });

        // attaching action for restartActivityButton
        restartActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("restartActivity", subjectId, newJSONTransferData.toString(), activityId);
            }
        });

        // attaching action for startOverButton
        startOverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Start over from beginning. Current activity: " + activityId);
                openDialog("startOver", subjectId, newJSONTransferData.toString(), activityId);

            }
        });
    }

    public void openDialog(String activity, String subjectId, String jsonObject, String activityId) {
        CustomDialog dialog = new CustomDialog(activity, subjectId, jsonObject, activityId);
        dialog.show(getSupportFragmentManager(), "customDialog");
    }

    // onClick for activityCompleteButton
    public void openNextInstructionActivity(String subjectId, String jsonObject, String activityId) {
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", Integer.toString((Integer.parseInt(activityId) + 1)));
        startActivity(intent);
    }

    // onClick for restartActivityButton
    public void restartCurrentActivity(String subjectId, String jsonObject, String activityId) {
       System.out.println("Restarting activity: " + activityId);
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", activityId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // onClick for startOverActivity
    public void startOverFromStart(String subjectId, String jsonObject) {
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", "1");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onYesClicked(String activity, String subjectId, String jsonObject, String activityId) {
        if (activity == "restartActivity") {
            restartCurrentActivity(subjectId, jsonObject, activityId);
        } else {
            startOverFromStart(subjectId, jsonObject);
        }
    }

}