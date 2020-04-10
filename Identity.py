import copy
from typing import Dict, Tuple
from transFunc import get_neighbourhood

import CACompute.CACompute as compute

offset_x, offset_y = 0, 0


def compare(first: Dict, second: Dict):
    global offset_x, offset_y
    keys_first = sorted(first)
    keys_second = sorted(second)
    if len(first) == len(second):
        offset_x, offset_y = keys_first[0][1] - keys_second[0][1], keys_first[0][0] - keys_second[0][0]
        for i in range(1, len(keys_first)):
            if (keys_first[i][1] - keys_second[i][1]) != offset_x or \
                    (keys_first[i][0] - keys_second[i][0]) != offset_y:
                return False
            if first[keys_first[i]] != second[keys_second[i]]:
                return False
        return True
    else:
        return False


def identify(dict_grid: Dict[Tuple[int, int], int], generations: int) -> str:
    global offset_x, offset_y
    start_grid = copy.deepcopy(dict_grid)

    copy_grid = copy.deepcopy(dict_grid)
    lower_x, upper_x, lower_y, upper_y = 0, 0, 0, 0
    period: int = 1

    # Compute New Grid Cells
    lower_x, upper_x, lower_y, upper_y, cells_changed, dict_grid = \
        compute.compute(get_neighbourhood(generations), dict_grid.keys(),
                        lower_x, upper_x, lower_y, upper_y, copy_grid, dict_grid, generations)

    while not compare(start_grid, dict_grid):
        copy_grid = copy.deepcopy(dict_grid)

        # Compute New Grid Cells
        lower_x, upper_x, lower_y, upper_y, cells_changed, dict_grid = \
            compute.compute(get_neighbourhood(generations), cells_changed,
                            lower_x, upper_x, lower_y, upper_y, copy_grid, dict_grid, generations)
        period += 1

        if period % 500 == 0: print(period)
        if period > 5000:
            return "failed"

    offset_x, offset_y = abs(offset_x), abs(offset_y)
    if period > 1 and offset_x == 0 and offset_y == 0:
        return f"Period {period} Oscillator"
    elif offset_x == offset_y and offset_x > 0:
        return f"Orthogonal {abs(offset_x)}c/{period} Spaceship"
    elif offset_y == 0 and offset_x != 0:
        return f"{abs(offset_x)}c/{period} Spaceship"
    elif offset_x == 0 and offset_y != 0:
        return f"{abs(offset_y)}c/{period} Spaceship"
    elif offset_x != 0 and offset_y != 0:
        return f"Oblique ({abs(offset_x)}, {abs(offset_y)})c/{period} Spaceship"
    else:
        return "Still Life"
