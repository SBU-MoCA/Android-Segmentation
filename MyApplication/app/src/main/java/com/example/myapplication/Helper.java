package com.example.myapplication;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Helper {
    public Map<String, Integer> roomMapping = new HashMap<String, Integer>();

    public int[] TURN_IN_BED_ACTIVITY_LIST = { 30, 31 };

    public int TAKE_OFF_SHOES_ACTIVITY = 27;

    public int[] FOOD_ACTIVITIES_CHECK = { 2, 6 };

    public int[] FAKE_FOOD_ACTIVITY_LIST = {3 , 7};
    public int[] REAL_FOOD_ACTIVITY_LIST = {4, 8};
    public static enum activityGroups {
        KITCHEN,
        COUCH,
        BATHROOM,
        BEDROOM,
        STUDY,
        SINK,
    };

    Helper() {
        roomMapping.put("KITCHEN", 1);
        roomMapping.put("COUCH", 2);
        roomMapping.put("BATHROOM", 3);
        roomMapping.put("BEDROOM", 4);
        roomMapping.put("STUDY", 5);
        roomMapping.put("SINK", 6);
    }
}
