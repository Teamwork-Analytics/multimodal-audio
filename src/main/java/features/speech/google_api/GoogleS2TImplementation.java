package features.speech.google_api;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import observers.TranscriptObservable;

import java.io.IOException;

import static utils.Constants.LANGUAGE_CODE;
import static utils.Constants.SAMPLE_RATE;

/**
 * The implementation of Google Speech To Text API
 * @author Riordan Alfredo (riordan.alfredo@gmail.com)
 * @see GoogleSpeechAPI for the actual application
 */
public class GoogleS2TImplementation {
    private SpeechClient speechClient;
    private ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
    private ClientStream<StreamingRecognizeRequest> clientStream = null;
    private StreamingRecognizeRequest request = null;
    private final String channelName;

    public GoogleS2TImplementation(String channelName){
        this.channelName = channelName;
    }

    /**
     * initialise the configuration
     * @param client the speech client from try
     */
    public void initConfig(SpeechClient client, TranscriptObservable transcriptObservable) throws IOException{
        speechClient = client;
        // set the response observer
        responseObserver =
                new GoogleSpeechResponseObserver<StreamingRecognizeResponse>(channelName, transcriptObservable);

        // a very long configuration for speech to text API.
        clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

        // set the recognition config = mic audio config
        RecognitionConfig recognitionConfig =
                RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setLanguageCode(LANGUAGE_CODE)
                        .setSampleRateHertz((int) SAMPLE_RATE)
                        .build();

        // streaming config
        StreamingRecognitionConfig streamingRecognitionConfig =
                StreamingRecognitionConfig.newBuilder()
                        .setConfig(recognitionConfig)
                        .setInterimResults(true)
                        .build();

        // set request instance
        request =
                StreamingRecognizeRequest.newBuilder()
                        .setStreamingConfig(streamingRecognitionConfig)
                        .build(); // The first request in a streaming call has to be a config

        // send the config to stream as the first data request
        clientStream.send(request);
    }

    /**
     * Send request to the Google API
     * @param audioData byte[] audio data
     */
    public void sendRequest(byte[] audioData){
        if(request == null) System.out.println("Request is null");
        if(clientStream == null) System.out.println("ClientStream is null");
        if(audioData.length != 0){
            //TODO: the time starts here
            request = StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(audioData))
                    .build();
            clientStream.send(request);
        }
    }

    /**
     * prints on complete in the observer
     */
    public void onComplete(){
        if(responseObserver == null) throw new NullPointerException("Cannot transcribe the speech because observer is empty");
        responseObserver.onComplete();
    }

    /**
     * Tear down stream and speech client
     */
    public void close(){
        clientStream.closeSend();
        speechClient.close();
    }

}
