rule_string = "12,13,14,15,16,17,18,19,20,21,22,23/15,16,17,18,19,20,21,22,23,45"
birth = set([int(x) for x in rule_string.split("/")[1].split(",")])
survival = set([int(x) for x in rule_string.split("/")[0].split(",")])

# Information about the Rule (Must be filled)
n_states = 2  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "OuterTotalistic_Rule_1"  # Rule Name


range_3_moore = []
for i in range(-3, 4):
    for j in range(-3, 4):
        if i == 0 and j == 0: continue
        range_3_moore.append((i, j))


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return range_3_moore


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):
    n = 0

    for i in range(len(neighbours)):
        n += neighbours[i]

    if neighbours[-1] == 1:
        if n in survival:
            return 1
        return 0

    elif neighbours[-1] == 0:
        if n in birth:
            return 1
        return 0


# Does the next state of the cell depend on its neighbours?
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
