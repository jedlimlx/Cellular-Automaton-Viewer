import re
rule_string = "[2/6+0,1,4]"
splitted_rule_string = re.split("[/+,]", rule_string)
S = int(splitted_rule_string[0][1:])
K = int(splitted_rule_string[1])
L = int(splitted_rule_string[2])
M = int(splitted_rule_string[3])
N = int(splitted_rule_string[4][:-1])

# Information about the Rule (Must be filled)
n_states = int(splitted_rule_string[0][1:]) + 1  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "Primodia"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(1, -1), (1, 1), (-1, 1), (-1, -1),  # Change to desired neighbourhood
            (1, 0), (0, 1), (-1, 0), (0, -1)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):  # Change Transition Function to Desired One
    n = sum(neighbours[:-1])
    if K <= n <= K + L:
        return neighbours[-1] + 1 if neighbours[-1] + 1 <= S else neighbours[-1]
    elif K - M <= n <= K + L + N:
        return neighbours[-1]
    else:
        return neighbours[-1] - 1 if neighbours[-1] - 1 >= 0 else neighbours[-1]


# Does the next state of the cell depend on its neighbours? (Speeds up Simulation, Useful for Generations)
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
