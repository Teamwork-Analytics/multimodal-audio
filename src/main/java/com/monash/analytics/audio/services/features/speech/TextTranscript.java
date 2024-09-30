package com.monash.analytics.audio.services.features.speech;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

/**
 * Text Transcript object
 */
public final class TextTranscript{
    private final String textPerLine;

    private final String speakerName;

    private final Instant timestamp;

    /**
     * Constructor
     * @param speakerName the speaker name / channel name
     * @param textPerLine the transcription text
     */
    public TextTranscript(String speakerName, String textPerLine){
        this.speakerName = speakerName;
        this.textPerLine = textPerLine;
        this.timestamp = Instant.now();
    }

    /**
     * Get current timestamp
     * @return a timestamp in the raw Instant object
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * A formatted time stamp
     * @param time the time
     * @return dd MMM yyyy HH:mm:ss.SSS format
     */
    private String formatTimestamp(Instant time){
        Date datetime = Date.from(time);
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
        return formatter.format(datetime);
    }

    /**
     * The transcription
     * @return [formatted-time-stamp] by [speaker]: [speech/text]
     */
    public String toString(){
        return String.format("%s, by %s: %s",
                formatTimestamp(timestamp),
                speakerName,
                textPerLine);
    }

}
