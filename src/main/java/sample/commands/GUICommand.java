package sample.commands;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import picocli.CommandLine;
import sample.controller.MainController;

import java.io.IOException;

@CommandLine.Command(name = "GUI", aliases = "", description = "Starts the CAViewer GUI")
public class GUICommand extends Application implements Runnable {
    private MainController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),700, 650, true, SceneAntialiasing.DISABLED);
        scene.getStylesheets().add("/style.css");

        controller = fxmlLoader.getController();

        stage.setScene(scene);
        stage.setTitle("Cellular Automaton Viewer");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/PulsarIcon.png")));
        stage.show();
    }

    @Override
    public void stop(){
        controller.onApplicationClosed();
    }

    @Override
    public void run() {
        launch();
    }
}
