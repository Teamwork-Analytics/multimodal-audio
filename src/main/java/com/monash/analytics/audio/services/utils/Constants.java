package com.monash.analytics.audio.services.utils;

import javax.sound.sampled.AudioFileFormat;

public class Constants {
    public static String APP_NAME = "Multimodal Audio Mixer";

    /**
     * By default, store audio data to
     */
    public static AudioFileFormat.Type AUDIO_FILE_TYPE = AudioFileFormat.Type.WAVE;
    /**
     * The session name
     * FIXME: create a GUI to insert session name
     */
    public static String SESSION_ID = "DEFAULT";
    /**
     * The session time when it is started.
     */
    public static String SESSION_TIME = "DEFAULT_TIME";

    /**
     *
     */
    public static String SESSION_TYPE = ""; // simulation is "", baseline is "baseline"

    public static String ROOT_PATH = "C:\\develop"; // System.getProperty("user.dir")

    /**
     * Default directory name to record all of data locally
     */
    public static final String DIR_NAME = "saved_data/";

    /**
     * For the audio.microphone, default 44.1kHz sample rate
     */
    public static final float SAMPLE_RATE = 44100; // 44100 Hz

    /**
     * For the audio.microphone, default 16 bit depth
     */
    public static final int BIT_DEPTH = 16; //16 bit

    /**
     * Using English Australia
     */
    public static final String LANGUAGE_CODE = "en-AU";

    /**
     *
     */
    public static final int BACKUP_PERIOD = 5 * 60 * 1000; // every 5 minutes

    /**
     * Allow the playback with the default speaker
     */
    public static boolean SPEAKER_OUT = false;

    public static String AUDIO_DRIVER_BAND_NAME = "Focusrite"; //This is for Focusrite Audio Interface i.e. - Focusrite Scrallet
    //public static String AUDIO_DRIVER_BAND_NAME = "TASCAM"; //This is for TASCAM Audio Systems (May need to double check)
}
