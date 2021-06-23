package microphone;

import java.util.ArrayList;

public class ChannelDivider {

    private byte[] audioBuffer;
    private int numberOfChannels;


    /* let's create one buffer per channel and place them in the
     * container
     */

    public ChannelDivider(byte[] audioBuffer, int numberOfChannels){
        this.audioBuffer = audioBuffer;
        this.numberOfChannels = numberOfChannels;
    }

    /**
     * Extract data
     * Retrieved from: https://stackoverflow.com/questions/25776803/javasound-extract-one-channel-of-stereo-audio
     * @return audio byte
     */
    public ArrayList<byte[]> extract16BitsSingleChannels() {

        /* A container which will receive our "per channel" buffers */
        ArrayList<byte[]> channelsData = new ArrayList<>();

        /* Parameters :
         *
         * audioBuffer : the buffer that has just been produced by
         * your targetDataLine.read();
         * channels : the number of channels defined in the AudioFormat you
         * use with the line
         *
         *  the AudioFormat which I tested :
         *       float sampleRate = 44100; // changed to 16000
         *       int sampleSizeInBits = 16;
         *       int channels = 8; // changed it to 16
         *       boolean signed = true;
         *       boolean bigEndian = true;
         */

        /**
         * Take care of adjusting the size of the audioBuffer so that
         * audioBuffer % channels == 0 is true ... because :
         */
        final int channelLength = audioBuffer.length/numberOfChannels;

        for (int c=0 ; c < numberOfChannels ; c++) {
            byte[] channel=new byte[channelLength];
            channelsData.add(channel);
        }

        /* then process bytes from audioBuffer and copy each channels byte
         * in its dedicated buffer
         */

        int byteIndex=0;

        for(int i = 0; i < channelLength; i+=2) //i+=2 for 16 bits=2 Bytes samples
        {
            for (int c=0 ; c < numberOfChannels ; c++) {
                channelsData.get(c)[i]=audioBuffer[byteIndex];   // 1st Byte
                byteIndex++;
                channelsData.get(c)[i+1]=audioBuffer[byteIndex]; // 2nd Byte
                byteIndex++;
            }

        }

        /* Returns each set of bytes from each channel in its buffer you can use to
            write on whatever Byte streamer you like. */

        return channelsData;
    }
}
