package com.monash.analytics.audio.service;

import com.monash.analytics.audio.service.exceptions.MixerException;
import com.monash.analytics.audio.service.mixer.MultimodalMixer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Audio web service implementation
 */
//@Service
public class LinuxAudioServiceAPI implements AudioServiceAPI{

    private final MultimodalMixer multimodalMixer;
    private boolean isInitialised = false;
    private final Map<Integer, String> selectedChannels;
    private final boolean isUsingPlayback = true;  //  using the play back (play the live-audio with the speaker)
    private final boolean isUsingTranscription = false; //   using the Speech API transcription

//    @Autowired
    public LinuxAudioServiceAPI(){
        this.multimodalMixer = new MultimodalMixer();
        this.selectedChannels = new HashMap<>();
        selectedChannels.put(0,"channel 1");
        selectedChannels.put(8,"channel 9");
    }

    /**
     * Init the audio (set-up)
     */
    private void initialiseAudio() throws MixerException {
        //TODO: hard-coded channels.
        multimodalMixer.initInstantMicrophone();
        selectedChannels.forEach((id,channelName) -> {
            System.out.println("listening to " + channelName);
            multimodalMixer.checkChannel(true, id, channelName);
        });
        multimodalMixer.checkIsSpeakerOut(isUsingPlayback);
        multimodalMixer.checkIsTranscribing(isUsingTranscription);
        isInitialised = true;
    }

    /**
     * Set a channel with id & name (new and update)
     * @param channelNumber integer index in a list: 0 is channel 1, and 1 is channel 2, and so on
     * @param channelName a String custom channel name if not provided
     */
    @Override
    public void selectAChannel(int channelNumber, String channelName) throws Exception {
        if(channelName.isEmpty()) throw new Exception("Channel name must not be empty");
        this.selectedChannels.put(channelNumber, channelName);
    }

    /**
     * Start recording
     * @throws Exception if it has not been initialised
     */
    @Override
    public void startRecording(String sessionId, String sessionType) throws Exception {
        //TODO: sessionType is not defined yet.
        if(!isInitialised) initialiseAudio();
        String sessionName = "session_"+sessionId;
        multimodalMixer.record(sessionName);
    }

    /**
     * Stop recording
     * @throws Exception if it has not been initialised
     */
    @Override
    public void stopRecording() throws Exception {
        if(!isInitialised) throw new Exception("Audio has not started!");
        multimodalMixer.pause();
    }

    @Override
    public void shutdown() throws Exception {
        //TODO: shutdown method.
    }
}
