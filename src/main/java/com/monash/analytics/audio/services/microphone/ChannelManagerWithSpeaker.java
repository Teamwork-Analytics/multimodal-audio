package com.monash.analytics.audio.services.microphone;

import com.monash.analytics.audio.services.microphone.channels.Channel;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import java.util.*;

public class ChannelManagerWithSpeaker extends ChannelManager{

    private final Microphone microphone;
    private final Map<Integer, Speaker> channelSpeakerMap;

    public ChannelManagerWithSpeaker(Microphone microphone){
        super(1);
        this.channels = new HashMap<>();
        this.microphone = microphone;
        this.channelSpeakerMap = new HashMap<>();
    }

    /**
     * Loop through all channels & store its audio byte in file
     */
    @Override
    public void saveAllChannelAudio(AudioFormat audioFormat) {
        for(Channel channel: channels.values()){
            Speaker speaker = channelSpeakerMap.get(channel.getId());
            channel.saveToAudioFile(audioFormat, speaker);
        }
    }

    /**
     * Calculate the number of maximum channel that it can hold
     * @return can be either 1,2,4,8,16
     */
    @Override
    public int calculateMaxNumOfChannel(){
        assert(microphone.getTargetDataLine() != null);
        List<Integer> availableChannelsList = this.getListOfTDLChannels();
        if(!availableChannelsList.isEmpty()){
            maxNumOfChannel = Collections.max(availableChannelsList);
        }
        return maxNumOfChannel;
    }

    /**
     * Get a list of channels in this audio.microphone/target data line.
     * @return list of available channels in the audio.mixer/audio.microphone e.g. [1,2,4,8,16]
     */
    private List<Integer> getListOfTDLChannels() {
        assert(microphone.getTargetDataLine() != null);
        LinkedHashSet<Integer> channelSet = new LinkedHashSet<>();
        DataLine.Info info = (DataLine.Info) microphone.getTargetDataLine().getLineInfo();
        for(AudioFormat format : info.getFormats()){
            int channel = format.getChannels();
            channelSet.add(channel);
        }
        return new ArrayList<>(channelSet);
    }

    /**
     * generate channels by using the map from the front-end
     * @param channels the map of channel instruction
     */
    @Override
    public void generateChannels(Map<Integer,String> channels){
        super.generateChannels(channels);
        channels.forEach((id,name) -> {
            try{
                this.channelSpeakerMap.put(id, new Speaker());
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void queueDataByChannels(ArrayList<byte[]> allChannelsAudioData){
        assert(allChannelsAudioData != null);
        channels.forEach((id,channel)->{
            byte[] channelData = allChannelsAudioData.get(id);
            Speaker speaker = channelSpeakerMap.get(id);
            channel.queueByteData(channelData, speaker);
        });
    }
}
