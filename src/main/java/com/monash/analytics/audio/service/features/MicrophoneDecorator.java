package com.monash.analytics.audio.service.features;

import com.monash.analytics.audio.service.enums.MicrophoneState;
import com.monash.analytics.audio.service.microphone.Listenable;
import com.monash.analytics.audio.service.microphone.Microphone;

/**
 * The parent class of Microphone Wrappers (Decorator pattern)
 */
public class MicrophoneDecorator implements Listenable, Archivable{
    /**
     * Shared audio.microphone instance with its children (concrete decorators)
     */
    protected Microphone microphone;

    /**
     * Constructor
     * @param microphone audio.microphone base object
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

    /**
     * Close the microphone
     */
    @Override
    public void close() {
        microphone.close(); // close mic
    }

    /**
     * Save the audio data
     */
    @Override
    public void save(){
        microphone.save(); // save audio
    };

}
