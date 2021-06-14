package microphone;

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

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.ArrayList;

public class MixerTest extends Application {
    JMetro jMetro = new JMetro(Style.LIGHT);


    public static void main(String[] args) {
        launch(args);
        // microphoneTest(); // test recording :)
    }

    /**
     * Practice microphone and mixer with JavaSound API
     */
    private static void microphoneTest(){
        try {
            MicrophonePractice microphone = new MicrophonePractice();
            microphone.sourceDataLine(5000);
            // microphone.recordTest();
        }
        catch (IOException | UnsupportedAudioFileException e){ e.printStackTrace();}
    }

    @Override
    public void start(Stage stage) throws Exception {
        JMetro jMetro = new JMetro();

        stage.setTitle("Multimodal Audio Mixer");

        MultimodalMixer multimodalMixer = new MultimodalMixer();
        ArrayList<String> micsList = multimodalMixer.getListOfMicNames();

        // Components
        ChoiceBox<String> listOfMics = new ChoiceBox<>();
        listOfMics.getItems().addAll(micsList);
        Label label = new Label("Select a microphone input");
        Button listenButton = new Button("Listen"); // Open target data line
        Button pauseButton = new Button("Pause"); // Close target data line

        // Layout
        VBox mainLayout = new VBox(20);
        HBox buttonsLayout = new HBox(20);
        buttonsLayout.getChildren().addAll(listenButton, pauseButton);
        buttonsLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(label,listOfMics, buttonsLayout);
        mainLayout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(mainLayout, 300,200);

        jMetro.setScene(scene);
        stage.setScene(scene);
        stage.show();
    }
}
