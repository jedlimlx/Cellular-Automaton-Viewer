import copy
import transFunc
import importlib
from typing import Dict, Tuple
from Dialogs import IdentityDialog

import CACompute.CACompute as compute
import CAComputeParse.CAComputeParse as parser

offset_x, offset_y = 0, 0
oscillate_in_bounds = False  # Check for Guns
replicator = False  # Check for Replicator

parser.load("rule.ca_rule")


def reload(use_parse: bool):
    if use_parse:
        # Load Rule
        parser.load("rule.ca_rule")
    else:
        importlib.reload(transFunc)


def compare(first: Dict, second: Dict, lower_x: int, upper_x: int, lower_y: int, upper_y: int, pattern_type):
    global offset_x, offset_y, oscillate_in_bounds, replicator
    keys_first = sorted(first)
    keys_second = sorted(second)
    if len(first) == len(second) and \
            (pattern_type == "Still Life / Oscillator / Spaceship" or pattern_type == "Could be anything"):
        offset_x, offset_y = keys_first[0][1] - keys_second[0][1], keys_first[0][0] - keys_second[0][0]
        for i in range(1, len(keys_first)):
            if (keys_first[i][1] - keys_second[i][1]) != offset_x or \
                    (keys_first[i][0] - keys_second[i][0]) != offset_y:  # Checking for Spaceship
                return False
            if first[keys_first[i]] != second[keys_second[i]]:
                return False
        return True

    elif len(first) * 2 == len(second) and \
            (pattern_type == "Replicator" or pattern_type == "Could be anything"):
        # print("=" * 10, len(first), len(second))

        offset_x, offset_y = keys_first[0][1] - keys_second[0][1], keys_first[0][0] - keys_second[0][0]
        for j in range(1, len(keys_second)):
            found = False
            # index_found = 0
            for i in range(len(keys_first)):
                if ((keys_first[i][1] - keys_second[j][1]) != offset_x and
                    (keys_first[i][1] - keys_second[j][1]) != -offset_x) and \
                        ((keys_first[i][0] - keys_second[j][0]) != offset_y and
                         (keys_first[i][0] - keys_second[j][0]) != -offset_y):  # Checking for Replicator Offset
                    """
                    print(offset_x, offset_y,
                          keys_first[i][1] - keys_second[j][1],
                          keys_first[i][0] - keys_second[j][0],
                          first[keys_first[i]], second[keys_second[j]], i, j,
                          ((keys_first[i][1] - keys_second[j][1]) == offset_x or
                           (keys_first[i][1] - keys_second[j][1]) == -offset_x) and \
                          ((keys_first[i][0] - keys_second[j][0]) == offset_y or
                           (keys_first[i][0] - keys_second[j][0]) == -offset_y),
                          "Offset Incorrect")
                    """
                    continue

                if first[keys_first[i]] != second[keys_second[j]]:
                    """
                    print(offset_x, offset_y,
                          keys_first[i][1] - keys_second[j][1],
                          keys_first[i][0] - keys_second[j][0],
                          first[keys_first[i]], second[keys_second[j]], i, j,
                          first[keys_first[i]] == second[keys_second[j]], "State Incorrect")
                    """
                    continue

                found = True
                # index_found = i  # For Debugging Purposes
                break

            """
            print(offset_x, offset_y,
                  keys_first[index_found][1] - keys_second[j][1], keys_first[index_found][0] - keys_second[j][0],
                  first[keys_first[index_found]], second[keys_second[j]], found, index_found, j)
            """

            if not found: return False

        replicator = True
        return True

    elif pattern_type == "Gun" or pattern_type == "Could be anything":
        refined_keys_second = []
        for cell in keys_second:
            if lower_x <= cell[1] <= upper_x and lower_y <= cell[0] <= upper_y:
                refined_keys_second.append(cell)

        refined_keys_second.sort()
        if len(refined_keys_second) == len(keys_first):
            for i in range(len(keys_first)):
                if first[keys_first[i]] != second[refined_keys_second[i]]:
                    return False

            oscillate_in_bounds = True
            return True

        return False


def identify(dict_grid: Dict[Tuple[int, int], int], generations: int, use_parse: bool) -> str:
    global offset_x, offset_y, oscillate_in_bounds, replicator
    identity_dialog = IdentityDialog()
    pattern_type = identity_dialog.get_results()
    if pattern_type == "": return "Operation Cancelled!"

    start_grid = copy.deepcopy(dict_grid)  # Starting Grid

    # Finding Bounds
    lower_x, upper_x, lower_y, upper_y = 10 ** 9, 0, 10 ** 9, 0
    for cell in start_grid:
        if cell[0] < lower_y:
            lower_y = cell[0]
        elif cell[0] > upper_y:
            upper_y = cell[0]

        if cell[1] < lower_x:
            lower_x = cell[1]
        elif cell[1] > upper_x:
            upper_x = cell[1]

    copy_grid = copy.deepcopy(dict_grid)
    period: int = 1

    if use_parse:  # Check if using parser
        cells_changed, dict_grid = parser.compute(dict_grid.keys(), dict_grid, generations)
    else:
        # Compute New Grid Cells
        cells_changed, dict_grid = compute.compute(transFunc.get_neighbourhood(generations),
                                                   dict_grid.keys(), copy_grid, dict_grid, generations)

    generations += 1

    while not compare(start_grid, dict_grid, lower_x, upper_x, lower_y, upper_y, pattern_type):
        copy_grid = copy.deepcopy(dict_grid)

        if use_parse:  # Check if using parser
            cells_changed, dict_grid = parser.compute(cells_changed, dict_grid, generations)
        else:
            # Compute New Grid Cells
            cells_changed, dict_grid = compute.compute(transFunc.get_neighbourhood(generations), cells_changed,
                                                       copy_grid, dict_grid, generations)
        period += 1
        generations += 1

        if period % 500 == 0:
            print(period)

        if period > 5000:
            return "The identification has failed. Your pattern has a period >5000 or " \
                   "is not supported by the software. There could also be a bug in the program. " \
                   "If you suspect a bug, please report it."

    offset_x, offset_y = abs(offset_x), abs(offset_y)

    # Oscillator
    if period > 1 and offset_x == 0 and offset_y == 0 and not oscillate_in_bounds and not replicator:
        return f"Period {period} Oscillator"

    # Gun
    elif oscillate_in_bounds and period > 1:
        oscillate_in_bounds = False
        return f"Period {period} Gun"

    # Replicators
    elif offset_x == offset_y and offset_x > 0 and replicator:
        replicator = False
        return f"Diagonal {abs(offset_x)}c/{period} Replicator"
    elif offset_y == 0 and offset_x != 0 and replicator:
        replicator = False
        return f"Orthogonal {abs(offset_x)}c/{period} Replicator"
    elif offset_x == 0 and offset_y != 0 and replicator:
        replicator = False
        return f"Orthogonal {abs(offset_y)}c/{period} Replicator"
    elif offset_x != 0 and offset_y != 0 and replicator:
        replicator = False
        return f"Oblique ({abs(offset_x)}, {abs(offset_y)})c/{period} Replicator"

    # Spaceship
    elif offset_x == offset_y and offset_x > 0:
        return f"Diagonal {abs(offset_x)}c/{period} Spaceship"
    elif offset_y == 0 and offset_x != 0:
        return f"Orthogonal {abs(offset_x)}c/{period} Spaceship"
    elif offset_x == 0 and offset_y != 0:
        return f"Orthogonal {abs(offset_y)}c/{period} Spaceship"
    elif offset_x != 0 and offset_y != 0:
        return f"Oblique ({abs(offset_x)}, {abs(offset_y)})c/{period} Spaceship"
    else:
        return "Still Life"  # Still Life
