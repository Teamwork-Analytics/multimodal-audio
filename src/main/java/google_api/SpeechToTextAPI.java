package google_api;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.ArrayList;

public class SpeechToTextAPI {
    private SpeechClient speechClient;
    private ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
    private ClientStream<StreamingRecognizeRequest> clientStream = null;
    private StreamingRecognizeRequest request = null;

    public void initConfig(SpeechClient client) throws IOException {
        speechClient = client;
        // set the response observer
        responseObserver =
                new ResponseObserver<StreamingRecognizeResponse>() {
                    ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                    public void onStart(StreamController controller) {}

                    public void onResponse(StreamingRecognizeResponse response) {
                        System.out.println(response);
                        responses.add(response);
                    }

                    public void onComplete() {
                        try{
                            for (StreamingRecognizeResponse response : responses) {
                                StreamingRecognitionResult result = response.getResultsList().get(0);
                                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                                System.out.printf("Transcript : %s\n", alternative.getTranscript());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    public void onError(Throwable t) {
                       t.printStackTrace();
                    }
                };

        // a very long configuration for speech to text API.
        clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

        // default
        RecognitionConfig recognitionConfig =
                RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16) // TODO: need to learn more about this encoding
                        .setLanguageCode("en-US")
                        .setSampleRateHertz(16000)
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
    }

    public void sendRequest(byte[] audioData){
        if(request == null) System.out.println("Request is null");
        if(clientStream == null) System.out.println("ClientStream is null");
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
