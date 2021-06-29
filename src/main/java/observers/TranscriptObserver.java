package observers;

import features.speech.TextTranscript;
import utils.Display;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class TranscriptObserver implements PropertyChangeListener {
    private final String channelName;

    private String transcript;

    private final List<TextTranscript> channelTranscripts;

    public TranscriptObserver(String channelName){
        this.channelName = channelName;
        this.channelTranscripts = new ArrayList<>();
    }

    public List<TextTranscript> getTextTranscripts(){
        return new ArrayList<>(channelTranscripts);
    }

    /**
     * TODO: connect this with GUI
     */
    public void displayTranscript(){
        Display.println(transcript);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String textTranscription = (String) evt.getNewValue();
        this.transcript = textTranscription;
        TextTranscript transcription = new TextTranscript(channelName, textTranscription);
        Display.println(transcription.toString());
        channelTranscripts.add(transcription);
    }
}
