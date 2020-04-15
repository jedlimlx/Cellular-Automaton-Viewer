"""
Rule Format (.ca_rule)
--------------
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

"""
import re
from typing import List, Tuple, Set

colour_palette_count: int = 0
colour_palette = []
parsing_colour_palette: bool = False

rule_name: str = ""
n_states: int = 0
state_weights: List[List[int]] = []


def load(filename):
    global colour_palette, colour_palette_count, parsing_colour_palette, \
        rule_name, n_states, state_weights
    file = open(filename, "r")
    rule: str = file.read()

    for section in rule.split("\n"):
        if "Colour Palette:" in section:
            parsing_colour_palette = True
            continue
        elif parsing_colour_palette:
            if "None" in section:
                colour_palette = None
                parsing_colour_palette = False
                continue
            colour_palette.append([int(re.sub("[() ]", "", x)) for x in section.split(",")])
            colour_palette_count += 1
            if colour_palette_count == n_states:
                parsing_colour_palette = False
            continue

        if "Name:" in section:
            rule_name = section.replace("Name: ", "")
        elif "State Weights:" in section:
            state_weights = [[int(x) for x in y.split(",")]
                             for y in section.replace("State Weights: ", "").split("|")]
            n_states = len(state_weights[0])
