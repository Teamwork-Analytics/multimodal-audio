import gui.MultiModalTextArea;
import gui.MultimodalTab;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import jfxtras.styles.jmetro.MDL2IconFont;
import jfxtras.styles.jmetro.Style;
import mixer.MultimodalMixer;
import utils.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static utils.Constants.APP_NAME;

public class MainApp extends Application {
    private final MultimodalMixer multimodalMixer = MultimodalMixer.getInstance();
    private ChoiceBox<String> listOfMicsComponent;
    private final ObservableList<String> list = FXCollections.observableArrayList(multimodalMixer.getAllMicNames());
    private final TextArea textArea = new TextArea();

    JMetro jMetro = new JMetro(Style.LIGHT);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle(APP_NAME);
        jMetro = new JMetro();

        // Components
        listOfMicsComponent = new ChoiceBox<>();
        listOfMicsComponent.setStyle("-fx-pref-width: 220;");
        listOfMicsComponent.setItems(list);
        listOfMicsComponent.setValue(list.get(0));

        BorderPane root = new BorderPane();
        TabPane tabs = new TabPane();

        HBox mixerLayout = new HBox(20);
        mixerLayout.setAlignment(Pos.CENTER);
        Label label = new Label("Select audio input:");

        Button refreshButton = refreshButtonComponent();
        Button spawnNewMicButton = addMicButtonComponent(tabs);

        mixerLayout.getChildren().addAll(label, listOfMicsComponent, refreshButton, spawnNewMicButton);
        mixerLayout.setStyle("-fx-padding: 20;");

        root.setTop(mixerLayout);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 800, 480);

        jMetro.setScene(scene);
        stage.setScene(scene);
        Display.println("Welcome to Multimodal Audio Mixer! Please select at least one channel from above.\n");

        stage.show();
    }

    private Button refreshButtonComponent(){
        // Refresh audio inputs button
        MDL2IconFont refreshIcon = new MDL2IconFont("\uE72C");
        Button refreshButton = new Button("Refresh"); // Close target data line
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setOnAction(e -> {
            multimodalMixer.initMixerInputs();
            list.setAll(multimodalMixer.getAllMicNames());
            listOfMicsComponent.setValue(list.get(0));
            Display.println("Successfully retrieved all audio inputs.");
        });
        return refreshButton;
    }

    private Button addMicButtonComponent(TabPane tabs){
        // Add microphone button
        MDL2IconFont plusIcon = new MDL2IconFont("\uE710");
        Button spawnNewMicButton = new Button("Add Mic");
        spawnNewMicButton.setGraphic(plusIcon);
        spawnNewMicButton.setOnAction(e -> {
            String inputName = listOfMicsComponent.getValue();
            if(inputName != null){
                if(multimodalMixer.selectAMixer(inputName)){ // spawn mic
                    tabs.getTabs().add(generateNewTab(inputName)); // create new tab
                    Display.println("Successfully added " + inputName);
                }
            }
        });
        return spawnNewMicButton;
    }

    private Tab generateNewTab(String tabName) {
        FlowPane flowPane = new FlowPane();
        flowPane.setVgap(20);
        flowPane.setHgap(20);
        flowPane.setPrefWrapLength(180);
        flowPane.setAlignment(Pos.TOP_CENTER);
        flowPane.orientationProperty().setValue(Orientation.VERTICAL);

        Map<Integer, StringBuffer> defaultChannels = multimodalMixer.getAllChannels();
        List<CheckBox> checkBoxes = new ArrayList<>();
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

        MultiModalTextArea multiModalTextArea = new MultiModalTextArea();
        TextArea textArea = multiModalTextArea.getTextArea();
        textArea.setPrefWidth(800);
        textArea.setPrefHeight(400);
        // Layout

        HBox buttonsLayout = new HBox(20);
        buttonsLayout.getChildren().addAll(
                multimodalTab.transcriptionCheckbox(),
                multimodalTab.pauseButton(),
                multimodalTab.recordButton());
        buttonsLayout.setAlignment(Pos.CENTER);

        VBox tabLayout = new VBox(20);
        tabLayout.setAlignment(Pos.CENTER);
        tabLayout.setStyle("-fx-padding: 20;");
        tabLayout.getChildren().addAll(flowPane, buttonsLayout, textArea);

        Tab tab = new Tab(tabName, tabLayout);
        tab.setOnCloseRequest(e -> {
            multimodalMixer.closeMicrophoneMixer();
            Display.println("Successfully closed " + tabName + " tab.");
        });
        return tab;
    }

}
