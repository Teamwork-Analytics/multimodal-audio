package features.speech.google_speech.infinite_stream;

import com.google.api.gax.rpc.ClientStream;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SpeechToTextInfinite {
    private S2TResponseObserverInfinite responseObserver = null;
    private ClientStream<StreamingRecognizeRequest> clientStream = null;
    private StreamingRecognizeRequest request = null;
    public static final int STREAMING_LIMIT = 290000; // ~5 minutes
    private static volatile BlockingQueue<byte[]> sharedQueue = new LinkedBlockingQueue();

    private final String languageCode = "en-AU"; // default
    private final int sampleHertz = 16000;

    public void initConfig() {
        try (SpeechClient client = SpeechClient.create()) {
            // set the response observer
            responseObserver = new S2TResponseObserverInfinite();

            clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

            RecognitionConfig recognitionConfig =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16) // TODO: need to learn more about this
                            .setLanguageCode(languageCode)
                            .setSampleRateHertz(sampleHertz)
                            .build();

            StreamingRecognitionConfig streamingRecognitionConfig =
                    StreamingRecognitionConfig.newBuilder()
                            .setConfig(recognitionConfig)
                            .build();

            this.request =
                    StreamingRecognizeRequest.newBuilder()
                            .setStreamingConfig(streamingRecognitionConfig)
                            .build(); // The first request in a streaming call has to be a config

            this.clientStream.send(request);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ByteString sendRequest(byte[] data){
        ByteString tempByteString = ByteString.copyFrom(data);
        request = StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(tempByteString)
                        .build();
        return tempByteString;
    }

    private void sendToClientStream(){
        clientStream.send(request);
    }

    public void sendInfiniteRequest(ByteString tempByteString){
        request = StreamingRecognizeRequest.newBuilder().setAudioContent(tempByteString).build();
        clientStream.send(request);
    }

    public void resendData(ArrayList<ByteString> lastAudioInput){
        // if this is the first audio from a new request
        // calculate amount of unfinalized audio from last request
        // resend the audio to the speech client before incoming audio
        double chunkTime = STREAMING_LIMIT / lastAudioInput.size();
        // ms length of each chunk in previous request audio arrayList
        if (chunkTime != 0) {
            int chunksFromMs = responseObserver.calculateChunks(chunkTime, lastAudioInput.size());
            for (int i = chunksFromMs; i < lastAudioInput.size(); i++) {
                request =
                        StreamingRecognizeRequest.newBuilder()
                                .setAudioContent(lastAudioInput.get(i))
                                .build();
                clientStream.send(request);
            }
        }
        responseObserver.newStream = false;
    }

    public class MicBuffer implements Runnable{
        private TargetDataLine targetDataLine;
        private int BYTES_PER_BUFFER = 6400;

        public MicBuffer(TargetDataLine tdl){
            targetDataLine = tdl;
        }

        @Override
        public void run() {
//            System.out.println("Start speaking...Press Ctrl-C to stop");
            targetDataLine.start();
            byte[] data = new byte[BYTES_PER_BUFFER];
            while (targetDataLine.isOpen()) {
                try {
                    int numBytesRead = targetDataLine.read(data, 0, data.length);
                    if ((numBytesRead <= 0) && (targetDataLine.isOpen())) {
                        continue;
                    }
                    sharedQueue.put(data.clone());
                } catch (InterruptedException e) {
                    System.out.println("Microphone input buffering interrupted : " + e.getMessage());
                }
            }
        }
    }
}
