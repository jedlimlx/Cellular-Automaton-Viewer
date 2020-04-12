import copy
from typing import Dict, Tuple
from transFunc import get_neighbourhood

import CACompute.CACompute as compute

oscillate_in_bounds = False  # Check for Guns
offset_x, offset_y = 0, 0


def compare(first: Dict, second: Dict, lower_x: int, upper_x: int, lower_y: int, upper_y: int):
    global offset_x, offset_y, oscillate_in_bounds
    keys_first = sorted(first)
    keys_second = sorted(second)
    if len(first) == len(second):
        offset_x, offset_y = keys_first[0][1] - keys_second[0][1], keys_first[0][0] - keys_second[0][0]
        for i in range(1, len(keys_first)):
            if (keys_first[i][1] - keys_second[i][1]) != offset_x or \
                    (keys_first[i][0] - keys_second[i][0]) != offset_y:  # Checking for Spaceship
                return False
            if first[keys_first[i]] != second[keys_second[i]]:
                return False
        return True
    else:  # Checking for Gun
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


def identify(dict_grid: Dict[Tuple[int, int], int], generations: int) -> str:
    global offset_x, offset_y, oscillate_in_bounds
    start_grid = copy.deepcopy(dict_grid)  # Starting Grid

    # Finding Bounds
    lower_x, upper_x, lower_y, upper_y = 10 ** 9, 0, 10 ** 9, 0
    for cell in start_grid:
        if cell[0] < lower_y: lower_y = cell[0]
        elif cell[0] > upper_y: upper_y = cell[0]

        if cell[1] < lower_x: lower_x = cell[1]
        elif cell[1] > upper_x: upper_x = cell[1]

    copy_grid = copy.deepcopy(dict_grid)
    period: int = 1

    # Compute New Grid Cells
    cells_changed, dict_grid = compute.compute(get_neighbourhood(generations),
                                               dict_grid.keys(), copy_grid, dict_grid, generations)

    generations += 1

    while not compare(start_grid, dict_grid, lower_x, upper_x, lower_y, upper_y):
        copy_grid = copy.deepcopy(dict_grid)

        # Compute New Grid Cells
        cells_changed, dict_grid = compute.compute(get_neighbourhood(generations), cells_changed,
                                                   copy_grid, dict_grid, generations)
        period += 1
        generations += 1

        if period % 500 == 0: print(period)
        if period > 5000:
            return "The identification has failed. Your pattern has a period >5000 or " \
                   "is not supported by the software."

    offset_x, offset_y = abs(offset_x), abs(offset_y)
    if period > 1 and offset_x == 0 and offset_y == 0 and not oscillate_in_bounds:
        return f"Period {period} Oscillator"
    elif oscillate_in_bounds and period > 1:
        oscillate_in_bounds = False
        return f"Period {period} Gun"
    elif offset_x == offset_y and offset_x > 0:
        return f"Diagonal {abs(offset_x)}c/{period} Spaceship"
    elif offset_y == 0 and offset_x != 0:
        return f"Orthogonal {abs(offset_x)}c/{period} Spaceship"
    elif offset_x == 0 and offset_y != 0:
        return f"Orthogonal {abs(offset_y)}c/{period} Spaceship"
    elif offset_x != 0 and offset_y != 0:
        return f"Oblique ({abs(offset_x)}, {abs(offset_y)})c/{period} Spaceship"
    else:
        return "Still Life"
