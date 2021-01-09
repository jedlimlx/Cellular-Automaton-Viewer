package sample.controller.dialogs.search;

import javafx.scene.control.MenuItem;
import sample.controller.MainController;
import sample.model.patterns.Pattern;
import sample.model.rules.RuleFamily;
import sample.model.search.SearchProgram;
import sample.model.search.rulesrc.RuleSearchParameters;
import sample.model.simulation.Grid;

import java.util.Map;

public class RuleSearchResultsDialog extends SearchResultsDialog {
    public RuleSearchResultsDialog(MainController mainController, SearchProgram searchProgram) {
        super(mainController, searchProgram);
        super.setTitle("Rule Search Results");

        // Displays the min rule of the pattern in the application
        MenuItem showMinRule = new MenuItem("Show Min Rule in Application");
        showMinRule.setOnAction(event -> loadMinRule());
        menu.getItems().add(1, showMinRule);

        // Displays the max rule of the pattern in the application
        MenuItem showMaxRule = new MenuItem("Show Max Rule in Application");
        showMaxRule.setOnAction(event -> loadMaxRule());
        menu.getItems().add(2, showMaxRule);
    }

    public void loadMinRule() {
        Pattern pattern = (Pattern) PatternsDialog.selected.getPattern();
        mainController.loadPattern(getSelectedRLE(pattern.getMinRule()));
    }

    public void loadMaxRule() {
        Pattern pattern = (Pattern) PatternsDialog.selected.getPattern();
        mainController.loadPattern(getSelectedRLE(pattern.getMaxRule()));
    }

    @Override
    public String getSelectedRLE() {
        Pattern pattern = (Pattern) PatternsDialog.selected.getPattern();
        return getSelectedRLE((RuleFamily) pattern.getRule());
    }

    public String getSelectedRLE(RuleFamily rule) {
        Grid targetPattern = ((RuleSearchParameters) searchProgram.getSearchParameters()).getTargetPattern();
        targetPattern.updateBounds();

        int width = targetPattern.getBounds().getValue1().subtract(targetPattern.getBounds().getValue0()).getX() + 2;
        int height = targetPattern.getBounds().getValue1().subtract(targetPattern.getBounds().getValue0()).getY() + 2;

        String rle = "x = " + width + ", y = " + height + ", rule = " + rule.getRulestring() + "\n";  // Header

        rle += targetPattern.toRLE(targetPattern.getBounds().getValue0(),
                targetPattern.getBounds().getValue1());  // Body

        return rle;
    }

    @Override
    public Map<String, String> getAdditionalInfo(Pattern pattern) {
        return pattern.additionalInfo();
    }
}
