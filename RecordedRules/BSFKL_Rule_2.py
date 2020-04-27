# BSFKL
birth = {7, 8, 16, 19, 21, 24}
survival = {6, 7, 8, 9, 10, 16, 18, 19, 20, 21}
forcing = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 22, 23}
killing = {6, 13, 14, 15, 17}
living = {0, 1, 2, 3, 4, 5, 6, 8, 9, 12, 13, 14, 15, 16, 17, 18, 20, 21, 24}

# Information about the Rule (Must be filled)
n_states = 3  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "BSFKL_Rule_2"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(1, -1), (1, 1), (-1, 1), (-1, -1),
            (1, 0), (0, 1), (-1, 0), (0, -1),
            (1, -2), (1, 2), (-1, 2), (-1, -2),
            (2, -1), (2, 1), (-2, 1), (-2, -1),
            (2, -2), (2, 2), (-2, 2), (-2, -2),
            (3, 0), (0, 3), (-3, 0), (0, -3)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):
    n_living = neighbours.count(1)
    n_destructive = neighbours.count(2)

    if neighbours[-1] == 1:
        if n_destructive in killing:
            return 0
        elif n_living in survival:
            return 1
        return 2

    elif neighbours[-1] == 2:
        if n_living in living:
            return 0
        return 2

    else:
        if n_destructive in forcing and n_living in birth:
            return 1
        return 0


# Does the next state of the cell depend on its neighbours?
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
