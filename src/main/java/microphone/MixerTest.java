package microphone;

import javax.sound.sampled.*;
import java.io.IOException;

public class MixerTest {
    public static void main(String[] args) {
        MultimodalMixer multimodalMixer = new MultimodalMixer();
        multimodalMixer.run();
        // microphoneTest(); // test recording :)
    }

    /**
     * Practice microphone and mixer with JavaSound API
     */
    private static void microphoneTest(){
        try {
            MicrophonePractice microphone = new MicrophonePractice();
            microphone.sourceDataLine(5000);
            // microphone.recordTest();
        }
        catch (IOException | UnsupportedAudioFileException e){ e.printStackTrace();}
    }
}
