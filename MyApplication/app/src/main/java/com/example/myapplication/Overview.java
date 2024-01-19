package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


class ActivityResource {
    public JSONArray instructions;
    public String gifImageName;
    public String activityName;

    public ActivityResource(JSONArray instructions, String gifImageName, String activityName) {
        this.instructions = instructions;
        this.gifImageName = gifImageName;
        this.activityName = activityName;
    }
}

public class Overview extends AppCompatActivity {


    public Overview() throws IOException {

    }
    Context context = this;
    Helper helperClass = new Helper();

    String subjectId;
    String activityId;
    String fileLocation;
    JSONObject jsonObject;

    Boolean alertToStart;
    ArrayList<ActivityResource> roomActivities = new ArrayList<>();
    int currentActivity = 0;
    private Handler handler;
    private Timer timer;

    MediaPlayer mp; // overview screen voice command.
    public String overviewScreenVoiceFile = "overview_screen";
    public int gifTransitionTime = 17000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_overview);
        Intent intent = getIntent();
        Resources res = context.getResources();

        TextView roomTitle = (TextView) findViewById(R.id.room_title);
        TextView activityNumber = (TextView) findViewById(R.id.act_number);
        TextView activityText = (TextView) findViewById(R.id.activity_name);
        Button startRoomButton = (Button) findViewById(R.id.start_room);
        startRoomButton.setVisibility(View.INVISIBLE);
        GifImageView gifImageView = (GifImageView) findViewById(R.id.overview_gif);
        TextView disclaimerText = (TextView) findViewById(R.id.disclaimer);
        ImageView previousGif = (ImageView) findViewById(R.id.previousGif);
        ImageView nextGif = (ImageView) findViewById(R.id.nextGif);

        // getting information passed from previous activity
        subjectId = intent.getStringExtra("subjectId");
        activityId = intent.getStringExtra("activityId");
        fileLocation = intent.getStringExtra("fileLocation");
        alertToStart = intent.getBooleanExtra("alertToStart", true);

        // getting jsonString passed from previous activity
        try {
            jsonObject = new JSONObject(Objects.requireNonNull(intent.getStringExtra("jsonData")));
        } catch (JSONException e) {
            System.out.println("CODE ERROR: while parsing JSON data in instructions: " + e);
            e.printStackTrace();
        }

        //Find the current room
        JSONObject activityDetails = null;
        String roomName = "";

        try {
            activityDetails = jsonObject.getJSONObject(activityId);
            roomName = activityDetails.getString("roomName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Set title with the currently found room
        String titleString = "Overview of activities: " + roomName + "  " + helperClass.roomMapping.get(roomName) + " / " + helperClass.roomMapping.size();
        roomTitle.setText(titleString);
        String disclaimerString = "No need to remember. App will guide through each activity.";
        disclaimerText.setText(disclaimerString);

        //Parse json to find all the rooms that match the current room
        //and store their gif paths and instructions

        for(int i = Integer.parseInt(activityId); i > 0; i++) {
            JSONArray instructions;
            String gifImageName;
            String activityName;
            String roomNameAct;

            try {
                activityDetails = jsonObject.getJSONObject(Integer.toString(i));
                instructions = activityDetails.getJSONArray("instructions");
                gifImageName = activityDetails.getString("gifFileName");
                activityName = activityDetails.getString("activityName");
                roomNameAct = activityDetails.getString("roomName");
            }
            catch(JSONException e) {
                break;
            }

            roomNameAct = roomNameAct.toLowerCase();

            if(!roomNameAct.equals(roomName.toLowerCase())) break;

            roomActivities.add(new ActivityResource(instructions, gifImageName, activityName));
        }

        startRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               System.out.println("clicked");

                try {
                    jsonObject = new JSONObject(Objects.requireNonNull(intent.getStringExtra("jsonData")));
                } catch (JSONException e) {
                    System.out.println("CODE ERROR: while parsing JSON data in instructions: " + e);
                    e.printStackTrace();
                }

                openInstructionActivities(jsonObject, activityId);
            }
        });

        startTimer(res, gifImageView, activityNumber, activityText, startRoomButton, disclaimerText);

        previousGif.setClickable(true);
        previousGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               currentActivity-= 2;
                if (currentActivity < 0) {
                    currentActivity = 0;
                }
                startTimer(res, gifImageView, activityNumber, activityText, startRoomButton, disclaimerText);
            }
        });

        nextGif.setClickable(true);
        nextGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentActivity == roomActivities.size()) {
                    currentActivity = 0;
                }
                startTimer(res, gifImageView, activityNumber, activityText, startRoomButton, disclaimerText);
            }
        });

        int nextSoundId = res.getIdentifier(overviewScreenVoiceFile, "raw", context.getPackageName());
        mp = MediaPlayer.create(this, nextSoundId);
        mp.start();
    }

    private void startTimer(Resources res, GifImageView gifImageView, TextView activityNumber, TextView activityText, Button startRoomButton, TextView disclaimerText) {
        removeTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            displayNextGif(res, gifImageView, activityNumber, activityText, startRoomButton, disclaimerText);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 0, gifTransitionTime); // Update every 5 seconds (adjust as needed)
    }

    private void removeTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    private void displayNextGif(Resources res, GifImageView gifImageView, TextView activityNumber, TextView activityText, Button startActivityButton, TextView disclaimerText) throws IOException {
        System.out.println("Next GIF");
        String gifImageName = roomActivities.get(currentActivity).gifImageName;
        JSONArray activityInstructions = roomActivities.get(currentActivity).instructions;
        String activityName = roomActivities.get(currentActivity).activityName;

        // Get resource identifier for the new GIF image
        final int drawableGifId = res.getIdentifier(gifImageName, "drawable", getPackageName());

        // Create a TransitionDrawable with a transparent drawable and the new GIF
        final TransitionDrawable crossFadeDrawable = new TransitionDrawable(new Drawable[]{
                new ColorDrawable(android.R.color.transparent),
                new GifDrawable(getResources(), drawableGifId)
        });

        // Set the cross-fade duration (adjust as needed)
        crossFadeDrawable.setCrossFadeEnabled(true);
        crossFadeDrawable.startTransition(3000); // milliseconds cross-fade duration

        // Set the TransitionDrawable to the GifImageView
        gifImageView.setImageDrawable(crossFadeDrawable);

        String activityTotal = Integer.toString(currentActivity + 1) + "/" + Integer.toString(roomActivities.size());
        activityNumber.setText(activityTotal);

        activityText.setText("Activity " + (currentActivity + 1) + ": " + activityName);


        currentActivity++;

        if(currentActivity == roomActivities.size()) {
            currentActivity = 0;
            disclaimerText.setVisibility(View.INVISIBLE);
            startActivityButton.setVisibility(View.VISIBLE);
        }

//        gifImageView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                gifImageView.setImageResource(drawableGifId);
//            }
//        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the timer when the activity is destroyed
        removeTimer();
    }

    public void openInstructionActivities(JSONObject jsonObject, String currentActivity) {
        if(mp.isPlaying()) mp.stop();
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject.toString());
        intent.putExtra("activityId", currentActivity);
        intent.putExtra("fileLocation", fileLocation);
        intent.putExtra("alertToStart", alertToStart);
        removeTimer();
        startActivity(intent);
    }
}
