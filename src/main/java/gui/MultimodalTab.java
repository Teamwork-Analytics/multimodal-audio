package gui;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import jfxtras.styles.jmetro.MDL2IconFont;
import mixer.MultimodalMixer;
import java.util.List;

public class MultimodalTab {
    private static MultimodalTab instance;
    private final MultimodalMixer multimodalMixer;
    private final List<CheckBox> checkBoxes;

    private MultimodalTab(MultimodalMixer multimodalMixer, List<CheckBox> checkBoxes){
        this.multimodalMixer = multimodalMixer;
        this.checkBoxes = checkBoxes;
    };

    public static MultimodalTab getInstance(MultimodalMixer multimodalMixer, List<CheckBox> checkBoxes){
        if(instance == null){
            instance = new MultimodalTab(multimodalMixer, checkBoxes);
        }
        return instance;
    }

    public Button recordButton(){
        // Record button
        MDL2IconFont recordIcon = new MDL2IconFont("\uE7C8");
        Button recordButton = new Button("Record"); // Open target data line
        recordButton.setGraphic(recordIcon);
        recordButton.setOnAction(e -> {
            if(multimodalMixer.record()){
                checkBoxes.forEach(c -> c.setDisable(true));
            }
        });
        return recordButton;
    }

    public Button pauseButton(){
        // Pause button
        MDL2IconFont pauseIcon = new MDL2IconFont("\uEDB4");
        Button pauseButton = new Button("Pause"); // Close target data line
        pauseButton.setGraphic(pauseIcon);
        pauseButton.setOnAction(e -> {
            multimodalMixer.pause();
            checkBoxes.forEach(c -> c.setDisable(false));
        });
        return pauseButton;
    }

    public CheckBox transcriptionCheckbox(){
        CheckBox transcriptionCheckbox = new CheckBox("using speech-to-text?");
        transcriptionCheckbox.setOnAction(e -> {
            multimodalMixer.checkIsTranscribing(transcriptionCheckbox.isSelected());
        });
        checkBoxes.add(transcriptionCheckbox);
        return transcriptionCheckbox;
    }

}
