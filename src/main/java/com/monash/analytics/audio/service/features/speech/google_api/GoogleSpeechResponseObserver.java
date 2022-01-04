package com.monash.analytics.audio.service.features.speech.google_api;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.monash.analytics.audio.service.observers.TranscriptObservable;
import com.monash.analytics.audio.service.utils.Display;

import java.util.ArrayList;

/**
 * The observer class (it was separated on from GoogleS2TImplementation)
 * @param <T> Ideally, STreamingRecognizeResponse (from Google Speech API)
 */
public class GoogleSpeechResponseObserver<T> implements ResponseObserver<StreamingRecognizeResponse> {
    /**
     * injected channel name
     */
    private final String channelName;

    /**
     * A transcription observable (to be sent to TranscriptObserver)
     * @see com.monash.analytics.audio.service.observers.TranscriptObserver
     */
    private final TranscriptObservable transcriptObservable;

    /**
     * Constructor
     * @param channelName injected channel name
     * @param transcriptObservable injected transcript observable (producer)
     */
    public GoogleSpeechResponseObserver(String channelName, TranscriptObservable transcriptObservable){
        this.channelName = channelName;
        this.transcriptObservable = transcriptObservable;
    }

    /**
     * List of responses (transcription data) (from Google Speech API)
     */
    private final ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

    /**
     * At the initial request
     * @param controller the controller (from Google Speech API)
     */
    public void onStart(StreamController controller) { }

    /**
     * When it receives any response from the Speech API
     * @param response a transcription JSON data
     */
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

    /**
     * When the client is closed, it can be called to print the responses
     */
    public void onComplete() {
        Display.println(channelName + " stops transcribing.");
    }

    /**
     * When there is any error during streaming
     * @param t the throwable error
     */
    public void onError(Throwable t) {
        t.printStackTrace();
    }
}
