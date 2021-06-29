package features;

import enums.MicrophoneState;
import microphone.Listenable;
import microphone.Microphone;

/**
 * The parent class of Microphone Wrappers (Decorator pattern)
 */
public class MicrophoneDecorator implements Listenable, Archivable{
    /**
     * Shared microphone instance with its children (concrete decorators)
     */
    protected Microphone microphone;

    /**
     * Constructor
     * @param microphone microphone base object
     */
    public MicrophoneDecorator(Microphone microphone){
        this.microphone = microphone;
    }

    /**
     * default mode without speech to text api
     */
    @Override
    public void listen() {
        microphone.listen(); // run the buffer
        new Thread(()->{
            while(microphone.getState().equals(MicrophoneState.OPENED)){
                microphone.getChannelManager().writeToBaosAllChannels();
            }
        }).start();
    }

    @Override
    public void close() {
        microphone.close(); // close mic
    }

    @Override
    public void save(){
        microphone.save(); // save audio
    };

}
