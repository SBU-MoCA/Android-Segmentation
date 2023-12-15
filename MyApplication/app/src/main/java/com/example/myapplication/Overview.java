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
import android.widget.TextView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
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
    public Overview() throws IOException {}
    Context context = this;

    String subjectId;
    String activityId;
    String fileLocation;
    JSONObject jsonObject;
    ArrayList<ActivityResource> roomActivities = new ArrayList<>();
    int currentActivity = 0;
    private Handler handler;
    private Timer timer;

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

        // getting information passed from previous activity
        subjectId = intent.getStringExtra("subjectId");
        activityId = intent.getStringExtra("activityId");
        fileLocation = intent.getStringExtra("fileLocation");

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
        String titleString = "Overview of activities: " + roomName;
        roomTitle.setText(titleString);
        disclaimerText.setText("You do not need to remember these activities. In the next step, the app will guide you through each activity step by step.");

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

        //Display on a timer
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            displayNextGif(res, gifImageView, activityNumber, activityText, startRoomButton);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 0, 5000); // Update every 5 seconds (adjust as needed)
    }

    private void displayNextGif(Resources res, GifImageView gifImageView, TextView activityNumber, TextView activityText, Button startActivityButton) throws IOException {
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
        crossFadeDrawable.startTransition(1000); // 500 milliseconds cross-fade duration

        // Set the TransitionDrawable to the GifImageView
        gifImageView.setImageDrawable(crossFadeDrawable);

        String activityTotal = Integer.toString(currentActivity + 1) + "/" + Integer.toString(roomActivities.size());
        activityNumber.setText(activityTotal);

        activityText.setText("Activity " + (currentActivity + 1) + ": " + activityName);


        currentActivity++;

        if(currentActivity == roomActivities.size()) {
            currentActivity = 0;
            startActivityButton.setVisibility(View.VISIBLE);
        }

        gifImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                gifImageView.setImageResource(drawableGifId);
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the timer when the activity is destroyed
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public void openInstructionActivities(JSONObject jsonObject, String currentActivity) {
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject.toString());
        intent.putExtra("activityId", currentActivity);
        intent.putExtra("fileLocation", fileLocation);
        startActivity(intent);
    }
}
