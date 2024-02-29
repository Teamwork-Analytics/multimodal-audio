package com.monash.analytics.audio.service.writers;

import com.monash.analytics.audio.service.utils.Constants;
import com.monash.analytics.audio.service.utils.Display;

import java.io.File;

import static com.monash.analytics.audio.service.utils.Constants.SESSION_TIME;
import static com.monash.analytics.audio.service.utils.DateTime.getDateTimeForFile;

public abstract class MultimodalFileWriter {

    /**
     * current session time in string
     */
    protected final String fileName;
    /**
     * Microphone name
     */
    protected final String channelName;

    /**
     *
     */
    protected String subDirName; // text/ or audio/

    /**
     * Directory structure:
     * recording_data
     * |_ session_name
     * |--audio
     * |--- channel_1
     * |---- channel 1_dd-MMM-YYYY_HH-mm_audio.wav
     * |--- channel_2
     * |--text
     * |--- channel_1
     * |---- channel 1-dd_MMM_YYYY-HH_mm-transcript.txt
     *
     * @param channelName channel file name
     */
    public MultimodalFileWriter(String channelName) {
        this.channelName = channelName;
        this.subDirName = "";
//        String savingFileTime = getDateTimeForFile();
        this.fileName = String.format("%s_%s", channelName, SESSION_TIME);
    }

    /**
     * Create path from directory to file (complete with its format)
     *
     * @return path string
     */
    protected String generatePath() {
        return generateDirPath() + "/" + generateFileFormat();
    }

    /**
     * Generate the path as a file to be stored
     *
     * @return the file object
     */
    public File getFile() {
        return new File(generatePath());
    }

    /**
     * @return a string of directory path
     */
    protected String generateDirPath() {
        String path = Constants.ROOT_PATH + "/" + Constants.DIR_NAME;
//        String path = System.getProperty("user.dir") + "/" + Constants.DIR_NAME; // get current path + DIR_NAME
        path += Constants.SESSION_ID + "/" + subDirName;
        File dir = new File(path);
        if (dir.mkdirs()) {
            Display.println("New directory path has been created:" + path);
        }
        return path;
    }

    protected abstract String generateFileFormat();

    public abstract void saveFile();

}
