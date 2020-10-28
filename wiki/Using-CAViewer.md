The GUI
=======
The GUI is made with JavaFX & Scene Builder.

Screenshots
-----------
![Screenshot 1](images/screenshot_1.png)

The Menu Bar
-----------
**File Menu**: <br>
*New Pattern* - Creates a new pattern <br>
*Open Pattern* - Opens a pattern file <br>
*Save Pattern* - Saves the pattern to a file <br>
*New Rule* - Opens the rule dialog to make a new rule <br>
*Close* - Closes the application <br> 

**Edit Menu**: <br>
*Copy* - Copies selected cells to an RLE <br>
*Paste* - Pastes the cells that are stored as an RLE on the clipboard <br>
*Delete* - Deletes the selected cells <br>
*Random Soup Settings* - Settings for the random soup generation <br>

**Control Menu**: <br>
*Set Generation* - Sets the generation based on user input <br>
*Set Step Size* - Sets the step size of the simulation <br>
*Set Simulation Speed* - Sets the maximum simulation speed <br>
*Clear Cell Cache* - Clears the cell cache to speed up the simulation (application may hang for a while) <br>
*Grid Lines* - Toggles between showing and not showing grid lines <br>
*View Population Graph* - Displays a graph of the population against generation <br>

**Search Menu**: <br>
*Generate Apgtable* - Generates an apgtable to be used with [apgsearch](https://gitlab.com/apgoucher/apgmera) <br>
*Run Rule Search* - Starts a search program that randomly enumerates rules to find spaceships & oscillators <br>
*Run Catalyst Search* - Starts a search program that searches for catalysts <br>

**Help Menu**:<br>
*About* - Opens the CAViewer Wiki in a built-in browser.<br>

The Tools Bar
-------------
*Run Simulation Button* - Runs the simulation <br>
*Drawing Button* - Go into drawing mode <br>
*Panning Button* - Go into panning mode <br>
*Selection Button* - Go into selection mode <br>
<br>
*Random Soup Button* - Generates a random soup <br>
*Flip Button* - Flips the pattern horizontally / vertically <br>
*Rotate Button* - Rotates the pattern clockwise / counter-clockwise (doesn't work) <br>
*Recording Button* - Records all patterns that move in the selection area and saves it as a \*.gif <br>
*Identify Button* - Identifies the pattern as a spaceship / oscillator <br>

The Status Bar
--------------
The generation of the pattern, the current population and simulation speed is shown.

Keyboard Shortcuts
------------------
<kbd>Space</kbd> - Step 1 Generation <br>
<kbd>Enter</kbd> - Toggle Simulation <br>
<kbd>Delete</kbd> - Deletes cells <br>

<kbd>Ctrl</kbd> + <kbd>N</kbd> - New Pattern <br>
<kbd>Ctrl</kbd> + <kbd>O</kbd> - Open Pattern <br>
<kbd>Ctrl</kbd> + <kbd>S</kbd> - Save Pattern to File <br>

<kbd>Ctrl</kbd> + <kbd>C</kbd> - Copy Pattern to RLE <br>
<kbd>Ctrl</kbd> + <kbd>X</kbd> - Cut Pattern (Copy + Delete) <br>
<kbd>Ctrl</kbd> + <kbd>V</kbd> - Paste Pattern (requires that an area is already selected) <br>
<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>O</kbd> - Load Pattern from Clipboard <br>

<kbd>Ctrl</kbd> + <kbd>A</kbd> - Select the entire pattern <br>
<kbd>Ctrl</kbd> + <kbd>Z</kbd> - Undo <br>
<kbd>Ctrl</kbd> + <kbd>Y</kbd> - Redo <br>

<kbd>Ctrl</kbd> + <kbd>R</kbd> - Change Rule <br>
<kbd>Ctrl</kbd> + <kbd>5</kbd> - Generate Random Soup <br>

<kbd>X</kbd> - Flip Selection Horizontally <br>
<kbd>Y</kbd> - Flip Selection Vertically <br>
<kbd>></kbd> - Rotate Clockwise <br>
<kbd><</kbd> - Rotate Counter-Clockwise <br>