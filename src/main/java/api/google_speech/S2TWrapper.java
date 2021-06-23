package api.google_speech;

import com.google.cloud.speech.v1.SpeechClient;
import mixer.MultimodalMixer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class S2TWrapper {
    /**
     * A shared queue
     * Must be static and volatile because it must retain its existence in memory
     */
    private static volatile BlockingQueue<byte[]> sharedQueue = new LinkedBlockingQueue<>();
    private SpeechToTextAPI s2t;
    private MultimodalMixer multimodalMixer;

    public S2TWrapper(MultimodalMixer multimodalMixer){
        this.s2t = new SpeechToTextAPI();
        this.multimodalMixer = new MultimodalMixer(s2t);
    }

    public void useSpeechToText(String mixerName){
        try (SpeechClient speechClient = SpeechClient.create()) {
            this.s2t.initConfig(speechClient);
            this.multimodalMixer.listen(mixerName);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void stop(){
        this.multimodalMixer.stop("");
        s2t.transcribe();
    }


}
