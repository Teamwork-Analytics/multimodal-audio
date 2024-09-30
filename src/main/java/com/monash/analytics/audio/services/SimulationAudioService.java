package com.monash.analytics.audio.services;

import com.monash.analytics.audio.services.mixer.JasioMixer;
import com.monash.analytics.audio.services.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

import static com.monash.analytics.audio.services.utils.DateTime.getDateTimeForFile;

@Service("simulation")
public class SimulationAudioService implements AudioServiceAPI {
    protected final JasioMixer jasioMixer;
    protected final Map<Integer, String> inputChannels;

    @Autowired
    public SimulationAudioService() throws Exception {
        this.jasioMixer = new JasioMixer();
        inputChannels = new HashMap<>();
        init();
    }

    protected void init() throws Exception {
        //FIXME: hard-coded
        inputChannels.put(0, "BLUE"); // channel 1
        inputChannels.put(1, "RED"); // channel 2
        inputChannels.put(2, "GREEN"); // channel 3
//        inputChannels.put(3, "YELLOW"); // channel 4
//        inputChannels.put(4, "WHITE"); // channel 5
//        inputChannels.put(5, "BLACK"); // channel 6
        jasioMixer.init(inputChannels);
    }

    /**
     * Register a channel to the channel map.
     *
     * @param channelIndex integer index in a list: 0 is channel 1, and 1 is channel 2, and so on
     * @param channelName  a String custom channel name if not provided
     * @throws Exception when something went wrong with the `put` method.
     */
    @Override
    public void addChannel(int channelIndex, String channelName) throws Exception {
        inputChannels.put(channelIndex, channelName);
    }

    @Override
    public void startRecording(String sessionId, String sessionType) throws Exception {
        Constants.SESSION_ID = sessionId; // update global session name
        Constants.SESSION_TIME = getDateTimeForFile();
        Constants.SESSION_TYPE = sessionType;
        jasioMixer.start();
        System.out.println("Audio starts recording...");
    }

    @Override
    public void stopRecording() throws Exception {
        jasioMixer.stop();
        Constants.SESSION_TIME = getDateTimeForFile();
        System.out.println("Audio has stopped recording.");
    }

    @Override
    public void stopRecording(Integer index, String customName) throws Exception {
        jasioMixer.stop(index, customName);
        Constants.SESSION_TIME = getDateTimeForFile();
        int printedChannel = index +1;
        System.out.println("Audio has stopped recording. Saving audio for a single channel " + printedChannel );
    }

    @Override
    public void shutdown() throws Exception {
        jasioMixer.shutdown();
        System.out.println("Audio system has stopped.");
    }

    @Override
    public String getServiceName() {
        return "simulation";
    }

    @PreDestroy
    public void onExit() {
        try {
            this.shutdown();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Unable to close the audio application properly! You must restart your PC.");
        }
    }
}
