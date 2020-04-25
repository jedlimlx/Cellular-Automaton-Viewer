rule_string = "rg3/l1/b3/s2,3/rb3,6/rs5,8"
birth = set([int(x) for x in rule_string.split("/")[2].replace("b", "").split(",")])
survival = set([int(x) for x in rule_string.split("/")[3].replace("s", "").split(",")])
regen_birth = set([int(x) for x in rule_string.split("/")[4].replace("rb", "").split(",")])
regen_survival = set([int(x) for x in rule_string.split("/")[5].replace("rs", "").split(",")])

birth_state = int(rule_string.split("/")[1].replace("l", ""))

# Information about the Rule (Must be filled)
n_states = int(rule_string.split("/")[0].replace("rg", ""))  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "RegenLife"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(1, -1), (1, 1), (-1, 1), (-1, -1),
            (1, 0), (0, 1), (-1, 0), (0, -1)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):
    n = 0
    for i in neighbours[:-1]:
        if i == 1: n += 1

    if neighbours[-1] == 0:
        if n in birth:
            return birth_state
        return 0
    elif neighbours[-1] == 1:
        if n in survival:
            return 1
        return 2
    else:
        if n in regen_birth:
            return neighbours[-1] - 1
        elif n in regen_survival:
            return neighbours[-1]
        return (neighbours[-1] + 1) % n_states


# Does the next state of the cell depend on its neighbours?
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
