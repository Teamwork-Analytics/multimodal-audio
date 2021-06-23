package mixer;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * A mixer that has multiple channel/multiple line inputs
 */
public class MultiChannelMixer {
    public static void main(String[] args) {
        try {
            String filename = "test.wav";

            File sourceFile = new File(filename);
            File leftTargetFile = new File("left_"+filename);
            File rightTargetFile = new File("right_"+filename);

            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sourceFile);
            AudioFileFormat.Type targetFileType = fileFormat.getType();
            AudioFormat audioFormat = fileFormat.getFormat();

            AudioInputStream inputAIS = AudioSystem.getAudioInputStream(sourceFile);

            ByteArrayOutputStream leftbaos = new ByteArrayOutputStream();
            ByteArrayOutputStream rightbaos = new ByteArrayOutputStream();

            byte[] bytes = new byte[(audioFormat.getSampleSizeInBits() / 8) * 2];

            while (true) {

                int readsize = inputAIS.read(bytes);

                if (readsize == -1) {
                    break;
                }

                rightbaos.write(bytes, 0, bytes.length / 2);
                leftbaos.write(bytes, bytes.length / 2, bytes.length / 2);
            }

            byte[] leftData = leftbaos.toByteArray();
            byte[] rightData = rightbaos.toByteArray();

            AudioFormat outFormat = new AudioFormat(audioFormat.getEncoding(), audioFormat.getSampleRate(), audioFormat.getSampleSizeInBits(), 1, audioFormat.getFrameSize() / 2, audioFormat.getFrameRate(), audioFormat.isBigEndian());

            ByteArrayInputStream leftbais = new ByteArrayInputStream(leftData);
            AudioInputStream leftoutputAIS = new AudioInputStream(leftbais, outFormat, leftData.length / outFormat.getFrameSize());
            AudioSystem.write(leftoutputAIS, targetFileType, leftTargetFile);

            ByteArrayInputStream rightbais = new ByteArrayInputStream(rightData);
            AudioInputStream rightoutputAIS = new AudioInputStream(rightbais, outFormat, rightData.length / outFormat.getFrameSize());
            AudioSystem.write(rightoutputAIS, targetFileType, rightTargetFile);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }

    }


}
