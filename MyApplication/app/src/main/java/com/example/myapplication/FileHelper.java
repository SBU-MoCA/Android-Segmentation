package com.example.myapplication;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileHelper {

    private Context mContext;
    private Object TimeUtil;

    public FileHelper(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 定义文件保存的方法，写入到文件中，所以是输出流
     */
    public void save(String time) {
        String content = time;
        FileOutputStream fos = null;
        try {
            // Context.MODE_PRIVATE私有权限，Context.MODE_APPEND追加写入到已有内容的后面
            fos = mContext.openFileOutput(getFileName(), Context.MODE_APPEND);
            fos.write(content.getBytes());
            fos.write("\r\n".getBytes());//写入换行
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 定义文件读取的方法
     */
    public String read(String filename) throws IOException {
        FileInputStream fis = mContext.openFileInput(filename);
        byte[] buff = new byte[1024];
        StringBuilder sb = new StringBuilder("");
        int len = 0;
        while ((len = fis.read(buff)) > 0) {
            sb.append(new String(buff, 0, len));
        }
        fis.close();
        return sb.toString();
    }

    /**
     * get file name such as 20171031.txt
     *
     * @return
     */
    private String getFileName() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd-HH-mm-ss ");
        String time =  format.format(calendar.getTime());
        return time + ".txt";
    }
}
