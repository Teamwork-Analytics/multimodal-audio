package com.monash.analytics.audio.services.features.speech;

import com.monash.analytics.audio.services.exceptions.MicException;
import com.monash.analytics.audio.services.features.Archivable;
import com.monash.analytics.audio.services.features.MicrophoneDecorator;
import com.monash.analytics.audio.services.features.speech.google_api.GoogleSpeechAPI;
import com.monash.analytics.audio.services.microphone.Microphone;
import com.monash.analytics.audio.services.utils.Display;
import com.monash.analytics.audio.services.writers.MultimodalFileWriter;
import com.monash.analytics.audio.services.writers.TextTranscriptWriter;


/**
 * Speech to text decorator/wrapper to wrap audio.microphone in the Speech To Text API
 */
public class SpeechToTextDecorator extends MicrophoneDecorator implements Archivable {

    private SpeechToTextAPI speechAPI;

    /**
     * Constructor
     * @param microphone from Listenable interface
     */
    public SpeechToTextDecorator(Microphone microphone) {
        super(microphone);
    }

    /**
     * Start the audio-to-text with Google Speech API (using bridge)
     * Instead of running super.listen(),
     * we pass the audio.microphone instance across to be wrapped by API
     */
    @Override
    public void listen() {
        try{
            this.speechAPI = new GoogleSpeechAPI(microphone);
            this.speechAPI.startTranscription();
        } catch (MicException e) {
            Display.println(e.getMessage());
        }
    }

    /**
     * Save the text data into file
     * TODO: use FileWriter class
     */
    @Override
    public void save() {
        // Create a new file writer to write transcriptions to text file
        new Thread(() -> {
            MultimodalFileWriter fileWriter =  new TextTranscriptWriter(this.speechAPI.getTranscriptions());
            fileWriter.saveFile();
        }).start();
        super.save();
    }

}
