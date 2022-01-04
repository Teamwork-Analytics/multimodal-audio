package com.monash.analytics.audio.service.mixer;

import com.monash.analytics.audio.service.enums.MicrophoneState;
import com.monash.analytics.audio.service.microphone.ChannelManager;
import com.monash.analytics.audio.service.utils.Constants;
import com.monash.analytics.audio.service.writers.AudioMicrophoneFileWriter;
import com.monash.analytics.audio.service.writers.MultimodalFileWriter;
import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JasioMixer implements AsioDriverListener {
    private final List<String> driverNameList;
    private final Set<AsioChannel> asioChannels;
    private AsioDriver asioDriver;
    private int bufferSize;
    private double sampleRate;
    private MicrophoneState status;
    private ChannelManager channelManager;
    private ByteArrayOutputStream combinedAudioBaos;

    public JasioMixer() {
        driverNameList = AsioDriver.getDriverNames();
        asioChannels = new HashSet<>();
        combinedAudioBaos = new ByteArrayOutputStream();
    }

    public void init(Map<Integer, String> inputChannelIds) throws Exception {
        if (asioDriver == null) {
            asioDriver = AsioDriver.getDriver(driverNameList.get(0)); // get the first one, i.e. ASIO4ALL v2
            registerChannels(inputChannelIds); // register channels
            sampleRate = asioDriver.getSampleRate();
            bufferSize = asioDriver.getBufferPreferredSize();
            //Activate these channels and assign this class as the listener
            asioDriver.addAsioDriverListener(this);
            asioDriver.createBuffers(asioChannels);
            status = MicrophoneState.OPENED;
            asioDriver.start();
        } else {
            throw new Exception("The ASIO driver is not running.");
        }
    }

    private void registerChannels(Map<Integer, String> inputChannelIds) throws IllegalArgumentException {
        if (asioDriver != null) {
            //Assertions
            if (inputChannelIds.size() > asioDriver.getNumChannelsInput())
                throw new IllegalArgumentException("Exceeding number of inputs in available channels! Have you turned ON the audio interface?");
            if (inputChannelIds.isEmpty()) throw new IllegalArgumentException("Cannot pass an empty number of inputs");
            //input
            for (Integer id : inputChannelIds.keySet()) {
                asioChannels.add(asioDriver.getChannelInput(id));
            }
            //output
            int outputChannelCount = asioDriver.getNumChannelsOutput();
            for (int i = 0; i < outputChannelCount; i++) {
                asioChannels.add(asioDriver.getChannelOutput(i));
            }
            channelManager = new ChannelManager(asioDriver.getNumChannelsInput());
            channelManager.generateChannels(inputChannelIds);
        }
    }

    public void stop() {
        this.save(); // save audio to file
        status = MicrophoneState.CLOSED;
        channelManager.reset();
        combinedAudioBaos = new ByteArrayOutputStream();
    }

    public void start() throws Exception {
        if (asioChannels.isEmpty()) throw new Exception(" AsioChannels are empty.");
        if (asioDriver == null) throw new NullPointerException("Missing ASIO driver. Please restart the audio service");
        if (channelManager == null) throw new NullPointerException(" Missing channel manager.");
        status = MicrophoneState.LISTENING;
    }

    public void shutdown() throws Exception {
        if (asioDriver != null) {
            asioDriver.shutdownAndUnloadDriver();
            asioChannels.clear();
            asioDriver = null;
            channelManager = null;
            status = MicrophoneState.CLOSED;
        } else {
            throw new Exception("The ASIO driver is not running.");
        }
    }

    private void save() {
        if (asioDriver != null) {
            AudioFormat audioFormat = new AudioFormat(
                    (float) sampleRate,
                    Constants.BIT_DEPTH,
                    asioDriver.getNumChannelsInput(),
                    true,
                    false);
            channelManager.saveAllChannelAudio(audioFormat);
            this.saveCombinedChannels(audioFormat);
        }
    }

    private void saveCombinedChannels(AudioFormat audioFormat) {
        MultimodalFileWriter fileWriter = new AudioMicrophoneFileWriter("allchannels", audioFormat, combinedAudioBaos);
        fileWriter.saveFile();
    }


    @Override
    public void sampleRateDidChange(double sampleRate) {
        System.out.println("sampleRateDidChange() callback received.");
    }

    @Override
    public void resetRequest() {
        System.out.println("resetRequest() callback received. Returning driver to INITIALIZED state.");

//        /*
//         * This thread will attempt to shut down the ASIO driver. However, it will
//         * block on the AsioDriver object at least until the current method has returned.
//         */
//        new Thread(() -> {
//            System.out.println("resetRequest() callback received. Returning driver to INITIALIZED state.");
//            asioDriver.returnToState(AsioDriverState.INITIALIZED);
//        }).start();
    }

    @Override
    public void resyncRequest() {
        System.out.println("resyncRequest() callback received.");
    }

    @Override
    public void bufferSizeChanged(int bufferSize) {
        System.out.println("bufferSizeChanged() callback received.");
    }

    @Override
    public void latenciesChanged(int inputLatency, int outputLatency) {
        System.out.println("latenciesChanged() callback received.");
    }

    @Override
    public void bufferSwitch(long sampleTime, long samplePosition, Set<AsioChannel> activeChannels) {
        final float[] outputFloatArray = new float[bufferSize]; // all channels
        // Microphone in
        for (AsioChannel activeChannel : activeChannels) {
            if (activeChannel.isInput()) {
                final float[] inputArray = new float[bufferSize];
                // this is one buffer data
                for (int i = 0; i < bufferSize; i++) {
                    final float val = ((float) activeChannel.getByteBuffer().getInt()) / Integer.MAX_VALUE;
                    outputFloatArray[i] += val;
                    inputArray[i] += val;
                }
                if (status.equals(MicrophoneState.LISTENING)) {
                    // convert to byte & store to each channel data.
                    final byte[] inputBuffer = new byte[outputFloatArray.length * 2];
                    writeFloat2Byte(inputArray, inputBuffer);
                    channelManager.getChannel(activeChannel.getChannelIndex()).storeToBaos(inputBuffer);
                }
            }
        }
        if (status.equals(MicrophoneState.LISTENING)) {
            // Record all channels audio
            final byte[] outputBuffer = new byte[outputFloatArray.length * 2];
            writeFloat2Byte(outputFloatArray, outputBuffer);
            combinedAudioBaos.write(outputBuffer, 0, outputBuffer.length);
        }
        for (AsioChannel activeChannel : activeChannels) {
            if (!activeChannel.isInput()) {
                activeChannel.write(outputFloatArray);
            }
        }
    }

    /**
     * A complex byte to write float array to byte array.
     *
     * @param byteBuffer  buffer array
     * @param outputArray float output array from one input channel
     */
    private void writeFloat2Byte(float[] outputArray, byte[] byteBuffer) {
        int bufferIndex = 0;
        for (int i = 0; i < byteBuffer.length; i++) {
            final int x = (int) (outputArray[bufferIndex++] * 32767.0);
            byteBuffer[i++] = (byte) x;
            byteBuffer[i] = (byte) (x >>> 8);
        }
    }
}
