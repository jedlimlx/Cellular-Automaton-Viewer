package sample.controller.dialogs;

import javafx.scene.control.Dialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;

public class About extends Dialog {
    public About() {
        super();

        super.setTitle("About");
        super.setResizable(true);

        WebView view = new WebView();
        WebEngine engine = view.getEngine();

        engine.load("https://github.com/jedlimlx/Cellular-Automaton-Viewer/wiki");
        super.getDialogPane().setContent(view);

        // Allows closing with close button
        Window window = super.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
    }
}
