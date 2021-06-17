package microphone;

import com.google.cloud.speech.v1.SpeechClient;
import google_api.SpeechToTextAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TestMixerDriver {
    public static void main(String[] args) {
        try (SpeechClient client = SpeechClient.create()) {
            SpeechToTextAPI speechToTextAPI = new SpeechToTextAPI();
            speechToTextAPI.initConfig(client);
            MultimodalMixer multimodalMixer = new MultimodalMixer(speechToTextAPI);
            HashMap<Integer, String> mixerHashMap = new HashMap<>();

            List<String> allInputs = multimodalMixer.getInputMixers().stream().map(multimodalMixer::getMixerName).collect(Collectors.toUnmodifiableList());
            int i = 0;
            for (String input : allInputs) {
                System.out.printf("%d. %s\n", i, input);
                mixerHashMap.put(i, input);
                i++;
            }

            Scanner scanner = new Scanner(System.in);
            System.out.println("Type the mixer name");
            int index = scanner.nextInt();
            String mixerName = mixerHashMap.get(index);

            try {
                multimodalMixer.listen(mixerName);
                Thread.sleep(15000);
                multimodalMixer.stop();
            } catch (InterruptedException | NullPointerException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}