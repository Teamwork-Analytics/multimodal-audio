package com.monash.analytics.audio.service.features.speech;
import java.util.List;

/**
 * The parent of speech to text api adapters (Adapter pattern - sort of)
 */
public interface SpeechToTextAPI {

    /**
     * Start the transcription of audio to text
     */
    void startTranscription();

    /**
     * get the collected transcriptions text
     * @return the list of text transcription
     */
    List<TextTranscript> getTranscriptions();
}
