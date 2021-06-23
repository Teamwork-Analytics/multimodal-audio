import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.MDL2IconFont;
import jfxtras.styles.jmetro.Style;
import mixer.MultimodalMixer;

public class RecordingApp extends Application {
    private MultimodalMixer multimodalMixer = new MultimodalMixer();
    private ChoiceBox<String> listOfMicsComponent;
    private ObservableList<String> list = FXCollections.observableArrayList(multimodalMixer.getAllMicNames());

    JMetro jMetro = new JMetro(Style.LIGHT);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("Multimodal Audio Mixer");
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

        // Refresh audio inputs button
        MDL2IconFont refreshIcon = new MDL2IconFont("\uE72C");
        Button refreshButton = new Button("Refresh"); // Close target data line
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setOnAction(e -> {
            multimodalMixer.initMixerInputs();
            list.setAll(multimodalMixer.getAllMicNames());
            listOfMicsComponent.setValue(list.get(0));
            System.out.println("Successfully retrieved all audio inputs.");
        });

        // Add microphone button
        MDL2IconFont plusIcon = new MDL2IconFont("\uE710");
        Button spawnNewMicButton = new Button("Add Mic");
        spawnNewMicButton.setGraphic(plusIcon);
        spawnNewMicButton.setOnAction(e -> {
            String inputName = listOfMicsComponent.getValue();
            if(inputName != null){
                if(multimodalMixer.createNewMicrophone(inputName)){ // spawn mic
                    tabs.getTabs().add(generateNewTab(inputName)); // create new tab
                    System.out.println("Successfully added " + inputName);
                }
            }
        });

        mixerLayout.getChildren().addAll(label, listOfMicsComponent, refreshButton, spawnNewMicButton);
        mixerLayout.setStyle("-fx-padding: 20;");

        root.setTop(mixerLayout);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 800,360);

        jMetro.setScene(scene);
        stage.setScene(scene);
        stage.show();
    }


    private Tab generateNewTab(String tabName) {
        Label label = new Label(tabName);
        // Record button
        // Add microphone button
        MDL2IconFont recordIcon = new MDL2IconFont("\uE7C8");
        Button listenButton = new Button("Record"); // Open target data line
        listenButton.setGraphic(recordIcon);
        listenButton.setOnAction(e -> {
            multimodalMixer.record(tabName);
        });

        // Pause button
        MDL2IconFont pauseIcon = new MDL2IconFont("\uEDB4");
        Button pauseButton = new Button("Pause"); // Close target data line
        pauseButton.setGraphic(pauseIcon);
        pauseButton.setOnAction(e -> {
            multimodalMixer.stop(tabName);
        });

        // Layout
        HBox buttonsLayout = new HBox(20);
        buttonsLayout.getChildren().addAll(label,  pauseButton, listenButton);
        buttonsLayout.setAlignment(Pos.CENTER);
        buttonsLayout.setStyle("-fx-padding: 20;");
        Tab tab = new Tab(tabName, buttonsLayout);
        tab.setOnCloseRequest(e -> {
            multimodalMixer.deleteMicrophone(tabName);
            System.out.println("Successfully closed " + tabName + " tab.");
        });
        return tab;
    }

}
