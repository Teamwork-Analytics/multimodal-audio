package mixer;

import exceptions.MixerException;
import microphone.channels.Channel;

import java.util.*;

public final class ChannelMixer {

    /**
     * Channel id and name.
     */
    private final Map<Integer, String> selectedChannels;

    /**
     * The injected microphone mixer
     */
    private final MicrophoneMixer microphoneMixer;

    /**
     * Constructor
     */
    public ChannelMixer(MicrophoneMixer microphoneMixer){
        this.selectedChannels = new HashMap<>();
        this.microphoneMixer = microphoneMixer;
    }

    /**
     * Deep Copy constructor
     * @param oldChannelMixer the previous channel mixer
     */
    public ChannelMixer(ChannelMixer oldChannelMixer){
        this.selectedChannels = oldChannelMixer.selectedChannels;
        this.microphoneMixer = oldChannelMixer.microphoneMixer;
    }

    /**
     * generate list of available channels
     * @return a map (channel id, channel name) to be printed
     */
    public Map<Integer, StringBuffer> initChannels(){
        Map<Integer, StringBuffer> channels = new HashMap<>();
        for(int i = 0; i < microphoneMixer.getMaxChannelSize(); i++){
            StringBuffer name = new StringBuffer("channel ");
            int indexName = i + 1;
            channels.put(i, name.append(indexName));
        }
        return channels;
    }

    /**
     * Store channels
     * @return a copy of selected channels in the form of channel id & name
     */
    public Map<Integer, String> getSelectedChannelsNameMap() {
        return Collections.unmodifiableMap(selectedChannels);
    }

    /**
     * Select a channel
     * @param channelIndex the channel index
     * @param channelName the name of channel
     */
    public void selectChannel(int channelIndex, String channelName){
        selectedChannels.put(channelIndex, channelName);
    }

    /**
     * Unselect a channel with index
     * @param index the integer/position of channel
     */
    public void unselectChannel(int index){
        selectedChannels.remove(index);
    }

    public boolean isEmpty(){
        return selectedChannels.isEmpty();
    }

    public void updateChannelName(int index, String newName){
        selectedChannels.put(index,newName);
    }

    public String getChannelName(int index){
        return selectedChannels.get(index);
    }

    public List<Integer> getListOfSelectedChannels() throws MixerException {
        if(selectedChannels.isEmpty()) {
            throw new MixerException("Cannot save empty channels! Please select at least one channel.");
        }
        return new ArrayList<>(selectedChannels.keySet());
    }

}
