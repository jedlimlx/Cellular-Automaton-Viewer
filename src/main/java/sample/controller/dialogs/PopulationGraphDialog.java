package sample.controller.dialogs;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class PopulationGraphDialog extends Dialog {
    public PopulationGraphDialog(ArrayList<Integer> population) {
        super();

        super.setTitle("Population Graph");
        super.setResizable(true);

        // VBox to hold the graph
        VBox vbox = new VBox();
        vbox.setFillWidth(true);
        vbox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Constructing the graph
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Generation");

        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Population");

        LineChart<Number, Number> populationGraph = new LineChart<>(xAxis, yAxis);
        populationGraph.setTitle("Population Graph");

        XYChart.Series<Number, Number> populationSeries = new XYChart.Series<>();
        populationSeries.setName("Population");

        for (int i = 0; i < population.size(); i++) {
            XYChart.Data<Number, Number> data = new XYChart.Data<>(i, population.get(i));
            Rectangle rect = new Rectangle(0, 0);
            rect.setVisible(false);
            data.setNode(rect);

            populationSeries.getData().add(data);
        }

        // Fill window
        populationGraph.prefWidthProperty().bind(super.widthProperty().subtract(50));
        populationGraph.prefHeightProperty().bind(super.heightProperty().subtract(50));

        populationGraph.getData().add(populationSeries);
        vbox.getChildren().add(populationGraph);

        // Button to save data
        Button saveData = new Button("Save Data");
        saveData.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Population Data");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "Comma-Separated Values Files (*.csv)", "*.csv"));
            File file = fileChooser.showSaveDialog(null);

            try {
                FileWriter writer = new FileWriter(file);
                writer.write("Generation,Population\n");
                for (int i = 0; i < population.size(); i++) writer.write(i + "," +
                        population.get(i) + "\n");

                writer.close();
            } catch (IOException exception) {
                LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).
                        log(Level.WARNING, exception.getMessage());

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error in saving data");
                alert.setHeaderText("The operation was unsuccessful.");
                alert.setContentText(exception.getMessage());
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                alert.showAndWait();
            }
        });

        vbox.getChildren().add(saveData);

        // Fill window
        vbox.prefWidthProperty().bind(super.widthProperty());
        vbox.prefHeightProperty().bind(super.heightProperty());

        super.getDialogPane().setContent(vbox);

        // Allows closing with close button
        Window window = super.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
    }
}
