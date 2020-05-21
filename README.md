# Cellular-Automaton-Viewer
Installation
=================================
For Windows, you can skip this step. <br>
For Linux, run the following commands (credit to martin.novy) <br>
```
/usr/bin/sudo  apt install  python3-pyqt5  python3-pyperclip  python3-numpy python3-pil
/usr/bin/sudo  apt install  cython3

cd CAViewer/

cd CACompute
touch __init__.py
cp -i Compute.cpp compute.cpp
python3  setup.py --help
python3  setup.py build_ext --inplace
cd ..

cd CAComputeParse/
touch __init__.py
cp -i Compute.cpp compute.cpp
python3  setup.py build_ext --inplace
cd ..


#export DISPLAY=:0
python3   Main.py 
```


Instructions
=================================
To Start the Program, Run RunMain.bat or Main.py<br>

Patterns are stored as rles.<br>

After you select the Pattern / Area with the Selection Tool<br>
To Copy, Press Ctrl+C<br>
To Cut, Press Ctrl+X<br>
To Delete, Press Del<br>
To Paste, in the area, Press Ctrl+V<br>
To Generate Random Soup, Press the Button with a Soup<br>
To Identify the Pattern, Press the Question Mark Button (Supports Still Lifes, Oscillators, Spaceships, Guns and Replicators)<br>
To Record the Patterns in the Area Selected, Press the Record Button and it will turn black.<br>
Press it again to stop the recording and save the gif.<br>

In the file menu, you can save, open, and create a new pattern.<br>
In the edit menu, you can copy, cut, delete and paste a pattern.<br>
In the control menu, you can adjust random soup and simulation settings and step forward 1 generation and start the simulation.<br>
In the view menu, you can zoom in, out, set the zoom and toggle grid lines.<br>

Note
----
When you load a pattern, it does not necessarily mean that the corresponding rule is loaded. <br>
You have to load the rule first. (Only true if you modified the rule or have not opened the rule before)

Rule Format
=================================
Rules are defined as Python Code. A sample rule is found in Rules/transFuncTemplate.py<br>

If you do not know how to code Python, use the .ca_rule format.<br>
The format is the following:<br>

```
Name: Hello World (Can be Anything you like)

Neighbourhood Range: 2 (Can be Any Number, >3 gets slow)

Neighbourhood: (Use Commas, Numbers are Weights, For Alternating Place '-' below it and continue)
0,0,0,0,0
0,1,1,1,0
0,1,0,1,0
0,1,1,1,0
0,0,0,0,0
######### (As many as you like, Recommended is Same Length)
0,0,1,0,0
0,1,1,1,0
1,1,0,1,1
0,1,1,1,0
0,0,1,0,0

State Weights: 0,1 -> Separate by Commas, For Alternating Put | (No Max, Don't Leave Spaces)

Rulespace: BSFKL / Extended Generations / Outer Totalistic / Regenerating Generations
Will Add 3-state Outer Totalistic Soon

B/S Conditions: Outer Totalistic / Double Totalistic / Range 1 Moore Isotropic Non-Totalistic / Range 2 Cross Isotropic Non-Totalistic / Range 2 Von Neumann Isotropic Non-Totalistic

Rulestring: -> For Alternating Put | (No Max, Don't Leave Spaces)
b1s2f3k4l5 or 1/2/3/4/5 (BSFKL)
b3,4,5s4d1-1-1 or 4/3,4,5/1-1-1 (Extended Generations)
b3s2,3 or 2,3/3 (Outer Totalistic)

(Must Add Commas because of Extended Neighbourhood (except if you use a bscondition that has a fixed neighbourhood), Don't Leave Spaces)

Colour Palette: -> RGB (To Tell Program to Auto Generate Put None Below Colour Palette)
(0, 0, 0)
(255, 255, 255)
```

Algorithm
=================================

Cells are stored in a sparse matrix represented by a dictionary.<br>
For example, {(1, 2): 2, (2, 2): 1, (3,2 ): 2}<br>

Obtaining Neighbours -> O(no. of neighbours)<br>
Updating Coordinates -> O(1)<br>

Steps
-----
Initialise an empty set cells_to_check<br>
Loop through the set cells_changed (contains cells that changed in the previous generation)<br>
For every cell in cells_changed<br>
&nbsp;&nbsp;&nbsp;&nbsp;Add the cell itself into cells_to_check as well as it's neighbours<br>

Loop through the set cells_to_check<br>
For every cell in cells_to_check<br>
&nbsp;&nbsp;&nbsp;&nbsp;Check if the next state of the cell depends on its neighbours (For example, it won't if it's generations)<br>
&nbsp;&nbsp;&nbsp;&nbsp;If it depends on neighbours<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Get neighbours of the cell and pass to transition function<br>
&nbsp;&nbsp;&nbsp;&nbsp;Update Cells in the dictionary<br>

Troubleshooting
================================
When you run the simulation, it the cells do not change, it could be because there is an error in your transition function or because there is an error in the program.<br>
Check the console for error output.<br>
If you suspect a bug, please report it.<br>

Sample Recording
================================
![Sample Recording as GIF](Recording.gif)
