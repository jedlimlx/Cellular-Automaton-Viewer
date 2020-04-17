rule_table_string = """
0,0,0,0,0,3,1,0,0,0,3,0,0,2,0,0,0,2,0,0,0,0,0,0,0,3,1,0,1,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0
2,2,2,2,2,2,2,2,1,2,2,1,2,2,1,2,2,2,2,2,2,2,2,2,2,2,1,2,2,2,2,2,2,3,2,1,1,2,2,2,2,1,2,2,2,1,2,1,2,2,2,3,1,1,2,1,1,1,1,1,2,2,2,2,2,2,2,1,2,2,2,2,1,2,2,2,1,1,2,2,1,2
3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,3,3,3,3,3,3,3,3,3,3,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3
0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"""

lst = []
rule_table = []
for i in rule_table_string.split("\n")[1:]:
    lst = []
    for j in i.split(",")[:-1]:
        lst.append(int(j))

    rule_table.append(lst)


# Information about the Rule (Must be filled)
n_states = len(rule_table)  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "Rule_1"  # Rule Name


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
    state_weights = [0, 1, 0, 0, 0]

    for i in range(len(neighbours) - 1):
        n += weights[i] * state_weights[neighbours[i]]

    try:
        if n >= 0:
            return rule_table[neighbours[-1]][n]
        else:
            return 0
    except IndexError:
        return 0


# Does the next state of the cell depend on its neighbours?
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
