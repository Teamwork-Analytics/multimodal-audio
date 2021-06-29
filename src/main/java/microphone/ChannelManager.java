package microphone;

import exceptions.MicException;
import microphone.channels.Channel;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import java.util.*;

public class ChannelManager {
    private int maxNumOfChannel = 1;
    private final Microphone microphone;

    /**
     * A list of selected channels
     */
    private final Map<Integer, Channel> channels;

    ChannelManager(Microphone microphone){
        this.channels = new HashMap<>();
        this.microphone = microphone;
    }

    /**
     * Get the maximum number of channel
     * @return maximum number of channel/ channel partition
     */
    public int getMaxNumOfChannel() {
        return maxNumOfChannel;
    }

    /**
     * Loop through all channels & store its audio byte in file
     */
    public void saveAllChannelAudio() {
        for(Channel channel: channels.values()){
            channel.saveToAudioFile(microphone.getAudioFormat(), maxNumOfChannel);
        }
    }

    /**
     * Calculate the number of maximum channel that it can hold
     * @return can be either 1,2,4,8,16
     */
    public int calculateMaxNumOfChannel(){
        assert(microphone.getTargetDataLine() != null);
        List<Integer> availableChannelsList = this.getListOfTDLChannels();
        if(!availableChannelsList.isEmpty()){
            maxNumOfChannel = Collections.max(availableChannelsList);
        }
        return maxNumOfChannel;
    }

    /**
     * Get a list of channels in this microphone/target data line.
     * @return list of available channels in the mixer/microphone e.g. [1,2,4,8,16]
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
     * FIXME: could it be privacy leaks?
     * @param id integer of channel id
     * @return A channel object. if cannot find any will return null.
     */
    public Channel getChannel(int id){
        return channels.get(id);
    }

    public List<Integer> getSelectedChannelKeys(){
        return new ArrayList<>(channels.keySet());
    }

    /**
     * generate channels by using the map from the front-end
     * @param channels the map of channel instruction
     */
    public void generateChannels(Map<Integer,String> channels){
        this.channels.clear();
        channels.forEach((id,name) -> {
            this.channels.put(id, new Channel(id,name));
        });
    }

    public String getChannelName(int channelId) throws MicException {
        Channel channel = this.getChannel(channelId);
        if(channel == null) throw new MicException("Cannot find id in the channels");
        return channel.getChannelName();
    }

    /**
     * Queue data according to its channel id
     * @param allChannelsAudioData a list of audio data [0,...,N-1] channels
     */
    public void queueDataByChannels(ArrayList<byte[]> allChannelsAudioData){
        assert(allChannelsAudioData != null);
        channels.forEach((id,channel)->{
            byte[] channelData = allChannelsAudioData.get(channel.getId());
            channel.queueByteData(channelData);
        });
    }

    /**
     * Tell all channels to transfer stored volatile byte[] list to ByteOutputStream
     * for further process.
     */
    public void writeToBaosAllChannels() {
        for(Channel channel: channels.values()){
            try {
                channel.writeDataToBaos();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
