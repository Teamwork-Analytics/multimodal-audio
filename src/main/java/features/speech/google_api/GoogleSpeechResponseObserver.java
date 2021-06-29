package features.speech.google_api;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import observers.TranscriptObservable;
import utils.Display;

import java.util.ArrayList;

public class GoogleSpeechResponseObserver<T> implements ResponseObserver<StreamingRecognizeResponse> {
    private final String channelName;
    private final TranscriptObservable transcriptObservable;

    public GoogleSpeechResponseObserver(String channelName, TranscriptObservable transcriptObservable){
        this.channelName = channelName;
        this.transcriptObservable = transcriptObservable;
    }
    final ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

    public void onStart(StreamController controller) { }

    public void onResponse(StreamingRecognizeResponse response) {
        // add to responses
        try{
            responses.add(response);

            // use observer/listener to store responses
            StreamingRecognitionResult result = response.getResultsList().get(0);
            SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
            if(result.getIsFinal()){
                float confidence = alternative.getConfidence() * 100;
                String transcript = String.format("(%.2f%%)%s",confidence,alternative.getTranscript());
                transcriptObservable.setTranscript(transcript);
            }
        } catch (Exception e) {
            Display.println("Error at Google Speech Response API.");
            e.printStackTrace();
        }
    }

    public void onComplete() {
        Display.println(channelName + " stops transcribing.");
    }

    public void onError(Throwable t) {
        t.printStackTrace();
    }
}
