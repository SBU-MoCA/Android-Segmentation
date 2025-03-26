package com.example.myapplication;
import java.io.BufferedReader;
import java.io.FileInputStream;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import android.os.Bundle;

public class JavaAppendFileWriter extends Activity {
    public FileWriter fw;

    public void writeToFile(FileWriter fw, String flag, String activityName) throws IOException {
        String ntpServerTime = NTPTimeSync.getFormattedDateTime();
        Character terminatingCharacter = flag.equals("stop") ? '\n' : ',';
        String textToWrite = flag + ": " + ntpServerTime + terminatingCharacter;
        if (flag.equals("start") && !activityName.equals("") && !(activityName.equals("\0"))) {
            textToWrite = activityName + " - " + textToWrite;
        }
        System.out.println("Writing to file: " + textToWrite);
        fw.append(textToWrite);
        fw.flush();
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



    public BufferedReader readFileData(String fileName){
        String result="";

        StringBuilder text = new StringBuilder();

        try{
            InputStream fis = getAssets().open("script.txt");
            if (fis != null){
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                return br;
            }

//            String line;

            // read line by line
//            while ((line = br.readLine()) != null) {
//                text.append(line + '\n');
//            }

//            //获取文件长度
//            int length = fis.available();
//            byte[] buffer = new byte[length];
//            fis.read(buffer);
//            //将byte数组转换成指定格式的字符串
//            result = new String(buffer, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }

//        return  text.toString();
        return null;
    }

    public static void removeLastEntryFromFile(String filePath) {
        try {
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            long length = file.length();

            // Check if the file is empty
            if (length == 0) {
                System.out.println("File is empty.");
                return;
            }

            long position = length - 1;
            file.seek(position);

            while (file.readByte() != '\n') {
                position--;
                // If we reach the beginning of the file, break the loop
                if (position <= 0) {
                    break;
                }
                file.seek(position);
            }
            // Truncate the file from the calculated position
            file.setLength(position);
            // Add a new line if the truncate position is not the first line
            if (position != 0) file.writeBytes("\n");
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void truncateFile(String filePath) throws IOException {
        FileWriter fw = new FileWriter(filePath);
        fw.flush();
        System.out.println("File Truncated: " + filePath);
    }
}
