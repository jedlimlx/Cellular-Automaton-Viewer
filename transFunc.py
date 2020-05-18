rule_string = "2j34-qt5p6/2e3"  # 2ch35n8/2e34o7 2j3456/2e3

hensel = {0: {"a": [0, 0, 0, 0, 0, 0, 0, 0]},
          1: {"a": [0, 0, 0, 0, 1, 0, 0, 0], "c": [1, 0, 0, 0, 0, 0, 0, 0]},
          2: {"a": [0, 0, 0, 0, 1, 1, 0, 0], "c": [0, 0, 0, 0, 1, 0, 1, 0],
              "e": [1, 0, 0, 0, 1, 0, 0, 0], "h": [0, 1, 0, 0, 1, 0, 0, 0],
              "i": [0, 0, 1, 0, 1, 0, 0, 0], "j": [1, 1, 0, 0, 0, 0, 0, 0],
              "n": [1, 0, 1, 0, 0, 0, 0, 0]},
          3: {"a": [0, 0, 0, 0, 1, 1, 1, 0], "c": [1, 0, 0, 0, 1, 1, 0, 0],
              "e": [0, 0, 1, 0, 1, 1, 0, 0], "h": [0, 0, 0, 1, 1, 0, 1, 0],
              "i": [1, 0, 0, 0, 1, 1, 0, 0], "j": [0, 0, 1, 1, 1, 0, 0, 0],
              "n": [0, 1, 0, 1, 1, 0, 0, 0], "o": [1, 0, 0, 1, 1, 0, 0, 0],
              "p": [1, 1, 1, 0, 0, 0, 0, 0]},
          4: {"a": [0, 0, 0, 0, 1, 1, 1, 1], "c": [0, 0, 1, 0, 1, 1, 1, 0],
              "e": [0, 0, 0, 1, 1, 1, 1, 0], "h": [1, 0, 0, 0, 1, 1, 1, 0],
              "i": [0, 0, 1, 1, 1, 1, 0, 0], "j": [0, 1, 0, 1, 1, 1, 0, 0],
              "n": [1, 0, 0, 1, 1, 1, 0, 0], "o": [1, 1, 0, 0, 1, 1, 0, 0],
              "p": [0, 1, 0, 1, 1, 0, 1, 0], "q": [1, 0, 1, 0, 1, 0, 1, 0],
              "r": [1, 0, 0, 1, 1, 0, 1, 0], "t": [1, 1, 0, 1, 1, 0, 0, 0],
              "u": [0, 1, 1, 1, 1, 0, 0, 0], "v": [1, 0, 1, 1, 1, 0, 0, 0],
              "w": [1, 1, 1, 1, 0, 0, 0, 0]},
          5: {"a": [1, 1, 1, 1, 0, 0, 0, 1], "c": [0, 1, 1, 1, 0, 0, 1, 1],
              "e": [1, 1, 0, 1, 0, 0, 1, 1], "h": [1, 1, 1, 0, 0, 1, 0, 1],
              "i": [0, 1, 1, 1, 0, 0, 1, 1], "j": [1, 1, 0, 0, 0, 1, 1, 1],
              "n": [1, 0, 1, 0, 0, 1, 1, 1], "o": [0, 1, 1, 0, 0, 1, 1, 1],
              "p": [0, 0, 0, 1, 1, 1, 1, 1]},
          6: {"a": [1, 1, 1, 1, 0, 0, 1, 1], "c": [1, 1, 1, 1, 0, 1, 0, 1],
              "e": [0, 1, 1, 1, 0, 1, 1, 1], "h": [1, 0, 1, 1, 0, 1, 1, 1],
              "i": [1, 1, 0, 1, 0, 1, 1, 1], "j": [0, 0, 1, 1, 1, 1, 1, 1],
              "n": [0, 1, 0, 1, 1, 1, 1, 1]},
          7: {"a": [1, 1, 1, 1, 0, 1, 1, 1], "c": [0, 1, 1, 1, 1, 1, 1, 1]},
          8: {"a": [1, 1, 1, 1, 1, 1, 1, 1]}}

"""
    0
    4
3 7 x 5 1
    6
    2

    3
    7
2 6 x 4 0
    5
    1

"""


def rotate(neighbours):
    """
    print("ROTATE", neighbours, [neighbours[3], neighbours[0], neighbours[1], neighbours[2],
                                 neighbours[7], neighbours[4], neighbours[5], neighbours[6]])
    """
    return [neighbours[3], neighbours[0], neighbours[1], neighbours[2],
            neighbours[7], neighbours[4], neighbours[5], neighbours[6]]


def reflect_1(neighbours):
    """
    print("REFLECT_1", neighbours, [neighbours[0], neighbours[3], neighbours[2], neighbours[1],
                                    neighbours[4], neighbours[7], neighbours[5], neighbours[6]])
    """
    return [neighbours[0], neighbours[3], neighbours[2], neighbours[1],
            neighbours[4], neighbours[7], neighbours[6], neighbours[5]]


def reflect_2(neighbours):
    """
    print("REFLECT_2", neighbours, [neighbours[2], neighbours[1], neighbours[0], neighbours[3],
                                    neighbours[6], neighbours[5], neighbours[4], neighbours[7]])
    """
    return [neighbours[2], neighbours[1], neighbours[0], neighbours[3],
            neighbours[6], neighbours[5], neighbours[4], neighbours[7]]


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


def get_trans(string):
    trans = []
    current_num = 0
    subtract = False
    totalistic = False
    for i in string:
        try:
            prev_num = current_num
            current_num = int(i)
            subtract = False
            if totalistic:
                for transition in hensel[prev_num]:
                    trans += rotate_4_reflect(hensel[prev_num][transition])

            if string[-1] == i:
                for transition in hensel[current_num]:
                    trans += rotate_4_reflect(hensel[current_num][transition])

            totalistic = True

        except ValueError:
            totalistic = False
            if i != "-":
                if not subtract:
                    trans += rotate_4_reflect(hensel[current_num][i])
                else:
                    for transition in rotate_4_reflect(hensel[current_num][i]):
                        trans.remove(transition)
            else:
                subtract = True
                for transition in hensel[current_num]:
                    trans += rotate_4_reflect(hensel[current_num][transition])

    return trans


birth_trans = get_trans(rule_string.split("/")[1])
survival_trans = get_trans(rule_string.split("/")[0])

birth_trans, survival_trans = set(birth_trans), set(survival_trans)

# Information about the Rule (Must be filled)
n_states = 2  # Number of States
alternating_period = 1  # For alternating rules / neighbourhoods
colour_palette = None  # Colours of the different states
rule_name = "Range 2 Cross Isotropic"  # Rule Name


# Neighbourhood of the Rule (Relative Distance from Central Cell)
def get_neighbourhood(generation):  # Note (y, x) not (x, y)
    return [(2, 0), (0, 2), (-2, 0), (0, -2),
            (1, 0), (0, 1), (-1, 0), (0, -1)]


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