package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.TimeZone;

import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    private Button btnShowDate1;
    private Button btnShowDate2;
    private Button finish;

    private JavaAppendFileWriter mAppendFileWriter = new JavaAppendFileWriter();
    private String fileName = mAppendFileWriter.getFileName();
    private FileWriter fw;
    private BufferedReader br;
//    private FileWriter fw = new FileWriter(fileName, true);

    public MainActivity() throws IOException {
    }

//    private BufferedReader br = mAppendFileWriter.readFileData("script.txt");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // open script file for guidance
        InputStream fis = null;
        try {
            fis = getAssets().open("script.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fis != null){
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(fis, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            br = new BufferedReader(isr);
        }

        setContentView(R.layout.activity_main);

        EditText inputText = (EditText) findViewById(R.id.FilenameView);

        TextView textViewADL=findViewById(R.id.next_activity);
        textViewADL.setText("Next Activity");

        // start button
        btnShowDate1=(Button)findViewById(R.id.button1);
        // stop button
        btnShowDate2=(Button)findViewById(R.id.button2);
        // finish button
        finish = (Button) findViewById(R.id.button3);

        btnShowDate1.setEnabled(false);
        btnShowDate2.setEnabled(false);
        finish.setEnabled(false);

        inputText.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event != null &&
                                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (event == null || !event.isShiftPressed()) {
                                // the user is done typing.
                                System.out.println(inputText.getText().toString());

                                fileName = fileName + '_' + inputText.getText().toString() + ".txt";
                                try {
                                    fw = new FileWriter(fileName, true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // read a line from "script.txt" and show
                                try {
                                    String line;
                                    if ((line = br.readLine()) != null){
                                        System.out.println("script:" + line);
                                        textViewADL.setText(line);

                                        btnShowDate1.setEnabled(true);
                                    }
                                    else {
                                        textViewADL.setText("No activities in script file!");
                                    }
                                } catch (IOException e) {
                                    System.out.println("script:exception");
                                    e.printStackTrace();

                                }

                                return true; // consume.
                            }
                        }
                        return false;
                    }

                }
        );




        // on click, start button
        btnShowDate1.setOnClickListener(v -> {

            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            Calendar calendar = Calendar.getInstance(timeZone);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            format.setTimeZone(timeZone);
            String time =  format.format(calendar.getTime());

            TextView textView = findViewById(R.id.textView1);


            textView.setText(time);
            try {
                mAppendFileWriter.writeToFile(fw, time, "start");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // disable start button, enable stop button
            btnShowDate1.setEnabled(false);
            btnShowDate2.setEnabled(true);

        });

        // on click, stop button
        btnShowDate2.setOnClickListener(v -> {
            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            Calendar calendar = Calendar.getInstance(timeZone);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            format.setTimeZone(timeZone);
            String time =  format.format(calendar.getTime());


            TextView textView = findViewById(R.id.textView2);
            textView.setText(time);
            try {
                mAppendFileWriter.writeToFile(fw, time, "stop");

            } catch (IOException e) {
                e.printStackTrace();
            }

            // read a line from "script.txt" and show
            try {
                String line;
                if ((line = br.readLine()) != null){
                    System.out.println("script:" + line);
                    textViewADL.setText(line);

                    // disable stop button, enable start button
                    btnShowDate1.setEnabled(true);
                    btnShowDate2.setEnabled(false);
                }
                else {
                    textViewADL.setText("All activities finished! Thanks for your cooperation! Click 'FINISH' to exit. ");

                    // disable stop button and start button, enable finish button
                    btnShowDate1.setEnabled(false);
                    btnShowDate2.setEnabled(false);
                    finish.setEnabled(true);
                }
            } catch (IOException e) {
                System.out.println("script:exception");
                e.printStackTrace();

            }


        });

        // on click, finish button
        finish.setOnClickListener(v -> {
            TextView exitView = findViewById(R.id.exitView);

            try {
                fw.close();
                System.out.println("close file successfully");
                exitView.setText("Program exiting!");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.exit(0);
                    }
                }, 1000);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}
