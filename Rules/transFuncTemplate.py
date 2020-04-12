# Information about the Rule (Must be filled)
# Change to suit the rule
n_states = 2  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "DiagonalLife"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(1, -1), (1, 1), (-1, 1), (-1, -1),  # Change to desired neighbourhood
            (2, 0), (0, 2), (-2, 0), (0, -2)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):  # Change Transition Function to Desired One
    n = sum(neighbours[:-1])
    if neighbours[-1] == 0:
        if n == 3:
            return 1
        return 0
    else:
        if n == 3 or n == 2:
            return 1
        return 0


# Does the next state of the cell depend on its neighbours? (Speeds up Simulation, Useful for Generations)
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
