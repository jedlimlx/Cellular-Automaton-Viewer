package sample.controller.dialogs;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;

import java.util.ArrayList;

public class PopulationGraphDialog extends Dialog {
    public PopulationGraphDialog(ArrayList<Integer> population) {
        super();

        super.setTitle("Population Graph");
        super.setResizable(true);

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

        populationGraph.getData().add(populationSeries);
        super.getDialogPane().setContent(populationGraph);

        // Allows closing with close button
        Window window = super.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
    }
}
