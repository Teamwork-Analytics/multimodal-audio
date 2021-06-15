package practices.javafx_practices;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AlertBox {
    public static void display(String title, String message){
        Stage window = new Stage();
        window.setTitle(title);
        // block input events on other windows
        window.initModality(Modality.APPLICATION_MODAL);

        Label label = new Label(message);
        Button closeButton = new Button("OK");
        closeButton.setOnAction( e -> window.close());

        VBox layout = new VBox(100);
        layout.getChildren().addAll(label, closeButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }
}
