package com.monash.analytics.audio.services.mixer;

import com.monash.analytics.audio.services.enums.MicrophoneState;
import com.monash.analytics.audio.services.exceptions.MicException;
import com.monash.analytics.audio.services.exceptions.MixerException;
import com.monash.analytics.audio.services.features.MicrophoneDecorator;
import com.monash.analytics.audio.services.features.speech.SpeechToTextDecorator;
import com.monash.analytics.audio.services.microphone.Microphone;
import com.monash.analytics.audio.services.utils.Constants;

import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A sub-part of main mixer that manages microphone functionalities
 */
public final class MicrophoneMixer {
    /**
     * A HashMap of checked/selected microphones
     */
    private final Microphone activeMicrophone; // mutable audio.microphone

    /**
     * The main microphone features initiator (with decorator pattern)
     */
    private MicrophoneDecorator micWrapper;

    /**
     * A sub-part of microphone mixer that controls channels
     */
    private final ChannelMixer channelMixer;

    /**
     * Back-up feature functionality
     * TODO: move this out if you have time
     */
    private Timer timer;

    /**
     * Decision to use the transcription feature
     */
    private boolean isUsingTranscriptionAPI = false;

    /**
     * Constructor
     * @param multimodalMixer the main audio.mixer
     */
    public MicrophoneMixer(MultimodalMixer multimodalMixer){
        String micName = multimodalMixer.getSelectedMixerName();
        this.activeMicrophone = new Microphone(micName, multimodalMixer.getMixerInstance(micName));
        this.micWrapper = new MicrophoneDecorator(activeMicrophone);
        this.channelMixer = new ChannelMixer(this);
        this.getMaxChannelSize(); // get the maximum number of channel from the audio.microphone
    }

    /**
     * Constructor
     * @param multimodalMixer the main audio.mixer
     */
    public MicrophoneMixer(MultimodalMixer multimodalMixer, boolean withoutMixer){
        String micName = multimodalMixer.getSelectedMixerName();
        this.activeMicrophone = new Microphone(micName);
        this.micWrapper = new MicrophoneDecorator(activeMicrophone);
        this.channelMixer = new ChannelMixer(this);
        this.getMaxChannelSize(); // get the maximum number of channel from the audio.microphone
    }

    /**
     * Stop listening from a audio.microphone
     */
    public void stop(){
        if(activeMicrophone.getState().equals(MicrophoneState.OPENED)){
            activeMicrophone.close();
            micWrapper.save(); // save text & audio
            timer.cancel();
            timer.purge();
        }
    }

    /**
     * Transcribe audio to text file
     */
    public void listen() throws MixerException, MicException {
        if(channelMixer.isEmpty()) throw new MixerException("Please select at least one channel!");
        if(activeMicrophone.getState().equals(MicrophoneState.CLOSED)){
            // collect selected channels
            this.generateChannels();
            // check if it is using transcription api or not
            if(isUsingTranscriptionAPI) {
                micWrapper = new SpeechToTextDecorator(activeMicrophone); //s2t api
            }else{
                micWrapper = new MicrophoneDecorator(activeMicrophone); // default
            }
            // open mic & store audio data
            micWrapper.listen();
            //run the back-up
            this.backup();
        }
    }

    /**
     * Create channels inside the active audio.microphone
     */
    private void generateChannels(){
        Map<Integer, String> selectedChannelCopy = channelMixer.getSelectedChannelsNameMap();
        activeMicrophone.getChannelManager().generateChannels(selectedChannelCopy);
    }


    /**
     * Get the channel size from the audio.microphone
     * @return either 0 (error) or number of available channels such as 1,2,4,8,16
     */
    public int getMaxChannelSize(){
        int value = 0;
        value = activeMicrophone.getChannelManager().calculateMaxNumOfChannel();
        return value;
    }

    /**
     * Using boolean to select/unselect a channel
     * @param isSelected if the front-end select a channel; or unselect a channel
     * @param id the channel id
     * @param name name of channel to be stored in the microphone later
     * TODO: there has to be a better way to achieve this ;)
     */
    public void flickChannel(boolean isSelected, int id, String name){
        if(isSelected){
            channelMixer.selectChannel(id,name);
        }else{
            channelMixer.unselectChannel(id);
        }
    }

    /**
     * initialise channels by getting all of pre-generated cahnnels
     * @return [Integer:id, StringBuffer:channel_name] a map
     */
    public Map<Integer, StringBuffer> getAllChannels(){
        return channelMixer.initChannels();
    }

    /**
     * Decide whether to use transcription API or not
     * @param isSelected apply decision to the flag
     * @return confirmation if it has been selected
     * TODO: there has to be a better way to achieve this!
     */
    public boolean setUsingTranscriptionAPI(boolean isSelected){
        isUsingTranscriptionAPI = isSelected;
        return isUsingTranscriptionAPI;
    }

    /**
     * TODO: move it inside the feature package
     * Automatically back-up the audio (every 5 minutes)
     * @see Constants#BACKUP_PERIOD
     */
    private void backup(){
        this.timer = new Timer();
        timer.schedule(new BackupTask(), Constants.BACKUP_PERIOD, Constants.BACKUP_PERIOD);
    }

    private class BackupTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("Back-up all files at " + Instant.now().toString());
            micWrapper.save();
        }
    }
}
