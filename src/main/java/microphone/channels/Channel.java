package microphone.channels;

import mixer.ChannelMixer;
import utils.Display;
import writers.AudioMicrophoneFileWriter;
import writers.MultimodalFileWriter;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class Channel {

    private final int id;
    private final String channelName;
    private volatile BlockingQueue<byte[]> channelQueue = new LinkedBlockingQueue<>();
    private final ByteArrayOutputStream baos; //FIXME: check if this doesnt work, use BlockingQueue.

    /**
     * Constructor
     */
    public Channel(int id, String channelName){
        this.id = id;
        this.channelName = channelName;
        baos = new ByteArrayOutputStream();
    }

    public int getId(){
        return id;
    }

    public String getChannelName() {
        return channelName;
    }

    /**
     * FIXME: Potentially may cause concurrency issue here
     * @param data audio data
     */
    public void queueByteData(byte[] data){
        channelQueue.add(data);
    }

    /**
     * write data to byte array stream from channel queue
     * @throws InterruptedException if channel queue is interrupted
     */
    public byte[] writeDataToBaos() throws InterruptedException {
        byte[] data = channelQueue.take();
        baos.write(data, 0, data.length);
        return data;
    }

    /**
     * Save to audio file
     * @param audioFormat audio input format
     */
    public void saveToAudioFile(AudioFormat audioFormat, int maxChannelSize){
        MultimodalFileWriter fileWriter = new AudioMicrophoneFileWriter(channelName, maxChannelSize, audioFormat, baos);
        fileWriter.saveFile();
    }

}
