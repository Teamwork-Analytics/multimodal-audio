package com.monash.analytics.audio.services.observers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * The producer of transcription
 */
public class TranscriptObservable {
    private final PropertyChangeSupport support;

    /**
     * use this to be printed in the GUI.
     */
    private String transcript;

    /**
     * Constructor
     */
    public TranscriptObservable(){
        this.support = new PropertyChangeSupport(this);
    }

    /**
     * Subscribe
     * @param pcl the subscriber
     */
    public void subscribe(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    /**
     * Unsubscribe
     * @param pcl the subscriber
     */
    public void unsubscribe(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    /**
     * Set transcription and tell all subscriber
     * @param text the text to be notified/sent across
     */
    public void setTranscript(String text) {
        support.firePropertyChange("transcript", this.transcript, text);
        this.transcript = text;
    }

}
