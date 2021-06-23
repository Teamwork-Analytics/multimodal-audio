package microphone;

import writers.AudioMicFileWriter;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static mixer.MultimodalMixer.sessionName;

public class LineChannel{
    /**
     * An arraylist of channel from a mixer
     * [0..N] where N is number of channel
     */
    private final ArrayList<ByteArrayOutputStream> baosArray = new ArrayList<>();
    /**
     * The number of channel that the mixer has
     */
    private final int numberOfChannel;

    public LineChannel(int numberOfChannel){
        if(numberOfChannel <= 0) throw new IllegalArgumentException("the number of channel cannot be less than 1!");
        // create number of BAOS;
        for(int i = 0; i < numberOfChannel; i++){
            baosArray.add(new ByteArrayOutputStream());
        }
        this.numberOfChannel = numberOfChannel;
    }

    public void divideChannel(){

    }

    public void writeIntoBAOS(int index, byte[] data){
        if(index >= numberOfChannel) throw new IllegalArgumentException("index is out of bound of number channel");
        baosArray.get(index).write(data,0,data.length);
    }

    public void writeToFile(int index, String micName, AudioFormat outFormat, AudioFileFormat.Type targetFileType){
        try {
            byte[] byteData = baosArray.get(index).toByteArray();
            File micTargetFile = new AudioMicFileWriter(sessionName, micName+"_"+index).getAudioFile();
            ByteArrayInputStream micBAIS = new ByteArrayInputStream(byteData);
            AudioInputStream outputAIS = new AudioInputStream(micBAIS, outFormat, byteData.length/outFormat.getFrameSize());
            AudioSystem.write(outputAIS, targetFileType, micTargetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
