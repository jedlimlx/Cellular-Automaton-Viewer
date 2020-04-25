import re
rule_string = "(2,3)2,3/(3,4)2,3"

birth = set([int(x) for x in re.findall("\((.*?)\)", rule_string.split("/")[1])[0].split(",")])
survival = set([int(x) for x in re.findall("\((.*?)\)", rule_string.split("/")[0])[0].split(",")])

other_birth = set([int(x) for x in re.sub("\(.*?\)", "", rule_string.split("/")[1]).split(",")])
other_survival = set([int(x) for x in re.sub("\(.*?\)", "", rule_string.split("/")[0]).split(",")])

# Information about the Rule (Must be filled)
n_states = 2  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "BokaBB_Rule_1"  # Rule Name


legit_neighbourhood = [(1, 0), (0, 1), (-1, 0), (0, -1)]
nonlegit_neighbourhood = []
for i in legit_neighbourhood:
    for j in legit_neighbourhood:
        nonlegit_neighbourhood.append((i[0] + j[0], i[1] + j[1]))

nonlegit_neighbourhood += legit_neighbourhood
nonlegit_neighbourhood = sorted(list(set(nonlegit_neighbourhood)))

# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return nonlegit_neighbourhood


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):
    n_birth = 0
    n_survival = 0
    for neighbour in legit_neighbourhood:
        n = 0
        for neighbour2 in legit_neighbourhood:
            if neighbours[get_neighbourhood(generation).index(
                    (neighbour[0] + neighbour2[0], neighbour[1] + neighbour2[1]))] == 0:
                n += 1

        if n in other_birth: n_birth += 1
        if n in other_survival: n_survival += 1

    if neighbours[-1] == 1:
        if n_survival in survival:
            return 1
        return 0

    elif neighbours[-1] == 0:
        if n_birth in birth:
            return 1
        return 0


# Does the next state of the cell depend on its neighbours?
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
