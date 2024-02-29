package com.monash.analytics.audio.service.microphone.channels;

import com.monash.analytics.audio.service.microphone.Speaker;
import com.monash.analytics.audio.service.writers.AudioMicrophoneFileWriter;
import com.monash.analytics.audio.service.writers.MultimodalFileWriter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A channel from one microphone
 */
public final class Channel {

    private final int id;
    private final String channelName;
    /**
     * The internal audio data storage (storing all of byte data) after concurrency has resolved
     */
    private final ByteArrayOutputStream baos; //FIXME: check if this doesnt work, use BlockingQueue.
    /**
     * A linked queue that stores audio data (byte array) directly into the Memory to solve concurrency problems
     */
    private volatile BlockingQueue<byte[]> channelQueue = new LinkedBlockingQueue<>();

    /**
     * Constructor
     */
    public Channel(int id, String channelName) {
        this.id = id;
        this.channelName = channelName;
        baos = new ByteArrayOutputStream();
    }

    /**
     * Get the channel id
     *
     * @return channel id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the name of channel
     *
     * @return channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * write data to speaker (if it has been enabled by user)
     *
     * @param data audio data
     */
    public void queueByteData(byte[] data, Speaker speaker) {
        speaker.write(data);
        this.queueByteData(data);
    }

    /**
     * write data to the queue
     *
     * @param data byte[] audio data
     */
    public void queueByteData(byte[] data) {
        channelQueue.add(data);
    }

    /**
     * write data to byte array stream from channel queue
     *
     * @throws InterruptedException if channel queue is interrupted
     */
    public byte[] writeDataToBaos() throws InterruptedException {
        byte[] data = channelQueue.take();
        this.storeToBaos(data);
        return data;
    }

    public void storeToBaos(byte[] data) {
        baos.write(data, 0, data.length);
    }

    /**
     * Save to audio file
     *
     * @param inputFormat audio input format
     */
    public void saveToAudioFile(AudioFormat inputFormat, Speaker speaker) {
        MultimodalFileWriter fileWriter = new AudioMicrophoneFileWriter(channelName, inputFormat, baos);
        fileWriter.saveFile();
        try {
            speaker.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save to audio file without speaker
     *
     * @param inputFormat audio input format
     */
    public void saveToAudioFile(AudioFormat inputFormat) {
        new Thread(() -> {
            MultimodalFileWriter fileWriter = new AudioMicrophoneFileWriter(channelName, inputFormat, baos);
            fileWriter.saveFile();
        }).start();
    }

    public void saveToAudioFile(AudioFormat inputFormat, String nameOfTheChannel) {
        new Thread(() -> {
            MultimodalFileWriter fileWriter = new AudioMicrophoneFileWriter(nameOfTheChannel, inputFormat, baos);
            fileWriter.saveFile();
        }).start();
    }

    public void clear(){
        channelQueue = new LinkedBlockingQueue<>();
    }

}
