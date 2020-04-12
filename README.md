# Cellular-Automaton-Viewer
Instructions
=================================
To Start the Program, Run RunMain.bat or Main.py

Patterns are stored as rles.

After you select the Pattern / Area with the Selection Tool
To Copy, Press Ctrl+C
To Cut, Press Ctrl+X
To Delete, Press Del
To Paste, in the area, Press Ctrl+V
To Generate Random Soup, Press the Button with a Soup
To Generate Multi-State Random Soup, Press the Button with a Soup and a 2 on it
To Identify the Pattern, Press the Question Mark Button (Supports Still Lifes, Oscillators and Spaceships)
To Record the Patterns in the Area Selected, Press the Record Button and it will turn black.
Press it again to stop the recording and save the gif.

In the file menu, you can save, open, and create a new pattern.
In the edit menu, you can copy, cut, delete and paste a pattern.
In the control menu, you can adjust random soup and simulation settings and step forward 1 generation.
In the zoom menu, you can zoom in and out and set the zoom.

Rule Format
=================================
Rules are defined as Python Code. A sample rule is found in Rules/transFuncTemplate.py

If you do not know how to code Python, use the .ca_rule format.
I have added a script to parse the .ca_rule format. It is transFunc.py in the root folder.
To change the rule, edit Rules/sample.ca_rule.
In future, the format will be supported natively.

The format is the following:

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

Rulespace: BSFKL / Extended Generations / Outer Totalistic
Will Add 3-state Outer Totalistic and Range 2 Isotropic Von Neumann (Need Help with This) Soon

Rulestring: -> For Alternating Put | (No Max, Don't Leave Spaces)
b1s2f3k4l5 or 1/2/3/4/5 (BSFKL)
b3,4,5s4d1-1-1 or 4/3,4,5/1-1-1 (Extended Generations)
b3s2,3 or 2,3/3 (Outer Totalistic)

(Must Add Commas because of Extended Neighbourhood, Don't Leave Spaces)

Colour Palette: -> RGB (To Tell Program to Auto Generate Put None Below Colour Palette)
1 (0, 0, 0)
2 (255, 255, 255)
```

Troubleshooting
================================
When you run the simulation, it the cells do not change, it could be because there is an error in your transition function or because there is an error in the program.
Check the console for error output.
If you suspect a bug, please report it.

Sample Recording
================================
![Sample Recording as GIF](Recording.gif)
