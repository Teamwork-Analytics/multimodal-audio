package mixer;
import exceptions.MicException;
import exceptions.MixerException;
import utils.Display;

import javax.sound.sampled.*;
import java.util.*;

/**
 * The multimodal audio mixer that can only detect microphone (target data line) inputs
 * @author Riordan Dervin Alfredo (riordan.alfredo@gmail.com)
 */
public final class MultimodalMixer {

    /**
     * Singleton instance
     */
    private static MultimodalMixer instance;

    /**
     * A hashmap that contains mixer name and mixer object
     */
    private final HashMap<String, Mixer> allMicrophones = new HashMap<>();

    /**
     * Also called as microphone name
     */
    private String selectedMixerName;

    /**
     *
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
     * @return only one instance of this mixer
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
     * Initialise mixer and collect all audio inputs sources
     */
    public void initMixerInputs() {
        allMicrophones.clear();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo(); // get all mixers data
//      printMixerInfo();
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
     * Getting all name of microphones that are connected to the mixer.
     * @return a list of microphones names
     */
    public ArrayList<String> getAllMicNames(){
        return new ArrayList<>(allMicrophones.keySet());
    }

    /**
     * Choose one mixer input
     * @param mixerName the name of mixer
     * @return true/false if the system can find the mixer or not respectively
     */
    public boolean selectAMixer(String mixerName){
        if(!allMicrophones.containsKey(mixerName)) return false;
        selectedMixerName = mixerName;
        this.microphoneMixer = new MicrophoneMixer(this);
        return true;
    }

    /**
     * Name getter of current active mixer
     * @return a selected mixer name
     */
    public String getSelectedMixerName(){
        String mixerName = "Plug-and-play mode";
        if(selectedMixerName != null) mixerName = selectedMixerName;
        return mixerName;
    }

    /**
     * Delete a microphone object from the mixer
     */
    public void closeMicrophoneMixer() {
        this.microphoneMixer = null; // close both microphone mixer & its channel
    }

    /**
     * Get a mixer with name as its key
     * TODO: privacy leaks?
     * @param mixerName the name of mixer (key)
     * @return the Mixer object
     */
    public Mixer getMixerInstance(String mixerName){
        Mixer selectedMixer = allMicrophones.get(mixerName);
        if(selectedMixer == null) {System.out.println("Could not find the mixer!");}
        return selectedMixer;
    }

    /**
     * Return a microphone mixer object
     * @return Microphone Mixer instance (mutable)
     * @throws MixerException if microphone has not been selected yet
     */
    private MicrophoneMixer getMicrophoneMixer() throws MixerException {
        if(microphoneMixer ==null) throw new MixerException("Microphone has not been selected yet");
        return microphoneMixer;
    }

    /**
     * Start recording
     */
    public boolean record() {
        try {
            getMicrophoneMixer().listen();
            return true;
        } catch (MixerException | MicException e){
            Display.println(e.getMessage());
            return false;
        }
    }

    /**
     * Pause recording & store files (audio and text)
     */
    public void pause(){
        try {
            getMicrophoneMixer().stop();
        } catch (MixerException e) {
            e.printStackTrace();
        }
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
            getMicrophoneMixer().checkChannel(isSelected,id, name);
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
}
