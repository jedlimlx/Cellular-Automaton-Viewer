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

file = open("Rules/sample.ca_rule", "r")
rule: str = file.read()

neighbourhood_count: int = 0
current_neighbourhood_weights: List[List[int]] = []
neighbourhood_weights: List[List[List[int]]] = []
parsing_neighbourhood: bool = False

colour_palette_count: int = 0
colour_palette = []
parsing_colour_palette: bool = False

rule_name: str = ""
rule_space: str = ""
rule_string: List[str] = ""
n_states: int = 0
neighbourhood_range: int = 0
state_weights: List[List[int]] = []

for section in rule.split("\n"):
    if "Neighbourhood:" in section or "#" in section:
        neighbourhood_count = 0
        current_neighbourhood_weights = []
        parsing_neighbourhood = True
        continue
    elif parsing_neighbourhood:
        current_neighbourhood_weights.append([int(x) for x in section.split(",")])
        neighbourhood_count += 1
        if neighbourhood_count == neighbourhood_range * 2 + 1:
            parsing_neighbourhood = False
            neighbourhood_weights.append(current_neighbourhood_weights)
        continue

    if "Colour Palette:" in section:
        parsing_colour_palette = True
        continue
    elif parsing_colour_palette:
        if "None" in section:
            colour_palette = None
            parsing_colour_palette = False
            continue
        colour_palette.append(tuple([int(re.sub("[() ]", "", x)) for x in section.split(",")]))
        colour_palette_count += 1
        if colour_palette_count == n_states:
            parsing_colour_palette = False
        continue

    if "Name:" in section:
        rule_name = section.replace("Name: ", "")
    elif "Neighbourhood Range:" in section:
        neighbourhood_range = int(section.replace("Neighbourhood Range: ", ""))
    elif "State Weights:" in section:
        state_weights = [[int(x) for x in y.split(",")]
                         for y in section.replace("State Weights: ", "").split("|")]
        n_states = len(state_weights[0])
    elif "Rulespace:" in section:
        rule_space = section.replace("Rulespace: ", "")
    elif "Rulestring:" in section:
        rule_string = section.replace("Rulestring: ", "").split("|")

neighbourhood: List[List[Tuple[int, int]]] = []
for weights in neighbourhood_weights:
    lst: List[Tuple[int, int]] = []
    for i in range(-neighbourhood_range, neighbourhood_range + 1):
        for j in range(-neighbourhood_range, neighbourhood_range + 1):
            if weights[i + neighbourhood_range][j + neighbourhood_range] != 0:
                lst.append((i, j))

    neighbourhood.append(lst)

alternating_period: int = len(state_weights)
copy_neighbourhood_weights = neighbourhood_weights[:]
neighbourhood_weights: List[List[int]] = []
for weights in copy_neighbourhood_weights:
    lst: List[int] = []
    for i in weights:
        for j in i:
            if j != 0:
                lst.append(j)

    neighbourhood_weights.append(lst)


def get_neighbourhood(generations):
    return neighbourhood[generations % alternating_period]


if rule_space == "Outer Totalistic":
    birth: List[Set[int]] = []
    survival: List[Set[int]] = []
    for rule in rule_string:
        if "/" in rule:
            birth.append(set([int(x) for x in rule.split("/")[1].split(",")]))
            survival.append(set([int(x) for x in rule.split("/")[0].split(",")]))
        else:
            birth.append(set([int(x) for x in re.split("[bs]", rule)[1].split(",")]))
            survival.append(set([int(x) for x in re.split("[bs]", rule)[2].split(",")]))


    def transition_func(neighbours, generations):
        n = 0
        for i in range(len(neighbours) - 1):
            n += neighbourhood_weights[generations % alternating_period][i] * \
                 state_weights[generations % alternating_period][neighbours[i]]

        if neighbours[-1] == 1:
            if n in survival[generations % alternating_period]:
                return 1
            return 0
        else:
            if n in birth[generations % alternating_period]:
                return 1
            return 0


    def depend_on_neighbours(state, generations):
        return -1

elif rule_space == "BSFKL":
    birth: List[Set[int]] = []
    survival: List[Set[int]] = []
    forcing: List[Set[int]] = []
    killing: List[Set[int]] = []
    living: List[Set[int]] = []
    for rule in rule_string:
        if "/" in rule:
            birth.append(set([int(x) for x in rule.split("/")[0].split(",")]))
            survival.append(set([int(x) for x in rule.split("/")[1].split(",")]))
            forcing.append(set([int(x) for x in rule.split("/")[2].split(",")]))
            killing.append(set([int(x) for x in rule.split("/")[3].split(",")]))
            living.append(set([int(x) for x in rule.split("/")[4].split(",")]))
        else:
            birth.append(set([int(x) for x in re.split("[bsfkl]", rule)[1].split(",")]))
            survival.append(set([int(x) for x in re.split("[bsfkl]", rule)[2].split(",")]))
            forcing.append(set([int(x) for x in re.split("[bsfkl]", rule)[3].split(",")]))
            killing.append(set([int(x) for x in re.split("[bsfkl]", rule)[4].split(",")]))
            living.append(set([int(x) for x in re.split("[bsfkl]", rule)[5].split(",")]))

    def transition_func(neighbours, generation):
        n_living, n_destructive = 0, 0
        for i in range(len(neighbours) - 1):
            if neighbours[i] == 1:
                n_living += neighbourhood_weights[generation % alternating_period][i]
            elif neighbours[i] == 2:
                n_destructive += neighbourhood_weights[generation % alternating_period][i]

        if neighbours[-1] == 1:
            if n_destructive in killing[generation % alternating_period]:
                return 0
            elif n_living in survival[generation % alternating_period]:
                return 1
            return 2

        elif neighbours[-1] == 2:
            if n_living in living[generation % alternating_period]:
                return 0
            return 2

        else:
            if n_destructive in forcing[generation % alternating_period] and \
                    n_living in birth[generation % alternating_period]:
                return 1
            return 0


    def depend_on_neighbours(state, generations):
        return -1

elif rule_space == "Extended Generations":
    birth: List[Set[int]] = []
    survival: List[Set[int]] = []
    activity_list: List[Set[int]] = []
    inactivity_list: List[Set[int]] = []
    for rule in rule_string:
        if "/" in rule:
            birth.append(set([int(x) for x in rule.split("/")[1].split(",")]))
            survival.append(set([int(x) for x in rule.split("/")[0].split(",")]))
            string: str = rule.split("/")[-1].split("-")
        else:
            birth.append(set([int(x) for x in re.split("[bsd]", rule)[1].split(",")]))
            survival.append(set([int(x) for x in re.split("[bsd]", rule)[2].split(",")]))
            string: str = re.split("[bsd]", rule)[-1].split("-")

        num, alt = 1, 1
        activity = set()
        inactivity = {0}
        for i in string:
            for j in range(num, int(i) + num):
                if alt > 0:
                    activity.add(j)
                else:
                    inactivity.add(j)
                num += 1

            alt *= -1

        activity_list.append(activity)
        inactivity_list.append(inactivity)

    def transition_func(neighbours, generations):
        n = 0
        for i in range(len(neighbours) - 1):
            n += neighbourhood_weights[generations % alternating_period][i] * \
                 state_weights[generations % alternating_period][neighbours[i]]

        if neighbours[-1] in activity_list[generations % alternating_period]:
            if n in survival[generations % alternating_period]:
                return neighbours[-1]
            return (neighbours[-1] + 1) % n_states

        elif neighbours[-1] == 0:
            if n in birth[generations % alternating_period]:
                return 1
            return 0

        else:
            return (neighbours[-1] + 1) % n_states

    def depend_on_neighbours(state, generations):
        if state in activity_list[generations % alternating_period] or state == 0:
            return -1
        else:
            return (state + 1) % n_states
