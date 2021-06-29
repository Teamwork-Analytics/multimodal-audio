package features.speech;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public final class TextTranscript{
    /**
     * The list of transcribed text per line
     */
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

    public Instant getTimestamp() {
        return timestamp;
    }

    private String formatTimestamp(Instant time){
        Date datetime = Date.from(time);
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
        return formatter.format(datetime);
    }

    public String toString(){
        return String.format("%s, by %s: %s",
                formatTimestamp(timestamp),
                speakerName,
                textPerLine);
    }

}
