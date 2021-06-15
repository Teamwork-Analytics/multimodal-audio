package practices.mic;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CustomMicrophone {
    private final String id;
    private final AudioFormat recordAudioFormat;
    private final Mixer mixer;
    private final int segmentationSize = 5;
    private final int recordingMilliSeconds = 5000; // TODO: remove it later
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // automatic adjustable array

    public CustomMicrophone(String id, Mixer mixer) throws IOException, UnsupportedAudioFileException{
        this.recordAudioFormat = new AudioFormat(44100f, 16, 1, true, false);
        this.mixer = mixer;
        this.id = id;
    }

    /**
     * Using the injected mixer to listen the corresponding microphone
     */
    public void readTargetDataLine(){
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, recordAudioFormat);
        try {
            final TargetDataLine tdl = (TargetDataLine) mixer.getLine(info);
            tdl.open();
            Thread targetThread = generateAudioReadingThread(tdl);
            targetThread.start();
            Thread.sleep(recordingMilliSeconds); //TODO: remove this later
            this.stopTargetDataLine(tdl);
        } catch (IllegalArgumentException e) {
            System.out.println("The input device is not suitable for target data line");
        } catch (InterruptedException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clean up target data line
     * TODO: convert it to button function
     * @param tdl TargetDataLine targetDataLine/mic input
     */
    private void stopTargetDataLine(TargetDataLine tdl){
        tdl.stop();
        tdl.close();
    }

    /**
     * modular Microphone's thread generator
     * @param tdl the targetdataline
     * @return a new Thread
     */
    private Thread generateAudioReadingThread(TargetDataLine tdl){
        // Create a new thread for target data line & record the audio byte
        return new Thread(()->{
            tdl.start();
            //putting bytes data from microphone into buffer java object
            byte[] data = new byte[tdl.getBufferSize()/ segmentationSize]; //divide buffer into 5 parts
            int readBytes;
            while(true){
                readBytes = tdl.read(data, 0, data.length);
                outputStream.write(data,0,readBytes);
            }
        });
    }

    /**
     * Using the source data line to test the Playback
     */
    public void listenSourceDataLine(){
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, recordAudioFormat);
        try {
            final SourceDataLine sdl = (SourceDataLine) AudioSystem.getLine(info) ;
            sdl.open();
            // Create a new thread for source data line (to hear the playback)
            Thread sourceThread = new Thread(() -> {
                sdl.start();
                while(true){
                    sdl.write(outputStream.toByteArray(),0,outputStream.size());
                }
            });
            // listen to the recorded audio
            sourceThread.start();
            Thread.sleep(recordingMilliSeconds);
            sdl.close();
            sdl.stop();
        } catch (LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
