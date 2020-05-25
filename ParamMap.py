import copy
import random
from typing import List, Tuple

import numpy as np
from PIL import Image

import CAComputeParse.CACompute as parser
import RuleParser


def reeeeeeeload():
    parser.load(f"PMap/rule.ca_rule")
    RuleParser.load(f"PMap/rule.ca_rule")


def generate_param_map(soup_size, rows, columns, max_generations):
    param_map_array = []
    for i in range(max_generations):
        param_map_array.append(np.zeros((soup_size * 3 * rows + 1, soup_size * 3 * columns + 1, 3),
                                        dtype=np.uint8))

    soup = {}  # Generating Random Soup
    for i in range(soup_size, soup_size * 2):
        for j in range(soup_size, soup_size * 2):
            if random.randint(0, 1):
                soup[(i, j)] = 1

    for y in range(rows):
        for x in range(columns):
            rule = open(f"PMap/rule_{x}_{y}.ca_rule", "r")
            rule_contents = rule.read()
            rule.close()

            new_rule = open("PMap/rule.ca_rule", "w+")  # Transferring Rule
            new_rule.write(rule_contents)
            new_rule.close()

            reeeeeeeload()

            if RuleParser.n_states > 2:
                colour_palette: List[Tuple[int, int, int]] = [(0, 0, 0)] + \
                                                             [(255, 255 // (RuleParser.n_states - 2) * x, 0)
                                                              for x in range(RuleParser.n_states - 1)]
            else:
                colour_palette: List[Tuple[int, int, int]] = [(0, 0, 0), (255, 255, 255)]

            try:
                dict_grid = copy.deepcopy(soup)
                cells_changed = soup.keys()  # Running Simulation
                for generations in range(max_generations):
                    if generations == 100: print(sorted(dict_grid))
                    cells_changed, dict_grid = parser.compute(cells_changed, dict_grid, generations)

                    keys_to_pop = []
                    for key in dict_grid:
                        if 0 <= key[0] <= soup_size * 3 and 0 <= key[1] <= soup_size * 3:  # Checking Within Bounds
                            param_map_array[generations][soup_size * 3 * y + key[1]][soup_size * 3 * x + key[0]] = \
                                colour_palette[dict_grid[key]]
                        else:
                            keys_to_pop.append(key)

                    for key in keys_to_pop:  # Delete cells outside boundary
                        dict_grid.pop(key)
            except IndexError:
                print("ASDAS")

    img_frames: List = [Image.fromarray(x) for x in param_map_array]
    img_frames[0].save("pmap.gif", format='GIF', append_images=img_frames[1:],
                       save_all=True, loop=0)
    print("Done!")
