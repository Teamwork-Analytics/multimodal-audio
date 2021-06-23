package mixer;

import com.google.cloud.speech.v1.SpeechClient;
import api.google_speech.SpeechToTextAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class TestMixerDriver {
    public static void main(String[] args) {
        try (SpeechClient client = SpeechClient.create()) {
            SpeechToTextAPI speechToTextAPI = new SpeechToTextAPI();
            speechToTextAPI.initConfig(client);
            MultimodalMixer multimodalMixer = new MultimodalMixer(speechToTextAPI);
            HashMap<Integer, String> mixerHashMap = new HashMap<>();

            List<String> allInputs = multimodalMixer.getAllMicNames();
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
                multimodalMixer.stop(mixerName);
            } catch (InterruptedException | NullPointerException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
