package com.monash.analytics.audio.services.mixer;

import com.monash.analytics.audio.services.enums.MicrophoneState;
import com.monash.analytics.audio.services.microphone.ChannelManager;
import com.monash.analytics.audio.services.utils.Constants;
import com.monash.analytics.audio.services.writers.AudioMicrophoneFileWriter;
import com.monash.analytics.audio.services.writers.MultimodalFileWriter;
import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioDriverListener;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

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

    /**
     * This method initalises the JasioMixer with the ASIO Driver that corresponds to the
     * Interface that will be used for the capture of audio for the system
     * DISCLAIMER - For this to work properly YOU MUST install the ASIO driver for the interface
     * You need to ensure that the audio device is connected and isn't used by any additional applications otherwise
     * You will get a Cannot open (AUDIO INTERFACE DRIVER NAME) ASIO. (Error code: 0x54f) i.e.Focusrite USB ASIO.
     * You may need to uninstall the base interface application installed with the driver.
     */
    public void init(Map<Integer, String> inputChannelIds) throws Exception {
        if (asioDriver == null) {
            boolean foundAudioInterface = false;
            System.out.println("\n===================================================");
            System.out.println("List of ASIO Drivers found on this Computer:");
            System.out.println("===================================================");
            for (String driverName : driverNameList) {
                asioDriver = AsioDriver.getDriver(driverName);
                System.out.println("ASIO Driver Name: " + asioDriver.getName());
                System.out.println("Number of Channel Inputs: " + asioDriver.getNumChannelsInput());
                System.out.println("---------------------------------------------------");
                //Check to see if there ASIO driver has the correct amout of ports and the Driver Name matches the ASIO driver that was found
                if (isAudioInterface(asioDriver, inputChannelIds)
                        && detectAudioInterfaces(Constants.AUDIO_DRIVER_BAND_NAME) ) {
                    foundAudioInterface = true;
                    System.out.println("ASIO Driver Selected: "+ asioDriver.getName());
                    System.out.println("---------------------------------------------------\n");
                    registerChannels(inputChannelIds); // register channels
                    sampleRate = asioDriver.getSampleRate();
                    bufferSize = asioDriver.getBufferPreferredSize();
                    //Activate these channels and assign this class as the listener
                    asioDriver.addAsioDriverListener(this);
                    asioDriver.createBuffers(asioChannels);
                    status = MicrophoneState.OPENED;
                    asioDriver.start();
                    break;
                }
            }
            if (!foundAudioInterface) {
                System.out.println("\u001B[33m"+ "\nNo ASIO driver was found that matches the Excepted Number of Input " +
                        "Devices. Refer to: " +"\u001B[31m"+ "SimulationAudioService.java"+ "\u001B[33m"+
                        " for InputChannel count (Input Devices)\nPlease also ensure that the "+ "\u001B[31m" +
                        "Constants.java variable AUDIO_DRIVER_BAND_NAME" +"\u001B[33m"+ " contains the ASIO Driver Name "+
                        "used by the Audio Inteface\nIf you can only see Realtek ASIO above it means the driver for the " +
                        "Audio Inteface's ASIO Driver hasn't been installed yet.\n");
                throw new Exception("No audio interface and/or ASIO driver is found. Please ensure you have connected"+
                    " an audio interface and restart the application. Please see JasioMixer.Java for details. "+
                        "Please also check the above console log");
            }
        } else {
            throw new Exception("The ASIO driver is already running.");
        }
        }

    private boolean isAudioInterface(AsioDriver driver, Map<Integer, String> inputChannelIds ) {
        //Check to see if the Audio Driver contains a greater or equal number of excepted input channel for the Interface
        return driver.getNumChannelsInput() >= inputChannelIds.size();
    }

    public boolean detectAudioInterfaces(String BrandNameOfInterface) {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            // Check the system to see if the Audio Interface Device is connected.
            if (mixerInfo.getName().toString().contains(BrandNameOfInterface)) {
                System.out.println("===================================================");
                System.out.println("Detected Audio Interface: " + mixerInfo.getName());
                System.out.println("===================================================");
                return true;
            }
        }
        System.out.println("==============================================================");
        return false;
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

    public void stop(Integer channelId, String customName){
        this.save(channelId, customName);
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

    private void save(int index, String customChannelName){
        if (asioDriver != null) {
            AudioFormat audioFormat = new AudioFormat(
                    (float) sampleRate,
                    Constants.BIT_DEPTH,
                    1,
                    true,
                    false);
            channelManager.saveSingleChannel(audioFormat,index, customChannelName);
            this.saveCombinedChannels(audioFormat);
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
