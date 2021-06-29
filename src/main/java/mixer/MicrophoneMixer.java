package mixer;


import enums.MicrophoneState;
import exceptions.MicException;
import exceptions.MixerException;
import features.MicrophoneDecorator;
import features.speech.SpeechToTextDecorator;
import microphone.Microphone;

import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static utils.Constants.BACKUP_PERIOD;

public final class MicrophoneMixer {
    /**
     * A HashMap of checked/selected microphones
     */
    private final Microphone activeMicrophone; // mutable microphone

    private MicrophoneDecorator micWrapper;

    private final ChannelMixer channelMixer;

    private Timer timer;

    private boolean isUsingTranscriptionAPI = false;

    /**
     * Constructor
     * @param multimodalMixer the main mixer
     */
    public MicrophoneMixer(MultimodalMixer multimodalMixer){
        String micName = multimodalMixer.getSelectedMixerName();
        this.activeMicrophone = new Microphone(micName, multimodalMixer.getMixerInstance(micName));
        this.micWrapper = new MicrophoneDecorator(activeMicrophone);
        this.channelMixer = new ChannelMixer(this);
        this.getMaxChannelSize(); // get the maximum number of channel from the microphone
    }

    /**
     * Constructor
     * @param multimodalMixer the main mixer
     */
    public MicrophoneMixer(MultimodalMixer multimodalMixer, boolean withoutMixer){
        String micName = multimodalMixer.getSelectedMixerName();
        this.activeMicrophone = new Microphone(micName);
        this.micWrapper = new MicrophoneDecorator(activeMicrophone);
        this.channelMixer = new ChannelMixer(this);
        this.getMaxChannelSize(); // get the maximum number of channel from the microphone
    }

    public String printStatus(){
        return activeMicrophone.getState().toString();
    }

    /**
     * Stop listening from a microphone
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
     * Create channels inside the active microphone
     */
    private void generateChannels(){
        Map<Integer, String> selectedChannelCopy = channelMixer.getSelectedChannelsNameMap();
        activeMicrophone.getChannelManager().generateChannels(selectedChannelCopy);
    }


    /**
     * Get the channel size from the microphone
     * @return either 0 (error) or number of available channels such as 1,2,4,8,16
     */
    public int getMaxChannelSize(){
        int value = 0;
        value = activeMicrophone.getChannelManager().calculateMaxNumOfChannel();
        return value;
    }

    public void checkChannel(boolean isSelected, int id, String name){
        if(isSelected){
            channelMixer.selectChannel(id,name);
        }else{
            channelMixer.unselectChannel(id);
        }
    }

    public Map<Integer, StringBuffer> getAllChannels(){
        return channelMixer.initChannels();
    }

    public boolean setUsingTranscriptionAPI(boolean isSelected){
        isUsingTranscriptionAPI = isSelected;
        return isUsingTranscriptionAPI;
    }

    private void backup(){
        this.timer = new Timer();
        timer.schedule(new BackupTask(), BACKUP_PERIOD, BACKUP_PERIOD);
    }

    private class BackupTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("Back-up all files at " + Instant.now().toString());
            micWrapper.save();
        }
    }

    public boolean isListening(){
        return activeMicrophone.getState().equals(MicrophoneState.OPENED);
    }

//    /**
//     * Record using microphone
//     * FIXME: will be replaced with listen
//     */
//    public void record(){
//        new Thread(()->{
//            String micName = multimodalMixer.getSelectedMixerName();
//            activeMicrophone.captureAudioToFile(sessionName, multimodalMixer.getMixerInstance(micName));
//        }).start();
//    }
}
