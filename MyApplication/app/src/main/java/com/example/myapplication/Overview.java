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
        GifImageView gifImageView = (GifImageView) findViewById(R.id.overview_gif);

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
            throw new RuntimeException(e);
        }

        //Set title with the currently found room
        String titleString = "Overview of the\n" + roomName + " Activities";
        roomTitle.setText(titleString);

        //Parse json to find all the rooms that match the current room
        //and store their gif paths and instructions

        for(int i = 1; i > 0; i++) {
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

                openInstructionActivities(jsonObject);
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
                        displayNextGif(res, gifImageView, activityNumber, activityText);
                    }
                });
            }
        }, 0, 5000); // Update every 5 seconds (adjust as needed)
    }

    private void displayNextGif(Resources res, GifImageView gifImageView, TextView activityNumber, TextView activityText) {
        String gifImageName = roomActivities.get(currentActivity).gifImageName;
        JSONArray activityInstructions = roomActivities.get(currentActivity).instructions;
        String activityName = roomActivities.get(currentActivity).activityName;

        int drawableGifId = res.getIdentifier(gifImageName, "drawable", getPackageName());
        GifDrawable gifFromResource = null;
        try {
            gifFromResource = new GifDrawable( getResources(), drawableGifId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gifImageView.setImageDrawable(gifFromResource);

        String activityTotal = Integer.toString(currentActivity + 1) + "/" + Integer.toString(roomActivities.size());
        activityNumber.setText(activityTotal);

        activityText.setText(activityName);

        currentActivity++;

        if(currentActivity == roomActivities.size()) {
            currentActivity = 0;
        }
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

    public void openInstructionActivities(JSONObject jsonObject) {
        Intent intent = new Intent(this, Instructions.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("jsonData", jsonObject.toString());
        intent.putExtra("activityId", "1");
        intent.putExtra("fileLocation", fileLocation);
        startActivity(intent);
    }
}
