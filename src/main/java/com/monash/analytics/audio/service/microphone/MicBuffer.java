package com.monash.analytics.audio.service.microphone;

import com.monash.analytics.audio.service.enums.MicrophoneState;
import com.monash.analytics.audio.service.exceptions.TDLUnavailableException;
import com.monash.analytics.audio.service.microphone.channels.ChannelDivider;
import com.monash.analytics.audio.service.utils.Display;

import javax.sound.sampled.AudioInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A thread to process audio (wave) to byte data.
 */
public class MicBuffer implements Runnable{
    /**
     * The main microphone that has been selected
     */
    private final Microphone microphone;

    /**
     * Constructor
     * @param microphone injected Microphone for further usage
     */
    public MicBuffer(Microphone microphone){
        this.microphone = microphone;
    }

    /**
     * Queue data and split data into the chosen number of channels
     * @param audioBytes full audio data
     * @param numberOfChannels number of channels as its divisor
     */
    private void queueDataToChannel(byte[] audioBytes, int numberOfChannels){
        // split one audio data into its selected channels
        ChannelDivider channelDivider = new ChannelDivider(audioBytes, numberOfChannels);
        ArrayList<byte[]> channelBytes = channelDivider.extract16BitsSingleChannels();
        microphone.getChannelManager().queueDataByChannels(channelBytes);
    }

    @Override
    public void run() {
        // open the audio.microphone if it has not been opened yet
        if(!microphone.getState().equals(MicrophoneState.OPENED)) {
            try {
                microphone.open();
                Display.println(microphone.getMicName() + ": listening...");
                int numberOfChannels = microphone.getChannelManager().getMaxNumOfChannel();
                AudioInputStream audio = new AudioInputStream(microphone.getTargetDataLine());
                int bytesPerFrame = audio.getFormat().getFrameSize();

                // Set an arbitrary buffer size of 1024 frames.
                int numBytes = 1024 * bytesPerFrame;
                byte[] audioBytes = new byte[numBytes];

                // Try to read numBytes bytes from the audio.microphone.
                while ((audio.read(audioBytes)) != -1 & !microphone.getState().equals(MicrophoneState.CLOSED)) {
                    queueDataToChannel(audioBytes, numberOfChannels);
                }
            } catch (TDLUnavailableException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
