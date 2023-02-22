package com.example.myapplication;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import android.app.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
public class FileOperationActivity extends Activity{
    String fileName = "test.txt";
    String content = "demo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//            setContentView(R.layout.main);
            writeFileData(fileName,  content); // 写入文件
            String result = readFileData(fileName); // 读取文件
//            tv_content = (TextView) findViewById(R.id.tv_content);
//            tv_content.setText(result);
            }
      //向指定的文件中写入指定的数据
    public void writeFileData(String filename, String content){

        try {
            FileOutputStream fos = this.openFileOutput(filename, MODE_PRIVATE);//获得FileOutputStream
            byte[]  bytes = content.getBytes();
            fos.write(bytes);//将byte数组写入文件
            fos.close();//关闭文件输出流
            }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    //打开指定文件，读取其数据，返回字符串对象
    public String readFileData(String fileName){
        String result="";
        try{
            FileInputStream fis = this.openFileInput(fileName);
             //获取文件长度
            int length = fis.available();
            byte[] buffer = new byte[length];
            fis.read(buffer);
            //将byte数组转换成指定格式的字符串
            result = new String(buffer, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }
 }