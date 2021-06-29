package writers;

import utils.Display;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static utils.Constants.AUDIO_FILE_TYPE;

public class AudioMicrophoneFileWriter extends MultimodalFileWriter {

    /**
     * Audio output format
     */
    private final AudioFormat outputFormat;

    /**
     * Audio data
     */
    private final ByteArrayOutputStream baos;

    /**
     * Constructor
     * @see MultimodalFileWriter directory structure
     * @param channelName channel file name
     */
    public AudioMicrophoneFileWriter(String channelName, Integer maxChannelSize, AudioFormat inputFormat, ByteArrayOutputStream baos){
        super(channelName) ;
        this.baos = baos;
        this.subDirName = "audio";
        this.outputFormat =
                new AudioFormat(inputFormat.getEncoding(),
                        inputFormat.getSampleRate(),
                        inputFormat.getSampleSizeInBits(),
                        1,
                        inputFormat.getFrameSize() / maxChannelSize,
                        inputFormat.getFrameRate(),
                        inputFormat.isBigEndian());;
    }

    @Override
    protected String generateFileFormat(){
        String audioFormatString = "wav";
        return String.format("%s_audio.%s", fileName, audioFormatString);
    }

    @Override
    public void saveFile() {
        try {
            byte[] byteData = baos.toByteArray();
            File micTargetFile = this.getFile();
            ByteArrayInputStream micBais = new ByteArrayInputStream(byteData);
            AudioInputStream outputAIS =
                    new AudioInputStream(micBais,
                            outputFormat,
                            byteData.length / outputFormat.getFrameSize());
            AudioSystem.write(outputAIS, AUDIO_FILE_TYPE, micTargetFile);
            Display.println("Saving audio data at:" + generatePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
