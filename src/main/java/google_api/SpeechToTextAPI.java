package google_api;

import com.google.api.gax.rpc.ClientStream;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.protobuf.ByteString;
import google_api.observers.S2TResponseObserver;

import java.io.IOException;

public class SpeechToTextAPI {

    private S2TResponseObserver responseObserver = null;
    private ClientStream<StreamingRecognizeRequest> clientStream = null;
    private StreamingRecognizeRequest request = null;

    public void initConfig(int numberOfChannels) {
        try (SpeechClient client = SpeechClient.create()) {
            // set the response observer
            responseObserver = new S2TResponseObserver();

            // a very long configuration for speech to text API.
            clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

            // default
            String languageCode = "en-AU";
            int sampleHertz = 16000;
            RecognitionConfig recognitionConfig =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16) // TODO: need to learn more about this encoding
                            .setLanguageCode(languageCode)
                            .setSampleRateHertz(sampleHertz)
//                          .setAudioChannelCount(numberOfChannels) // open 2 channels
//                          .setEnableSeparateRecognitionPerChannel(true) // separate the recognition
                            .build();

            StreamingRecognitionConfig streamingRecognitionConfig =
                    StreamingRecognitionConfig.newBuilder()
                            .setConfig(recognitionConfig)
                            .build();

            request =
                    StreamingRecognizeRequest.newBuilder()
                            .setStreamingConfig(streamingRecognitionConfig)
                            .build(); // The first request in a streaming call has to be a config

            clientStream.send(request); // send it to the clientStream
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(byte[] audioData){
        request = StreamingRecognizeRequest.newBuilder()
                .setAudioContent(ByteString.copyFrom(audioData))
                .build();
        clientStream.send(request);
    }

    public void transcribe(){
        if(responseObserver == null) throw new NullPointerException("Cannot transcribe the speech because observer is empty");
        responseObserver.onComplete();
    }
}
