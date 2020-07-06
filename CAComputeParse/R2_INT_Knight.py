hensel = {0: {"a": [0, 0, 0, 0, 0, 0, 0, 0]},
          1: {"a": [1, 0, 0, 0, 0, 0, 0, 0]},
          2: {"a": [1, 1, 0, 0, 0, 0, 0, 0], "c": [1, 0, 1, 0, 0, 0, 0, 0],
              "e": [1, 0, 0, 1, 0, 0, 0, 0], "h": [1, 0, 0, 0, 1, 0, 0, 0],
              "i": [1, 0, 0, 0, 0, 1, 0, 0], "j": [1, 0, 0, 0, 0, 0, 0, 1]},
          3: {"a": [1, 1, 1, 0, 0, 0, 0, 0], "c": [1, 1, 0, 1, 0, 0, 0, 0],
              "e": [1, 1, 0, 0, 1, 0, 0, 0], "h": [1, 0, 1, 0, 1, 0, 0, 0],
              "i": [1, 0, 1, 0, 0, 1, 0, 0], "j": [1, 0, 1, 0, 0, 0, 0, 1],
              "n": [1, 0, 0, 1, 0, 0, 0, 1]},
          4: {"a": [1, 1, 1, 1, 0, 0, 0, 0], "c": [1, 1, 1, 0, 1, 0, 0, 0],
              "e": [1, 1, 1, 0, 0, 1, 0, 0], "h": [1, 1, 1, 0, 0, 0, 1, 0],
              "i": [1, 1, 1, 0, 0, 0, 0, 1], "j": [1, 1, 0, 1, 1, 0, 0, 0],
              "n": [1, 1, 0, 1, 0, 1, 0, 0], "o": [1, 1, 0, 1, 0, 0, 1, 0],
              "p": [1, 1, 0, 0, 1, 1, 0, 0], "q": [1, 0, 1, 0, 1, 0, 1, 0],
              "r": [1, 0, 1, 0, 1, 0, 0, 1], "t": [1, 0, 1, 0, 0, 1, 0, 1],
              "u": [1, 0, 1, 1, 0, 0, 0, 1], "v": [1, 0, 0, 1, 1, 0, 0, 1]},
          5: {"a": [0, 0, 0, 1, 1, 1, 1, 1], "c": [0, 0, 1, 0, 1, 1, 1, 1],
              "e": [0, 0, 1, 1, 0, 1, 1, 1], "h": [0, 1, 0, 1, 0, 1, 1, 1],
              "i": [0, 1, 0, 1, 1, 0, 1, 1], "j": [0, 1, 0, 1, 1, 1, 1, 0],
              "n": [0, 1, 1, 0, 1, 1, 1, 0]},
          6: {"a": [0, 0, 1, 1, 1, 1, 1, 1], "c": [0, 1, 0, 1, 1, 1, 1, 1],
              "e": [0, 1, 1, 0, 1, 1, 1, 1], "h": [0, 1, 1, 1, 0, 1, 1, 1],
              "i": [0, 1, 1, 1, 1, 0, 1, 1], "j": [0, 1, 1, 1, 1, 1, 1, 0]},
          7: {"a": [0, 1, 1, 1, 1, 1, 1, 1]},
          8: {"a": [1, 1, 1, 1, 1, 1, 1, 1]}}


def rotate(neighbours):
    return [neighbours[6], neighbours[7]] + neighbours[0:6]


def reflect_1(neighbours):
    return [neighbours[3], neighbours[2], neighbours[1], neighbours[0],
            neighbours[7], neighbours[6], neighbours[5], neighbours[4]]


def reflect_2(neighbours):
    """
    0,2,0,3,0
    1,0,0,0,4
    0,0,0,0,0
    8,0,0,0,5
    0,7,0,6,0
    """
    return [neighbours[7], neighbours[6], neighbours[5], neighbours[4],
            neighbours[3], neighbours[2], neighbours[1], neighbours[0]]


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


def get_trans_knight(string):
    string = string.decode("utf-8")

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

    return set(trans)
