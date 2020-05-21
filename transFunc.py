def parse(rulestring):
    transitions = set()
    current_trans = []
    for i in rulestring:
        current_trans.append(int(i))
        if len(current_trans) == 2:
            transitions.add(tuple(current_trans))
            current_trans = []

    return transitions


rule_string = "3,0,4,1,5,2/4,0,5,1,6,2/2,0,3,0,3,1,4,1,4,2,5,2,5,3/0,3,1,4,2,5/0,4,1,5,2,6/0,2,0,3,1,3,1,4,2,4,2,5,3,5"
birth_1 = parse(rule_string.split("/")[0].split(","))
mutate_1 = parse(rule_string.split("/")[1].split(","))
survival_1 = parse(rule_string.split("/")[2].split(","))
birth_2 = parse(rule_string.split("/")[3].split(","))
mutate_2 = parse(rule_string.split("/")[4].split(","))
survival_2 = parse(rule_string.split("/")[5].split(","))

# Information about the Rule (Must be filled)
n_states = 3  # Number of States
alternating_period = 2  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "BMS"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(1, 1), (1, -1), (-1, 1), (-1, -1),
            (0, 1), (1, 0), (-1, 0), (0, -1)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):
    n1 = neighbours[:-1].count(1)
    n2 = neighbours[:-1].count(2)

    if neighbours[-1] == 0:
        if (n1, n2) in birth_1:
            return 1
        elif (n1, n2) in birth_2:
            return 2
        return 0
    elif neighbours[-1] == 1:
        if (n1, n2) in survival_1:
            return 1
        elif (n1, n2) in mutate_1:
            return 2
        return 0
    else:
        if (n1, n2) in survival_2:
            return 2
        elif (n1, n2) in mutate_2:
            return 1
        return 0


# Does the next state of the cell depend on its neighbours?
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
