package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTime {
    public static String getDateTimeForFile(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss");
        return formatter.format(new Date());
    }

    public static String getDateTimeForTranscription(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
        return formatter.format(new Date());
    }
}
