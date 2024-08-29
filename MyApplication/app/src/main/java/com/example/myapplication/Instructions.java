package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.Objects;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class Instructions extends AppCompatActivity implements CustomDialog.CustomDialogListener, TimedActivityAlert.TimedAlertListener, StartAlert.StartAlertListener {
    public Instructions() throws IOException {}
    private boolean isAppInBackground = false;
    Helper helperClass = new Helper();
    String FINAL_ACTIVITY_NUMBER = "41"; // TODO: Update when number of activity changes.
    private JavaAppendFileWriter mAppendFileWriter = new JavaAppendFileWriter();
    private FileWriter fw;
    public Boolean alertToStart;
    Context context = this;
    MediaPlayer mp = new MediaPlayer(); // media for Start button
    MediaPlayer mp1 = new MediaPlayer(); // media for next button
    MediaPlayer mp2 = new MediaPlayer(); // media for timed activity popup button.
    MediaPlayer mp3 = new MediaPlayer(); // media for ready to start alert.

    CountDownTimer cdt = null;

    TextView instructionsTextView;

    // for time specific alerts.
    private Handler handler;

    private Handler showStartAlertHandler;

    private Runnable showStartDialogRunnable;
    private Runnable showDialogRunnable;
    private Vibrator vibrator;

    // getting jsonString passed from previous activity
    JSONObject jsonObject = null;
    JSONObject activityDetails = null;
    JSONArray activityStartInstructions = null;
    JSONArray activityNextInstructions = null;
    String audioStartFilename = null;
    String audioNextFilename = null;
    String gifImageName = null;
    Boolean alertUser = false;
    Boolean isOptionalActivity = false;
    Integer alertAfterSeconds = 0;
    String activityName = "";
    String timeLogString = "";
    String currentSubjectId = "";
    String startInstructionSet = "";
    String nextInstructionSet = "";
    Boolean showOptionalActivities = true;
    Boolean turnInBed = true;
    Boolean takeOffShoes = true;
    Boolean realFood = true;

    Integer roomActivitySize;
    Integer currentActivity;

    public String activityCompleteVoiceFile = "timed_activity_voice";
    public String startActivityAlertVoiceFile = "start_activity_ready";
    public int startActivityAlertTimer = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        Intent intent = getIntent();
        Resources res = context.getResources();
        Button playButton = (Button) findViewById(R.id.play_audio);
        Button activityCompleteButton = (Button) findViewById(R.id.activity_complete);
        Button restartActivityButton = (Button) findViewById(R.id.restart_activity);
        activityCompleteButton.setVisibility(View.INVISIBLE);
        restartActivityButton.setVisibility(View.INVISIBLE);
        Button startActivitybutton = (Button) findViewById(R.id.start_activity);
        instructionsTextView = (TextView) findViewById(R.id.instructions_text);
        TextView activityLength = (TextView) findViewById(R.id.activity_length);
        GifImageView gifImageView = (GifImageView) findViewById(R.id.instruction_gif);

        // getting information passed from previous activity
        String subjectId = intent.getStringExtra("subjectId");
        String activityId = intent.getStringExtra("activityId");
        String fileLocation = intent.getStringExtra("fileLocation");
        boolean restartedActivity = intent.getBooleanExtra("restartActivity", false);
        alertToStart = intent.getBooleanExtra("alertToStart", true);
        turnInBed = intent.getBooleanExtra("turnInBed", true);
        realFood = intent.getBooleanExtra("realFood", true);
        takeOffShoes = intent.getBooleanExtra("takeOffShoes", true);
        roomActivitySize =  intent.getIntExtra("roomActivitySize", 0);
        currentActivity = intent.getIntExtra("currentActivity", 1);
        if (restartedActivity) {
            timeLogString = "restart";
        } else {
            timeLogString = "start";
        }
        // adding subjectId as title of the page.
        getSupportActionBar().setTitle("Participant: " + subjectId);

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
        System.out.println("ACTIVITY ID: " + activityId);
        try {
            activityDetails = jsonObject.getJSONObject(activityId);
            activityName = activityDetails.getString("activityName");
            activityStartInstructions = activityDetails.getJSONArray("startInstructions");
            activityNextInstructions = activityDetails.getJSONArray("nextInstructions");
            audioStartFilename = activityDetails.getString("audioStartFileName");
            audioNextFilename = activityDetails.getString("audioNextFileName");
            gifImageName = activityDetails.getString("gifFileName");
            alertUser = activityDetails.getBoolean("alertUser");
            alertAfterSeconds = activityDetails.getInt("alertAfter");
            isOptionalActivity = activityDetails.getBoolean("optionalActivity");
        } catch (JSONException e) {
            System.out.println("CODE ERROR: while getting activity JSON data: " + e);
            e.printStackTrace();
        }

        if (activityId.equals("1")) {
            new NTPTimeSync();
            // All the functions in this package are static.
        }
        // getting the audioFileName
        int soundId = res.getIdentifier(audioStartFilename, "raw", context.getPackageName());
        mp = MediaPlayer.create(this, soundId);

        if (roomActivitySize != 0) {
            String activityLengthText = "Activity: " + currentActivity + "/" + roomActivitySize;
            activityLength.setText(activityLengthText);
            activityLength.setVisibility(View.VISIBLE);
        } else {
            activityLength.setVisibility(View.INVISIBLE);
        }

        // attaching play/pause feature to the button
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp.isPlaying()){
                    mp.pause();
                } else {
                    mp.start();
                }
                removeTimerForStartButton();
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

        int nextSoundId = res.getIdentifier(audioNextFilename, "raw", context.getPackageName());
        mp1 = MediaPlayer.create(this, nextSoundId);

        // attaching instructions text

        for (int i = 0; i < activityStartInstructions.length(); i += 1) {
            try {
                startInstructionSet += ( i + 1) + ".)  " + activityStartInstructions.getString(i);
                startInstructionSet += "\n";
            } catch (JSONException e) {
                System.out.println("CODE ERROR: Failed to parse instructions: " + e);
                throw new RuntimeException(e);
            }
        }

        for (int i = 0; i < activityNextInstructions.length(); i += 1) {
            try {
                nextInstructionSet += ( i + 1) + ".)  " + activityNextInstructions.getString(i);
                nextInstructionSet += "\n";
            } catch (JSONException e) {
                System.out.println("CODE ERROR: Failed to parse instructions: " + e);
                throw new RuntimeException(e);
            }
        }
        instructionsTextView.setText(startInstructionSet);
        String finalNextInstructionSet = nextInstructionSet;
        // start activity button press
        startActivitybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButtonPressed(restartActivityButton, startActivitybutton, playButton, activityCompleteButton, subjectId,
                        newJSONTransferData, activityId, fileLocation);

                instructionsTextView.setText(finalNextInstructionSet);
            }
        });

        activityCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp1.isPlaying()) mp1.stop();
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
        mp.start(); // start the audio once when page opens

        if (activityId.equals(FINAL_ACTIVITY_NUMBER)) {
            startActivitybutton.setVisibility(View.INVISIBLE);
            restartActivityButton.setVisibility(View.INVISIBLE);
            activityCompleteButton.setVisibility(View.VISIBLE);
        } else if (alertToStart) {
            // alert user to start after 10 seconds of
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    showStartAlertHandler =  new Handler();
                   showStartDialogRunnable = new Runnable() {
                       @Override
                       public void run() {
                           showStartAlert(restartActivityButton, startActivitybutton, playButton, activityCompleteButton, subjectId,
                                   newJSONTransferData, activityId, fileLocation);
                       }
                   };
                   removeAllTimers();
                   vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                   startTimerForStartButton();
                }
            });
        }

    }


    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the message and negative button
        builder.setMessage("Do you want to exit the app?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });

        // Set the positive button with a custom color
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
            }
        });

        // Get the AlertDialog and set a custom style to change the button color
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.primary_button));
                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextColor(getResources().getColor(R.color.mild_red));
            }
        });

        alertDialog.show();
    }
    @Override
    public void onBackPressed() {
       showExitConfirmationDialog();
    }

    public void startCountDownTimer(Integer time) {
        CountTimerDialog countTimerDialog = new CountTimerDialog(nextInstructionSet);
        countTimerDialog.setCancelable(false);
        countTimerDialog.show(getSupportFragmentManager(), "countDownTimerDialog");
        cdt = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeInSeconds = String.valueOf(millisUntilFinished / 1000);
                countTimerDialog.setCountDownText(timeInSeconds);
            }
            @Override
            public void onFinish() {
                countTimerDialog.dismiss();
                cancelCountDownTimer();
            }
        };
        cdt.start();
    }

    //cancel timer
    void cancelCountDownTimer() {
        if(cdt != null)
            cdt.cancel();
    }

    public void stopAllMusicPlayers() {
        if (mp != null && mp.isPlaying()) mp.stop();
        if (mp1 != null && mp1.isPlaying()) mp1.stop();
        if (mp2 != null && mp2.isPlaying()) mp2.stop();
        if (mp3 != null && mp3.isPlaying()) mp3.stop();
    }

    public void releaseAllMediaPlayers() {
        if (mp != null) mp.release();
        if (mp1 != null) mp1.release();
        if (mp2 != null) mp2.release();
        if (mp3 != null) mp3.release();
    }
    public String getFileNameFormat(String fileLocation, String subjectId) {
        currentSubjectId = fileLocation;
        return  fileLocation + '_' + subjectId + ".txt";
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.restart_activity_menu, menu);
        return true;
    }

    private void startTimerForStartButton() {
        showStartAlertHandler.postDelayed(showStartDialogRunnable, startActivityAlertTimer);
    }

    private void removeTimerForStartButton() {
        if (showStartAlertHandler != null) {
            showStartAlertHandler.removeCallbacks(showStartDialogRunnable);
        }
    }

    private void removeAllTimers() {
        removeTimerForStartButton();
        removeTimer();
    }

    private void showStartAlert(Button restartActivityButton, Button startActivitybutton, Button playButton,  Button activityCompleteButton,
                                String subjectId, JSONObject newJSONTransferData, String activityId, String fileLocation) {
        StartAlert dialog = new StartAlert(
                restartActivityButton, startActivitybutton, playButton, activityCompleteButton, subjectId,
                newJSONTransferData, activityId, fileLocation
        );
        dialog.setCancelable(false);
        stopAllMusicPlayers();
        Resources res = context.getResources();
        int soundId = res.getIdentifier(startActivityAlertVoiceFile, "raw", context.getPackageName());
        mp3 = MediaPlayer.create(this, soundId);
        mp3.start();
        vibrate();
        dialog.show(getSupportFragmentManager(), "startAlertDialog");
    }

    private void startButtonPressed(
            Button restartActivityButton, Button startActivitybutton, Button playButton,  Button activityCompleteButton,
            String subjectId, JSONObject newJSONTransferData, String activityId, String fileLocation
    ) {
        stopAllMusicPlayers();
        restartActivityButton.setVisibility(View.VISIBLE);
        startActivitybutton.setVisibility(View.INVISIBLE);
        // log the starting activity time
        logActivityTimings(fw, mAppendFileWriter, timeLogString, activityName);
        Integer alertTimeInSeconds = 30; // default
        JSONObject activityJSONObject = null;
        try {
            activityJSONObject = newJSONTransferData.getJSONObject(activityId);
            alertTimeInSeconds = activityJSONObject.getInt("alertAfter");
        } catch (JSONException e) {
            System.out.println("ERROR in startButtonPressed JSON Fetch" + e);
            e.printStackTrace();
        }
        // initialize required values if we need to alert user after x seconds
        if (alertUser) {
            activityCompleteButton.setVisibility(View.INVISIBLE);
            startCountDownTimer(alertTimeInSeconds * 1000);
            // Initialize handler and runnable
            initializeHandlerAndRunnable(
                    subjectId, newJSONTransferData.toString(), activityId, fileLocation,
                    alertAfterSeconds);
        } else {
            activityCompleteButton.setVisibility(View.VISIBLE);
        }
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp1.isPlaying()){
                    mp1.pause();
                } else {
                    mp1.start();
                }
            }
        });
        mp1.start();
        removeTimerForStartButton();
        instructionsTextView.setText(nextInstructionSet);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.restart_all_button:
                openDialog("startOver", "", "", "", "" );
        }
        return super.onOptionsItemSelected(item);
    }

    private void logActivityTimings(FileWriter fileWriter, JavaAppendFileWriter fileAppendWriter, String flag, String activityName) {
        try {
            fileAppendWriter.writeToFile(fileWriter, flag, activityName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // onClick for activityCompleteButton
    private void openNextInstructionActivity(String subjectId, String jsonObject, String activityId, FileWriter fw, JavaAppendFileWriter mAppendFileWriter, String fileLocation) {
        removeAllTimers();
        stopAllMusicPlayers();
        cancelCountDownTimer();
        logActivityTimings(fw, mAppendFileWriter, "stop", "");

        //Determine whether to call the overview activity
        Intent intent;
         if (activityId.equals(FINAL_ACTIVITY_NUMBER)) {
            intent = new Intent(this, StartupActivity.class);
        }
        else if(this.isLastActivity(jsonObject, activityId)) {
            intent = new Intent(this, Overview.class);
        }
        else {
            intent = new Intent(this, Instructions.class);
        }

        // check if next activity is the activity to skip if the turnInBed switch if off.
        if (!turnInBed) {
            String tempActivityId = activityId;
            // skip the next activity if it's an optional activity
            for (int element : helperClass.TURN_IN_BED_ACTIVITY_LIST) {
                if (element == (Integer.parseInt(tempActivityId, 10) + 1)) {
                    tempActivityId = Integer.toString((Integer.parseInt(tempActivityId, 10) + 1));
                }
            }
            activityId = tempActivityId;
        }

        // check if next activity is the activity to skip if takeOffShoes switch is off
        if (!takeOffShoes) {
            if (helperClass.TAKE_OFF_SHOES_ACTIVITY == ((Integer.parseInt(activityId, 10) + 1))) {
                activityId = Integer.toString((Integer.parseInt(activityId, 10) + 1));
            }
        }
       // skip real food activities if artificial food is selected.
        String tempActivityId = activityId;
        for (int element : helperClass.FAKE_FOOD_ACTIVITY_LIST) {
            if (element == (Integer.parseInt(tempActivityId, 10)) && !realFood) {
               tempActivityId = Integer.toString((Integer.parseInt(tempActivityId, 10) + 1));
                break;
            }
        }
        // Filter whether to show real or fake food activity.
        // change the activities
        for (int element : helperClass.FOOD_ACTIVITIES_CHECK) {
            System.out.println("ACTIVITY: " + activityId);
            if (element == (Integer.parseInt(tempActivityId, 10))) {
                int incrementor = 0;
                if (realFood) {
                    incrementor = 1;
                }
                tempActivityId = Integer.toString((Integer.parseInt(tempActivityId, 10) + incrementor));

                System.out.println("NEW ACTIVITY: " + tempActivityId + realFood);
                break;
            }
        }
        activityId = tempActivityId;
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", Integer.toString((Integer.parseInt(activityId) + 1)));
        intent.putExtra("fileLocation", fileLocation);
        intent.putExtra("alertToStart", alertToStart);
        intent.putExtra("roomActivitySize", roomActivitySize);
        intent.putExtra("currentActivity", currentActivity + 1);
        intent.putExtra("turnInBed", turnInBed);
        intent.putExtra("takeOffShoes", takeOffShoes);
        intent.putExtra("realFood", realFood);
        releaseAllMediaPlayers();
        startActivity(intent);
    }

    private boolean isLastActivity(String jsonString, String activityId) {
        //Check whether this is the last activity in the room
        String nextActivity = Integer.toString((Integer.parseInt(activityId) + 1));

        JSONObject jsonObject = null;
        String roomNameCurrent = "";
        String roomNameNext = "";

        try {
            jsonObject = new JSONObject(jsonString);

            JSONObject activityDetails = jsonObject.getJSONObject(activityId);
            roomNameCurrent = activityDetails.getString("roomName");

            activityDetails = jsonObject.getJSONObject(nextActivity);
            roomNameNext = activityDetails.getString("roomName");
        } catch (JSONException e) {
            System.out.println("CODE ERROR: while parsing JSON: " + e);
            e.printStackTrace();
        }

        roomNameCurrent = roomNameCurrent.toLowerCase();
        roomNameNext = roomNameNext.toLowerCase();

        return !roomNameCurrent.equals(roomNameNext);
    }

    // onClick for restartActivityButton
    private void restartCurrentActivity(String subjectId, String jsonObject, String activityId, String fileLocation) {
        stopAllMusicPlayers();
        removeAllTimers();
        cancelCountDownTimer();
        System.out.println("Restarting activity: " + activityId);
//        JavaAppendFileWriter.removeLastEntryFromFile(getFileNameFormat(fileLocation, subjectId));
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", activityId);
        intent.putExtra("fileLocation", fileLocation);
        intent.putExtra("restartActivity", true);
        intent.putExtra("alertToStart", alertToStart);
        intent.putExtra("roomActivitySize", roomActivitySize);
        intent.putExtra("currentActivity", currentActivity);
        intent.putExtra("turnInBed", turnInBed);
        intent.putExtra("takeOffShoes", takeOffShoes);
        intent.putExtra("realFood", realFood);
        releaseAllMediaPlayers();
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // onClick for startOverActivity
    private void startOverFromStart(String subjectId, String jsonObject, String activityId, String fileLocation) throws IOException {
        stopAllMusicPlayers();
        removeAllTimers();
        cancelCountDownTimer();
        Intent intent = new Intent(this, StartupActivity.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject);
        intent.putExtra("activityId", "1");
        intent.putExtra("fileLocation", fileLocation);
        intent.putExtra("alertToStart", alertToStart);
        intent.putExtra("roomActivitySize", roomActivitySize);
        intent.putExtra("currentActivity", currentActivity + 1);
        intent.putExtra("turnInBed", turnInBed);
        intent.putExtra("takeOffShoes", takeOffShoes);
        intent.putExtra("realFood", realFood);
        releaseAllMediaPlayers();
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

    // overridden from StartAlert Class.
    @Override
    public void onStartActivity( Button restartActivityButton, Button startActivitybutton, Button playButton,  Button activityCompleteButton,
                                 String subjectId, JSONObject newJSONTransferData, String activityId, String fileLocation) {
        removeTimerForStartButton();
        startButtonPressed(restartActivityButton, startActivitybutton, playButton, activityCompleteButton, subjectId,
                newJSONTransferData, activityId, fileLocation);
    }

    @Override
    public void onBackToInstructions() {
        stopAllMusicPlayers();
        mp.start();
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

    private void removeTimer() {
        if (handler != null) {
            handler.removeCallbacks(showDialogRunnable);
        }
    }
    private void vibrate() {
        vibrator.vibrate(500);
    }
    private void playActivityComplete() {
        if (mp.isPlaying()) mp.stop();
        if (mp1.isPlaying()) mp1.stop();
        if (mp2.isPlaying()) mp2.stop();
        Resources res = context.getResources();
        int soundId = res.getIdentifier(activityCompleteVoiceFile, "raw", context.getPackageName());
        mp2 = MediaPlayer.create(this, soundId);
        mp2.start();
    }

    public void initializeHandlerAndRunnable(
            String subjectId, String jsonObject, String activityId, String fileLocation,
            Integer timeoutSeconds)
    {
        handler = new Handler();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        showDialogRunnable = new Runnable() {
            @Override
            public void run() {
                // Show the dialog when the user has been inactive for x seconds
                alertTimeoutDialog(
                    subjectId, jsonObject, activityId, fileLocation
                );
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
            String subjectId, String jsonObject, String activityId, String fileLocation
    ) {
        TimedActivityAlert timeoutDialogReminder =  new TimedActivityAlert(
                subjectId,
                jsonObject,
                activityId,
                fileLocation,
                "",
                "Next",
                "Restart Activity"
        );
        timeoutDialogReminder.setCancelable(false);
        vibrate();
        playActivityComplete();
        timeoutDialogReminder.show(getSupportFragmentManager(), "inactiveDialogReminder");
    }

    public void timedAlertButtonPress(
            Boolean positiveActivity,
            String subjectId,
            String jsonObject,
            String activityId,
            String fileLocation) {
        if(mp2.isPlaying()) mp2.stop();
        if (positiveActivity) {
            openNextInstructionActivity(subjectId, jsonObject, activityId, fw, mAppendFileWriter, fileLocation);
        } else {
            restartCurrentActivity(subjectId, jsonObject, activityId, fileLocation);
        }
    }

}