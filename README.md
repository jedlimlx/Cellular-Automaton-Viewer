
# CAViewer [![Build CAViewer](https://github.com/jedlimlx/Cellular-Automaton-Viewer/workflows/Build%20CAViewer/badge.svg)][Builds link] ![Test CAViewer](https://github.com/jedlimlx/Cellular-Automaton-Viewer/workflows/Test%20CAViewer/badge.svg)

[Builds link]: https://github.com/jedlimlx/Cellular-Automaton-Viewer/actions

See python branch for CAViewer v1.

Installation
============
CAViewer v2 is written in Java.<br>
You will need the Java Runtime Environment / Java Development Kit to run the *.jar file.<br>

If you can't use Java, download the precompiled binaries 
[here](https://github.com/jedlimlx/Cellular-Automaton-Viewer/actions). <br>
On Linux and Mac, the binary may not be exectuable by default so run `chmod +x CAViewer`.
This requires a github account.

Alternatively, get the binaries [here](https://github.com/jedlimlx/Cellular-Automaton-Viewer/releases).

Note that this *.jar runs on the Java Runtime Environment / Java Development Kit 11 and above.

See the [wiki](wiki/Home.md) for instructions on how to build the application from source and how to modify it.

What is this?
=============
CAViewer is a cellular automaton simulation program written in Java <br>
It aims to support rulespaces unsupported by other CA simulation programs such as Golly and LifeViewer.

The GUI
=======
The GUI is made with JavaFX & Scene Builder.

The Menu Bar
-----------
**File Menu**: <br>
*New Pattern* - Creates a new pattern <br>
*Open Pattern* - Opens a pattern file <br>
*Save Pattern* - Saves the pattern to a file <br>
*New Rule* - Opens the rule dialog to make a new rule <br>
*Set Rule Directory* - Sets the directory for CAViewer to look for rules <br>
*Close* - Closes the application <br> 

**Edit Menu**: <br>
*Copy* - Copies selected cells to an RLE <br>
*Paste* - Pastes the cells that are stored as an RLE on the clipboard <br>
*Delete* - Deletes the selected cells <br>
*Random Soup Settings* - Settings for the random soup generation <br>

**View Menu**: <br>
*Adjust Colours* - Adjusts the colours of the current rule <br>
*Grid Lines* - Toggles between showing and not showing grid lines <br>
*View Population Graph* - Displays a graph of the population against generation <br>

**Control Menu**: <br>
*Set Generation* - Sets the generation based on user input <br>
*Set Step Size* - Sets the step size of the simulation <br>
*Set Simulation Speed* - Sets the maximum simulation speed <br>
*Clear Cell Cache* - Clears the cell cache to speed up the simulation (application may hang for a while) <br>

**Search Menu**: <br>
*Generate Apgtable* - Generates an apgtable to be used with [apgsearch](https://gitlab.com/apgoucher/apgmera) <br>
*Run Rule Search* - Starts a search program that randomly enumerates rules to find spaceships & oscillators <br>
*Run Agar Search* - Starts a search program that searches for agars <br>
*Run Catalyst Search* - Starts a search program that searches for catalysts <br>
*Run Brute Force Search* - Starts a brute force search program similiar to gsearch <br>

**Help Menu**:<br>
*About* - Opens the CAViewer Wiki in a built-in browser.<br>
*Rule Info* - Provides information about the currently loaded rule.<br>

The Tools Bar
-------------
*Run Simulation Button* - Runs the simulation <br>
*Simulate in Selection Button* - Runs the simulation in the selection <br>
*Simulate outside Selection Button* - Runs the simulation outside the selection <br>
*Drawing Button* - Go into drawing mode <br>
*Panning Button* - Go into panning mode <br>
*Selection Button* - Go into selection mode <br>
<br>
*Random Soup Button* - Generates a random soup <br>
*Flip Button* - Flips the pattern horizontally / vertically <br>
*Rotate Button* - Rotates the pattern clockwise / counter-clockwise <br>
*Increase / Decrease Step Size Button* - Increases / decreases the step size <br>
*Recording Button* - Records all patterns that move in the selection area and saves it as a \*.gif <br>
*Identify Button* - Identifies the pattern as a spaceship / oscillator <br>

The Status Bar
--------------
The generation of the pattern, the current population and simulation speed is shown.

Keyboard Shortcuts
------------------
<kbd>Space</kbd> - Step 1 Generation <br>
<kbd>Delete</kbd> - Deletes cells <br>

<kbd>Enter</kbd> - Toggle Simulation <br>
<kbd>Shift</kbd> + <kbd>Enter</kbd> - Toggle Simulation (In Selection) <br>
<kbd>Ctrl</kbd> + <kbd>Enter</kbd> - Toggle Simulation (Outside Selection) <br>

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

GUI TODO List
-------------
- [x] Add tooltips
- [x] Add menu buttons for delete, paste, copy and cut cells
- [x] Add menu button for new pattern
- [x] Add menu button to open rules & patterns
- [x] Add menu button to save patterns
- [x] Add button to view search results and save to a file
- [x] Add grid lines
- [x] Add help information to the about button
- [x] Add more information to the status bar
- [ ] Add dialog to ask "Would you like to save changes to untitled"
- [ ] Custom key binds

Editing Features
================
- [x] Drawing Cells
- [x] Selecting Cells
- [x] Delete Cells
- [x] Copy Cells to RLE
- [x] Flip Horizontally / Vertically
- [x] Rotate Clockwise / Counter-Clockwise
- [x] Pasting Cells from RLE
- [x] Undo
- [x] Select All
- [x] More Random Soup Symmetries
- [x] Simulate in selection
- [x] Simulate outside selection
- [ ] Reset Pattern to Generation 0
- [ ] Nudging up / down / left / right
- [ ] Invert Cells

Supported / Planned Rulespaces
==============================
- [ ] Higher Range Outer Totalistic (HROT)
    - [x] 2 State HROT
        - [x] B0 rules
        - [x] Weighted rules
        - [x] Apgtable Generation for B0 rules
        - [x] Apgtable Generation for non-B0 rules
        - [x] Apgtable Generation for weighted rules
    - [ ] HROT BSFKL
        - [ ] B0, F0 rules
        - [ ] Neighbourhood Weights
        - [ ] Apgtable Generation for all rules
    - [x] HROT Generations
        - [x] B0 Rules
        - [x] State Weights
        - [x] Neighbourhood Weights
        - [x] Apgtable Generation for B0 rules
        - [x] Apgtable Generation for non-B0 rules
        - [x] Apgtable Generation for rules with neighbourhood weights
        - [x] Apgtable Generation for rules with both state weights & neighbourhood weights
    - [x] HROT Extended Generations
        - [x] B0 Rules
        - [x] Neighbourhood Weights
        - [x] Apgtable Generation for all rules
    - [x] HROT Regenerating Generations
        - [x] B0 Rules
        - [x] Neighbourhood Weights
        - [x] Apgtable Generation for all rules
    - [ ] 3-state HROT
        - [ ] B0 Rules
        - [ ] Weighted Rules
        - [ ] Apgtable Generation for all rules
    - [x] Integer HROT
        - [x] Weighted Rules
        - [x] Apgtable Generation for all rules
    - [x] Deficient HROT
        - [x] B0 Rules
        - [x] Weighted Rules
        - [x] Apgtable Generation for all rules
    - [x] Multi-state Cyclic HROT
        - [x] Weighted Rules
        - [x] Apgtable Generation for all rules
- [ ] Isotropic Non-Totalistic (INT)
    - [x] 2 State INT
        - [x] B0 Rules
        - [x] Apgtable Generation
    - [ ] INT BSFKL
    - [x] INT Generations
        - [x] B0 Rules
        - [x] Apgtable Generation
    - [ ] INT Extended Generations
    - [ ] INT Regenerating Generations
    - [ ] 3-state INT
    - [x] Deficient INT
        - [x] B0 Rules
        - [x] Apgtable Generation
- [ ] Magma's Notation
- [ ] Primodia
- [ ] Naive Rules
    - [x] Orthogonal Reading Order (O)
    - [ ] Diagonal Reading Order (D)
    - [ ] Snake Reading Order (SN)
    - [ ] Spiral Reading Order (SP)
    - [x] Custom Reading Orders
- [x] Alternating Rules
    - [x] Strobing Rules
    - [x] Apgtable Generation
- [ ] Second Order Rules
- [ ] Multiple Neighbourhoods Cellular Automaton (MNCA)
- [ ] MCell Cyclic CA
- [x] Langton's Ant / Turmites
    - [x] Von Neumann Turmites
    - [x] Hexagonal Turmites
    - [ ] Moore Turmites
    - [ ] Triangular Turmites
    - [ ] Splitting Turmites
- [ ] Margolus
- [x] 1D CA
- [ ] [R]History
    - [x] 2-state HROT
    - [ ] HROT Generations
    - [x] 2-state INT
    - [ ] INT Generations
- [ ] [R]Symbiosis
    - [x] 2-state HROT
    - [ ] HROT Generations
    - [ ] 2-state INT
    - [ ] INT Generations
- [ ] [R]DeadlyEnemies
    - [x] 2-state HROT
    - [ ] HROT Generations
    - [ ] 2-state INT
    - [ ] INT Generations
- [ ] [R]Energetic
    - [ ] 2-state HROT
    - [ ] HROT Generations
    - [x] 2-state INT
    - [ ] INT Generations
- [ ] Custom Rules (*.rule)
    - [ ] Golly Ruletables
        - [x] Basic Functionality
        - [x] Unbounded Variables
        - [x] Arbitary Neighbourhoods
        - [x] B0 Rules
        - [ ] Block CA
        - [ ] Nutshell-like Syntax
    - [x] Golly Ruletrees
        - [x] Basic Functionality
        - [x] Arbitary Neighbourhoods
        - [x] B0 Rules
    - [x] Square Cell Ruletables
        - [x] Basic Functionality
        - [x] Arbitary Neighbourhoods
        - [x] B0 Rules

Supported / Planned INT Neighbourhoods
======================================
- [x] Range 1 Moore Isotropic Non-Totalistic (M)
- [x] Range 1 Hexagonal Isotropic Non-Totalistic (H)
- [x] Range 2 Von Neumann Isotropic Non-Totalistic (V2)
- [x] Range 2 Checkerboard Isotropic Non-Totalistic (B2)
- [x] Range 2 Far Corners Isotropic Non-Totalistic (FC)
- [x] Range 2 Knight Life Isotropic Non-Totalistic (K)
- [x] Range 2 Cross Isotropic Non-Totalistic (C2)
- [x] Range 3 Cross Isotropic Non-Totalistic (C3)
- [x] Range 3 Far Edges Isotropic Non-Totalistic (FE)

Supported / Planned Named HROT Neighbourhoods
=============================================
- [x] Asterisk (A)
- [x] Checkerboard (B)
- [x] Aligned Checkerboard (b)
- [x] Circular (C)
- [ ] Triangular Neighbourhood on Triangular Grid (D)
- [x] Gaussian Weighted Neighbourhood (G)
- [x] Hexagonal (H)
- [x] Triangular Moore (L)
- [x] Moore (M)
- [x] Von Neumann (N)
- [ ] Triangular Neighbourhood on Hexagonal Grid (T)
- [x] Saltire (X)
- [x] Euclidean (2)
- [x] Tripod (3)
- [ ] Hexagram (6)
- [x] Star (*)
- [x] Cross (+)
- [x] Hash (#)
- [x] CoordCA Format (Custom Neighbourhoods @)
- [x] LifeViewer Format (Weighted Neighbourhoods W)
- [x] LifeViewer Format (State Weights W)

Search Programs
===================
- [ ] Rule Search Program
    - [x] More high tech repetition detection function
    - [x] Identification for linear growth patterns, replicators
    - [ ] Fine tune RNG function manually
    - [ ] Fine tune RNG function via genetic algorithms?
    - [ ] Object separation?
- [ ] Agar Search Program
  - [ ] Better repetition detection
  - [ ] Better wave detection
- [ ] Catalyst Search Program
    - [x] Mutiple catalysts
    - [ ] Brute force option
    - [ ] Symmetries
    - [ ] Repetition detection
- [ ] Brute Force / Soup Search Program
    - [ ] Object separation
    - [ ] Symmetries
    - [ ] Finish Documentation
- [ ] Methuselah Search Program
- [ ] Ship / Oscillator Search Program

Long-term TODO List
===================
- [x] Custom search programs
- [x] Move stuff to the wiki
- [x] Triangular Rules
- [x] Command Line Interface
    - [x] Simulation
    - [x] Identification
    - [x] Search Programs
    - [x] Apgtable Generation
    - [x] Random Soup / Symmetries Generation
- [ ] Add Unit Tests
    - [x] Write the first one
    - [x] Rule Families
    - [x] Identification
    - [x] Simulator
    - [ ] Pattern Manipulation
    - [ ] GUI
- [ ] Bounded Grids
    - [x] Bounded
    - [x] Torus
    - [ ] Klein Bottle & Cross Surface
    - [ ] Spherical
- [ ] Hexagonal Rendering
- [ ] Triangular Rendering
- [ ] Accept some LifeViewer commands like STEP & RANDOMISE
- [ ] Scripting in Python via Jython?
- [ ] Implement a faster algorithm (QuickLife, HashLife...)

Known Bugs
==========
- [x] RLEs are pasted rotated (Fixed)
- [x] Open pattern is bugged (Fixed)
- [x] HROT B0 isn't working as intended (Fixed)
- [x] Keyboard shortcuts only work when the ScrollPane is in focus (Fixed)
- [x] Rule Dialog don't show the most updated rule (Fixed)
- [x] ConcurrentModificationException is thrown (Fixed)
- [x] Step with space causes some cells to be rendered incorrectly (Fixed)
- [ ] Viewport doesn't move the center when a new pattern is loaded
