package com.monash.analytics.audio.service.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date Time generators
 */
public class DateTime {
    /**
     * Date time for file/directory name
     * @return dd-MMM-yyyy_HH-mm-ss format
     */
    public static String getDateTimeForFile(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss-SSS");
        return formatter.format(new Date());
    }

    /**
     * Date time for a transcription text (to be printed later)
     * @return dd-MMM-yyyy HH:mm:ss.SSS (until milliseconds)
     */
    public static String getDateTimeForTranscription(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
        return formatter.format(new Date());
    }
}
