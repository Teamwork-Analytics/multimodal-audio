package com.monash.analytics.audio.service;

import com.monash.analytics.audio.service.mixer.JasioMixer;
import com.monash.analytics.audio.service.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

import static com.monash.analytics.audio.service.utils.DateTime.getDateTimeForFile;

@Service
public class WindowsAudioServiceAPI implements AudioServiceAPI {
    private final JasioMixer jasioMixer;
    private final Map<Integer, String> inputChannels;

    @Autowired
    public WindowsAudioServiceAPI() throws Exception {
        this.jasioMixer = new JasioMixer();
        inputChannels = new HashMap<>();
        init();
    }

    private void init() throws Exception {
        //FIXME: hard-coded
        inputChannels.put(0, "RED"); // channel 1
        inputChannels.put(2, "BLUE"); // channel 3
        inputChannels.put(4, "GREEN"); // channel 5
        inputChannels.put(6, "YELLOW"); // channel 7
        inputChannels.put(8, "WHITE"); // channel 9
        inputChannels.put(9, "BLACK"); // channel 10
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
    public void selectAChannel(int channelIndex, String channelName) throws Exception {
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
    public void shutdown() throws Exception {
        jasioMixer.shutdown();
        System.out.println("Audio system has stopped.");
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
