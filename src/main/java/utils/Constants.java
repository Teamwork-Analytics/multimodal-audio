package utils;

import javax.sound.sampled.AudioFileFormat;

public class Constants {
    public static String APP_NAME = "Multimodal Audio Mixer";

    public static AudioFileFormat.Type AUDIO_FILE_TYPE = AudioFileFormat.Type.WAVE;
    /**
     * The session name
     * FIXME: create a GUI to insert session name
     */
    public static final String SESSION_NAME = "TESTING_SESSION";

    public static final String DIR_NAME = "recording-data/";

    public static final float SAMPLE_RATE = 44100; // 44100 Hz

    public static final int BIT_DEPTH=16; //16 bit

    public static final String LANGUAGE_CODE = "en-AU";

    public static final int BACKUP_PERIOD = 5 * 60 * 1000; // every 5 minutes
}
