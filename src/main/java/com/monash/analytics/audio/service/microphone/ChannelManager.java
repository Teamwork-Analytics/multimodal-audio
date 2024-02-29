package com.monash.analytics.audio.service.microphone;

import com.monash.analytics.audio.service.AudioServiceAPI;
import com.monash.analytics.audio.service.exceptions.MicException;
import com.monash.analytics.audio.service.microphone.channels.Channel;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage everything that relates to channels
 */
public class ChannelManager {
    protected int maxNumOfChannel;

    /**
     * A list of selected channels
     */
    protected Map<Integer, Channel> channels;


    public ChannelManager(int maxNumOfChannel) {
        this.channels = new HashMap<>();
        this.maxNumOfChannel = maxNumOfChannel;
    }

    /**
     * Get the maximum number of channel
     *
     * @return maximum number of channel/ channel partition
     */
    public int getMaxNumOfChannel() {
        return maxNumOfChannel;
    }

    /**
     * Loop through all channels & store its audio byte in file
     */
    public void saveAllChannelAudio(AudioFormat audioFormat) {
        for (Channel channel : channels.values()) {
            channel.saveToAudioFile(audioFormat);
        }
    }

    public void saveSingleChannel(AudioFormat audioFormat, Integer index, String customChannelName){
        channels.get(index).saveToAudioFile(audioFormat, customChannelName);
    }

    /**
     * Calculate the number of maximum channel that it can hold
     *
     * @return can be either 1,2,4,8,16
     */
    public int calculateMaxNumOfChannel() {
        return maxNumOfChannel;
    }

    /**
     * FIXME: could it be privacy leaks?
     *
     * @param id integer of channel id
     * @return A channel object. if cannot find any will return null.
     */
    public Channel getChannel(int id) {
        return channels.get(id);
    }

    /**
     * @return list of selected channel ids
     */
    public List<Integer> getSelectedChannelKeys() {
        return new ArrayList<>(channels.keySet());
    }

    /**
     * generate channels by using the map from the front-end
     *
     * @param channels the map of channel instruction
     */
    public void generateChannels(Map<Integer, String> channels) {
        this.channels.clear();
        channels.forEach((id, name) -> {
            Channel channel = new Channel(id, name);
            this.channels.put(id, channel);
        });
    }

    /**
     * Get channel name
     *
     * @param channelId using channel id to get the name
     * @return String of channel name
     * @throws MicException when it cannot find any id in the channels
     */
    public String getChannelName(int channelId) throws MicException {
        Channel channel = this.getChannel(channelId);
        if (channel == null) throw new MicException("Cannot find id in the channels");
        return channel.getChannelName();
    }

    /**
     * Queue data according to its channel id
     *
     * @param allChannelsAudioData a list of audio data [0,...,N-1] channels
     */
    public void queueDataByChannels(ArrayList<byte[]> allChannelsAudioData) {
        assert (allChannelsAudioData != null);
        channels.forEach((id, channel) -> {
            byte[] channelData = allChannelsAudioData.get(id);
            channel.queueByteData(channelData);
        });
    }

    /**
     * Tell all channels to transfer stored volatile byte[] list to ByteOutputStream
     * for further process.
     */
    public void writeToBaosAllChannels() {
        for (Channel channel : channels.values()) {
            try {
                channel.writeDataToBaos();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Take old channels, and generate a new one.
     * It will clear up all recorded data.
     */
    public void reset(){
        Map<Integer, String> tempChannels = new HashMap<>();
        for(Channel channel : channels.values()){
            tempChannels.put(channel.getId(), channel.getChannelName());
        }
        this.generateChannels(tempChannels);
    }
}
