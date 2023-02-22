package com.example.myapplication;
import java.io.BufferedReader;
import java.io.FileInputStream;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import android.os.Bundle;

public class JavaAppendFileWriter extends Activity {


//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }

    public FileWriter fw;

    public void main(FileWriter fw, String timestamp, String flag) throws IOException {
        if(Objects.equals(flag, "start")) {
            fw.append("start:" + timestamp + ",");
        }
        else{
            fw.append("stop:" + timestamp + "\n");
        }
        System.out.println(timestamp);

    }

    public String getFileName() throws IOException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String time =  format.format(calendar.getTime());
//        String fileName = "/data/data/com.example.myapplication/" + time + ".txt";
        String fileName = "/data/data/com.example.myapplication/" + time;

        return fileName;
    }

    public void getFiles(String path){
        File file = new File(path);
        File[] files = file.listFiles();
        for(int i=0; i<files.length; i++){
            System.out.println(files[i].getAbsolutePath());
        }


    }



    public String readFileData(String fileName){
        String result="";

        StringBuilder text = new StringBuilder();
        try{
            FileInputStream fis = null;



            fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String line;

            while ((line = br.readLine()) != null) {
                text.append(line + '\n');
            }

//            //获取文件长度
//            int length = fis.available();
//            byte[] buffer = new byte[length];
//            fis.read(buffer);
//            //将byte数组转换成指定格式的字符串
//            result = new String(buffer, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return  text.toString();
    }
}
