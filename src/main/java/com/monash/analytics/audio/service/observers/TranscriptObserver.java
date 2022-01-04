package com.monash.analytics.audio.service.observers;

import com.monash.analytics.audio.service.features.speech.TextTranscript;
import com.monash.analytics.audio.service.utils.Display;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The consumer of transcription
 */
public class TranscriptObserver implements PropertyChangeListener {
    /**
     * The name of channel
     */
    private final String channelName;

    /**
     * Transcription text
     */
    private String transcript;

    /**
     * Collection of transcriptions
     */
    private final List<TextTranscript> channelTranscripts;

    /**
     * Constructor
     * @param channelName the name of channel
     */
    public TranscriptObserver(String channelName){
        this.channelName = channelName;
        this.channelTranscripts = new ArrayList<>();
    }

    /**
     *
     * @return collection of transcriptions
     */
    public List<TextTranscript> getTextTranscripts(){
        return new ArrayList<>(channelTranscripts);
    }

    /**
     * TODO: connect this with GUI
     */
    public void displayTranscript(){
        Display.println(transcript);
    }

    /**
     * When it detects any changes in the property, it will execute this method
     * @param evt the event from producer
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String textTranscription = (String) evt.getNewValue();
        this.transcript = textTranscription;
        TextTranscript transcription = new TextTranscript(channelName, textTranscription);
        Display.println(transcription.toString());
        channelTranscripts.add(transcription);
    }
}
