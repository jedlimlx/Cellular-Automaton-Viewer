rule_string = "0xgxlxn1cccdcgcickcl2cacccdcenanc3ca/0xn1cgcickcl2cccdcenc3ca"
outside_four_cells = {
    "a": [0, 0, 0, 0],
    "c": [1, 0, 0, 0],
    "d": [0, 1, 0, 0],
    "e": [0, 0, 1, 0],
    "f": [0, 0, 0, 1],
    "g": [1, 1, 0, 0],
    "i": [0, 1, 1, 0],
    "j": [0, 0, 1, 1],
    "k": [1, 0, 0, 1],
    "l": [1, 0, 1, 0],
    "m": [0, 1, 0, 1],
    "n": [1, 1, 1, 0],
    "o": [0, 1, 1, 1],
    "p": [1, 0, 1, 1],
    "q": [1, 1, 0, 1],
    "r": [1, 1, 1, 1]
}
hensel = {0: {"a": [0, 0, 0, 0, 0, 0, 0, 0]},
          1: {"c": [1, 0, 0, 0, 0, 0, 0, 0], "e": [0, 1, 0, 0, 0, 0, 0, 0]},
          2: {"c": [1, 0, 1, 0, 0, 0, 0, 0], "e": [0, 1, 0, 0, 0, 0, 0, 1],
              "k": [0, 1, 0, 0, 1, 0, 0, 0], "a": [1, 1, 0, 0, 0, 0, 0, 0],
              "i": [0, 1, 0, 0, 0, 1, 0, 0], "n": [1, 0, 0, 0, 1, 0, 0, 0]},
          3: {"c": [1, 0, 1, 0, 1, 0, 0, 0], "e": [0, 1, 0, 1, 0, 0, 0, 1],
              "k": [0, 1, 0, 0, 1, 0, 0, 1], "a": [1, 1, 0, 0, 0, 0, 0, 1],
              "i": [1, 0, 0, 0, 0, 0, 1, 1], "n": [1, 0, 1, 0, 0, 0, 0, 1],
              "y": [1, 0, 1, 0, 0, 1, 0, 0], "q": [1, 0, 0, 0, 1, 0, 0, 1],
              "j": [0, 0, 1, 1, 0, 1, 0, 0], "r": [0, 1, 1, 0, 0, 1, 0, 0]},
          4: {"c": [1, 0, 1, 0, 1, 0, 1, 0], "e": [0, 1, 0, 1, 0, 1, 0, 1],
              "k": [0, 1, 1, 0, 1, 0, 0, 1], "a": [1, 0, 0, 0, 0, 1, 1, 1],
              "i": [1, 0, 1, 1, 0, 0, 0, 1], "n": [1, 0, 0, 0, 1, 0, 1, 1],
              "y": [1, 0, 1, 0, 0, 1, 1, 0], "q": [1, 1, 0, 0, 1, 0, 0, 1],
              "j": [0, 0, 1, 1, 0, 1, 0, 1], "r": [0, 1, 1, 1, 0, 1, 0, 0],
              "t": [1, 1, 1, 0, 0, 1, 0, 0], "w": [1, 0, 0, 0, 1, 1, 0, 1],
              "z": [1, 1, 0, 0, 1, 1, 0, 0]},
          5: {"c": [0, 1, 0, 1, 0, 1, 1, 1], "e": [1, 0, 1, 0, 1, 1, 1, 0],
              "k": [1, 0, 1, 1, 0, 1, 1, 0], "a": [0, 0, 1, 1, 1, 1, 1, 0],
              "i": [0, 1, 1, 1, 1, 1, 0, 0], "n": [0, 1, 0, 1, 1, 1, 1, 0],
              "y": [0, 1, 0, 1, 1, 0, 1, 1], "q": [0, 1, 1, 1, 0, 1, 1, 0],
              "j": [1, 1, 0, 0, 1, 0, 1, 1], "r": [1, 0, 0, 1, 1, 0, 1, 1]},
          6: {"c": [0, 1, 0, 1, 1, 1, 1, 1], "e": [1, 0, 1, 1, 1, 1, 1, 0],
              "k": [1, 0, 1, 1, 0, 1, 1, 1], "a": [0, 0, 1, 1, 1, 1, 1, 1],
              "i": [1, 0, 1, 1, 1, 0, 1, 1], "n": [0, 1, 1, 1, 0, 1, 1, 1]},
          7: {"c": [0, 1, 1, 1, 1, 1, 1, 1], "e": [1, 0, 1, 1, 1, 1, 1, 1]},
          8: {"a": [1, 1, 1, 1, 1, 1, 1, 1]}}


def rotate(neighbours):
    return neighbours[2:8] + neighbours[:2] + neighbours[9:12] + [neighbours[8]]


def reflect_1(neighbours):
    return [neighbours[2], neighbours[1], neighbours[0], neighbours[7],
            neighbours[6], neighbours[5], neighbours[4], neighbours[3],
            neighbours[8], neighbours[11], neighbours[10], neighbours[9]]


def reflect_2(neighbours):
    return [neighbours[6], neighbours[5], neighbours[4], neighbours[3],
            neighbours[0], neighbours[1], neighbours[2], neighbours[7],
            neighbours[10], neighbours[9], neighbours[8], neighbours[11]]


def rotate_4_reflect(neighbours):
    lst = []
    rotate_1 = rotate(neighbours)
    rotate_2 = rotate(rotate_1)
    rotate_3 = rotate(rotate_2)

    lst.append(tuple(neighbours))
    lst.append(tuple(reflect_1(neighbours)))
    lst.append(tuple(reflect_2(neighbours)))

    lst.append(tuple(rotate_1))
    lst.append(tuple(reflect_1(rotate_1)))
    lst.append(tuple(reflect_2(rotate_1)))

    lst.append(tuple(rotate_2))
    lst.append(tuple(reflect_1(rotate_2)))
    lst.append(tuple(reflect_2(rotate_2)))

    lst.append(tuple(rotate_3))
    lst.append(tuple(reflect_1(rotate_3)))
    lst.append(tuple(reflect_2(rotate_3)))
    return lst


current_num = 0
current_trans = ""
birth_trans = []
birth_string = rule_string.split("/")[1]

for i in range(len(birth_string)):
    try:
        current_num = int(birth_string[i])
    except ValueError:
        current_trans += birth_string[i]
        if len(current_trans) == 2:
            if current_trans[0] != "x":
                birth_trans += rotate_4_reflect(
                    hensel[current_num][current_trans[0]] + outside_four_cells[current_trans[1]])
            else:
                for key in hensel[current_num]:
                    birth_trans += rotate_4_reflect(
                        hensel[current_num][key] + outside_four_cells[current_trans[1]])

            current_trans = ""

current_num = 0
current_trans = ""
survival_trans = []
survival_string = rule_string.split("/")[0]

for i in range(len(survival_string)):
    try:
        current_num = int(survival_string[i])
    except ValueError:
        current_trans += survival_string[i]
        if len(current_trans) == 2:
            if current_trans[0] != "x":
                survival_trans += rotate_4_reflect(
                    hensel[current_num][current_trans[0]] + outside_four_cells[current_trans[1]])
            else:
                for key in hensel[current_num]:
                    survival_trans += rotate_4_reflect(
                        hensel[current_num][key] + outside_four_cells[current_trans[1]])
            current_trans = ""

birth_trans, survival_trans = set(birth_trans), set(survival_trans)

# Information about the Rule (Must be filled)
n_states = 2  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "Range 2 Isotropic Von Neumann"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(1, -1), (1, 0), (1, 1), (0, 1),
            (-1, 1), (-1, 0), (-1, -1), (0, -1),
            (2, 0), (0, 2), (-2, 0), (0, -2)]


# Transition Function of Rule, Last Element of Neighbours is the Central Cell
def transition_func(neighbours, generation):
    if neighbours[-1] == 1:
        if tuple(neighbours[:-1]) in survival_trans:
            return 1
        return 0

    elif neighbours[-1] == 0:
        if tuple(neighbours[:-1]) in birth_trans:
            return 1
        return 0


# Does the next state of the cell depend on its neighbours?
# If yes, return next state
# If no, return -1
def depend_on_neighbours(state, generation):
    return -1
