# Information about the Rule (Must be filled)
n_states = 5  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "OscillatorBuilder"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(1, -1), (1, 1), (-1, 1), (-1, -1),
            (1, 0), (0, 1), (-1, 0), (0, -1)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):
    n = 0
    for i in range(len(neighbours) - 1):
        if neighbours[i] > 0 and not neighbours[i] % 2:
            n += neighbours[i]

    if 2 <= n <= 3 or n == 10 or 12 <= n <= 18: return neighbours[8]
    if (n == 4) and neighbours[8] < 4: return neighbours[8] + 1
    if neighbours[8] > 1: return neighbours[8] - 1
    return 0


# Does the next state of the cell depend on its neighbours? (Speeds up Simulation, Useful for Generations)
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
