package practices.javafx_practices;
import javafx.application.Application;

import javafx.scene.Scene; // content inside the window
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage; // entire window
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;

public class Main extends Application{
    Stage window;
    Scene mainScene, secondScene;
    JMetro jMetro = new JMetro(Style.LIGHT);

    public static void main(String[] args) {
        launch(args); // set up for JavaFX application
    }

    @Override
    public void start(Stage stage) throws Exception {
        window = stage;
        Label label1 = new Label("Select the microphone");
        Button button1 = new Button("Go to next scene");
        button1.setOnAction(e -> {
            boolean res = ConfirmBox.display("Title of Window", "Yay!");
            System.out.println(res);
        });

        window.setOnCloseRequest(e -> window.close());

        // Layout 1 - vertical column
        VBox layout1 = new VBox(100); // stack layout
        layout1.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        layout1.getChildren().addAll(label1, button1);
        mainScene = new Scene(layout1, 200, 200);

        jMetro.setScene(mainScene);
        window.setScene(mainScene);
        window.setTitle("Multimodal Audio Mixer - MTA");
        window.show();
    }

    private void close(){

    }



}
