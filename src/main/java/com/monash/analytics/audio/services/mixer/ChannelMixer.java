package com.monash.analytics.audio.services.mixer;

import com.monash.analytics.audio.services.exceptions.MixerException;

import java.util.*;

/**
 * A sub-part of microphone mixer that controls channels
 */
public final class ChannelMixer {

    /**
     * Channel id and name.
     */
    private final Map<Integer, String> selectedChannels;

    /**
     * The injected audio.microphone audio.mixer
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
     * @param oldChannelMixer the previous channel audio.mixer
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

    /**
     * Check if selected channels are empty or not
     * @return true/false whether the user has selected any channels or not
     */
    public boolean isEmpty(){
        return selectedChannels.isEmpty();
    }

    /**
     * Update channel name
     * @param index the index of channel (index 0 is channel 1)
     */
    public void updateChannelName(int index, String newName){
        selectedChannels.put(index,newName);
    }

    /**
     * Get the channel name with the index
     * @param index the index of channel (index 0 is channel 1)
     * @return the channel name
     */
    public String getChannelName(int index){
        return selectedChannels.get(index);
    }

    /**
     * Get list of selected channels
     * @return the selected channels indexes
     * @throws MixerException when the selected channels is empty
     */
    public List<Integer> getListOfSelectedChannels() throws MixerException {
        if(selectedChannels.isEmpty()) {
            throw new MixerException("Cannot save empty channels! Please select at least one channel.");
        }
        return new ArrayList<>(selectedChannels.keySet());
    }

}
