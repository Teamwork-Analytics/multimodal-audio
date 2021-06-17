package google_api.samples;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class S2TSample {

    public static void main(String[] args) {
        try {
            streamingMicRecognize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Performs microphone streaming speech recognition with a duration of 1 minute. */
    public static void streamingMicRecognize() throws Exception {

        ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
        try (SpeechClient client = SpeechClient.create()) {

            responseObserver =
                    new ResponseObserver<StreamingRecognizeResponse>() {
                        ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                        public void onStart(StreamController controller) {}

                        public void onResponse(StreamingRecognizeResponse response) {
                            responses.add(response);
                        }

                        public void onComplete() {
                            for (StreamingRecognizeResponse response : responses) {
                                System.out.println(response);
                                StreamingRecognitionResult result = response.getResultsList().get(0);
                                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                                System.out.printf("Transcript : %s\n", alternative.getTranscript());
                            }
                        }

                        public void onError(Throwable t) {
                            System.out.println(t);
                        }
                    };

            ClientStream<StreamingRecognizeRequest> clientStream =
                    client.streamingRecognizeCallable().splitCall(responseObserver);

            RecognitionConfig recognitionConfig =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setLanguageCode("en-US")
                            .setSampleRateHertz(16000)
                            .build();
            StreamingRecognitionConfig streamingRecognitionConfig =
                    StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build();

            StreamingRecognizeRequest request =
                    StreamingRecognizeRequest.newBuilder()
                            .setStreamingConfig(streamingRecognitionConfig)
                            .build(); // The first request in a streaming call has to be a config

            clientStream.send(request);

            // TODO: Set up microphones
            // SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed: true,
            // bigEndian: false
            AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo(); // get all mixers data
            HashMap<Integer, Mixer> mixerHashMap = new HashMap<>();
            int i = 0;
            for(Mixer.Info info : mixerInfos){
                Mixer mixer = AudioSystem.getMixer(info);
                Line.Info[] lineInfos = mixer.getTargetLineInfo();
                if(lineInfos.length>=1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)){//Only prints out info is it is a Microphone
                    System.out.printf("%d. %s\n", i, info.getName());
                    mixerHashMap.put(i, mixer);
                    i++;
                }
            }

            System.out.println("Select audio input:");
            Scanner scanner = new Scanner(System.in);
            int index = scanner.nextInt();
            Mixer mixer = mixerHashMap.get(index);

            // Set the system information to read from the microphone audio stream
            DataLine.Info targetInfo = new DataLine.Info( TargetDataLine.class, audioFormat);

            if (!mixer.isLineSupported(targetInfo)) {
                System.out.println("Microphone not supported");
                System.exit(0);
            }
            // Target data line captures the audio stream the microphone produces.
            TargetDataLine targetDataLine = (TargetDataLine) mixer.getLine(targetInfo);

            targetDataLine.open(audioFormat);
            targetDataLine.start();
            System.out.println("Start speaking");

            File audioFile = new File("test.wav");

            long startTime = System.currentTimeMillis();
            AudioInputStream audio = new AudioInputStream(targetDataLine);
            while (true) {
                long estimatedTime = System.currentTimeMillis() - startTime;
                byte[] data = new byte[6400];
                audio.read(data);
                if (estimatedTime > 15000) { //TODO: 60 seconds speaking time
                    System.out.println("Stop speaking.");
                    targetDataLine.stop();
                    targetDataLine.close();
                    break;
                }

                request =
                        StreamingRecognizeRequest.newBuilder()
                                .setAudioContent(ByteString.copyFrom(data))
                                .build();
                clientStream.send(request);
            }
            AudioSystem.write(audio, AudioFileFormat.Type.WAVE, audioFile);

            System.out.println("Stopped Recording");

        } catch (Exception e) {
            e.printStackTrace();
        }
        responseObserver.onComplete();
    }
}
