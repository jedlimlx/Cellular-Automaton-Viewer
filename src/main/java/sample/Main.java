package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/main.fxml")),
                700, 650, true, SceneAntialiasing.DISABLED);
        scene.getStylesheets().add("/style.css");

        stage.setScene(scene);
        stage.setTitle("Cellular Automaton Viewer");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/PulsarIcon.png")));
        stage.show();
    }
}
