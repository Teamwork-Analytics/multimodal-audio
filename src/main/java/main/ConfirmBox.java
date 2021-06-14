package main;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmBox {

    static boolean answer;

    public static boolean display(String title, String message){
        Stage window = new Stage();
        window.setTitle(title);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(250);

        Label label = new Label(message);

        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        okButton.setOnAction(e -> {
            answer = true;
            window.close();
        });
        cancelButton.setOnAction(e -> {
            answer = false;
            window.close();
        });

        VBox vLayout = new VBox(100);
        HBox xLayout = new HBox(100);
        xLayout.getChildren().addAll(okButton, cancelButton);
        xLayout.setAlignment(Pos.CENTER);
        vLayout.getChildren().addAll(label, xLayout);
        vLayout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vLayout);
        window.setScene(scene);
        window.showAndWait();
        return answer;
    }
}
