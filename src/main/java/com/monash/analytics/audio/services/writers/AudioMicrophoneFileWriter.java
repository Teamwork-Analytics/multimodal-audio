package com.monash.analytics.audio.services.writers;

import com.monash.analytics.audio.services.utils.Display;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static com.monash.analytics.audio.services.utils.Constants.AUDIO_FILE_TYPE;
import static com.monash.analytics.audio.services.utils.Constants.SESSION_TYPE;

public class AudioMicrophoneFileWriter extends MultimodalFileWriter {

    /**
     * Audio output format
     */
    private final AudioFormat outputFormat;

    /**
     * Audio data
     */
    private final ByteArrayOutputStream baos;

    private int bufferLength;

    /**
     * Constructor
     *
     * @param channelName channel file name
     * @see MultimodalFileWriter directory structure
     */
    public AudioMicrophoneFileWriter(String channelName, AudioFormat inputFormat, ByteArrayOutputStream baos) {
        super(channelName);
        this.baos = baos;
        this.outputFormat = new AudioFormat(inputFormat.getSampleRate(), inputFormat.getSampleSizeInBits(), 1, true, false);
        this.bufferLength = baos.toByteArray().length / outputFormat.getFrameSize();
// FIXME: Use this if we are using MacOS or Linux (with JavaSound).
//  this.outputFormat =
//                new AudioFormat(inputFormat.getEncoding(),
//                        inputFormat.getSampleRate(),
//                        inputFormat.getSampleSizeInBits(),
//                        1,
//                        inputFormat.getFrameSize() / maxChannelSize,
//                        inputFormat.getFrameRate(),
//                        inputFormat.isBigEndian());
    }

    @Override
    protected String generateFileFormat() {
        String audioFormatString = "wav";
        String appendedFileName = String.format("%s_%s",SESSION_TYPE, fileName);
        return String.format("%s_audio.%s", appendedFileName, audioFormatString);
    }

    @Override
    public void saveFile() {
        try {
            byte[] byteData = baos.toByteArray();
            File micTargetFile = this.getFile();
            ByteArrayInputStream micBais = new ByteArrayInputStream(byteData);
            AudioInputStream outputAIS =
                    new AudioInputStream(micBais,
                            outputFormat, bufferLength);
            AudioSystem.write(outputAIS, AUDIO_FILE_TYPE, micTargetFile);
            Display.println("Saving audio data at:" + generatePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
