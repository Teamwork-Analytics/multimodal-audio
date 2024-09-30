package com.monash.analytics.audio.services.exceptions;

public class MicException extends Exception{
    MicException(){
        super("Microphone error");
    }
    public MicException(String message){
        super(message);
    }

}
