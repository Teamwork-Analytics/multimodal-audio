package microphone;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class MultimodalMixer {
    private final Mixer.Info[] mixerInfos;
    private final ArrayList<Mixer> mixers;
    private final Scanner scanner = new Scanner(System.in);  // Create a Scanner object, TODO: don't use this, see FIT2099 code

    MultimodalMixer(){
        this.mixerInfos = AudioSystem.getMixerInfo();
        this.mixers = new ArrayList<>();
    }

    public void run() {
        // ask user
        this.getFromOneMicrophone();

        // loop and create thread
        for (Mixer mixer : this.mixers) {
            Mixer.Info info = mixer.getMixerInfo();
            String mixerName = info.getName();
            Thread thread = new Thread(() ->{
                try {
                    Microphone microphone = new Microphone(mixerName, mixer);
                    System.out.println("Start of recording from "+ mixerName);
                    microphone.readTargetDataLine();
                    System.out.println("End of recording...");
                    microphone.listenSourceDataLine();
                    System.out.println("End of sample program");
                } catch (IOException | UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }

    private void getFromOneMicrophone(){
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
