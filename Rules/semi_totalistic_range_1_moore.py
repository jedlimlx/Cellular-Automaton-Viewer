rule_string = "1c0e0c2e2c0e0c3e1c2e2c1e3c0e/0c2e1c2e2c1e"

current_trans = []
birth, survival = set(), set()
for i in rule_string.split("/")[1]:
    if i == "e" or i == "c": continue
    else:
        current_trans.append(int(i))
        if len(current_trans) == 2:
            birth.add(tuple(current_trans))
            current_trans = []

current_trans = []
for i in rule_string.split("/")[0]:
    if i == "e" or i == "c": continue
    else:
        current_trans.append(int(i))
        if len(current_trans) == 2:
            survival.add(tuple(current_trans))
            current_trans = []


# Information about the Rule (Must be filled)
# Change to suit the rule
n_states = 2  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "Semi-Totalistic"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(1, -1), (1, 1), (-1, 1), (-1, -1),  # Change to desired neighbourhood
            (1, 0), (0, 1), (-1, 0), (0, -1)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):  # Change Transition Function to Desired One
    n_edge, n_corner = 0, 0
    for i in range(len(neighbours) - 1):
        if 0 <= i <= 3:
            n_corner += neighbours[i]
        else:
            n_edge += neighbours[i]

    if neighbours[-1] == 0:
        if (n_corner, n_edge) in birth:
            return 1
        return 0
    elif neighbours[-1] == 1:
        if (n_corner, n_edge) in survival:
            return 1
        return 0


# Does the next state of the cell depend on its neighbours? (Speeds up Simulation, Useful for Generations)
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
