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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class Instructions extends AppCompatActivity implements CustomDialog.CustomDialogListener {
    public Instructions() throws IOException {}
    private JavaAppendFileWriter mAppendFileWriter = new JavaAppendFileWriter();
    private FileWriter fw;
    Context context = this;
    MediaPlayer mp = new MediaPlayer();


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
        String fileLocation = intent.getStringExtra("fileLocation");
        // adding subjectId as title of the page.
        getSupportActionBar().setTitle("Patient: " + subjectId);

        // set the filename for writing
        String fileName = fileLocation + '_' + subjectId + ".txt";
        try {
            fw = new FileWriter(fileName, true);
        } catch (IOException e) {
            System.out.println("Failed to load file: " + fileName);
            e.printStackTrace();
        }

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
        int soundId = res.getIdentifier(audioFilename, "raw", context.getPackageName());
        mp = MediaPlayer.create(this, soundId);

        // attaching play/pause feature to the button
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp.isPlaying()){
                    mp.pause();
                } else {
                    mp.start();
                }
            }
        });

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
                openNextInstructionActivity(subjectId, newJSONTransferData.toString(), activityId, fw, mAppendFileWriter, fileLocation);
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

        mp.start(); // start the audio once when page opens

        // log the starting activity time
        // TODO: Add a buffer time for subject to comprehend the instruction.
        logActivityTimings(fw, mAppendFileWriter, "start");
    }

    public void logActivityTimings(FileWriter fileWriter, JavaAppendFileWriter fileAppendWriter, String flag) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        format.setTimeZone(timeZone);
        String time =  format.format(calendar.getTime());
        try {
            fileAppendWriter.writeToFile(fileWriter, time, flag);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void openDialog(
            String activity, String subjectId, String jsonObject, String activityId
    ) {
        CustomDialog dialog = new CustomDialog(
                activity,
                subjectId,
                jsonObject,
                activityId
                );
        dialog.show(getSupportFragmentManager(), "customDialog");
    }

    // onClick for activityCompleteButton
    public void openNextInstructionActivity(String subjectId, String jsonObject, String activityId, FileWriter fw, JavaAppendFileWriter mAppendFileWriter, String fileLocation) {
        logActivityTimings(fw, mAppendFileWriter, "stop");
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", Integer.toString((Integer.parseInt(activityId) + 1)));
        intent.putExtra("fileLocation", fileLocation);
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

    // if yes clicked on dialog, perform the respective action
    @Override
    public void onYesClicked(String activity, String subjectId, String jsonObject, String activityId) {
        if (activity == "restartActivity") {
            restartCurrentActivity(subjectId, jsonObject, activityId);
        } else {
            startOverFromStart(subjectId, jsonObject);
        }
    }

}