package microphone;

import enums.MicrophoneState;
import exceptions.TDLUnavailableException;
import microphone.channels.ChannelDivider;
import utils.Display;

import javax.sound.sampled.AudioInputStream;

import java.io.IOException;
import java.util.ArrayList;

import static enums.MicrophoneState.CLOSED;

public class MicBuffer implements Runnable{
    private final Microphone microphone;
    public MicBuffer(Microphone microphone){
        this.microphone = microphone;
    }
    private void queueDataToChannel(byte[] audioBytes, int numberOfChannels){
        // split one audio data into its selected channels
        ChannelDivider channelDivider = new ChannelDivider(audioBytes, numberOfChannels);
        ArrayList<byte[]> channelBytes = channelDivider.extract16BitsSingleChannels();
        microphone.getChannelManager().queueDataByChannels(channelBytes);
    }

    @Override
    public void run() {
        // open the microphone if it has not been opened yet
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

                // Try to read numBytes bytes from the microphone.
                while ((audio.read(audioBytes)) != -1 & !microphone.getState().equals(CLOSED)) {
                    queueDataToChannel(audioBytes, numberOfChannels);
                }
            } catch (TDLUnavailableException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
