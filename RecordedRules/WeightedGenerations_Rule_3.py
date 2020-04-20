rule_string = "26,29,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,54,55,56,61,62,63/" \
              "18,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,46,65,67/1-2-1-8"
birth = set([int(x) for x in rule_string.split("/")[1].split(",")])
survival = set([int(x) for x in rule_string.split("/")[0].split(",")])

num, alt = 1, 1
activity = set()
inactivity = {0}
for i in rule_string.split("/")[-1].split("-"):
    for j in range(num, int(i) + num):
        if alt > 0:
            activity.add(j)
        else:
            inactivity.add(j)
        num += 1

    alt *= -1

# Information about the Rule (Must be filled)
n_states = sum([int(x) for x in rule_string.split("/")[-1].split("-")]) + 1  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "WeightedGenerations_Rule_3"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(2, -2), (2, -1), (2, 0), (2, 1), (2, 2),
            (1, -2), (1, -1), (1, 0), (1, 1), (1, 2),
            (0, -2), (0, -1), (0, 0), (0, 1), (0, 2),
            (-1, -2), (-1, -1), (-1, 0), (-1, 1), (-1, 2),
            (-2, -2), (-2, -1), (-2, 0), (-2, 1), (-2, 2)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):
    n = 0
    weights = [1, 2, 3, 2, 1,
               2, 4, 6, 4, 2,
               3, 6, 9, 6, 3,
               2, 4, 6, 4, 2,
               1, 2, 3, 2, 1]

    for i in range(len(neighbours) - 1):
        if neighbours[i] in activity: n += weights[i]

    if neighbours[-1] in activity:
        if n in survival:
            return neighbours[-1]
        return (neighbours[-1] + 1) % n_states

    elif neighbours[-1] == 0:
        if n in birth:
            return 1
        return 0

    else:
        return (neighbours[-1] + 1) % n_states


# Does the next state of the cell depend on its neighbours?
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    if state in activity or state == 0:
        return -1
    else:
        return (state + 1) % n_states
