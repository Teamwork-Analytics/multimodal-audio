package microphone;

import practices.mic.CustomMicrophone;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class MultimodalMixer {
    private final Mixer.Info[] mixerInfos;
    private final HashMap<String, Mixer> mixerHashMap = new HashMap<>();
    private final ArrayList<Mixer> mixers = new ArrayList<>();
    private Mixer selectedMixer;
    private final Scanner scanner = new Scanner(System.in);  // Create a Scanner object, TODO: don't use this, see FIT2099 code

    MultimodalMixer(){
        this.mixerInfos = AudioSystem.getMixerInfo(); // get all mixers data
        this.initMixer();
    }

    public void execute(String mixerName) {
        // ask user
//        this.chooseOneMicrophone();
        if(this.selectAMixer(mixerName)){

            // TODO: loop and create thread for each mixer
            Microphone microphone = new Microphone(AudioFileFormat.Type.WAVE);
            microphone.initTargetDataLineFromMixer(selectedMixer);
            microphone.open();
            AudioInputStream audio = new AudioInputStream(microphone.getTargetDataLine());

            new Thread(() -> {
                int nBytesRead = 0;
                byte[] trimBuffer;
                int audioDataLength = 1024;
                ByteBuffer audioDataBuffer = ByteBuffer.allocate(audioDataLength);
                audioDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
                try {
                    // System.out.println("Inside Stream Player Run method")
                    int toRead = audioDataLength;
                    int totalRead = 0;

                    // Reads up a specified maximum number of bytes from audio stream
                    for (; toRead > 0 && (nBytesRead = audio.read(audioDataBuffer.array(), totalRead,
                            toRead)) != -1; toRead -= nBytesRead, totalRead += nBytesRead) {
                    }




//                  System.err.println(totalRead);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();
        }

        //spawnMicrophoneRecorder(info.getName(), mixer); // spawn a simple recording system. TEST

    }


    private void initMixer(){
        for(Mixer.Info info : mixerInfos){
            Mixer mixer = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            if(lineInfos.length>=1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)){//Only prints out info is it is a Microphone
                mixers.add(mixer);
                mixerHashMap.put(info.getName(), mixer);
            }
        }
    }

    /**
     * Getting all name of microphones that are connected to the mixer.
     * @return a list of microphones names
     */
    public ArrayList<Mixer> getInputMixers(){
        return new ArrayList<>(mixers);
    }

    private boolean selectAMixer(String mixerName){
        Mixer mixer = mixerHashMap.get(mixerName);
        if(mixer == null) {System.out.println("Cannot find mixer"); return false;}
        this.selectedMixer = mixer;
        return true;
    }


    public String getMixerName(Mixer mixer){
        if(mixer == null) return "";
        Mixer.Info info = mixer.getMixerInfo();
        return info.getName();
    }

    /**
     * A test private method to understand how does the Microphone work (using the custom microphone);
     * @param mixerName name of mixer/audio line
     * @param mixer name of mixers.
     */
    private void spawnMicrophoneRecorder(String mixerName, Mixer mixer){
        new Thread(() ->{
            try {
                CustomMicrophone microphone = new CustomMicrophone(mixerName, mixer);
                System.out.println("Start of recording from "+ mixerName);
                microphone.readTargetDataLine();
                System.out.println("End of recording...");
                microphone.listenSourceDataLine();
                System.out.println("End of sample program");
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void chooseOneMicrophone(){
        int id = 0;
        boolean flag = true;
        for(Mixer.Info info : mixerInfos){
            System.out.printf("[%d] %s --- %s\n",id, info.getName(), info.getDescription());
            id++;
        }
        //mixer input selection
        while(flag){
            try{
                System.out.println("Please enter the mixer id: ");
                int input = Integer.parseInt(scanner.nextLine());
                this.mixers.add(AudioSystem.getMixer(mixerInfos[input]));
                flag = false;
            }
            catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e){
                System.err.println("Incorrect input!");
                e.printStackTrace();
            }
        }
    }
}
