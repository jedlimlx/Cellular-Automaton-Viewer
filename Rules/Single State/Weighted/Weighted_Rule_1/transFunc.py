rule_string = "4,5,6/6,7"
birth = set([int(x) for x in rule_string.split("/")[1].split(",")])
survival = set([int(x) for x in rule_string.split("/")[0].split(",")])

# Information about the Rule (Must be filled)
n_states = 2  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "Weighted_Rule_1"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(1, -1), (1, 1), (-1, 1), (-1, -1),
            (1, 0), (0, 1), (-1, 0), (0, -1),
            (2, 0), (0, 2), (-2, 0), (0, -2)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):
    n = 0
    weights = [1, 1, 1, 1,
               2, 2, 2, 2,
               3, 3, 3, 3]

    for i in range(len(neighbours) - 1):
        n += neighbours[i] * weights[i]

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
