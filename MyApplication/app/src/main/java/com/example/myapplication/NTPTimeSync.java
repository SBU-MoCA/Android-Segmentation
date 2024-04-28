package com.example.myapplication;

import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.InvalidNtpServerResponseException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NTPTimeSync {
    Thread ntpTimeSyncThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try  {
                // TODO: get the timezone of edge server from cloud server and set timezone of app.
                TrueTime ntpTimeSync = TrueTime.build()
                        .withNtpHost("debian.pool.ntp.org") // same connection on the edge server as well
                        .withLoggingEnabled(true);
                try {
                    ntpTimeSync.initialize();
                } catch (IOException e) {
                    // Handle the InvalidNtpServerResponseException: root delay violation by trying the same request multiple times.
                    System.out.println("TIME SYNC ERROR: " + e);
                    throw new IOException(e);
                }
                System.out.println("Time from NTP: " + TrueTime.now());
            } catch (Exception e) {
                System.out.println("TIME SYNC RUNTIME ERROR: " + e);
                throw new RuntimeException(e);
            }
        }
    });

    NTPTimeSync() {
        ntpTimeSyncThread.start();
    }
    public static void clearCache() {
        TrueTime.clearCachedInfo();
    }

    public static Date getCurrentDate() {
        return TrueTime.now();
    }

    public static long getCurrentTimeInMilliseconds() {
        return TrueTime.now().getTime();
    }

    public static String getFormattedDateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        return format.format(TrueTime.now());
    }

    public static boolean isInitialized() {
        return TrueTime.isInitialized();
    }



}
