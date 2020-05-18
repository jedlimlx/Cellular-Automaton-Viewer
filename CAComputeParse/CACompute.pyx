# distutils: language=c++

import re
from libcpp.vector cimport vector
from libcpp.pair cimport pair
from libcpp.map cimport map
from libcpp cimport bool
from libcpp.string cimport string
from libcpp.algorithm cimport sort
from libcpp.unordered_map cimport unordered_map
from libcpp.unordered_set cimport unordered_set

from CAComputeParse.R1_INT_Moore import get_trans_moore
from CAComputeParse.R2_INT_Cross import get_trans_cross
from CAComputeParse.R2_INT_Von_Neumann import get_trans_von_neumann

cdef unordered_map[pair[int, int], int] depends_cache
cdef map[pair[vector[int], int], int] transition_func_cache
cdef vector[vector[int]] colour_palette
cdef string rule_name
cdef string rule_space, bsconditions
cdef int n_states
cdef vector[unordered_map[pair[int, int], int]] index_map
cdef vector[vector[int]] state_weights
cdef vector[vector[pair[int, int]]] neighbourhood, original_neighbourhood
cdef vector[vector[int]] neighbourhood_weights
cdef int alternating_period, birth_state
cdef vector[unordered_set[int]] birth, survival, forcing, killing, living, \
    regen_birth, regen_survival, activity_list, other_birth, other_survival, \
    other_regen_birth, other_regen_survival, other_forcing, other_killing, other_living
cdef vector[unordered_set[pair[int, int]]] birth_semi_1, survival_semi_1, forcing_semi_1, killing_semi_1, \
    living_semi_1, regen_birth_semi_1, regen_survival_semi_1
cdef vector[string] naive_lst, direction_lst
cdef vector[int] corner_lst, xy_lst
cdef int corner, xy
cdef string direction

birth_trans, survival_trans, forcing_trans, killing_trans, living_trans, \
    regen_birth_trans, regen_survival_trans = [], [], [], [], [], [], []

cdef extern from "compute.cpp":
    pass

cpdef load(filename):
    global colour_palette, rule_name, rule_space, n_states, state_weights, neighbourhood, neighbourhood_weights,\
        alternating_period, birth, survival, forcing, killing, living, \
        regen_birth, regen_survival, activity_list, birth_state, other_birth, other_survival, bsconditions, \
        original_neighbourhood, index_map, other_forcing, other_killing, other_living, naive_lst, direction_lst, \
        corner_lst, xy_lst, birth_semi_1, survival_semi_1, forcing_semi_1, killing_semi_1, living_semi_1,\
        regen_birth_semi_1, regen_survival_semi_1, birth_trans, survival_trans, forcing_trans, killing_trans, \
        living_trans, regen_birth_trans, regen_survival_trans

    colour_palette.clear()
    rule_name = b""
    rule_space = b""
    bsconditions = b""
    n_states = 0
    state_weights.clear()
    neighbourhood.clear()
    neighbourhood_weights.clear()
    original_neighbourhood.clear()
    alternating_period = 0
    birth_state = 0
    birth.clear()
    survival.clear()
    forcing.clear()
    killing.clear()
    living.clear()
    index_map.clear()
    activity_list.clear()
    regen_birth.clear()
    regen_survival.clear()
    other_birth.clear()
    other_survival.clear()
    other_forcing.clear()
    other_killing.clear()
    other_living.clear()
    other_regen_birth.clear()
    other_regen_survival.clear()
    birth_semi_1.clear()
    survival_semi_1.clear()
    forcing_semi_1.clear()
    killing_semi_1.clear()
    living_semi_1.clear()
    regen_birth_semi_1.clear()
    regen_survival_semi_1.clear()
    naive_lst.clear()
    direction_lst.clear()
    corner_lst.clear()
    xy_lst.clear()
    depends_cache.clear()
    transition_func_cache.clear()
    corner = -1
    xy = -1
    direction = b"o"

    birth_trans, survival_trans, forcing_trans, killing_trans, living_trans, \
        regen_birth_trans, regen_survival_trans = [], [], [], [], [], [], []

    cdef string rule

    cdef int neighbourhood_count = 0
    cdef vector[vector[int]] current_neighbourhood_weights
    cdef vector[vector[vector[int]]] unflattened_neighbourhood_weights
    cdef unordered_set[pair[int, int]] set_neighbourhood
    cdef pair[int, int] neighbour, neighbour2
    cdef bool parsing_neighbourhood = False

    cdef int colour_palette_count = 0
    cdef vector[int] temp
    cdef bool parsing_colour_palette = False

    cdef string section
    cdef vector[string] rule_string

    cdef int neighbourhood_range = 0

    cdef int i, j
    cdef vector[vector[int]] weights
    cdef vector[pair[int, int]] pair_temp
    cdef unordered_map[pair[int, int], int] map_temp

    cdef vector[int] k

    cdef int num, alt
    cdef unordered_set[int] set_temp
    cdef unordered_set[pair[int, int]] temp_semi_1
    cdef vector[int] extended

    file = open(filename, "r")
    rule = file.read().encode("utf-8")

    for section in rule.split(b"\n"):
        if section.find(b"Neighbourhood:") != -1 or section.find(b"#") != -1:
            neighbourhood_count = 0
            parsing_neighbourhood = True
            current_neighbourhood_weights.clear()
            continue
        elif parsing_neighbourhood:
            temp.clear()
            for x in section.split(b","):
                temp.push_back(int(x))
            current_neighbourhood_weights.push_back(temp)
            neighbourhood_count += 1
            if neighbourhood_count == neighbourhood_range * 2 + 1:
                parsing_neighbourhood = False
                unflattened_neighbourhood_weights.push_back(current_neighbourhood_weights)
            continue

        if section.find(b"Colour Palette:") != -1:
            parsing_colour_palette = True
            continue
        elif parsing_colour_palette:
            if section.find(b"None") != -1:
                colour_palette.clear()
                parsing_colour_palette = False
                continue

            temp.clear()
            for x in section.split(b","):
                temp.push_back(int(re.sub(b"[() ]", b"", x)))
            colour_palette.push_back(temp)
            colour_palette_count += 1
            if colour_palette_count == n_states:
                parsing_colour_palette = False
            continue

        if section.find(b"Name:") != -1:
            rule_name = section.replace(b"Name: ", b"")
        elif section.find(b"Neighbourhood Range:") != -1:
            neighbourhood_range = int(section.replace(b"Neighbourhood Range: ", b""))
        elif section.find(b"State Weights:") != -1:
            for x in section.replace(b"State Weights: ", b"").split(b"|"):
                temp.clear()
                for y in x.split(b","):
                    temp.push_back(int(y))
                state_weights.push_back(temp)
            n_states = state_weights[0].size()
        elif section.find(b"Rulespace:") != -1:
            rule_space = section.replace(b"Rulespace: ", b"")
        elif section.find(b"B/S Conditions:") != -1:
            bsconditions = section.replace(b"B/S Conditions: ", b"")
        elif section.find(b"Rulestring:") != -1:
            rule_string = section.replace(b"Rulestring: ", b"").split(b"|")

    alternating_period = state_weights.size()

    for weights in unflattened_neighbourhood_weights:
        pair_temp.clear()
        for i in range(-neighbourhood_range, neighbourhood_range + 1):
            for j in range(-neighbourhood_range, neighbourhood_range + 1):
                if weights[i + neighbourhood_range][j + neighbourhood_range] != 0:
                    pair_temp.push_back(pair[int, int] (i, j))

        neighbourhood.push_back(pair_temp)

    if bsconditions == b"Double Totalistic":
        original_neighbourhood = neighbourhood
        neighbourhood.clear()
        for i in range(alternating_period):
            set_neighbourhood.clear()
            for neighbour in original_neighbourhood[i]:
                for neighbour2 in original_neighbourhood[i]:
                    set_neighbourhood.insert(pair[int, int] (neighbour.first + neighbour2.first,
                                                             neighbour.second + neighbour2.second))
                set_neighbourhood.insert(neighbour)

            pair_temp.clear()
            pair_temp.insert(pair_temp.end(), set_neighbourhood.begin(), set_neighbourhood.end())
            neighbourhood.push_back(pair_temp)

            map_temp.clear()
            for j in range(len(neighbourhood[i])):
                map_temp[neighbourhood[i][j]] = j
            index_map.push_back(map_temp)

    for weights in unflattened_neighbourhood_weights:
        temp.clear()
        for k in weights:
            for j in k:
                if j != 0:
                    temp.push_back(j)

        neighbourhood_weights.push_back(temp)

    if bsconditions == b"Range 1 Moore Isotropic Non-Totalistic":
        neighbourhood = [[(1, -1), (1, 0), (1, 1), (0, 1),
                          (-1, 1), (-1, 0), (-1, -1), (0, -1)] for x in range(alternating_period)]
    elif bsconditions == b"Range 2 Cross Isotropic Non-Totalistic":
        neighbourhood = [[(2, 0), (0, 2), (-2, 0), (0, -2),
                          (1, 0), (0, 1), (-1, 0), (0, -1)] for x in range(alternating_period)]
    elif bsconditions == b"Range 2 Von Neumann Isotropic Non-Totalistic":
        neighbourhood = [[(1, -1), (1, 0), (1, 1), (0, 1),
                          (-1, 1), (-1, 0), (-1, -1), (0, -1),
                          (2, 0), (0, 2), (-2, 0), (0, -2)] for x in range(alternating_period)]

    if rule_space == b"Single State":
        for individual_rule_string in rule_string:
            if bsconditions == b"Outer Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[1].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[2])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    set_temp.clear()
                    for x in re.split(b"b|s|nn", individual_rule_string)[1].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.split(b"b|s|nn", individual_rule_string)[2].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    try: naive_lst.push_back(re.split(b"b|s|nn", individual_rule_string)[3])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Double Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[1])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[0])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[1]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[0]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_survival.push_back(set_temp)

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[2])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"b|s|nn", individual_rule_string)[1])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"b|s|nn", individual_rule_string)[2])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"b|s|nn", individual_rule_string)[1]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"b|s|nn", individual_rule_string)[2]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_survival.push_back(set_temp)

                    try: naive_lst.push_back(re.split(b"b|s|nn", individual_rule_string)[3])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 1 Moore Semi Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    temp_semi_1.clear()
                    current_trans = []
                    for x in individual_rule_string.split(b"/")[1]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    birth_semi_1.push_back(temp_semi_1)

                    temp_semi_1.clear()
                    current_trans = []
                    for x in individual_rule_string.split(b"/")[0]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    survival_semi_1.push_back(temp_semi_1)

                    file = open("log.log", "a")
                    file.write(str(birth_semi_1) + " " + str(survival_semi_1) + "\n")
                    file.close()

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[2])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    temp_semi_1.clear()
                    current_trans = []
                    for x in re.split(b"b|s|nn", individual_rule_string)[1]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    birth_semi_1.push_back(temp_semi_1)

                    temp_semi_1.clear()
                    for x in re.split(b"b|s|nn", individual_rule_string)[2]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    survival_semi_1.push_back(temp_semi_1)

                    try: naive_lst.push_back(re.split(b"b|s|nn", individual_rule_string)[3])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 1 Moore Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_trans.append(get_trans_moore(individual_rule_string.split(b"/")[1]))
                    survival_trans.append(get_trans_moore(individual_rule_string.split(b"/")[0]))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[2])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_trans.append(get_trans_moore(re.split(b"b|s|nn", individual_rule_string)[1]))
                    survival_trans.append(get_trans_moore(re.split(b"b|s|nn", individual_rule_string)[2]))

                    try: naive_lst.push_back(re.split(b"b|s|nn", individual_rule_string)[3])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 2 Cross Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_trans.append(get_trans_cross(individual_rule_string.split(b"/")[1]))
                    survival_trans.append(get_trans_cross(individual_rule_string.split(b"/")[0]))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[2])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_trans.append(get_trans_cross(re.split(b"b|s|nn", individual_rule_string)[0]))
                    survival_trans.append(get_trans_cross(re.split(b"b|s|nn", individual_rule_string)[1]))

                    try: naive_lst.push_back(re.split(b"b|s|nn", individual_rule_string)[2])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 2 Von Neumann Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[1]))
                    survival_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[0]))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[2])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_trans.append(get_trans_von_neumann(re.split(b"b|s|nn", individual_rule_string)[0]))
                    survival_trans.append(get_trans_von_neumann(re.split(b"b|s|nn", individual_rule_string)[1]))

                    try: naive_lst.push_back(re.split(b"b|s|nn", individual_rule_string)[2])
                    except IndexError: naive_lst.push_back(b"-1")
    elif rule_space == b"BSFKL":
        for individual_rule_string in rule_string:
            if bsconditions == b"Outer Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[1].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[2].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    forcing.push_back(set_temp)

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[3].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    killing.push_back(set_temp)

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[4].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    living.push_back(set_temp)

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[5])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    set_temp.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[1].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[2].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[3].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    forcing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[4].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    killing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[5].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    living.push_back(set_temp)

                    try: naive_lst.push_back(re.split(b"b|s|f|k|l|nn", individual_rule_string)[6])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Double Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[0])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[1])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[2])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    forcing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[3])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    killing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[4])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    living.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[0]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[1]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[2]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_forcing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[3]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_killing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[4]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_living.push_back(set_temp)

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[5])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"b|s|f|k|l|nn", individual_rule_string)[1])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"b|s|f|k|l|nn", individual_rule_string)[2])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"b|s|f|k|l|nn", individual_rule_string)[3])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    forcing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"b|s|f|k|l|nn", individual_rule_string)[4])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    killing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"b|s|f|k|l|nn", individual_rule_string)[5])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    living.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"b|s|f|k|l|nn", individual_rule_string)[1]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"b|s|f|k|l|nn", individual_rule_string)[2]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"b|s|f|k|l|nn", individual_rule_string)[3]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_forcing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"b|s|f|k|l|nn", individual_rule_string)[4]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_killing.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"b|s|f|k|l|nn", individual_rule_string)[5]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_living.push_back(set_temp)

                    try: naive_lst.push_back(re.split(b"b|s|f|k|l|nn", individual_rule_string)[6])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 1 Moore Semi Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    current_trans = []
                    temp_semi_1.clear()
                    for x in individual_rule_string.split(b"/")[0]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    birth_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in individual_rule_string.split(b"/")[1]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    survival_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in individual_rule_string.split(b"/")[2]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    forcing_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in individual_rule_string.split(b"/")[3]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    killing_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in individual_rule_string.split(b"/")[4]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    living_semi_1.push_back(temp_semi_1)

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[5])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    current_trans = []
                    temp_semi_1.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[1]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    birth_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[2]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    survival_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[3]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    forcing_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[4]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    killing_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in re.split(b"b|s|f|k|l|nn", individual_rule_string)[5]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    living_semi_1.push_back(temp_semi_1)

                    try: naive_lst.push_back(re.split(b"b|s|f|k|l|nn", individual_rule_string)[6])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 1 Moore Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_trans.append(get_trans_moore(individual_rule_string.split(b"/")[0]))
                    survival_trans.append(get_trans_moore(individual_rule_string.split(b"/")[1]))
                    forcing_trans.append(get_trans_moore(individual_rule_string.split(b"/")[2]))
                    killing_trans.append(get_trans_moore(individual_rule_string.split(b"/")[3]))
                    living_trans.append(get_trans_moore(individual_rule_string.split(b"/")[4]))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[5])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_trans.append(get_trans_moore(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[1]))
                    survival_trans.append(get_trans_moore(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[2]))
                    forcing_trans.append(get_trans_moore(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[3]))
                    killing_trans.append(get_trans_moore(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[4]))
                    living_trans.append(get_trans_moore(
                       re.split(b"b|s|f|k|l|nn", individual_rule_string)[5]))

                    try: naive_lst.push_back(re.split(b"b|s|f|k|l|nn", individual_rule_string)[6])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 2 Cross Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_trans.append(get_trans_cross(individual_rule_string.split(b"/")[0]))
                    survival_trans.append(get_trans_cross(individual_rule_string.split(b"/")[1]))
                    forcing_trans.append(get_trans_cross(individual_rule_string.split(b"/")[2]))
                    killing_trans.append(get_trans_cross(individual_rule_string.split(b"/")[3]))
                    living_trans.append(get_trans_cross(individual_rule_string.split(b"/")[4]))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[5])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_trans.append(get_trans_cross(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[1]))
                    survival_trans.append(get_trans_cross(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[2]))
                    forcing_trans.append(get_trans_cross(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[3]))
                    killing_trans.append(get_trans_cross(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[4]))
                    living_trans.append(get_trans_cross(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[5]))

                    try: naive_lst.push_back(re.split(b"b|s|f|k|l|nn", individual_rule_string)[6])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 2 Von Neumann Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[0]))
                    survival_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[1]))
                    forcing_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[2]))
                    killing_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[3]))
                    living_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[4]))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[5])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_trans.append(get_trans_von_neumann(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[1]))
                    survival_trans.append(get_trans_von_neumann(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[2]))
                    forcing_trans.append(get_trans_von_neumann(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[3]))
                    killing_trans.append(get_trans_von_neumann(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[4]))
                    living_trans.append(get_trans_von_neumann(
                        re.split(b"b|s|f|k|l|nn", individual_rule_string)[5]))

                    try: naive_lst.push_back(re.split(b"b|s|f|k|l|nn", individual_rule_string)[6])
                    except IndexError: naive_lst.push_back(b"-1")
    elif rule_space == b"Extended Generations":
        for individual_rule_string in rule_string:
            if bsconditions == b"Outer Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[1].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    extended.clear()
                    for x in individual_rule_string.split(b"/")[2].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(int(individual_rule_string.split(b"/")[3]))
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    set_temp.clear()
                    for x in re.split(b"b|s|d|nn", individual_rule_string)[1].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.split(b"b|s|d|nn", individual_rule_string)[2].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    extended.clear()
                    for x in re.split(b"b|s|d|nn", individual_rule_string)[3].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(re.split(b"b|s|d|nn", individual_rule_string)[4])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Double Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[1])[0].split(b","):
                        set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[0])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[1]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[0]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_survival.push_back(set_temp)

                    extended.clear()
                    for x in individual_rule_string.split(b"/")[2].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[3])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"[bsd]", individual_rule_string)[1])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"[bsd]", individual_rule_string)[2])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"[bsd]", individual_rule_string)[1]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"[bsd]", individual_rule_string)[2]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_survival.push_back(set_temp)

                    extended.clear()
                    for x in re.split(b"[bsd]", individual_rule_string)[3].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(re.split(b"b|s|d|nn", individual_rule_string)[4])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 1 Moore Semi Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    temp_semi_1.clear()
                    current_trans = []
                    for x in individual_rule_string.split(b"/")[1]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    birth_semi_1.push_back(temp_semi_1)

                    temp_semi_1.clear()
                    current_trans = []
                    for x in individual_rule_string.split(b"/")[0]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    survival_semi_1.push_back(temp_semi_1)

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[3])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    temp_semi_1.clear()
                    current_trans = []
                    for x in re.split(b"b|s|d|nn", individual_rule_string)[1]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    birth_semi_1.push_back(temp_semi_1)

                    temp_semi_1.clear()
                    for x in re.split(b"b|s|d|nn", individual_rule_string)[2]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    survival_semi_1.push_back(temp_semi_1)

                    extended.clear()
                    for x in re.split(b"b|s|d|nn", individual_rule_string)[3].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(re.split(b"b|s|nn", individual_rule_string)[4])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 1 Moore Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_trans.append(get_trans_moore(individual_rule_string.split(b"/")[1]))
                    survival_trans.append(get_trans_moore(individual_rule_string.split(b"/")[0]))

                    extended.clear()
                    for x in individual_rule_string.split(b"/")[2].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[3])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_trans.append(get_trans_moore(re.split(b"b|s|d|nn", individual_rule_string)[1]))
                    survival_trans.append(get_trans_moore(re.split(b"b|s|d|nn", individual_rule_string)[2]))

                    extended.clear()
                    for x in re.split(b"b|s|d|nn", individual_rule_string)[4].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(re.split(b"b|s|d|nn", individual_rule_string)[3])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 2 Cross Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_trans.append(get_trans_cross(individual_rule_string.split(b"/")[1]))
                    survival_trans.append(get_trans_cross(individual_rule_string.split(b"/")[0]))

                    extended.clear()
                    for x in individual_rule_string.split(b"/")[2].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[3])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_trans.append(get_trans_cross(re.split(b"b|s|d|nn", individual_rule_string)[0]))
                    survival_trans.append(get_trans_cross(re.split(b"b|s|d|nn", individual_rule_string)[1]))

                    extended.clear()
                    for x in re.split(b"b|s|d|nn", individual_rule_string)[3].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(re.split(b"b|s|d|nn", individual_rule_string)[4])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 2 Von Neumann Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[1]))
                    survival_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[0]))

                    extended.clear()
                    for x in individual_rule_string.split(b"/")[2].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[3])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_trans.append(get_trans_von_neumann(re.split(b"b|s|d|nn", individual_rule_string)[0]))
                    survival_trans.append(get_trans_von_neumann(re.split(b"b|s|d|nn", individual_rule_string)[1]))

                    extended.clear()
                    for x in re.split(b"b|s|d|nn", individual_rule_string)[3].split(b"-"):
                        extended.push_back(int(x))

                    try: naive_lst.push_back(re.split(b"b|s|d|nn", individual_rule_string)[4])
                    except IndexError: naive_lst.push_back(b"-1")

            num, alt = 1, 1
            set_temp.clear()
            for i in extended:
                for j in range(num, i + num):
                    if alt > 0:
                        set_temp.insert(j)
                    num += 1
                alt *= -1

            activity_list.push_back(set_temp)
    elif rule_space == b"Regenerating Generations":
        for individual_rule_string in rule_string:
            if bsconditions == b"Outer Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_state = int(individual_rule_string.split(b"/")[1])

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[2].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[3].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[4].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    regen_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in individual_rule_string.split(b"/")[5].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    regen_survival.push_back(set_temp)

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[6])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_state = int(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[2])

                    set_temp.clear()
                    for x in re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[3].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[4].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[5].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    regen_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[6].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    regen_survival.push_back(set_temp)

                    try: naive_lst.push_back(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[7])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Double Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_state = int(individual_rule_string.split(b"/")[1])

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[2])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[3])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[2]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[3]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[4])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    regen_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", individual_rule_string.split(b"/")[5])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    regen_survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[4]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_regen_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", individual_rule_string.split(b"/")[5]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_regen_survival.push_back(set_temp)

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[6])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_state = int(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[2])

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[3])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[4])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[3]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[4]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[5])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    regen_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.findall(b"\((.*?)\)", re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[6])[0].split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    regen_survival.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[5]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_regen_birth.push_back(set_temp)

                    set_temp.clear()
                    for x in re.sub(b"\(.*?\)", b"", re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[6]).split(b","):
                        if x.find(b"-") != -1:
                            for i in range(int(x.split(b"-")[0]), int(x.split(b"-")[1]) + 1):
                                set_temp.insert(i)
                        else:
                            set_temp.insert(int(x))
                    other_regen_survival.push_back(set_temp)

                    try: naive_lst.push_back(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[7])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 1 Moore Semi Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_state = int(individual_rule_string.split(b"/")[1])

                    current_trans = []
                    temp_semi_1.clear()
                    for x in individual_rule_string.split(b"/")[2]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    birth_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in individual_rule_string.split(b"/")[3]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    survival_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in individual_rule_string.split(b"/")[4]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    regen_birth_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in individual_rule_string.split(b"/")[5]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    regen_survival_semi_1.push_back(temp_semi_1)

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[6])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_state = int(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[2])

                    current_trans = []
                    temp_semi_1.clear()
                    for x in re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[3]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    birth_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[4]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    survival_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[5]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    regen_birth_semi_1.push_back(temp_semi_1)

                    current_trans = []
                    temp_semi_1.clear()
                    for x in re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[6]:
                        current_trans.append(int(str(x)))
                        if len(current_trans) == 2:
                            temp_semi_1.insert(pair[int, int] (current_trans[0] - 48, current_trans[1] - 48))
                            current_trans = []
                    regen_survival_semi_1.push_back(temp_semi_1)

                    try: naive_lst.push_back(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[7])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 1 Moore Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_state = int(individual_rule_string.split(b"/")[1])
                    birth_trans.append(get_trans_moore(individual_rule_string.split(b"/")[2]))
                    survival_trans.append(get_trans_moore(individual_rule_string.split(b"/")[3]))
                    regen_birth_trans.append(get_trans_moore(individual_rule_string.split(b"/")[4]))
                    regen_survival_trans.append(get_trans_moore(individual_rule_string.split(b"/")[5]))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[6])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_state = int(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[2])
                    birth_trans.append(
                        get_trans_moore(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[3]))
                    survival_trans.append(
                        get_trans_moore(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[4]))
                    regen_birth_trans.append(
                        get_trans_moore(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[5]))
                    regen_survival_trans.append(
                        get_trans_moore(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[6]))

                    try: naive_lst.push_back(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[7])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 2 Cross Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_state = int(individual_rule_string.split(b"/")[1])
                    birth_trans.append(get_trans_cross(individual_rule_string.split(b"/")[2]))
                    survival_trans.append(get_trans_cross(individual_rule_string.split(b"/")[3]))
                    regen_birth_trans.append(get_trans_cross(individual_rule_string.split(b"/")[4]))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[6])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_state = int(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[2])
                    birth_trans.append(
                        get_trans_cross(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[3]))
                    survival_trans.append(
                        get_trans_cross(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[4]))
                    regen_birth_trans.append(
                        get_trans_cross(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[5]))
                    regen_survival_trans.append(
                        get_trans_cross(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[6]))

                    try: naive_lst.push_back(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[7])
                    except IndexError: naive_lst.push_back(b"-1")
            elif bsconditions == b"Range 2 Von Neumann Isotropic Non-Totalistic":
                if individual_rule_string.find(b"/") != -1:
                    birth_state = int(individual_rule_string.split(b"/")[1])
                    birth_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[2]))
                    survival_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[3]))
                    regen_birth_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[4]))
                    regen_survival_trans.append(get_trans_von_neumann(individual_rule_string.split(b"/")[5]))

                    try: naive_lst.push_back(individual_rule_string.split(b"/")[6])
                    except IndexError: naive_lst.push_back(b"-1")
                else:
                    birth_state = int(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[2])
                    birth_trans.append(
                        get_trans_von_neumann(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[3]))
                    survival_trans.append(
                        get_trans_von_neumann(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[4]))
                    regen_birth_trans.append(
                        get_trans_von_neumann(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[5]))
                    regen_survival_trans.append(
                        get_trans_von_neumann(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[6]))

                    try: naive_lst.push_back(re.split(b"rg|l|b|s|rb|rs|nn", individual_rule_string)[7])
                    except IndexError: naive_lst.push_back(b"-1")

    for x in naive_lst:
        if x != b"-1":
            corner_lst.push_back(int(x.decode("utf-8")[0]))
            direction_lst.push_back(str(x.decode("utf-8")[1]).encode("utf-8"))
            xy_lst.push_back(int(x.decode("utf-8")[2]))
        else:
            corner_lst.push_back(-1)
            direction_lst.push_back(b"")
            xy_lst.push_back(-1)


cpdef vector[pair[int, int]] get_neighbourhood(int generations):
    return neighbourhood[generations % alternating_period]

cpdef int get_n_states():
    return n_states

cpdef vector[vector[int]] get_colour_palette():
    return colour_palette

cpdef string get_rule_name():
    return rule_name

cdef int transition_func(vector[int] neighbours, int generations):
    cdef int n_living = 0, n_destructive = 0, n = 0, n_birth = 0, n_survival = 0, index, found_index, idx, \
        n_regen_birth = 0, n_regen_survival = 0, n_forcing = 0, n_killing = 0, n1 = 0, n2 = 0, \
        n_edge = 0, n_corner = 0, n_edge_destructive = 0, n_corner_destructive = 0
    cdef pair[int, int] neighbour, neighbour2, alive_semi_1, destructive_semi_1

    if rule_space == b"BSFKL":
        if bsconditions == b"Outer Totalistic":
            for i in range(neighbours.size() - 1):
                if neighbours[i] == 1:
                    n_living += neighbourhood_weights[generations % alternating_period][i]
                elif neighbours[i] == 2:
                    n_destructive += neighbourhood_weights[generations % alternating_period][i]

            if neighbours[neighbours.size() - 1] == 1:
                if killing[generations % alternating_period].find(n_destructive) != \
                        killing[generations % alternating_period].end():
                    return 0
                elif survival[generations % alternating_period].find(n_living) != \
                        survival[generations % alternating_period].end():
                    return 1
                return 2
            elif neighbours[neighbours.size() - 1] == 2:
                if living[generations % alternating_period].find(n_living) != \
                        living[generations % alternating_period].end():
                    return 0
                return 2
            else:
                if forcing[generations % alternating_period].find(n_destructive) != \
                        forcing[generations % alternating_period].end() and \
                        birth[generations % alternating_period].find(n_living) != \
                        birth[generations % alternating_period].end():
                    return 1
                return 0
        elif bsconditions == b"Double Totalistic":
            n_birth = 0
            n_survival = 0
            n_living = 0
            n_forcing = 0
            n_killing = 0
            for neighbour in original_neighbourhood[generations % alternating_period]:
                n1, n2 = 0, 0
                idx = 0
                for neighbour2 in original_neighbourhood[generations % alternating_period]:
                    if neighbours[index_map[generations % alternating_period][pair[int, int]
                         (neighbour.first + neighbour2.first, neighbour.second + neighbour2.second)]] == 1:
                        n1 += neighbourhood_weights[generations % alternating_period][idx]
                    if neighbours[index_map[generations % alternating_period][pair[int, int]
                         (neighbour.first + neighbour2.first, neighbour.second + neighbour2.second)]] == 2:
                        n2 += neighbourhood_weights[generations % alternating_period][idx]
                    idx += 1

                if other_birth[generations % alternating_period].find(n1) != \
                        other_birth[generations % alternating_period].end():
                    n_birth += 1
                if other_survival[generations % alternating_period].find(n1) != \
                        other_survival[generations % alternating_period].end():
                    n_survival += 1
                if other_living[generations % alternating_period].find(n1) != \
                        other_living[generations % alternating_period].end():
                    n_living += 1
                if other_forcing[generations % alternating_period].find(n2) != \
                        other_forcing[generations % alternating_period].end():
                    n_forcing += 1
                if other_killing[generations % alternating_period].find(n2) != \
                        other_killing[generations % alternating_period].end():
                    n_killing += 1

            if neighbours[neighbours.size() - 1] == 1:
                if killing[generations % alternating_period].find(n_killing) != \
                        killing[generations % alternating_period].end():
                    return 0
                elif survival[generations % alternating_period].find(n_survival) != \
                        survival[generations % alternating_period].end():
                    return 1
                return 2

            elif neighbours[neighbours.size() - 1] == 2:
                if living[generations % alternating_period].find(n_living) != \
                        living[generations % alternating_period].end():
                    return 0
                return 2

            else:
                if forcing[generations % alternating_period].find(n_forcing) != \
                        forcing[generations % alternating_period].end() and \
                        birth[generations % alternating_period].find(n_birth) != \
                        birth[generations % alternating_period].end():
                    return 1
                return 0
        elif bsconditions == b"Range 1 Moore Semi Totalistic":
            for i in range(neighbours.size() - 1):
                if neighbourhood[generations % alternating_period][i] == pair[int, int] (0, -1) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (0, 1) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (1, 0) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (-1, 0):
                    if neighbours[i] == 1:
                        n_edge += 1
                    elif neighbours[i] == 2:
                        n_edge_destructive += 1
                else:
                    if neighbours[i] == 1:
                        n_corner += 1
                    elif neighbours[i] == 2:
                        n_corner_destructive += 1

            alive_semi_1 = pair[int, int] (n_corner, n_edge)
            destructive_semi_1 = pair[int, int] (n_corner_destructive, n_edge_destructive)
            if neighbours[neighbours.size() - 1] == 1:
                if killing_semi_1[generations % alternating_period].find(destructive_semi_1) != \
                        killing_semi_1[generations % alternating_period].end():
                    return 0
                elif survival_semi_1[generations % alternating_period].find(alive_semi_1) != \
                        survival_semi_1[generations % alternating_period].end():
                    return 1
                return 2

            elif neighbours[neighbours.size() - 1] == 2:
                if living_semi_1[generations % alternating_period].find(alive_semi_1) != \
                        living_semi_1[generations % alternating_period].end():
                    return 0
                return 2

            else:
                if forcing_semi_1[generations % alternating_period].find(destructive_semi_1) != \
                        forcing_semi_1[generations % alternating_period].end() and \
                        birth_semi_1[generations % alternating_period].find(alive_semi_1) != \
                        birth_semi_1[generations % alternating_period].end():
                    return 1
                return 0
        elif bsconditions == b"Range 1 Moore Isotropic Non-Totalistic" or \
                bsconditions == b"Range 2 Cross Isotropic Non-Totalistic" or \
                bsconditions == b"Range 2 Von Neumann Isotropic Non-Totalistic":
            new_neighbours_living = []
            new_neighbours_destructive = []
            for i in range(neighbours.size() - 1):
                if neighbours[i] == 1:
                    new_neighbours_living.append(1)
                    new_neighbours_destructive.append(0)
                elif neighbours[i] == 2:
                    new_neighbours_living.append(0)
                    new_neighbours_destructive.append(1)
                else:
                    new_neighbours_living.append(0)
                    new_neighbours_destructive.append(0)

            if neighbours[neighbours.size() - 1] == 1:
                if tuple(new_neighbours_destructive) in killing_trans[generations % alternating_period]:
                    return 0
                elif tuple(new_neighbours_living) in survival_trans[generations % alternating_period]:
                    return 1
                return 2
            elif neighbours[neighbours.size() - 1] == 2:
                if tuple(new_neighbours_living) in living_trans[generations % alternating_period]:
                    return 0
                return 2
            else:
                if tuple(new_neighbours_destructive) in forcing_trans[generations % alternating_period] and \
                        tuple(new_neighbours_living) in birth_trans[generations % alternating_period]:
                    return 1
                return 0
    elif rule_space == b"Extended Generations":
        if bsconditions == b"Outer Totalistic":
            for i in range(neighbours.size() - 1):
                n += neighbourhood_weights[generations % alternating_period][i] * \
                     state_weights[generations % alternating_period][neighbours[i]]

            if activity_list[generations % alternating_period].find(neighbours[neighbours.size() - 1]) != \
                    activity_list[generations % alternating_period].end():
                if survival[generations % alternating_period].find(n) != \
                        survival[generations % alternating_period].end():
                    return neighbours[neighbours.size() - 1]
                return (neighbours[neighbours.size() - 1] + 1) % n_states

            elif neighbours[neighbours.size() - 1] == 0:
                if birth[generations % alternating_period].find(n) != \
                        birth[generations % alternating_period].end():
                    return 1
                return 0
        elif bsconditions == b"Double Totalistic":
            n_birth = 0
            n_survival = 0
            for neighbour in original_neighbourhood[generations % alternating_period]:
                n = 0
                idx = 0
                for neighbour2 in original_neighbourhood[generations % alternating_period]:
                    n += neighbourhood_weights[generations % alternating_period][idx] * \
                         state_weights[generations % alternating_period][neighbours[index_map[
                             generations % alternating_period][pair[int, int]
                         (neighbour.first + neighbour2.first, neighbour.second + neighbour2.second)]]]
                    idx += 1

                if other_birth[generations % alternating_period].find(n) != \
                        other_birth[generations % alternating_period].end():
                    n_birth += 1
                if other_survival[generations % alternating_period].find(n) != \
                        other_survival[generations % alternating_period].end():
                    n_survival += 1

            if activity_list[generations % alternating_period].find(neighbours[neighbours.size() - 1]) != \
                    activity_list[generations % alternating_period].end():
                if survival[generations % alternating_period].find(n_survival) != \
                    survival[generations % alternating_period].end():
                    return 1
                return 2

            elif neighbours[neighbours.size() - 1] == 0:
                if birth[generations % alternating_period].find(n_birth) != \
                    birth[generations % alternating_period].end():
                    return 1
                return 0
        elif bsconditions == b"Range 1 Moore Semi Totalistic":
            n_edge, n_corner = 0, 0
            for i in range(neighbours.size() - 1):
                if neighbourhood[generations % alternating_period][i] == pair[int, int] (0, -1) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (0, 1) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (1, 0) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (-1, 0):
                    n_edge += state_weights[generations % alternating_period][neighbours[i]]
                else:
                    n_corner += state_weights[generations % alternating_period][neighbours[i]]

            if neighbours[neighbours.size() - 1] == 0:
                if birth_semi_1[generations % alternating_period].find(pair[int, int] (n_corner, n_edge)) != \
                    birth_semi_1[generations % alternating_period].end():
                    return 1
                return 0
            elif neighbours[neighbours.size() - 1] == 1:
                if survival_semi_1[generations % alternating_period].find(pair[int, int] (n_corner, n_edge)) != \
                    survival_semi_1[generations % alternating_period].end():
                    return 1
                return 2
        elif bsconditions == b"Range 1 Moore Isotropic Non-Totalistic" or \
                bsconditions == b"Range 2 Cross Isotropic Non-Totalistic" or \
                bsconditions == b"Range 2 Von Neumann Isotropic Non-Totalistic":
            new_neighbours = []
            for i in range(neighbours.size() - 1):
                if activity_list[generations % alternating_period].find(neighbours[i]) != \
                    activity_list[generations % alternating_period].end():
                    new_neighbours.append(1)
                else:
                    new_neighbours.append(0)

            if activity_list[generations % alternating_period].find(neighbours[neighbours.size() - 1]) != \
                    activity_list[generations % alternating_period].end():
                if tuple(new_neighbours) in survival_trans[generations % alternating_period]:
                    return 1
                return 2
            else:
                if tuple(new_neighbours) in birth_trans[generations % alternating_period]:
                    return 1
                return 0
    elif rule_space == b"Single State":
        if bsconditions == b"Outer Totalistic":
            for i in range(neighbours.size() - 1):
                n += neighbourhood_weights[generations % alternating_period][i] * \
                     state_weights[generations % alternating_period][neighbours[i]]

            if neighbours[neighbours.size() - 1] == 1:
                if survival[generations % alternating_period].find(n) != \
                        survival[generations % alternating_period].end():
                    return 1
                return 0
            else:
                if birth[generations % alternating_period].find(n) != \
                        birth[generations % alternating_period].end():
                    return 1
                return 0
        elif bsconditions == b"Double Totalistic":
            n_birth = 0
            n_survival = 0
            for neighbour in original_neighbourhood[generations % alternating_period]:
                n = 0
                idx = 0
                for neighbour2 in original_neighbourhood[generations % alternating_period]:
                    n += neighbourhood_weights[generations % alternating_period][idx] * \
                         state_weights[generations % alternating_period][neighbours[index_map[
                             generations % alternating_period][pair[int, int]
                         (neighbour.first + neighbour2.first, neighbour.second + neighbour2.second)]]]
                    idx += 1

                if other_birth[generations % alternating_period].find(n) != \
                        other_birth[generations % alternating_period].end():
                    n_birth += 1
                if other_survival[generations % alternating_period].find(n) != \
                        other_survival[generations % alternating_period].end():
                    n_survival += 1

            if neighbours[neighbours.size() - 1] == 1:
                if survival[generations % alternating_period].find(n_survival) != \
                    survival[generations % alternating_period].end():
                    return 1
                return 0

            elif neighbours[neighbours.size() - 1] == 0:
                if birth[generations % alternating_period].find(n_birth) != \
                    birth[generations % alternating_period].end():
                    return 1
                return 0
        elif bsconditions == b"Range 1 Moore Semi Totalistic":
            n_edge, n_corner = 0, 0
            for i in range(neighbours.size() - 1):
                if neighbourhood[generations % alternating_period][i] == pair[int, int] (0, -1) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (0, 1) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (1, 0) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (-1, 0):
                    n_edge += neighbours[i]
                else: 
                    n_corner += neighbours[i]

            if neighbours[neighbours.size() - 1] == 0:
                if birth_semi_1[generations % alternating_period].find(pair[int, int] (n_corner, n_edge)) != \
                    birth_semi_1[generations % alternating_period].end():
                    return 1
                return 0
            elif neighbours[neighbours.size() - 1] == 1:
                if survival_semi_1[generations % alternating_period].find(pair[int, int] (n_corner, n_edge)) != \
                    survival_semi_1[generations % alternating_period].end():
                    return 1
                return 0
        elif bsconditions == b"Range 1 Moore Isotropic Non-Totalistic" or \
                bsconditions == b"Range 2 Cross Isotropic Non-Totalistic" or \
                bsconditions == b"Range 2 Von Neumann Isotropic Non-Totalistic":
            new_neighbours = []
            for i in range(neighbours.size() - 1):
                new_neighbours.append(neighbours[i])

            if neighbours[neighbours.size() - 1] == 1:
                if tuple(new_neighbours) in survival_trans[generations % alternating_period]:
                    return 1
                return 0
            else:
                if tuple(new_neighbours) in birth_trans[generations % alternating_period]:
                    return 1
                return 0
    elif rule_space == b"Regenerating Generations":
        if bsconditions == b"Outer Totalistic":
            for i in range(neighbours.size() - 1):
                n += neighbourhood_weights[generations % alternating_period][i] * \
                     state_weights[generations % alternating_period][neighbours[i]]

            if neighbours[neighbours.size() - 1] == 0:
                if birth[generations % alternating_period].find(n) != \
                        birth[generations % alternating_period].end():
                    return birth_state
                return 0
            elif neighbours[neighbours.size() - 1] == 1:
                if survival[generations % alternating_period].find(n) != \
                        survival[generations % alternating_period].end():
                    return 1
                return 2
            else:
                if regen_birth[generations % alternating_period].find(n) != \
                        regen_birth[generations % alternating_period].end():
                    return neighbours[neighbours.size() - 1] - 1
                elif regen_survival[generations % alternating_period].find(n) != \
                        regen_survival[generations % alternating_period].end():
                    return neighbours[neighbours.size() - 1]
                return (neighbours[neighbours.size() - 1] + 1) % n_states
        elif bsconditions == b"Double Totalistic":
            n_birth, n_survival, n_regen_birth, n_regen_survival = 0, 0, 0, 0
            for neighbour in original_neighbourhood[generations % alternating_period]:
                n = 0
                idx = 0
                for neighbour2 in original_neighbourhood[generations % alternating_period]:
                    n += neighbourhood_weights[generations % alternating_period][idx] * \
                         state_weights[generations % alternating_period][neighbours[index_map[
                             generations % alternating_period][pair[int, int]
                         (neighbour.first + neighbour2.first, neighbour.second + neighbour2.second)]]]
                    idx += 1
                if other_birth[generations % alternating_period].find(n) != \
                        other_birth[generations % alternating_period].end():
                    n_birth += 1
                if other_survival[generations % alternating_period].find(n) != \
                        other_survival[generations % alternating_period].end():
                    n_survival += 1
                if other_regen_birth[generations % alternating_period].find(n) != \
                        other_regen_birth[generations % alternating_period].end():
                    n_regen_birth += 1
                if other_regen_survival[generations % alternating_period].find(n) != \
                        other_regen_survival[generations % alternating_period].end():
                    n_regen_survival += 1

            if neighbours[neighbours.size() - 1] == 0:
                if birth[generations % alternating_period].find(n_birth) != \
                        birth[generations % alternating_period].end():
                    return birth_state
                return 0
            elif neighbours[neighbours.size() - 1] == 1:
                if survival[generations % alternating_period].find(n_survival) != \
                        survival[generations % alternating_period].end():
                    return 1
                return 2
            else:
                if regen_birth[generations % alternating_period].find(n_regen_birth) != \
                        regen_birth[generations % alternating_period].end():
                    return neighbours[neighbours.size() - 1] - 1
                elif regen_survival[generations % alternating_period].find(n_regen_survival) != \
                        regen_survival[generations % alternating_period].end():
                    return neighbours[neighbours.size() - 1]
                return (neighbours[neighbours.size() - 1] + 1) % n_states
        elif bsconditions == b"Range 1 Moore Semi Totalistic":
            n_edge, n_corner = 0, 0
            for i in range(neighbours.size() - 1):
                if neighbourhood[generations % alternating_period][i] == pair[int, int] (0, -1) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (0, 1) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (1, 0) or \
                        neighbourhood[generations % alternating_period][i] == pair[int, int] (-1, 0):
                    n_edge += state_weights[generations % alternating_period][neighbours[i]]
                else:
                    n_corner += state_weights[generations % alternating_period][neighbours[i]]

            if neighbours[neighbours.size() - 1] == 0:
                if birth_semi_1[generations % alternating_period].find(pair[int, int] (n_edge, n_corner)) != \
                        birth_semi_1[generations % alternating_period].end():
                    return birth_state
                return 0
            elif neighbours[neighbours.size() - 1] == 1:
                if survival_semi_1[generations % alternating_period].find(pair[int, int] (n_edge, n_corner)) != \
                        survival_semi_1[generations % alternating_period].end():
                    return 1
                return 2
            else:
                if regen_birth_semi_1[generations % alternating_period].find(pair[int, int] (n_edge, n_corner)) != \
                        regen_birth_semi_1[generations % alternating_period].end():
                    return neighbours[neighbours.size() - 1] - 1
                elif regen_survival_semi_1[generations % alternating_period].find(
                        pair[int, int] (n_edge, n_corner)) != \
                        regen_survival_semi_1[generations % alternating_period].end():
                    return neighbours[neighbours.size() - 1]
                return (neighbours[neighbours.size() - 1] + 1) % n_states
        elif bsconditions == b"Range 1 Moore Isotropic Non-Totalistic" or \
                bsconditions == b"Range 2 Cross Isotropic Non-Totalistic" or \
                bsconditions == b"Range 2 Von Neumann Isotropic Non-Totalistic":
            new_neighbours = []
            for i in range(neighbours.size() - 1):
                if state_weights[generations % alternating_period][neighbours[i]] >= 1:
                    new_neighbours.append(1)
                else:
                    new_neighbours.append(0)

            if neighbours[neighbours.size() - 1] == 0:
                if tuple(new_neighbours) in birth_trans[generations % alternating_period]:
                    return 1
                return 0
            elif neighbours[neighbours.size() - 1] == 1:
                if tuple(new_neighbours) in survival_trans[generations % alternating_period]:
                    return 1
                return 2
            else:
                if tuple(new_neighbours) in regen_birth_trans[generations % alternating_period]:
                    return neighbours[neighbours.size() - 1] - 1
                elif tuple(new_neighbours) in regen_survival_trans[generations % alternating_period]:
                    return neighbours[neighbours.size() - 1]
                return (neighbours[neighbours.size() - 1] + 1) % n_states

cdef int depend_on_neighbours(int state, int generations):
    if rule_space == b"BSFKL" or rule_space == b"Single State" or rule_space == b"Regenerating Generations":
        return -1
    elif rule_space == b"Extended Generations":
        if activity_list[generations % alternating_period].find(state) != \
                activity_list[generations % alternating_period].end() or state == 0:
            return -1
        else:
            return (state + 1) % n_states

cdef bool compare_pairs(pair[int, int] a, pair[int, int] b):
    if direction == b"o":
        if corner == 0:
            if xy == 0:
                if a.second == b.second:
                    return a.first < b.first
                return a.second < b.second
            elif xy == 1:
                if a.first == b.first:
                    return a.second < b.second
                return a.first < b.first

        elif corner == 1:
            if xy == 0:
                if a.second == b.second:
                    return a.first < b.first
                return a.second > b.second
            elif xy == 1:
                if a.first == b.first:
                    return a.second > b.second
                return a.first < b.first

        elif corner == 2:
            if xy == 0:
                if a.second == b.second:
                    return a.first > b.first
                return a.second < b.second
            elif xy == 1:
                if a.first == b.first:
                    return a.second < b.second
                return a.first > b.first

        elif corner == 3:
            if xy == 0:
                if a.second == b.second:
                    return a.first > b.first
                return a.second > b.second
            elif xy == 1:
                if a.first == b.first:
                    return a.second > b.second
                return a.first > b.first
    elif direction == b"d":
        if corner == 0:
            if xy == 0:
                if a.first + a.second == b.first + b.second:
                    return a.first < b.first
                return a.first + a.second < b.first + b.second
            elif xy == 1:
                if a.first + a.second == b.first + b.second:
                    return a.first > b.first
                return a.first + a.second < b.first + b.second

        elif corner == 1:
            if xy == 0:
                if a.first - a.second == b.first - b.second:
                    return a.first < b.first
                return a.first - a.second < b.first - b.second
            elif xy == 1:
                if a.first - a.second == b.first - b.second:
                    return a.first > b.first
                return a.first - a.second < b.first - b.second

        elif corner == 2:
            if xy == 0:
                if a.first - a.second == b.first - b.second:
                    return a.first < b.first
                return a.first - a.second > b.first - b.second
            elif xy == 1:
                if a.first - a.second == b.first - b.second:
                    return a.first > b.first
                return a.first - a.second > b.first - b.second

        elif corner == 3:
            if xy == 0:
                if a.first + a.second == b.first + b.second:
                    return a.first < b.first
                return a.first + a.second > b.first + b.second
            elif xy == 1:
                if a.first + a.second == b.first + b.second:
                    return a.first > b.first
                return a.first + a.second > b.first + b.second

cpdef compute(unordered_set[pair[int, int]] cells_changed,
              unordered_map[pair[int, int], int] copy_grid, unordered_map[pair[int, int], int] dict_grid,
              int generations):

    global corner, direction, xy

    cdef vector[int] neighbours
    neighbours.reserve(neighbourhood[generations % alternating_period].size() + 1)

    cdef unordered_set[pair[int, int]] cells_to_check

    cdef int i, j
    cdef int ans
    cdef vector[pair[int, int]] cells_to_check_vector
    cdef pair[int, int] coordinates, coordinates2
    cdef pair[int, int] neighbour

    for coor in cells_changed:
        for neighbour in neighbourhood[generations % alternating_period]:
            coordinates.first = coor.first + neighbour.first
            coordinates.second = coor.second + neighbour.second
            cells_to_check.insert(coordinates)

        cells_to_check.insert(coor)

    if alternating_period > 1:
        if generations % (alternating_period - 1) == 0:
            cells_changed.clear()
    else:
        cells_changed.clear()

    corner = corner_lst[generations % alternating_period]
    direction = direction_lst[generations % alternating_period]
    xy = xy_lst[generations % alternating_period]

    if corner == -1:
        for coordinates in cells_to_check:
            neighbours.clear()
            ans = -1

            if copy_grid.find(coordinates) == copy_grid.end():
                if depends_cache.find(pair[int, int] (0, generations % alternating_period)) == \
                        depends_cache.end():
                    ans = depend_on_neighbours(0, generations % alternating_period)
                    depends_cache[pair[int, int] (0, generations % alternating_period)] = ans
                else:
                    ans = depends_cache[pair[int, int] (0, generations % alternating_period)]
            else:
                if depends_cache.find(pair[int, int] (copy_grid[coordinates],
                                                      generations % alternating_period)) == \
                        depends_cache.end():
                    ans = depend_on_neighbours(copy_grid[coordinates], generations % alternating_period)
                    depends_cache[pair[int, int] (copy_grid[coordinates], generations % alternating_period)] = ans
                else:
                    ans = depends_cache[pair[int, int] (copy_grid[coordinates], generations % alternating_period)]

            if ans == -1:
                for neighbour in neighbourhood[generations % alternating_period]:
                    coordinates2 = pair[int, int] (coordinates.first + neighbour.first,
                                                   coordinates.second + neighbour.second)
                    if copy_grid.find(coordinates2) != copy_grid.end():
                        neighbours.push_back(copy_grid[coordinates2])
                    else:
                        neighbours.push_back(0)

            if copy_grid.find(coordinates) != copy_grid.end():
                neighbours.push_back(copy_grid[coordinates])
                if transition_func_cache.find(
                        pair[vector[int], int] (neighbours, generations % alternating_period)) == \
                        transition_func_cache.end():
                    if ans == -1:
                        ans = transition_func(neighbours, generations % alternating_period)
                        transition_func_cache[
                            pair[vector[int], int] (neighbours, generations % alternating_period)] = ans
                else:
                    if ans == -1: ans = transition_func_cache[
                        pair[vector[int], int] (neighbours, generations % alternating_period)]

                if ans == 0:
                    dict_grid.erase(coordinates)
                    cells_changed.insert(coordinates)
                elif ans != copy_grid[coordinates]:
                    dict_grid[coordinates] = ans
                    cells_changed.insert(coordinates)
            else:
                neighbours.push_back(0)
                if transition_func_cache.find(
                        pair[vector[int], int] (neighbours, generations % alternating_period)) == \
                        transition_func_cache.end():
                    if ans == -1:
                        ans = transition_func(neighbours, generations % alternating_period)
                        transition_func_cache[
                            pair[vector[int], int] (neighbours, generations % alternating_period)] = ans
                else:
                    if ans == -1: ans = transition_func_cache[
                        pair[vector[int], int] (neighbours, generations % alternating_period)]

                if ans != 0:
                    dict_grid.insert(pair[pair[int, int], int] (coordinates, ans))
                    cells_changed.insert(coordinates)
    else:
        cells_to_check_vector.assign(cells_to_check.begin(), cells_to_check.end())
        sort(cells_to_check_vector.begin(), cells_to_check_vector.end(), compare_pairs)
        for coordinates in cells_to_check_vector:
            neighbours.clear()
            ans = -1

            if dict_grid.find(coordinates) == dict_grid.end():
                if depends_cache.find(pair[int, int] (0, generations % alternating_period)) == \
                        depends_cache.end():
                    ans = depend_on_neighbours(0, generations % alternating_period)
                    depends_cache[pair[int, int] (0, generations % alternating_period)] = ans
                else:
                    ans = depends_cache[pair[int, int] (0, generations % alternating_period)]
            else:
                if depends_cache.find(pair[int, int] (dict_grid[coordinates],
                                                      generations % alternating_period)) == \
                        depends_cache.end():
                    ans = depend_on_neighbours(dict_grid[coordinates], generations % alternating_period)
                    depends_cache[pair[int, int] (dict_grid[coordinates], generations % alternating_period)] = ans
                else:
                    ans = depends_cache[pair[int, int] (dict_grid[coordinates], generations % alternating_period)]

            if ans == -1:
                for neighbour in neighbourhood[generations % alternating_period]:
                    coordinates2 = pair[int, int] (coordinates.first + neighbour.first,
                                                   coordinates.second + neighbour.second)
                    if dict_grid.find(coordinates2) != dict_grid.end():
                        neighbours.push_back(dict_grid[coordinates2])
                    else:
                        neighbours.push_back(0)

            if dict_grid.find(coordinates) != dict_grid.end():
                neighbours.push_back(dict_grid[coordinates])
                if transition_func_cache.find(
                        pair[vector[int], int] (neighbours, generations % alternating_period)) == \
                        transition_func_cache.end():
                    if ans == -1:
                        ans = transition_func(neighbours, generations % alternating_period)
                        transition_func_cache[
                            pair[vector[int], int] (neighbours, generations % alternating_period)] = ans
                else:
                    if ans == -1: ans = transition_func_cache[
                        pair[vector[int], int] (neighbours, generations % alternating_period)]

                if ans == 0:
                    dict_grid.erase(coordinates)
                    cells_changed.insert(coordinates)
                elif ans != dict_grid[coordinates]:
                    dict_grid[coordinates] = ans
                    cells_changed.insert(coordinates)
            else:
                neighbours.push_back(0)
                if transition_func_cache.find(
                        pair[vector[int], int] (neighbours, generations % alternating_period)) == \
                        transition_func_cache.end():
                    if ans == -1:
                        ans = transition_func(neighbours, generations % alternating_period)
                        transition_func_cache[
                            pair[vector[int], int] (neighbours, generations % alternating_period)] = ans
                else:
                    if ans == -1: ans = transition_func_cache[
                        pair[vector[int], int] (neighbours, generations % alternating_period)]

                if ans != 0:
                    dict_grid.insert(pair[pair[int, int], int] (coordinates, ans))
                    cells_changed.insert(coordinates)

    return cells_changed, dict_grid
