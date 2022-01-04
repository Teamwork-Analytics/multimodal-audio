package com.monash.analytics.audio.service.exceptions;

public class MicException extends Exception{
    MicException(){
        super("Microphone error");
    }
    public MicException(String message){
        super(message);
    }

}
