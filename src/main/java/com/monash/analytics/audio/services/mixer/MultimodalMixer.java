package com.monash.analytics.audio.services.mixer;
import com.monash.analytics.audio.services.exceptions.MicException;
import com.monash.analytics.audio.services.exceptions.MixerException;
import com.monash.analytics.audio.services.utils.Constants;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.util.*;

/**
 * The multimodal audio audio.mixer that can only detect audio.microphone (target data line) inputs
 * @author Riordan Dervin Alfredo (riordan.alfredo@gmail.com)
 */
@Component
public final class MultimodalMixer {

    /**
     * Singleton instance
     */
    private static MultimodalMixer instance;

    /**
     * A hashmap that contains audio.mixer name and audio.mixer object
     */
    private final HashMap<String, Mixer> allMicrophones = new HashMap<>();

    /**
     * Also called as audio.microphone name
     */
    private String selectedMixerName = "Audio Interface";

    /**
     * The audio.microphone audio.mixer
     */
    private MicrophoneMixer microphoneMixer;

    /**
     * Constructor
     */
    public MultimodalMixer(){
        this.initMixerInputs();
    }

    /**
     * Singleton to get instance
     * @return only one instance of this audio.mixer
     */
    public static MultimodalMixer getInstance(){
        if(instance == null){
            instance = new MultimodalMixer();
        }
        return instance;
    }

    /**
     *
     */
    public void initInstantMicrophone(){
        this.microphoneMixer = new MicrophoneMixer(this, true);
    }

    /**
     * Initialise audio.mixer and collect all audio inputs sources
     */
    public void initMixerInputs() {
        allMicrophones.clear();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo(); // get all mixers data
//        MixerInfo.printMixerInfo();
        for(Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            String name = info.getName();

            if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                //Only prints out info is it is a TargetDataLine type (audio input)
                allMicrophones.put(name, mixer);
            }
        }
    }

    /**
     * Getting all name of microphones that are connected to the audio.mixer.
     * @return a list of microphones names
     */
    public ArrayList<String> getAllMicNames(){
        return new ArrayList<>(allMicrophones.keySet());
    }

    /**
     * Choose one audio.mixer input
     * @param mixerName the name of audio.mixer
     * @return true/false if the system can find the audio.mixer or not respectively
     */
    public boolean selectAMixer(String mixerName){
        if(selectedMixerName != null && selectedMixerName.equals(mixerName)) return false;
        selectedMixerName = mixerName;
        this.microphoneMixer = new MicrophoneMixer(this);
        return true;
    }

    /**
     * Name getter of current active audio.mixer
     * @return a selected audio.mixer name
     */
    public String getSelectedMixerName(){
        String mixerName = "Audio Interface";
        if(selectedMixerName != null) mixerName = selectedMixerName;
        return mixerName;
    }

    /**
     * Delete a audio.microphone object from the audio.mixer
     */
    public void closeMicrophoneMixer() {
        this.selectedMixerName = null;
        this.microphoneMixer = null; // close both audio.microphone audio.mixer & its channel
    }

    /**
     * Get a audio.mixer with name as its key
     * TODO: privacy leaks?
     * @param mixerName the name of audio.mixer (key)
     * @return the Mixer object
     */
    public Mixer getMixerInstance(String mixerName){
        Mixer selectedMixer = allMicrophones.get(mixerName);
        if(selectedMixer == null) {System.out.println("Could not find the audio.mixer!");}
        return selectedMixer;
    }

    /**
     * Return a audio.microphone audio.mixer object
     * @return Microphone Mixer instance (mutable)
     * @throws MixerException if audio.microphone has not been selected yet
     */
    private MicrophoneMixer getMicrophoneMixer() throws MixerException {
        if(microphoneMixer ==null) throw new MixerException("Microphone has not been selected yet");
        return microphoneMixer;
    }

    /**
     * Start recording
     */
    public void record(String sessionName) throws MixerException, MicException{
        Constants.SESSION_ID = sessionName; // update global session name
        getMicrophoneMixer().listen();
    }

    /**
     * Pause recording & store files (audio and text)
     */
    public void pause() throws MixerException{
        getMicrophoneMixer().stop();
    }


    /**
     * Get list of all available channels
     * @return map of channel id & channel name
     */
    public Map<Integer, StringBuffer> getAllChannels(){
        try {
            return getMicrophoneMixer().getAllChannels();
        } catch (MixerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Select/Unselect a channel
     * @param isSelected boolean to select(true)/unselect(false) a channel
     * @param id channel id to register
     * @param name channel name to register name
     */
    public void checkChannel(boolean isSelected, int id, String name){
        try{
            getMicrophoneMixer().flickChannel(isSelected,id, name);
        } catch (MixerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check to use transcription API (Speech-to-text)
     * @param isSelected boolean
     */
    public void checkIsTranscribing(boolean isSelected){
        try{
            getMicrophoneMixer().setUsingTranscriptionAPI(isSelected);
        } catch (MixerException e) {
            e.printStackTrace();
        }
    }

    public void checkIsSpeakerOut(boolean isSelected){
        Constants.SPEAKER_OUT = isSelected;
    }
}
