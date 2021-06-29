package features.speech;

import exceptions.MicException;
import features.Archivable;
import features.MicrophoneDecorator;
import features.speech.google_api.GoogleSpeechAPI;
import microphone.Microphone;
import utils.Display;
import writers.MultimodalFileWriter;
import writers.TextTranscriptWriter;


/**
 * Speech to text decorator/wrapper to wrap microphone in the Speech To Text API
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
     * we pass the microphone instance across to be wrapped by API
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
