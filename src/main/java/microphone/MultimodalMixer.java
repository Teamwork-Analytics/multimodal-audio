package microphone;

import google_api.SpeechToTextAPI;
import practices.mic.CustomMicrophone;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class MultimodalMixer {
    private final Mixer.Info[] mixerInfos;
    private final HashMap<String, Mixer> mixerHashMap = new HashMap<>();
    private final ArrayList<Mixer> mixers = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);  // Create a Scanner object, TODO: don't use this, see FIT2099 code
    private final Microphone microphone = new Microphone(AudioFileFormat.Type.WAVE); // FIXME: create a hashmap for multiple mic.
    private SpeechToTextAPI s2t;

    MultimodalMixer(SpeechToTextAPI s2t){
        this.mixerInfos = AudioSystem.getMixerInfo(); // get all mixers data
        this.initMixer();
        this.s2t = s2t;
    }

    public void listen(String mixerName) {
        if(this.selectAMixer(mixerName)){
            microphone.open();
            System.out.println("Start listening...");
            File audioFile = new File("test.wav");
            microphone.transcribeSpeechToText(s2t);
        }
        //spawnMicrophoneRecorder(info.getName(), mixer); // spawn a simple recording system. TEST
    }

    /**
     * Stop listening from a  microphone
     * FIXME: can stop a selected microphone
     */
    public void stop(){
        microphone.close();
        s2t.transcribe();
        System.out.println("Stop listening");
    }


    /**
     * Initialise mixer and collect all audio inputs sources
     */
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

    /**
     * Choose one mixer input
     * @param mixerName the name of mixer
     * @return true/false if the system can find the mixer or not respectively
     */
    private boolean selectAMixer(String mixerName){
        Mixer selectedMixer = mixerHashMap.get(mixerName);
        if(selectedMixer == null) {System.out.println("Cannot find mixer"); return false;}
        microphone.initTargetDataLineFromMixer(selectedMixer);
        return true;
    }

    /**
     * Get the mixer name
     * @param mixer mixer object
     * @return mixer name
     */
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
                microphone.open();
                System.out.println("End of recording...");
                microphone.listenSourceDataLine();
                System.out.println("End of sample program");
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Console log to choose one microphone
     */
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
