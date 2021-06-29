package writers;

import utils.Display;

import java.io.File;
import static utils.Constants.DIR_NAME;
import static utils.Constants.SESSION_NAME;
import static utils.DateTime.getDateTimeForFile;

public abstract class MultimodalFileWriter {

    /**
     * current session time in string
     */
    protected final String fileName;
    /**
     * Microphone name
     */
    protected final String channelName;

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
    public MultimodalFileWriter (String channelName){
        this.channelName = channelName;
        this.subDirName = "";
        String sessionTime = getDateTimeForFile();
        this.fileName = String.format("%s_%s", channelName,sessionTime);
    }

    /**
     * Create path from directory to file (complete with its format)
     * @return path string
     */
    protected String generatePath(){
        return generateDirPath() + "/" + generateFileFormat();
    }

    /**
     * Generate the path as a file to be stored
     * @return the file object
     */
    public File getFile(){
        return new File(generatePath());
    }

    /**
     *
     * @return a string of directory path
     */
    protected String generateDirPath(){
        String path = System.getProperty("user.dir") + "/" + DIR_NAME; // get current path + DIR_NAME
        path += SESSION_NAME + "/" + subDirName + "/" + channelName;
        File dir = new File(path);
        if(dir.mkdirs()) {
            Display.println("New directory path has been created:" + path);
        }
        return path ;
    }

    protected abstract String generateFileFormat();

    public abstract void saveFile();

}
