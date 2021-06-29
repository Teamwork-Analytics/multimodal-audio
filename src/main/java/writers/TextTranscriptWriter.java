package writers;

import features.speech.TextTranscript;
import utils.Display;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class TextTranscriptWriter extends MultimodalFileWriter{

    private final List<TextTranscript> transcriptions;
    /**
     * Directory structure:
     * recording_data
     * |_ session_name
     * |--audio
     * |--- channel_1
     * |---- channel 1_dd-MMM-YYYY_HH-mm_audio.wav
     * |--- channel_2
     * |--text
     * |--- channel_1
     * |---- channel 1-dd_MMM_YYYY-HH_mm-transcript.txt
     *
     */
    public TextTranscriptWriter(List<TextTranscript> transcriptions) {
        super("text");//to be combined as one
        this.transcriptions = transcriptions;
    }

    @Override
    protected String generateFileFormat() {
        return String.format("%s_text.%s", fileName, "txt");
    }

    @Override
    public void saveFile() {
        try{
            FileWriter targetFile = new FileWriter(this.generatePath(),true);
            PrintWriter printer = new PrintWriter(targetFile);
            transcriptions.forEach(t -> {
                printer.println(t.toString());
            });
            printer.close();
            Display.println("Saving text data at:" + generatePath());
        } catch (IOException e) {
            Display.println("Cannot open the file!");
            e.printStackTrace();
        }

    }
}
