import time
import copy
import importlib
import random
from collections import Counter

from libs import RuleParser
from typing import Dict, List, Set

import CACompute.CACompute as compute
import CAComputeParse.CAComputeParse as parser

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


def to_rle(dict_grid, use_parse: bool, bound_x: int, bound_y: int, bound_type: str) -> str:
    if len(dict_grid) == 0: return "EMPTY"

    y_bound = min([k[0] for k in dict_grid]), max([k[0] for k in dict_grid])
    x_bound = min([k[1] for k in dict_grid]), max([k[1] for k in dict_grid])
    
    # RLE Header
    header: str = f"x = {x_bound[1] - x_bound[0]}, y = {y_bound[1] - y_bound[0]}, rule = " \
                  f"{RuleParser.rule_name if use_parse else transFunc.rule_name}:" \
                  f"{bound_type.decode('utf-8')}{bound_x},{bound_y}\n"
    rle: str = ""

    # First add all data into the string
    for y in range(y_bound[1] - y_bound[0]):
        for x in range(x_bound[1] - x_bound[0]):
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


def agar_search(use_parse: bool, bound_x: int, bound_y: int, num_soups: int, bound_type: str, folder_location: str):
    print("Starting agar search...")
    print(f"On {bound_type.decode('utf-8')}{bound_x},{bound_y}")
    num_agars: Dict[int, int] = {}
    known_agars: Set[Counter] = set()

    if use_parse:
        parser.load("rule.ca_rule")
        parser.set_bounds(bound_x, bound_y, bound_type)
    else:
        importlib.reload(transFunc)
        compute.set_bounds(bound_x, bound_y, bound_type)

    start_time = time.time()
    for i in range(num_soups):
        if i % 200 == 0 and i != 0: print(f"{i} soups completed, {i / (time.time() - start_time)} soups/s")

        dict_grid = {}
        for x in range(bound_x + 1):
            for y in range(bound_y + 1):
                if random.randint(0, 100) > 50:
                    dict_grid[(y, x)] = 1  # Generating Random Soup

        # Initialising Variables
        generation: int = 0
        init_pat = copy.deepcopy(dict_grid)
        cells_changed = dict_grid.keys()
        hash_dict: Dict[int, int] = {}
        population_lst: List = []
        while True:
            # Running Simulation
            if use_parse:
                cells_changed, dict_grid = parser.compute(cells_changed, dict_grid, generation)
            else:
                cells_changed, dict_grid = compute.compute(transFunc.get_neighbourhood(generation),
                                                           cells_changed, dict_grid, dict_grid, generation)
            generation += 1

            grid_hash = make_hash(dict_grid)  # Hash the pattern
            if grid_hash in hash_dict: break  # Break if the pattern is periodic

            hash_dict[grid_hash] = generation  # Add hash & generation to the dictionary
            population_lst.append(tuple(Counter(dict_grid.values()).values()))  # Convert to hashable tuple

        period = generation - hash_dict[grid_hash]
        if period > 1:
            # Convert to tuple since list is unhashable
            if tuple(population_lst[hash_dict[grid_hash]:generation]) in known_agars:
                continue

            # Add all possible sequences.
            pop_seq = population_lst[hash_dict[grid_hash]:generation + 1]
            for generation in range(period):
                known_agars.add(tuple(pop_seq[generation:] + pop_seq[:generation]))

            # Write Agar to File
            file = open(f"{folder_location}/P{period}_{num_agars[period] if period in num_agars else 1}.rle", "w+")
            file.write(to_rle(dict_grid, use_parse, bound_x, bound_y, bound_type))
            file.close()

            # Write Predecessor to File
            file = open(f"{folder_location}/P{period}_Predecessor_"
                        f"{num_agars[period] if period in num_agars else 1}.rle", "w+")
            file.write(to_rle(init_pat, use_parse, bound_x, bound_y, bound_type))
            file.close()

            if period in num_agars:
                num_agars[period] += 1
            else:
                num_agars[period] = 1

    print("Search complete!")
    total_num = 0
    for i in sorted(num_agars.keys()):
        # 's' if num_agars[i] > 1 else '' to prevent grammar errors
        print(f"{num_agars[i]} P{i} agar{'s' if num_agars[i] > 1 else ''}")
        total_num += num_agars[i]

    print("="*20)
    print(f"{total_num} agar{'s' if total_num > 1 else ''} found in total")
    print(f"Search took {time.time() - start_time}s")


if __name__ == "__main__":
    RuleParser.load("rule.ca_rule")
    agar_search(True, 8, 8, 10000, b"T", "C:/Users/jedli/Downloads/agar")
