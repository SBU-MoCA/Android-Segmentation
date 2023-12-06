package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class Instructions extends AppCompatActivity implements CustomDialog.CustomDialogListener, TimedActivityAlert.TimedAlertListener {
    public Instructions() throws IOException {}
    private JavaAppendFileWriter mAppendFileWriter = new JavaAppendFileWriter();
    private FileWriter fw;
    Context context = this;
    MediaPlayer mp = new MediaPlayer();

    // for time specific alerts.
    private Handler handler;
    private Runnable showDialogRunnable;
    private Vibrator vibrator;

    // getting jsonString passed from previous activity
    JSONObject jsonObject = null;
    JSONObject activityDetails = null;
    JSONArray activityInstructions = null;
    String audioFilename = null;
    String gifImageName = null;
    Boolean alertUser = false;
    Integer alertAfterSeconds = 0;
    String alertSuccessText = "Activity Complete. Please continue to next activity.";
    String activityName = "";
    String timeLogString = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        Intent intent = getIntent();
        Resources res = context.getResources();
        Button playButton = (Button) findViewById(R.id.play_audio);
        Button activityCompleteButton = (Button) findViewById(R.id.activity_complete);
        Button restartActivityButton = (Button) findViewById(R.id.restart_activity);
        activityCompleteButton.setVisibility(View.INVISIBLE);
        restartActivityButton.setVisibility(View.INVISIBLE);
        Button startActivitybutton = (Button) findViewById(R.id.start_activity);
        TextView instructionsTextView = (TextView) findViewById(R.id.instructions_text);
        TextView timedActivityTextView = (TextView) findViewById(R.id.timed_activity_text);
        GifImageView gifImageView = (GifImageView) findViewById(R.id.instruction_gif);

        // getting information passed from previous activity
        String subjectId = intent.getStringExtra("subjectId");
        String activityId = intent.getStringExtra("activityId");
        String fileLocation = intent.getStringExtra("fileLocation");
        Boolean restartedActivity = intent.getBooleanExtra("restartActivity", false);
        if (restartedActivity) {
            timeLogString = "restart";
        } else {
            timeLogString = "start";
        }
        // adding subjectId as title of the page.
        getSupportActionBar().setTitle("Patient: " + subjectId);

        // set the filename for writing
        String fileName = getFileNameFormat(fileLocation, subjectId);
        try {
            fw = new FileWriter(fileName, true);
        } catch (IOException e) {
            System.out.println("Failed to load file: " + fileName);
            e.printStackTrace();
        }
        try {
           jsonObject = new JSONObject(intent.getStringExtra("jsonData"));
        } catch (JSONException e) {
            System.out.println("CODE ERROR: while parsing JSON data in instructions: " + e);
            e.printStackTrace();
        }
        try {
            activityDetails = jsonObject.getJSONObject(activityId);
            activityName = activityDetails.getString("activityName");
            activityInstructions = activityDetails.getJSONArray("instructions");
            audioFilename = activityDetails.getString("audioFileName");
            gifImageName = activityDetails.getString("gifFileName");
            alertUser = activityDetails.getBoolean("alertUser");
            alertAfterSeconds = activityDetails.getInt("alertAfter");
            alertSuccessText = activityDetails.getString("alertUserSuccessText");
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
        // attaching action for activity complete button
        final JSONObject newJSONTransferData = jsonObject;

        // start activity button press
        startActivitybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp.isPlaying()) {
                    mp.stop();
                }
                restartActivityButton.setVisibility(View.VISIBLE);
                startActivitybutton.setVisibility(View.INVISIBLE);
                // log the starting activity time
                logActivityTimings(fw, mAppendFileWriter, timeLogString, activityName);
                // initialize required values if we need to alert user after x seconds
                if (alertUser) {
                    activityCompleteButton.setVisibility(View.INVISIBLE);
                    timedActivityTextView.setText("Please Continue. A prompt will be displayed when it's time.");
                    // Initialize handler and runnable
                    initializeHandlerAndRunnable(
                            subjectId, newJSONTransferData.toString(), activityId, fileLocation,
                            alertAfterSeconds, alertSuccessText);
                } else {
                    activityCompleteButton.setVisibility(View.VISIBLE);
                }

            }
        });

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
                openDialog("restartActivity", subjectId, newJSONTransferData.toString(), activityId, fileLocation);
            }
        });

// startOverButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.out.println("Start over from beginning. Current activity: " + activityId);
//                openDialog("startOver", subjectId, newJSONTransferData.toString(), activityId, fileLocation);
//            }
//        });

        mp.start(); // start the audio once when page opens

    }

    public String getFileNameFormat(String fileLocation, String subjectId) {
        return  fileLocation + '_' + subjectId + ".txt";
    }

    private void logActivityTimings(FileWriter fileWriter, JavaAppendFileWriter fileAppendWriter, String flag, String activityName) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        format.setTimeZone(timeZone);
        String time =  format.format(calendar.getTime());
        try {
            fileAppendWriter.writeToFile(fileWriter, time, flag, activityName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // onClick for activityCompleteButton
    private void openNextInstructionActivity(String subjectId, String jsonObject, String activityId, FileWriter fw, JavaAppendFileWriter mAppendFileWriter, String fileLocation) {
        logActivityTimings(fw, mAppendFileWriter, "stop", "");
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", Integer.toString((Integer.parseInt(activityId) + 1)));
        intent.putExtra("fileLocation", fileLocation);
        startActivity(intent);
    }

    // onClick for restartActivityButton
    private void restartCurrentActivity(String subjectId, String jsonObject, String activityId, String fileLocation) {
        System.out.println("Restarting activity: " + activityId);
//        JavaAppendFileWriter.removeLastEntryFromFile(getFileNameFormat(fileLocation, subjectId));
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", activityId);
        intent.putExtra("fileLocation", fileLocation);
        intent.putExtra("restartActivity", true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // onClick for startOverActivity
    private void startOverFromStart(String subjectId, String jsonObject, String activityId, String fileLocation) throws IOException {
        JavaAppendFileWriter.truncateFile(getFileNameFormat(fileLocation, subjectId));
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", "1");
        intent.putExtra("fileLocation", fileLocation);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // if yes clicked on dialog, perform the respective action
    @Override
    public void onYesClicked(String activity, String subjectId, String jsonObject, String activityId, String fileLocation) throws IOException {
        if (activity == "restartActivity") {
            restartCurrentActivity(subjectId, jsonObject, activityId, fileLocation);
        } else {
            startOverFromStart(subjectId, jsonObject, "1", fileLocation);
        }
    }

    public void openDialog(
            String activity, String subjectId, String jsonObject, String activityId, String fileLocation
    ) {
        CustomDialog dialog = new CustomDialog(
                activity,
                subjectId,
                jsonObject,
                activityId,
                fileLocation
        );
        dialog.show(getSupportFragmentManager(), "customDialog");
    }

    private void startTimer(Integer timeoutSeconds) {
        long timeoutInSeconds = timeoutSeconds * 1000;
        // Post the runnable to the handler after TIMEOUT_MS
        handler.postDelayed(showDialogRunnable, timeoutInSeconds);
    }

    private void resetTimer(Integer timeoutSeconds) {
        // Remove any existing callbacks and post the runnable again
        handler.removeCallbacks(showDialogRunnable);
        startTimer(timeoutSeconds);
    }
    private void vibrate() {
        vibrator.vibrate(500);
    }

    public void initializeHandlerAndRunnable(
            String subjectId, String jsonObject, String activityId, String fileLocation,
            Integer timeoutSeconds, String alertText)
    {
        handler = new Handler();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        showDialogRunnable = new Runnable() {
            @Override
            public void run() {
                // Show the dialog when the user has been inactive for x seconds
                alertTimeoutDialog(
                    subjectId, jsonObject, activityId, fileLocation, alertText
                );
                vibrate();
            }
        };
        // Set up touch listener to reset the timer on user interaction
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Reset the timer on user interaction
                resetTimer(timeoutSeconds);
                return true;
            }
        });
        // Start the timer when the activity is created
        startTimer(timeoutSeconds);
    }

    public void alertTimeoutDialog(
            String subjectId, String jsonObject, String activityId, String fileLocation,
            String alertText
    ) {
        TimedActivityAlert timeoutDialogReminder =  new TimedActivityAlert(
                subjectId,
                jsonObject,
                activityId,
                fileLocation,
                alertText,
                "Next",
                ""
        );
        timeoutDialogReminder.show(getSupportFragmentManager(), "inactiveDialogReminder");
    }

    public void timedAlertButtonPress(
            Boolean positiveActivity,
            String subjectId,
            String jsonObject,
            String activityId,
            String fileLocation) {
        if (positiveActivity == true) {
            openNextInstructionActivity(subjectId, jsonObject, activityId, fw, mAppendFileWriter, fileLocation);
        } else {
            restartCurrentActivity(subjectId, jsonObject, activityId, fileLocation);
        }
    }

}