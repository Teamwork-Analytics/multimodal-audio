package exceptions;

public class MicException extends Exception{
    MicException(){
        super("Microphone error");
    }
    public MicException(String message){
        super(message);
    }

}
