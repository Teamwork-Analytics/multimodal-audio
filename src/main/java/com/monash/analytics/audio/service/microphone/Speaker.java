package com.monash.analytics.audio.service.microphone;
import com.monash.analytics.audio.service.utils.Constants;
import javax.sound.sampled.*;

import static com.monash.analytics.audio.service.utils.Constants.SPEAKER_OUT;

/**
 * The source data line (audio out through default speaker)
 */
public class Speaker {
    /**
     * The main source data line
     */
    private final SourceDataLine sdl;

    /**
     * Constructor
     * @throws LineUnavailableException when the speaker cannot be created
     */
    public Speaker() throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, Constants.SAMPLE_RATE, Constants.BIT_DEPTH, 1, 2, Constants.SAMPLE_RATE, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        sdl = (SourceDataLine) AudioSystem.getLine(info);
        this.open();
    }

    /**
     * Open the speaker
     * TODO: Remove static variable!
     * @throws LineUnavailableException when the SDL has not been initiated
     */
    public void open() throws LineUnavailableException {
        if(SPEAKER_OUT){
            sdl.open();
            sdl.start();
        }
    }

    /**
     * Close the speaker to reduce memory consumption
     * @throws LineUnavailableException when the SDL has not been initiated
     */
    public void close() throws LineUnavailableException {
        if(SPEAKER_OUT){
            sdl.close();
        }
    }

    /**
     * Basically, translate byte data to sound in the speaker.
     * @param data the byte audio data from target data line/ microphone
     */
    public void write(byte[] data){
        if(SPEAKER_OUT){
            sdl.write(data, 0, data.length);
        }
    }
}