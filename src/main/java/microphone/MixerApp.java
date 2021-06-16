package microphone;

import com.google.cloud.speech.v1.SpeechClient;
import google_api.SpeechToTextAPI;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.util.List;
import java.util.stream.Collectors;

public class MixerApp extends Application {
    private static SpeechClient client;
    JMetro jMetro = new JMetro(Style.LIGHT);

    public static void main(String[] args) {
        try (SpeechClient speechClient = SpeechClient.create()) {
            client = speechClient;
            launch(args);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        SpeechToTextAPI speechToTextAPI = new SpeechToTextAPI();
        speechToTextAPI.initConfig(client);
        MultimodalMixer multimodalMixer = new MultimodalMixer(speechToTextAPI);
        JMetro jMetro = new JMetro();

        stage.setTitle("Multimodal Audio Mixer");

        // Components
        ChoiceBox<String> listOfMics = new ChoiceBox<>();
        listOfMics.getItems().addAll(getAllMicrophonesName(multimodalMixer));

        Label label = new Label("Select a microphone input");
        Button listenButton = new Button("Listen"); // Open target data line
        listenButton.setOnAction(e -> {
            System.out.println(listOfMics.getValue());
            if(listOfMics.getValue() != null )
            multimodalMixer.listen(listOfMics.getValue());
        });
        Button pauseButton = new Button("Pause"); // Close target data line
        pauseButton.setOnAction(e -> {
            multimodalMixer.stop();
        });

        // Layout
        VBox mainLayout = new VBox(20);
        HBox buttonsLayout = new HBox(20);
        buttonsLayout.getChildren().addAll(listenButton, pauseButton);
        buttonsLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(label,listOfMics, buttonsLayout);
        mainLayout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(mainLayout, 500,200);

        jMetro.setScene(scene);
        stage.setScene(scene);
        stage.show();
    }


    private List<String> getAllMicrophonesName(MultimodalMixer multimodalMixer){
        return multimodalMixer.getInputMixers().stream().map(multimodalMixer::getMixerName).collect(Collectors.toUnmodifiableList());
    }
}
