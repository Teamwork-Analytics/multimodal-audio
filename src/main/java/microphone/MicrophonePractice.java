package microphone;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MicrophonePractice {

    MicrophonePractice() throws IOException, UnsupportedAudioFileException{
    }

    public void sourceDataLine(int recordingMiliSeconds) {
        try {
            // PCM_Signed is for wave format
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16,2,4,44100, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            final SourceDataLine sdl = (SourceDataLine) AudioSystem.getLine(info) ;
            sdl.open(); // grab all system resources it needs to conduct audio i/o

            TargetDataLine tdl = createTargetDataLine(audioFormat);
            if(tdl == null) return;
            tdl.open();

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // automatic adjustable array

            // Create a new thread for source data line (to hear the playback)
            Thread sourceThread = new Thread(() -> {
                sdl.start();
                while(true){
                    //
                    sdl.write(outputStream.toByteArray(),0,outputStream.size());
                }
            });

            // Create a new thread for target data line
            Thread targetThread = new Thread(()->{
                tdl.start();
                //putting bytes data from microphone into buffer -> Java object
                byte[] data = new byte[tdl.getBufferSize()/5]; //segmentation
                int readBytes;
                while(true){
                    readBytes = tdl.read(data, 0, data.length);
                    outputStream.write(data,0,readBytes);
                }
            });
            try {
                targetThread.start();
                System.out.println("Started recording...");
                Thread.sleep(recordingMiliSeconds);
                tdl.stop();
                tdl.close();
                System.out.println("End of recording");
                System.out.println("Started Playback...");
                sourceThread.start();
                Thread.sleep(recordingMiliSeconds);
                sdl.stop();
                sdl.close();
                System.out.println("Ended Playback...");
            }
            catch (InterruptedException e){ e.printStackTrace(); }

        } catch (LineUnavailableException e) { e.printStackTrace();}
    }

    private TargetDataLine createTargetDataLine(AudioFormat audioFormat){
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            return (TargetDataLine) AudioSystem.getLine(info);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void recordTest(){
        try{
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16,2,4,44100, false);
            TargetDataLine targetDataLine = createTargetDataLine(audioFormat);
            if(targetDataLine == null) return;
            targetDataLine.open();
            // --
            System.out.println("Starting Recording...");
            targetDataLine.start();

            Thread thread = new Thread(() -> {
                AudioInputStream audioInputStream = new AudioInputStream(targetDataLine);
                File audioFile = new File("record.wav");
                try { AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile); }
                catch (IOException e) {e.printStackTrace();}
                System.out.println("Stopped Recording");
            });
            thread.start();
            Thread.sleep(5000);
            //below closures need to be in order
            targetDataLine.stop();
            targetDataLine.close();
            System.out.println("Ended sound test!");
        }
        catch (LineUnavailableException | InterruptedException lue) { lue.printStackTrace();}

    }
}
