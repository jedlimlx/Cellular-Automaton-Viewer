Modifying the GUI
-----------------
You will need to install [SceneBuilder].

CAViewer uses the MVC or Model, View, Controller framework. <br>
[Model] contains the classes the run the simulation as well as search programs. <br>
[Controller] contains the event handling classes and the dialogs. <br>
[Resources] contains the resources needed to render the application such as the *.fxml files and icons. <br>

[main.fxml] contains the GUI of the main window. Open it in [SceneBuilder] to modify it. <br>
Events from [main.fxml] are handled by [MainController.java]. See the comments / javadoc in [MainController.java] for more information.<br>

[Model]: ../src/main/java/sample/model
[Controller]: ../src/main/java/sample/controller
[Resources]: ../src/main/resources
[SceneBuilder]: https://gluonhq.com/products/scene-builder/
[main.fxml]: ../src/main/resources/main.fxml
[MainController.java]: ../src/main/java/sample/controller/MainController.java

Adding custom rule families
-----------------
1. Create a new class for your rule family. It should inherit from [RuleFamily.java].
2. Implement the necessary methods from [RuleFamily.java] and [Rule.java].

Do ensure that you also add a dummy constructor with no arguments. 
This ensures that Jackson is able to construct the rule from the serialised JSON stored in settings.json.

Some useful methods in writing the new rule family would be [APGTable.java] for apgtable generation 
(Unbounded variables and B0 support (must provide the background)!), [CommentGenerator.java] for generating the 
RLE comments / multiline rulestrings, [NeighbourhoodGenerator.java] for handling the different neighbourhoods 
supported by CAViewer and [Utils.java] with useful methods for parsing strings and canonising rulestrings.

3. Create a new class for the rule family's dialog. It should inherit from [RuleWidget.java].
4. Implement the necessary methods from [RuleWidget.java]. Some other widgets that would be helpful are
- [x] [NeighbourhoodSelector.java]
- [x] [StateWeightSelector.java]

If you use these widgets, construct them via the methods in [SharedWidgets.java] 
so that the values in these widgets do not change when the rulespace is changed.

[Rule.java]: ../src/main/java/sample/model/rules/Rule.java
[RuleFamily.java]: ../src/main/java/sample/model/rules/RuleFamily.java
[RuleWidget.java]: ../src/main/java/sample/controller/dialogs/rule/RuleWidget.java
[SharedWidget.java]: ../src/main/java/sample/controller/dialogs/rule/RuleWidget.java
[APGTable.java]: ../src/main/java/sample/model/APGTable.java
[CommentGenerator.java]: ../src/main/java/sample/model/CommentGenerator.java
[Utils.java]: ../src/main/java/sample/model/Utils.java
[StateWeightSelector.java]: ../src/main/java/sample/controller/StateWeightSelector.java
[NeighbourhoodSelector.java]: ../src/main/java/sample/controller/NeighbourhoodSelector.java
[rules]: ../src/main/java/sample/model/rules
[javadocs]: ../javadoc/index.html

See the [javadocs] for more information on implementation and the other classes.
Also see the [rules] folder for sample implementations of other rulespaces.

Adding custom named neighbourhood types
-----------------
1. Add a generator function in [NeighbourhoodGenerator.java]. 
It should generate an array of Coordinates.
2. Add a new case in the switch in the *generateFromSymbol* method.
3. If the neighbourhood has weights, add another generator function for the weights. 
It should generate an array of integers which correspond to the weights of each of the 
coordinates in the first generator function.
4. Add a new case in the switch in the *generateWeightsFromSymbol* method.
5. Add a new case in the switch in the *generateTilingFromSymbol* method.
6. Finally, add the symbol used to the static *neighbourhoodSymbols* variable.

[NeighbourhoodGenerator.java]: ../src/main/java/sample/model/NeighbourhoodGenerator.java
