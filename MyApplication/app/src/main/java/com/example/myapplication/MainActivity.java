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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.sql.Timestamp;

import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    private Button btnShowDate1;
    private Button btnShowDate2;
    private Button finish;

    private JavaAppendFileWriter mAppendFileWriter = new JavaAppendFileWriter();
    private String fileName = mAppendFileWriter.getFileName();
    private FileWriter fw;
//    private FileWriter fw = new FileWriter(fileName, true);

    public MainActivity() throws IOException {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText inputText = (EditText) findViewById(R.id.FilenameView);

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


                                return true; // consume.
                            }
                        }
                        return false;
                    }

                }
        );


        btnShowDate1=(Button)findViewById(R.id.button1);

        btnShowDate1.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String time =  format.format(calendar.getTime());

            TextView textView = findViewById(R.id.textView1);


            textView.setText(time);
            try {
                mAppendFileWriter.main(fw, time, "start");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnShowDate2=(Button)findViewById(R.id.button2);
        btnShowDate2.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String time =  format.format(calendar.getTime());

            TextView textView = findViewById(R.id.textView2);
            textView.setText(time);
            try {
                mAppendFileWriter.main(fw, time, "stop");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        finish = (Button) findViewById(R.id.button3);
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
