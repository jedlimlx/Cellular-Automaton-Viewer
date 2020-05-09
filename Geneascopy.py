import random
import copy
import transFunc
import threading
import statistics
import importlib
import time
import numpy as np
import CACompute.CACompute as compute
import CAComputeParse.CACompute as parser
from typing import Dict, Tuple
from PyQt5.QtWidgets import QMessageBox

populations = []
soups_done: int = 0


parser.load("rule.ca_rule")


def reload(use_parse: bool):
    if use_parse:
        # Load Rule
        parser.load("rule.ca_rule")
    else:
        importlib.reload(transFunc)


def run_soup(use_parse: bool, max_generations: int):
    soup_size: int = 16
    generations: int = 0
    dict_grid: Dict[Tuple[int, int], int] = {}
    for x in range(soup_size):
        for y in range(soup_size):
            if random.randint(0, 1):
                dict_grid[(y, x)] = 1  # Generating Random Soup

    population = np.zeros(max_generations)
    cells_changed = dict_grid.keys()
    for i in range(max_generations):
        copy_grid = copy.deepcopy(dict_grid)
        population[generations] = len(copy_grid)
        if not generations % 250: print(generations)
        generations += 1
        if use_parse:  # Check if using parser
            cells_changed, dict_grid = parser.compute(cells_changed, copy_grid, dict_grid, generations)
        else:
            # Compute New Grid Cells
            cells_changed, dict_grid = compute.compute(transFunc.get_neighbourhood(generations),
                                                       cells_changed, copy_grid, dict_grid, generations)

    return population


def run_geneascopy(use_parse: bool, max_generations: int, num_soups: int):
    global populations, soups_done

    for i in range(num_soups):
        populations.append(run_soup(use_parse, max_generations))
        soups_done += 1
        print("Length", len(populations))


def main(use_parse: bool, max_generations: int, num_soups: int, filename: str):
    global populations
    for i in range(5):  # Threading to Speed Up the Process
        thread = threading.Thread(target=lambda: run_geneascopy(use_parse, max_generations, num_soups // 5))
        thread.start()

    while soups_done != num_soups:
        time.sleep(0.5)

    # Write to CSV File
    file = open(filename, "w+")
    file.write("Generations,Population\n")
    for generations in range(max_generations):
        generation_pop = []
        for soup_index in range(num_soups):
            generation_pop.append(populations[soup_index][generations])
        file.write(f"{generations},{statistics.mean(generation_pop)}\n")
    file.close()

    # Notify User
    QMessageBox.information(None, "Geneascopy Complete",
                            "The Geneascopy is Complete!",
                            QMessageBox.Ok, QMessageBox.Ok)
