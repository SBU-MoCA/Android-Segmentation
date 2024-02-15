package com.example.myapplication;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Helper {
    public Map<String, Integer> roomMapping = new HashMap<String, Integer>();

    public Integer[] OPTIONAL_ACTIVITY_LIST = { 29 };
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
