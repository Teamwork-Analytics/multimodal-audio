package practices.mic;

import javax.sound.sampled.*;
import java.io.IOException;

public class MicPracticeApp {
    public static void main(String[] args) {
        try {
            MicrophonePractice microphone = new MicrophonePractice();
            microphone.sourceDataLine(5000);
            // microphone.recordTest();
        }
        catch (IOException | UnsupportedAudioFileException e){ e.printStackTrace();}
    }
}
