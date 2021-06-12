package main;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene; // content inside the window
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage; // entire window
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class Main extends Application {
    Button button;
    JMetro jMetro = new JMetro(Style.LIGHT);

    public static void main(String[] args) {
        launch(args); // set up for JavaFX application
//        jMetro.setScene(scene);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Multimodal Audio Mixer - MTA");
        button = new Button();
        button.setText("Click me");

        StackPane layout = new StackPane();
        layout.getChildren().add(button);

        Scene scene = new Scene(layout, 300, 250);
        jMetro.setScene(scene);
        stage.setScene(scene);
        stage.show();
    }
}
