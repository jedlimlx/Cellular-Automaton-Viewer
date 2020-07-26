module CAViewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    opens sample;
    opens sample.controller;
}