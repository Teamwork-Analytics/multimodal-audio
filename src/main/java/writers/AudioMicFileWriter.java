package writers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioMicFileWriter {
    /**
     * Audio format
     */
    private final String AUDIO_FORMAT = "wav";
    /**
     * Directory name
     */
    private final String DIR_NAME = "recording-data/";
    /**
     * The current running session name
     */
    private String sessionName = "";
    /**
     *
     */
    private final String sessionTime;
    /**
     * Microphone name
     */
    private final String micName;


    /**
     * Directory structure:
     * recording_data
     *  |_ session name
     *      |_ microphone/mixer name + date & time (hours & minutes)
     *          |_ channel_name
     *             |_ file.wav
     *          |_ file.wav (default)
     * @param sessionName
     * @param microphoneName
     */
    public AudioMicFileWriter(String sessionName, String microphoneName){
        this.sessionName = sessionName;
        this.micName = microphoneName;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy_HH-mm");
        this.sessionTime = formatter.format(new Date());
    }

    public File getAudioFile(){
        return new File(generatePath());
    }

    public File getAudioFile(String channelNumber){
        String path = generateDirPath() + generateFileFormat(channelNumber);
        return new File(path);
    }

    private String generatePath(){
        return generateDirPath() + "/" + generateFileFormat();
    }

    /**
     *
     * @return a string of directory path
     */
    private String generateDirPath(){
        String path = System.getProperty("user.dir") + "/" + DIR_NAME; // get current path + DIR_NAME
        if(!sessionName.equals("")){// if session name is not empty
            path += sessionName + "_"+ sessionTime;
        }
        File dir = new File(path);
        if(dir.mkdirs()) {
            System.out.println("New directory path has been created:" + path);
        }
        return path ;
    }

    private String generateFileFormat(){
        return String.format("%s.%s", micName, AUDIO_FORMAT);
    }

    /**
     *
     * @param channelNumber must be > 0 and already formatted ready to be printed
     * @return a string of file name
     */
    private String generateFileFormat(String channelNumber){
        return String.format("%s_channel%s.%s", micName,channelNumber, AUDIO_FORMAT);
    }


}
