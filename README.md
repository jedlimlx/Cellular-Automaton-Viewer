# Cellular-Automaton-Viewer

See python branch for CAViewer v1.

Installation
============
CAViewer v2 is written in Java.<br>
You will need the Java Runtime Environment / Java Development Kit to run the *.jar file.<br>
If you use Windows, you can use the *.exe file that doesn't need Java.<br>

Compiling from source
=====================
CAViewer v2 uses JavaFX. You will need to download JavaFX from https://gluonhq.com/products/javafx/. <br>
Then, compile to a *.jar using FakeMain.java.

Supported / Planned Rulespaces
====================
- [x] Single State HROT
- [ ] Integer HROT
- [ ] HROT BSFKL
- [ ] HROT Generations
- [ ] HROT Extended Generations
- [ ] HROT Regenerating Generations
- [ ] 3-state HROT


- [ ] Single State INT
- [ ] INT BSFKL
- [ ] INT Generations
- [ ] INT Extended Generations
- [ ] INT Regenerating Generations
- [ ] 3-state INT


- [ ] Naive Rules
- [ ] Alternating Rules


- [ ] Langton's Ant


- [ ] Golly Ruletables
- [ ] Golly Ruletrees


- [ ] Square Cell Ruletables

Supported / Planned INT Neighbourhoods
======================================
- [ ] Range 1 Moore Isotropic Non-Totalistic
- [ ] Range 1 Hexagonal Isotropic Non-Totalistic
- [ ] Range 2 Von Neumann Isotropic Non-Totalistic
- [ ] Range 2 Checkerboard Isotropic Non-Totalistic
- [ ] Range 2 Far Corners Isotropic Non-Totalistic
- [ ] Range 2 Far Edges Isotropic Non-Totalistic
- [ ] Range 2 Knight Life Isotropic Non-Totalistic
- [ ] Range 2 Cross Isotropic Non-Totalistic
- [ ] Range 3 Cross Isotropic Non-Totalistic

Long-term TODO List
===================
- [ ] Bounded Grids
- [ ] Agar Searching Program
- [ ] Catalyst Search Program
- [ ] Triangular Rules
- [ ] Hexagonal Rendering
- [ ] Implement a faster algorithm (QuickLife, HashLife...)
- [ ] Accept some LifeViewer commands like STEP & RANDOMISE
