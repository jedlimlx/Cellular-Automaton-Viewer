import bisect
import copy
import importlib
import random
from typing import List

import CACompute.CACompute as compute
import CAComputeParse.CACompute as parser

import transFunc


def freeze(o):
    if isinstance(o, dict):
        return frozenset({k: freeze(v) for k, v in o.items()}.items())

    if isinstance(o, list):
        return tuple([freeze(v) for v in o])

    return o


def make_hash(o):
    """
    makes a hash out of anything that contains only list,dict and hashable types including string and numeric types
    """
    return hash(freeze(o))


def to_rle(dict_grid) -> str:
    if len(dict_grid) == 0: return "EMPTY"
    
    # RLE Header
    header: str = f"x = 0, y = 0, rule = Life\n"
    rle: str = ""

    # First add all data into the string
    for y in range(min([k[0] for k in dict_grid]), max([k[0] for k in dict_grid])):
        for x in range(min([k[1] for k in dict_grid]), max([k[1] for k in dict_grid])):
            if (y, x) in dict_grid:
                rle += str(chr(64 + dict_grid[(y, x)]))
            else:
                rle += "."

        rle += "$"

    prev_char: str = ''
    rle_final: str = ''
    count: int = 1

    for char in rle:
        # If the prev and current characters don't match
        if char != prev_char:
            # Add the count and character to our encoding
            if prev_char:
                if count == 1:
                    rle_final += prev_char
                else:
                    rle_final = rle_final + str(count) + prev_char

            count = 1
            prev_char = char
        else:
            # If they do, increment the counter
            count += 1
    else:
        # Finish off the encoding
        rle_final = rle_final + str(count) + prev_char

    return header + rle_final + "!"


def agar_search(use_parse: bool, bound_x: int, bound_y: int, num_soups: int, bound_type: str):
    print("Starting search...")
    num_agars = {}

    if use_parse:
        parser.load("rule.ca_rule")
        parser.set_bounds(bound_x, bound_y, bound_type)
    else:
        importlib.reload(transFunc)
        compute.set_bounds(bound_x, bound_y, bound_type)

    for i in range(num_soups):
        dict_grid = {}
        for x in range(bound_x + 1):
            for y in range(bound_y + 1):
                if random.randint(0, 1):
                    dict_grid[(y, x)] = 1  # Generating Random Soup

        generation: int = 0
        init_pat = copy.deepcopy(dict_grid)
        cells_changed = dict_grid.keys()
        hash_list: List[int] = []
        generation_lst: List[int] = []
        while True:
            # Running Simulation
            if use_parse:
                cells_changed, dict_grid = parser.compute(cells_changed, dict_grid, generation)
            else:
                cells_changed, dict_grid = compute.compute(transFunc.get_neighbourhood(generation),
                                                           cells_changed, dict_grid, dict_grid, generation)
            generation += 1

            grid_hash = make_hash(dict_grid)
            index = bisect.bisect_left(hash_list, grid_hash)

            # Break from loop if pattern becomes periodic
            if len(hash_list) > index and hash_list[index] == grid_hash: break

            # Add pattern to hash_list and keep track of what generation it is in
            hash_list = hash_list[:index] + [grid_hash]
            generation_lst = generation_lst[:index] + [generation]

        period = generation - generation_lst[index]
        if period > 1:
            print(f"Period {period}")
            print(to_rle(init_pat))
            print(to_rle(dict_grid))
            print("=" * 20)

            if period in num_agars:
                num_agars[period] += 1
            else:
                num_agars[period] = 1

    print("Search complete!")
    total_num = 0
    for i in sorted(num_agars.keys()):
        # 's' if num_agars[i] > 1 else '' to prevent grammar errors
        print(f"{num_agars[i]} period {i} agar{'s' if num_agars[i] > 1 else ''}")
        total_num += num_agars[i]

    print(f"{total_num} agar{'s' if num_agars[i] > 1 else ''} found in total")
