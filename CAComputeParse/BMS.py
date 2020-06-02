def bms_parse(rulestring):
    rulestring = rulestring.decode("utf-8").split(",")

    allll = False
    to_subtract_1 = set()
    to_subtract_2 = set()
    transitions = set()
    current_trans = []
    for i in rulestring:
        if "l" not in i:
            current_trans.append(int(i))
        else:
            if "-" not in i:
                current_trans.append(i)
                allll = True
            else:
                if len(current_trans) == 0:
                    for j in i[2:].split("-"):
                        to_subtract_1.add(int(j))
                else:
                    for j in i[2:].split("-"):
                        to_subtract_2.add(int(j))
                current_trans.append("l")

        if len(current_trans) == 2:
            if not allll:
                transitions.add(tuple(current_trans))
            elif "l" in str(current_trans[0]) and "l" in str(current_trans[1]):
                for j in range(9):
                    for k in range(9):
                        if j not in to_subtract_1 and k not in to_subtract_2:
                            transitions.add((int(j), int(k)))
                allll = False
            elif "l" in str(current_trans[0]):
                for j in range(9):
                    if j not in to_subtract_2:
                        transitions.add((int(j), int(current_trans[1])))
                allll = False
            elif "l" in str(current_trans[1]):
                for j in range(9):
                    if j not in to_subtract_1:
                        transitions.add((int(current_trans[0]), int(j)))
                allll = False

            current_trans = []
            to_subtract_1 = set()
            to_subtract_2 = set()

    print(transitions)
    return transitions
