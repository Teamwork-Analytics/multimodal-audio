import gui.MultiModalTextArea;
import gui.MultimodalTab;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;
import mixer.MultimodalMixer;
import utils.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static utils.Constants.APP_NAME;

public class SimplifiedApp extends Application {
    private final MultimodalMixer multimodalMixer = MultimodalMixer.getInstance();
    private final List<CheckBox> checkBoxes = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle(APP_NAME);

        BorderPane root = new BorderPane();
        FlowPane flowPane = new FlowPane();
        flowPane.setVgap(20);
        flowPane.setHgap(20);
        flowPane.setPrefWrapLength(180);
        flowPane.setAlignment(Pos.TOP_CENTER);
        flowPane.orientationProperty().setValue(Orientation.VERTICAL);

        multimodalMixer.initInstantMicrophone();
        Map<Integer, StringBuffer> defaultChannels = multimodalMixer.getAllChannels();
        assert defaultChannels != null;
        defaultChannels.forEach((k, v) -> {
            String channelName = v.toString();
            CheckBox checkBox = new CheckBox(channelName);
            checkBox.setId(k.toString());
            checkBox.setOnAction(e -> {
                multimodalMixer.checkChannel(checkBox.isSelected(), k, channelName);
            });
            checkBoxes.add(checkBox);
            flowPane.getChildren().add(checkBox);
        });

        MultimodalTab multimodalTab = MultimodalTab.getInstance(multimodalMixer, checkBoxes);

        // Layout
        HBox buttonsLayout = new HBox(20);
        buttonsLayout.getChildren().addAll(
                multimodalTab.transcriptionCheckbox(),
                multimodalTab.pauseButton(),
                multimodalTab.recordButton());
        buttonsLayout.setAlignment(Pos.CENTER);

        MultiModalTextArea multiModalTextArea = new MultiModalTextArea();
        TextArea textArea = multiModalTextArea.getTextArea();

        VBox tabLayout = new VBox(20);
        tabLayout.setAlignment(Pos.CENTER);
        tabLayout.setStyle("-fx-padding: 20;");
        tabLayout.getChildren().addAll(flowPane, buttonsLayout);

        root.setCenter(tabLayout);
        root.setBottom(textArea);
        root.getStyleClass().add(JMetroStyleClass.BACKGROUND);

        Scene scene = new Scene(root, 800,440);

        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);
        stage.setScene(scene);

        Display.println("Welcome to Multimodal Audio Mixer! Please select at least one channel from above.\n");

        stage.show();
    }

}
