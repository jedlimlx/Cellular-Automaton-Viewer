import json
import copy
import logging
import random
import sys
import threading
import traceback
import importlib
import os
from functools import partial
from time import sleep, time
from typing import Dict, Tuple, Set, List

import RuleParser
import transFunc
import CACompute.CACompute as compute
import CAComputeParse.CACompute as parser
import numpy as np
import pyperclip
from PIL import Image
from PyQt5.Qt import pyqtSignal, QRect, QSize, QPoint, QFileDialog, QMessageBox
from PyQt5.QtGui import QPainter, QColor, QPixmap, QPen, QMouseEvent, QIcon
from PyQt5.QtWidgets import QLabel, QWidget, QGridLayout, QScrollArea, QPushButton, QRubberBand

from Identity import identify, reload

logging.basicConfig(filename='log.log', level=logging.INFO)

settings = json.load(open("settings.json", "r"))
use_parse: bool = settings["UseParse"]

if use_parse:
    RuleParser.load("rule.ca_rule")
    num_states = RuleParser.n_states
    ca_rule_name = RuleParser.rule_name
    colours = RuleParser.colour_palette
else:
    num_states = transFunc.n_states
    ca_rule_name = transFunc.rule_name
    colours = transFunc.colour_palette


class CACanvas(QWidget):
    global use_parse
    zoom_in = pyqtSignal()
    zoom_out = pyqtSignal()
    reset = pyqtSignal()
    reset_and_load = pyqtSignal(dict)
    change_title = pyqtSignal(str)

    def __init__(self, cell_size: int):
        super().__init__()

        # Initialising the Colours Used for the Different States
        if colours is None:
            if num_states > 2:
                self.colour_palette: List[Tuple[int, int, int]] = [(0, 0, 0)] + \
                                                                  [(255, 255 // (num_states - 2) * x, 0)
                                                                   for x in range(num_states - 1)]
            else:
                self.colour_palette: List[Tuple[int, int, int]] = [(0, 0, 0), (255, 255, 255)]
        else:
            try:  # Check that the Format is Valid
                assert isinstance(colours, list)
                for i in range(len(colours)):
                    if isinstance(colours[i], list):
                        colours[i] = tuple(colours[i])

                    assert isinstance(colours[i], tuple)
                    assert len(colours[i]) == 3
                    for j in colours[i]:
                        assert isinstance(j, int)

            except AssertionError:
                QMessageBox.warning(self, "Invalid Data in Colour Palette",  # Display Error Message
                                    "There is invalid data in the specified colour Palette. "
                                    "The colours should be in a tuple / list of 3 ints, RGB.",
                                    QMessageBox.Ok, QMessageBox.Ok)
                self.load_new_rule()
                sys.exit()  # Close the Program

            if len(colours) != num_states:
                QMessageBox.warning(self, "Error with Colour Palette",  # Display Error Message
                                    "Colour Palette with invalid length",
                                    QMessageBox.Ok, QMessageBox.Ok)
                self.load_new_rule()
                sys.exit()  # Close the Program

            self.colour_palette = colours

        self.current_state: int = 1

        # Initialising Cell Size -> Cells represented by cell_size * cell_size squares
        self.cell_size: int = cell_size

        # Initialising Dict Grid -> Data stored in sparse matrix (Dictionary)
        self.dict_grid: Dict[Tuple[int, int]] = {}

        # Initialising Pattern Bounds
        self.lower_x: int = 10 ** 9
        self.lower_y: int = 10 ** 9
        self.upper_x: int = 0
        self.upper_y: int = 0
        self.generations: int = 1

        # Initialising Cells Changed -> Keep Track of Changes in Prev Generation
        self.cells_changed: Set[Tuple[int, int]] = set()

        # Time between Updates
        self.pause: float = 0.002

        # Max Gen Speed
        self.max_speed: int = 100

        # Is the Simulation Running?
        self.running: bool = False

        # Is the Recording Running?
        self.recording: bool = False

        # Current Mode -> Painting or Selecting
        self.mode: str = "painting"

        # Density Parameter for Random Soup Generation
        self.density: float = 0.5

        # States to Include in Random Soup
        self.include_states: List[int] = [1]

        """
        # Dynamic Programming Optimization
        self.DP_neighbourhood = []
        neighbourhood = get_neighbourhood(0)
        for i in neighbourhood:
            for j in neighbourhood:
                self.DP_neighbourhood.append((i[0] + j[0], i[1] + j[1]))

        self.DP_neighbourhood = list(set(self.DP_neighbourhood))  # Eliminate Duplicates
        """

        # Soup Symmetry
        self.symmetry = "C1"

        # Frames
        self.frames: List = []

        # Initialising Recording Pattern Bounds
        self.recording_lower_x: int = 0
        self.recording_lower_y: int = 0
        self.recording_upper_x: int = 0
        self.recording_upper_y: int = 0

        # Rubber Band -> Selection Rectangle
        self.rubber_band = QRubberBand(QRubberBand.Rectangle, self)

        if use_parse:
            # Load Rule
            parser.load("rule.ca_rule")

        # Grid to Place Widgets
        grid = QGridLayout()
        grid.setHorizontalSpacing(1)
        grid.setVerticalSpacing(1)
        self.setLayout(grid)

        # QPixmap stored in Label -> Used to display cells
        self.label = QLabel()
        self.canvas = QPixmap(10000, 10000)
        self.canvas.fill(color=QColor(0, 0, 0))
        self.label.setPixmap(self.canvas)

        # Scroll Area to move around pattern
        self.scroll_area = QScrollArea()
        self.scroll_area.setWidget(self.label)
        grid.addWidget(self.scroll_area, 0, 0)

        # Grid to Store Buttons
        btns = QWidget()
        btn_grid = QGridLayout()
        btn_grid.setHorizontalSpacing(2)
        btn_grid.setContentsMargins(1, 1, 1, 1)

        btns.setLayout(btn_grid)

        # Button to Run Generations
        self.btn_run = QPushButton()
        self.btn_run.setIcon(QIcon("Icons/GliderPlayBtn1.png"))
        self.btn_run.setToolTip("Start the Simulation")
        self.btn_run.clicked.connect(self.toggle_simulation)
        btn_grid.addWidget(self.btn_run, 0, 0)

        # Button to Select Area
        self.btn_selection = QPushButton()
        self.btn_selection.setIcon(QIcon("Icons/SelectionIcon.png"))
        self.btn_selection.setToolTip("Selected an Area")
        self.btn_selection.clicked.connect(self.change_mode_selecting)
        btn_grid.addWidget(self.btn_selection, 0, 1)

        # Button to Draw Cells
        self.btn_painting = QPushButton()
        self.btn_painting.setIcon(QIcon("Icons/PaintBrush.png"))
        self.btn_painting.setToolTip("Draw on the Canvas")
        self.btn_painting.clicked.connect(lambda: self.change_state(1))
        btn_grid.addWidget(self.btn_painting, 0, 2)

        # Button to Zoom in
        self.btn_zoom_in = QPushButton()
        self.btn_zoom_in.setIcon(QIcon("Icons/ZoomIn.png"))
        self.btn_zoom_in.setToolTip("Zoom In")
        self.btn_zoom_in.clicked.connect(self.zoom_in.emit)
        btn_grid.addWidget(self.btn_zoom_in, 0, 3)

        # Button to Zoom out
        self.btn_zoom_out = QPushButton()
        self.btn_zoom_out.setIcon(QIcon("Icons/ZoomOut.png"))
        self.btn_zoom_out.setToolTip("Zoom Out")
        self.btn_zoom_out.clicked.connect(self.zoom_out.emit)
        btn_grid.addWidget(self.btn_zoom_out, 0, 4)

        grid.addWidget(btns, 1, 0)

        # New Grid for State Buttons
        state_btn_grid = QGridLayout()
        state_btn_grid.setHorizontalSpacing(2)
        state_btn_grid.setContentsMargins(1, 1, 1, 1)

        self.state_btns = QWidget()
        self.state_btns.setLayout(state_btn_grid)

        # List to Store the Buttons
        state_btn_lst: List[QPushButton] = []

        # State Chooser
        for index, colour in enumerate(self.colour_palette):
            img = Image.new('RGB', (250, 250), color=colour)
            img.save("Icons/Colour.png")

            state_btn = QPushButton("{index}".format(index=index), self)
            state_btn.setIcon(QIcon("Icons/Colour.png"))
            state_btn.setToolTip(f"Paint with State {index}")

            state_btn.clicked.connect(partial(self.change_state, index))

            state_btn_lst.append(state_btn)
            state_btn_grid.addWidget(state_btn_lst[index], 0, index)

        grid.addWidget(self.state_btns, 2, 0)

        # Hide the Buttons
        self.state_btns.hide()

        # Grid for Selection Tools
        grid_selection = QGridLayout()
        grid_selection.setHorizontalSpacing(2)
        grid_selection.setContentsMargins(1, 1, 1, 1)

        self.selection_tools = QWidget()
        self.selection_tools.setLayout(grid_selection)

        # Button to Generate Random Soup
        btn_random = QPushButton()
        btn_random.setIcon(QIcon("Icons/RandomSoupIcon.png"))
        btn_random.setToolTip("Generate Random Soup")
        btn_random.setIconSize(QSize(20, 20))
        btn_random.clicked.connect(self.random_soup)

        grid_selection.addWidget(btn_random, 0, 0)

        # Button to Flip Pattern Horizontally
        btn_flip_horizontal = QPushButton()
        btn_flip_horizontal.setIcon(QIcon("Icons/FlipHorizontal.png"))
        btn_flip_horizontal.setToolTip("Flip Pattern Horizontally")
        btn_flip_horizontal.setIconSize(QSize(20, 20))
        btn_flip_horizontal.clicked.connect(self.flip_horizontal)

        grid_selection.addWidget(btn_flip_horizontal, 0, 1)

        # Button to Flip Pattern Horizontally
        btn_flip_vertical = QPushButton()
        btn_flip_vertical.setIcon(QIcon("Icons/FlipVertical.png"))
        btn_flip_vertical.setToolTip("Flip Pattern Vertically")
        btn_flip_vertical.setIconSize(QSize(20, 20))
        btn_flip_vertical.clicked.connect(self.flip_vertical)

        grid_selection.addWidget(btn_flip_vertical, 0, 2)

        # Button to Rotate Pattern Clockwise
        btn_rotate_clockwise = QPushButton()
        btn_rotate_clockwise.setIcon(QIcon("Icons/RotateCW.png"))
        btn_rotate_clockwise.setToolTip("Rotate Pattern Clockwise")
        btn_rotate_clockwise.setIconSize(QSize(20, 20))
        btn_rotate_clockwise.clicked.connect(self.rotate_clockwise)

        grid_selection.addWidget(btn_rotate_clockwise, 0, 3)

        # Button to Rotate Pattern Counter-Clockwise
        btn_rotate_counterclockwise = QPushButton()
        btn_rotate_counterclockwise.setIcon(QIcon("Icons/RotateCCW.png"))
        btn_rotate_counterclockwise.setToolTip("Rotate Pattern Counter-Clockwise")
        btn_rotate_counterclockwise.setIconSize(QSize(20, 20))
        btn_rotate_counterclockwise.clicked.connect(self.rotate_counter_clockwise)

        grid_selection.addWidget(btn_rotate_counterclockwise, 0, 4)

        # Button to Identify Pattern
        btn_identify = QPushButton()
        btn_identify.setText("?")
        btn_identify.setToolTip("Identify Pattern")
        btn_identify.clicked.connect(self.identify_selection)

        grid_selection.addWidget(btn_identify, 0, 5)

        # Button to Record Images
        self.btn_record = QPushButton()
        self.btn_record.setIcon(QIcon("Icons/RecordLogo.png"))
        self.btn_record.clicked.connect(self.record_pattern)
        self.btn_record.setIconSize(QSize(20, 20))

        grid_selection.addWidget(self.btn_record, 0, 6)

        grid.addWidget(self.selection_tools, 2, 0)

        # Label to Display Status of the Simulation
        self.status_label = QLabel(text="Simulation Paused")
        grid.addWidget(self.status_label, 3, 0)

        # Start the Thread which runs the Simulation
        simulationThread = threading.Thread(target=self.run_simulation)
        simulationThread.start()

    def add_cell(self, state: int, x: int, y: int) -> None:
        if state > num_states - 1:
            raise IndexError

        # Add Cells to cells_changed
        self.cells_changed.add((y, x))

        # Add Cell to Dictionary
        if state > 0:
            self.dict_grid[(y, x)] = state
        elif (y, x) in self.dict_grid:
            self.dict_grid.pop((y, x))

        # Update Bounds
        self.lower_x = min(self.lower_x, x)
        self.lower_y = min(self.lower_y, y)
        self.upper_x = max(self.upper_x, x)
        self.upper_y = max(self.upper_y, y)

        pen = QPen()
        pen.setWidth(self.cell_size)

        # Set Colour
        pen.setColor(QColor(self.colour_palette[state][0],
                            self.colour_palette[state][1],
                            self.colour_palette[state][2]))

        painter = QPainter(self.label.pixmap())
        painter.setPen(pen)

        # Draws the cell as cell_size * cells_size squares
        painter.drawPoint(x * self.cell_size, y * self.cell_size)

        painter.end()

    def change_state(self, state: int) -> None:
        logging.log(logging.INFO, f"Changed Mode to Painting with State {state}")
        self.current_state = state
        self.mode = "painting"
        self.state_btns.show()
        self.selection_tools.hide()

    def change_mode_selecting(self) -> None:
        logging.log(logging.INFO, "Changed Mode to Selecting")
        self.mode = "selecting"
        self.state_btns.hide()
        self.selection_tools.show()

    def random_soup(self) -> None:
        try:
            if self.mode == "selecting":
                logging.log(logging.INFO, f"Generating {self.include_states} " +
                            f"Soup of density {self.density} and symmetry {self.symmetry}")

                # Getting Bounds
                lower_x, upper_x, lower_y, upper_y = self.selection_bounds()
                if self.symmetry == "C1":
                    for x in range(lower_x, upper_x):
                        for y in range(lower_y, upper_y):
                            if random.uniform(0, 1) < self.density:  # Should the Cell be Filled?
                                # Check if the fill is multi-state
                                self.add_cell(random.choice(self.include_states), x, y)
                            else:
                                self.add_cell(0, x, y)

                else:
                    upper_x = upper_x - (upper_x - lower_x) % 2 + 1
                    upper_y = upper_y - (upper_y - lower_y) % 2 + 1

                if self.symmetry == "C2_1":  # Ensure Odd by Odd
                    for x in range(lower_x, (lower_x + upper_x) // 2):
                        for y in range(lower_y, (lower_y + upper_y) // 2):
                            if random.uniform(0, 1) < self.density:  # Should the Cell be Filled?
                                # Check if the fill is multi-state
                                self.add_cell(random.choice(self.include_states), x, y)

                                # Add Cells on the Bottom Right
                                self.add_cell(random.choice(self.include_states),
                                              upper_x + lower_x - x - 3, upper_y + lower_y - y - 3)
                            else:
                                self.add_cell(0, x, y)
                                self.add_cell(0, upper_x + lower_x - x - 3, upper_y + lower_y - y - 3)

                elif self.symmetry == "C2_2":
                    for x in range(lower_x, (lower_x + upper_x) // 2):
                        for y in range(lower_y, (lower_y + upper_y) // 2):
                            if random.uniform(0, 1) < self.density:  # Should the Cell be Filled?
                                # Check if the fill is multi-state
                                self.add_cell(random.choice(self.include_states), x, y)

                                # Add Cells on the Bottom Right
                                self.add_cell(random.choice(self.include_states),
                                              upper_x + lower_x - x - 2, upper_y + lower_y - y - 3)
                            else:
                                self.add_cell(0, x, y)
                                self.add_cell(0, upper_x + lower_x - x - 2, upper_y + lower_y - y - 3)

                elif self.symmetry == "C2_4":  # Ensure Even by Even
                    for x in range(lower_x, (lower_x + upper_x) // 2):
                        for y in range(lower_y, (lower_y + upper_y) // 2):
                            if random.uniform(0, 1) < self.density:  # Should the Cell be Filled?
                                # Check if the fill is multi-state
                                self.add_cell(random.choice(self.include_states), x, y)

                                # Add Cells on the Bottom Right
                                self.add_cell(random.choice(self.include_states),
                                              upper_x + lower_x - x - 2, upper_y + lower_y - y - 2)
                            else:
                                self.add_cell(0, x, y)
                                self.add_cell(0, upper_x + lower_x - x - 2, upper_y + lower_y - y - 2)

                elif self.symmetry == "D2_+1":
                    for x in range(lower_x, upper_x):
                        for y in range(lower_y, (lower_y + upper_y) // 2):
                            if random.uniform(0, 1) < self.density:  # Should the Cell be Filled?
                                # Check if the fill is multi-state
                                self.add_cell(random.choice(self.include_states), x, y)

                                # Add Cells on the Bottom Right
                                self.add_cell(random.choice(self.include_states),
                                              x, upper_y + lower_y - y - 3)
                            else:
                                self.add_cell(0, x, y)
                                self.add_cell(0, x, upper_y + lower_y - y - 3)

                elif self.symmetry == "D2_+2":
                    for x in range(lower_x, upper_x):
                        for y in range(lower_y, (lower_y + upper_y) // 2):
                            if random.uniform(0, 1) < self.density:  # Should the Cell be Filled?
                                # Check if the fill is multi-state
                                self.add_cell(random.choice(self.include_states), x, y)

                                # Add Cells on the Bottom Right
                                self.add_cell(random.choice(self.include_states),
                                              x, upper_y + lower_y - y - 2)
                            else:
                                self.add_cell(0, x, y)
                                self.add_cell(0, x, upper_y + lower_y - y - 2)

                elif self.symmetry == "D4_+1":
                    for x in range(lower_x, (lower_x + upper_x) // 2):
                        for y in range(lower_y, (lower_y + upper_y) // 2):
                            if random.uniform(0, 1) < self.density:  # Should the Cell be Filled?
                                # Check if the fill is multi-state
                                self.add_cell(random.choice(self.include_states), x, y)

                                # Add Cells on the Bottom Right
                                self.add_cell(random.choice(self.include_states),
                                              upper_x + lower_x - x - 3, upper_y + lower_y - y - 3)

                                # Add Cells on the Bottom Left
                                self.add_cell(random.choice(self.include_states),
                                              x, upper_y + lower_y - y - 3)

                                # Add Cells on the Top Right
                                self.add_cell(random.choice(self.include_states),
                                              upper_x + lower_x - x - 3, y)
                            else:
                                self.add_cell(0, x, y)
                                self.add_cell(0, upper_x + lower_x - x - 3, upper_y + lower_y - y - 3)
                                self.add_cell(0, x, upper_y + lower_y - y - 3)
                                self.add_cell(0, upper_x + lower_x - x - 3, y)

                elif self.symmetry == "D4_+2":
                    for x in range(lower_x, (lower_x + upper_x) // 2):
                        for y in range(lower_y, (lower_y + upper_y) // 2):
                            if random.uniform(0, 1) < self.density:  # Should the Cell be Filled?
                                # Check if the fill is multi-state
                                self.add_cell(random.choice(self.include_states), x, y)

                                # Add Cells on the Bottom Right
                                self.add_cell(random.choice(self.include_states),
                                              upper_x + lower_x - x - 2, upper_y + lower_y - y - 3)

                                # Add Cells on the Bottom Left
                                self.add_cell(random.choice(self.include_states),
                                              x, upper_y + lower_y - y - 3)

                                # Add Cells on the Top Right
                                self.add_cell(random.choice(self.include_states),
                                              upper_x + lower_x - x - 2, y)
                            else:
                                self.add_cell(0, x, y)
                                self.add_cell(0, upper_x + lower_x - x - 2, upper_y + lower_y - y - 3)
                                self.add_cell(0, x, upper_y + lower_y - y - 3)
                                self.add_cell(0, upper_x + lower_x - x - 2, y)

                elif self.symmetry == "D4_+4":
                    for x in range(lower_x, (lower_x + upper_x) // 2):
                        for y in range(lower_y, (lower_y + upper_y) // 2):
                            if random.uniform(0, 1) < self.density:  # Should the Cell be Filled?
                                # Check if the fill is multi-state
                                self.add_cell(random.choice(self.include_states), x, y)

                                # Add Cells on the Bottom Right
                                self.add_cell(random.choice(self.include_states),
                                              upper_x + lower_x - x - 2, upper_y + lower_y - y - 2)

                                # Add Cells on the Bottom Left
                                self.add_cell(random.choice(self.include_states),
                                              x, upper_y + lower_y - y - 2)

                                # Add Cells on the Top Right
                                self.add_cell(random.choice(self.include_states),
                                              upper_x + lower_x - x - 2, y)
                            else:
                                self.add_cell(0, x, y)
                                self.add_cell(0, upper_x + lower_x - x - 2, upper_y + lower_y - y - 2)
                                self.add_cell(0, x, upper_y + lower_y - y - 2)
                                self.add_cell(0, upper_x + lower_x - x - 2, y)

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

        except AttributeError:
            QMessageBox.warning(self, "Error Generating Random Soup",
                                "Error Generating Random Soup\nNo Area Selected Yet",
                                QMessageBox.Ok, QMessageBox.Ok)


    def toggle_simulation(self) -> None:
        self.running = not self.running
        if self.running:
            self.status_label.setText(f"Generation: {self.generations}, "
                                      f"Population: {len(self.dict_grid)}\n"
                                      f"Simulation Running")
        else:
            self.status_label.setText(f"Generation: {self.generations}, "
                                      f"Population: {len(self.dict_grid)}\n"
                                      f"Simulation Paused")

    def run_simulation(self) -> None:
        count: int = 0
        while True:
            # https://logomakr.com/9XTuWZ
            if self.running:  # If Simulation is Running
                self.btn_run.setIcon(QIcon(f"Icons/StopButtonEater.png"))  # Show Stop Btn Icon
                try:
                    self.update_cells()  # Update the Cells
                except Exception:
                    print(traceback.format_exc())
                    logging.log(logging.ERROR, "Error in Runnning Simulation", exc_info=True)
            else:
                self.btn_run.setIcon(QIcon(f"Icons/GliderPlayBtn{count + 1}.png"))  # Play Glider Animation
                sleep(0.1)

            count += 1
            count %= 4
            sleep(self.pause)

    def update_cells(self) -> None:
        start_time: float = time()  # Get Time the Computation Started
        copy_grid = copy.deepcopy(self.dict_grid)  # Create a deepcopy as dictionaries are mutable
        if use_parse:
            # Compute New Grid Cells
            self.cells_changed, self.dict_grid = \
                parser.compute(self.cells_changed, copy_grid, self.dict_grid, self.generations)
        else:
            self.cells_changed, self.dict_grid = \
                compute.compute(transFunc.get_neighbourhood(self.generations), self.cells_changed, copy_grid,
                                self.dict_grid, self.generations)

        computation_time_taken: float = time() - start_time

        # Get Time the Visualisation Started
        start_time: float = time()
        self.generations += 1

        pen = QPen()
        pen.setWidth(self.cell_size)

        painter = QPainter(self.label.pixmap())
        painter.setPen(pen)

        for cell in self.cells_changed:
            # key -> (y, x)
            # * self.cell_size -> cells are represented by cell_size * cell_size squares
            if cell not in self.dict_grid:
                pen.setColor(QColor(self.colour_palette[0][0],
                                    self.colour_palette[0][1],
                                    self.colour_palette[0][2]))
                painter.setPen(pen)
                painter.drawPoint(cell[1] * self.cell_size, cell[0] * self.cell_size)

                if self.recording_lower_x <= cell[1] <= self.recording_upper_x and \
                        self.recording_lower_y <= cell[0] <= self.recording_upper_y and self.recording:
                    self.img[cell[0] - self.recording_lower_y][cell[1] - self.recording_lower_x] = \
                        self.colour_palette[0]
            else:
                pen.setColor(QColor(self.colour_palette[self.dict_grid[cell]][0],
                                    self.colour_palette[self.dict_grid[cell]][1],
                                    self.colour_palette[self.dict_grid[cell]][2]))
                painter.setPen(pen)
                painter.drawPoint(cell[1] * self.cell_size, cell[0] * self.cell_size)

                # Updating Bounds (Not needed above as the top one is converting cells to 0)
                if cell[1] < self.lower_x:
                    self.lower_x = cell[1]
                elif cell[1] > self.upper_x:
                    self.upper_x = cell[1]

                if cell[0] < self.lower_y:
                    self.lower_y = cell[0]
                elif cell[0] > self.upper_y:
                    self.upper_y = cell[0]

                if self.recording_lower_x <= cell[1] <= self.recording_upper_x and \
                        self.recording_lower_y <= cell[0] <= self.recording_upper_y and self.recording:
                    self.img[cell[0] - self.recording_lower_y][cell[1] - self.recording_lower_x] = \
                        self.colour_palette[self.dict_grid[cell]]

        painter.end()

        if self.recording: self.frames.append(self.img.copy())  # Add to Frames

        # Update Everything
        self.scroll_area.update()
        self.label.update()
        self.update()

        visualisation_time_taken: float = time() - start_time

        self.pause = 1 / self.max_speed - (visualisation_time_taken + computation_time_taken)
        if self.pause < 0: self.pause = 0

        # Inform User of the Speed of the Simulation
        gen_per_s: float = 1 / (visualisation_time_taken + computation_time_taken + self.pause)

        self.status_label.setText(f"Generation: {self.generations}, Population: {len(self.dict_grid)}\n"
                                  f"Simulation Running, Computation Time: {computation_time_taken}s, "
                                  f"Visualisation Time: {visualisation_time_taken}s, "
                                  f"Speed: {gen_per_s} gen/s.")
        self.status_label.update()

    def load_from_dict(self, dictionary: Dict[Tuple[int, int], int], offset_x=0, offset_y=0) -> None:
        self.label.pixmap().fill(color=QColor(0, 0, 0))  # Clear the Pixmap

        for key in dictionary:
            self.add_cell(dictionary[key], key[1] + offset_y, key[0] + offset_x)

        # Update Everything
        self.scroll_area.update()
        self.label.update()
        self.update()

    def to_rle(self, lower_x: int, lower_y: int, upper_x: int, upper_y: int) -> str:
        # RLE Header
        header: str = f"x = {upper_x - lower_x}, y = {upper_y - lower_y}, rule = {ca_rule_name}\n"

        # If converting entire pattern into RLE
        if lower_x == self.lower_x and upper_x == self.upper_x and \
                lower_y == self.lower_y and upper_y == self.upper_y:

            dict_grid = self.dict_grid

        else:
            dict_grid: Dict[Tuple[int, int], int] = {}  # Checking what cells are in the selection
            for key in self.dict_grid:
                if lower_x <= key[1] <= upper_x and lower_y <= key[0] <= upper_y:
                    dict_grid[key] = self.dict_grid[key]

        rle: str = ""

        # First add all data into the string
        for y in range(lower_y, upper_y):
            for x in range(lower_x, upper_x):
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

    @staticmethod
    def from_rle(rle: str) -> Dict[Tuple[int, int], int]:
        current_coor: List[int, int] = [0, 0]
        dict_grid: Dict[Tuple[int, int], int] = {}
        prev_int: bool = True
        first: bool = True
        num: int = 0

        parse_area: str = ""  # Handle more than 1 \n in rle file
        for i in rle.split("\n")[1:]:
            parse_area += i

        parse_area = parse_area.replace("\r", "")
        for i in parse_area[:-1]:
            try:  # Test if the string can be converted to int
                int(i)
                convert_to_int = True
            except ValueError:
                convert_to_int = False

            # Concatenate Digits Together
            if prev_int and convert_to_int:
                num = num * 10 + int(i)
            elif convert_to_int:
                num = int(i)

            if not convert_to_int and first: num = 1
            if i != "." and i != "$" and not convert_to_int:
                for j in range(num):
                    dict_grid[(current_coor[0], current_coor[1])] = ord(i) - 64

                    current_coor[1] += 1

            elif i == "$":
                current_coor[0] += num
                current_coor[1] = 0

            elif i == ".":
                current_coor[1] += num

            prev_int = convert_to_int
            if not convert_to_int: num = 1

            if first and not convert_to_int:
                first = False

        return dict_grid

    def selection_bounds(self) -> Tuple[int, int, int, int]:
        lst = [14, 12, 10, 9, 8, 7, 7, 8, 9, 10, 6, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]  # Necessary Offsets

        # Mapping coordinates based on ScrollArea
        x_offset: int = self.scroll_area.horizontalScrollBar().value() - lst[self.cell_size - 1]
        y_offset: int = self.scroll_area.verticalScrollBar().value() - lst[self.cell_size - 1]

        # Get Lower and Upper Bounds of the Selection
        lower_x, upper_x = sorted([(self.origin.x() + x_offset) // self.cell_size,
                                   (self.selection_release.x() + x_offset) // self.cell_size])

        lower_y, upper_y = sorted([(self.origin.y() + y_offset) // self.cell_size,
                                   (self.selection_release.y() + y_offset) // self.cell_size])
        return lower_x, upper_x, lower_y, upper_y

    def save_pattern(self) -> None:
        try:
            rle: str = self.to_rle(self.lower_x, self.lower_y, self.upper_x, self.upper_y)  # Get RLE

            # Open File Dialog
            file_name, _ = QFileDialog.getSaveFileName(caption="Save RLE File", filter="RLE Files (*.rle)")

            # Write to the file
            file = open(file_name, "w+")
            file.write(rle)
            file.close()

        except FileNotFoundError:
            pass

    def load_rule_from_file(self, filename: str, file_format: str) -> None:
        global use_parse, num_states, colours, ca_rule_name
        new_rule_file = open(filename, "r")
        if file_format == ".py":
            rule = new_rule_file.read()
            logging.log(logging.INFO, rule)

            # Open Rule File
            rule_file = open("transFunc.py", "w")
            rule_file.write(rule)
            rule_file.close()
            new_rule_file.close()

            # Update Settings
            use_parse = False
            settings = json.load(open("settings.json", "r"))
            settings["UseParse"] = False
            json.dump(settings, open("settings.json", "w"), indent=4)

            # Reload Rule
            importlib.reload(transFunc)
            num_states = transFunc.n_states
            ca_rule_name = transFunc.rule_name
            colours = transFunc.colour_palette
            compute.reload()
            reload(False)

            # Write to Recorded Rules
            recorded_rule_file = open("RecordedRules/" + ca_rule_name + ".py", "w+")
            recorded_rule_file.write(rule)
            recorded_rule_file.close()

        elif file_format == ".ca_rule":
            rule = new_rule_file.read()
            logging.log(logging.INFO, rule)

            # Open Rule File
            rule_file = open("rule.ca_rule", "w")
            rule_file.write(rule)
            rule_file.close()
            new_rule_file.close()

            # Reload File
            parser.load("rule.ca_rule")
            RuleParser.load("rule.ca_rule")
            reload(True)

            # Reload Variables
            num_states = RuleParser.n_states
            ca_rule_name = RuleParser.rule_name
            colours = RuleParser.colour_palette

            # Write to Recorded Rules
            recorded_rule_file = open("RecordedRules/" + ca_rule_name + ".ca_rule", "w+")
            recorded_rule_file.write(rule)
            recorded_rule_file.close()

            # Update Settings
            use_parse = True
            settings = json.load(open("settings.json", "r"))
            settings["UseParse"] = True
            json.dump(settings, open("settings.json", "w"), indent=4)

    def load_new_rule(self) -> None:
        global use_parse, num_states, colours, ca_rule_name

        # Start File Dialog
        filename, file_format = QFileDialog.getOpenFileName(caption="Select rule file",
                                                            filter="Python Files (*.py);;CA Rule Files (*.ca_rule)")

        # Open New Rule File
        try:
            self.load_rule_from_file(filename, ".py" if file_format == "Python Files (*.py)" else ".ca_rule")
            self.reset.emit()  # Reset Canvas

            self.change_title.emit(f"Cellular Automaton Viewer [{ca_rule_name}, No Pattern, "
                                   f"Number of States: {num_states}]")

        except FileNotFoundError:
            pass

        except Exception:
            print(traceback.format_exc())

    def reload_rule(self):
        global use_parse, num_states, colours, ca_rule_name

        # Reload File
        parser.load("rule.ca_rule")
        RuleParser.load("rule.ca_rule")

        # Reload Variables
        num_states = RuleParser.n_states
        ca_rule_name = RuleParser.rule_name
        colours = RuleParser.colour_palette

        # Update Settings
        use_parse = True
        settings = json.load(open("settings.json", "r"))
        settings["UseParse"] = True
        json.dump(settings, open("settings.json", "w"), indent=4)

    def record_pattern(self) -> None:
        try:
            if self.recording:
                self.btn_record.setIcon(QIcon("Icons/RecordLogo.png"))

                self.recording = False  # Stop Recording
                file_name, _ = QFileDialog.getSaveFileName(caption="Save *.gif File",
                                                           filter="GIF Files (*.gif)")

                img_frames: List = [Image.fromarray(x) for x in self.frames]
                img_frames[0].save(file_name, format='GIF', append_images=img_frames[1:],
                                   save_all=True, loop=0)
            else:
                self.btn_record.setIcon(QIcon("Icons/RecordIcon2.png"))

                # Start Recording
                self.recording = True

                # Getting Bounds for Recording
                self.recording_lower_x, self.recording_upper_x, \
                self.recording_lower_y, self.recording_upper_y = self.selection_bounds()

                self.img = np.zeros((self.recording_upper_y - self.recording_lower_y + 1,
                                     self.recording_upper_x - self.recording_lower_x + 1, 3),
                                    dtype=np.uint8)

                for key in self.dict_grid:
                    if self.recording_lower_x <= key[1] <= self.recording_upper_x and \
                            self.recording_lower_y <= key[0] <= self.recording_upper_y:
                        self.img[key[0] - self.recording_lower_y][key[1] - self.recording_lower_x] = \
                            np.array(self.colour_palette[self.dict_grid[key]])

                self.frames.append(self.img.copy())

        except FileNotFoundError:
            pass

        except Exception:
            print(traceback.format_exc())

    def flip_horizontal(self) -> None:
        if self.mode == "selecting":
            lower_x, upper_x, lower_y, upper_y = self.selection_bounds()

            grid: Dict[Tuple[int, int], int] = {}

            # Remove all cells in the box
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    if (y, x) in self.dict_grid: grid[(y, x)] = self.dict_grid[(y, x)]
                    self.add_cell(0, x, y)

            # Flipping Horizontally
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    if (y, x) in grid:
                        self.add_cell(grid[(y, x)], upper_x - x + lower_x, y)
                    else:
                        self.add_cell(0, upper_x - x + lower_x, y)

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

    def flip_vertical(self) -> None:
        if self.mode == "selecting":
            lower_x, upper_x, lower_y, upper_y = self.selection_bounds()

            grid: Dict[Tuple[int, int], int] = {}

            # Remove all cells in the box
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    if (y, x) in self.dict_grid: grid[(y, x)] = self.dict_grid[(y, x)]
                    self.add_cell(0, x, y)

            # Flipping Vertically
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    if (y, x) in grid:
                        self.add_cell(grid[(y, x)], x, upper_y - y + lower_y)
                    else:
                        self.add_cell(0, x, upper_y - y + lower_y)

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

    def rotate_clockwise(self):
        if self.mode == "selecting":
            lower_x, upper_x, lower_y, upper_y = self.selection_bounds()

            grid: Dict[Tuple[int, int], int] = {}

            # Remove all cells in the box
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    if (y, x) in self.dict_grid: grid[(y, x)] = self.dict_grid[(y, x)]
                    self.add_cell(0, x, y)

            # Flipping Vertically
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    if (y, x) in grid:
                        self.add_cell(grid[(y, x)], upper_x - y + lower_y, x - lower_x + lower_y)
                    else:
                        self.add_cell(0, upper_x - y + lower_y, x - lower_x + lower_y)

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

    def rotate_counter_clockwise(self):
        if self.mode == "selecting":
            lower_x, upper_x, lower_y, upper_y = self.selection_bounds()

            grid: Dict[Tuple[int, int], int] = {}

            # Remove all cells in the box
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    if (y, x) in self.dict_grid: grid[(y, x)] = self.dict_grid[(y, x)]
                    self.add_cell(0, x, y)

            # Rotate Counter Clockwise
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    if (y, x) in grid:
                        self.add_cell(grid[(y, x)], y - lower_y + lower_x, upper_y - x + lower_x)
                    else:
                        self.add_cell(0, y - lower_y + lower_x, upper_y - x + lower_x)

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

    def copy_selection(self) -> None:
        if self.mode == "selecting":
            lower_x, upper_x, lower_y, upper_y = self.selection_bounds()

            # Add to Clipboard
            pyperclip.copy(self.to_rle(lower_x, lower_y, upper_x, upper_y))

    def delete_selection(self) -> None:
        if self.mode == "selecting":
            lower_x, upper_x, lower_y, upper_y = self.selection_bounds()

            # Remove all cells in the box
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    self.add_cell(0, x, y)

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

    def cut_selection(self) -> None:
        if self.mode == "selecting":
            lower_x, upper_x, lower_y, upper_y = self.selection_bounds()

            # Add to Clipboard
            pyperclip.copy(self.to_rle(lower_x, lower_y, upper_x, upper_y))

            # Remove all cells in the box
            for x in range(lower_x, upper_x):
                for y in range(lower_y, upper_y):
                    self.add_cell(0, x, y)

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

    def paste_clipboard(self) -> None:
        rle: str = pyperclip.paste()
        pattern_rule_name: str = rle.split("\n")[0].split(",")[-1].replace(" rule = ", "")
        try:
            # Mapping coordinates based on ScrollArea
            x_offset: int = self.scroll_area.horizontalScrollBar().value()
            y_offset: int = self.scroll_area.verticalScrollBar().value()

            # Add the cells to the canvas
            grid: Dict[Tuple[int, int], int] = self.from_rle(rle)
            for key in grid:
                self.add_cell(grid[key], key[1] + (self.origin.x() + x_offset) // self.cell_size,
                              key[0] + (self.origin.y() + y_offset) // self.cell_size)

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

        except AttributeError:
            QMessageBox.warning(self, "RLE Error", "No Area has been selected yet",
                                QMessageBox.Ok, QMessageBox.Ok)

        except IndexError:
            QMessageBox.warning(self, "Error", "The number of states in the rule currently loaded is lower than "
                                               "the number of states in the rle. "
                                               "There could also be an unidentified character in the rle. "
                                               f"The current rule is {ca_rule_name} with {num_states} states. "
                                               f"Pattern's rule is {pattern_rule_name}. "
                                               f"Please load the correct rule first.",
                                QMessageBox.Ok, QMessageBox.Ok)

        except Exception:
            logging.log(logging.ERROR, f"Error Parsing RLE\n{rle}", exc_info=True)
            QMessageBox.warning(self, "RLE Parsing Error", traceback.format_exc(),
                                QMessageBox.Ok, QMessageBox.Ok)

    def identify_selection(self):
        # Getting Bounds
        lower_x, upper_x, lower_y, upper_y = self.selection_bounds()

        dict_grid: Dict[Tuple[int, int], int] = {}  # Checking what cells are in the selection
        for key in self.dict_grid:
            if lower_x <= key[1] <= upper_x and lower_y <= key[0] <= upper_y:
                dict_grid[key] = self.dict_grid[key]

        try:
            QMessageBox.information(self, "Identification Complete",
                                    identify(dict_grid, self.generations, use_parse),
                                    QMessageBox.Ok, QMessageBox.Ok)
        except Exception:
            print(traceback.format_exc())

    def open_pattern(self) -> None:
        pattern_rule_name = "???"
        try:
            logging.log(logging.INFO, "Opening Pattern...")

            # Open File Dialog
            filename, _ = QFileDialog.getOpenFileName(caption="Select .rle file",
                                                      filter="RLE Files (*.rle)")
            file = open(filename, "r")

            rle: str = file.read()

            # Get Pattern's Name
            grid: Dict[Tuple[int, int], int] = self.from_rle(rle)
            loaded_rule: bool = False
            pattern_rule_name: str = rle.split("\n")[0].split(",")[-1].replace(" rule = ", "")
            if pattern_rule_name != ca_rule_name:
                for i in os.listdir("RecordedRules"):
                    if i.split(".")[0] == pattern_rule_name:
                        self.load_rule_from_file("RecordedRules/" + i, "." + i.split(".")[1])
                        self.reset_and_load.emit(grid)
                        loaded_rule = True
                if not loaded_rule:
                    raise IndexError

            if not loaded_rule:
                # Add the cells to the canvas
                for key in grid:
                    self.add_cell(grid[key], key[1] + 100, key[0] + 100)

            # Close File
            file.close()

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

            # Change Title
            pattern_name = filename.split("/")[-1]
            self.change_title.emit(f"Cellular Automaton Viewer [{ca_rule_name}, {pattern_name}, "
                                   f"Number of States: {num_states}]")

        except FileNotFoundError:
            logging.log(logging.INFO, "Cancelled Operation")

        except IndexError:
            print(traceback.format_exc())
            QMessageBox.warning(self, "Error", "The rule currently loaded is not the same as the "
                                               "rule needed by the pattern. "
                                               f"Please load the correct rule. \n"
                                               f"Current Rule: {ca_rule_name} \n"
                                               f"Required Rule: {pattern_rule_name}",
                                QMessageBox.Ok, QMessageBox.Ok)
            self.load_new_rule()

        except Exception:
            logging.log(logging.INFO, "Error Parsing RLE", exc_info=True)
            QMessageBox.warning(self, "RLE Parsing Error", traceback.format_exc(),
                                QMessageBox.Ok, QMessageBox.Ok)

    def mouseMoveEvent(self, event: QMouseEvent) -> None:
        if self.mode == "painting":
            if self.running:  # Pause the Simulation
                self.toggle_simulation()

            # Mapping coordinates based on ScrollArea
            new_x: int = self.scroll_area.horizontalScrollBar().value() + event.x()
            new_y: int = self.scroll_area.verticalScrollBar().value() + event.y()

            # Add Cell to Grid and Canvas
            self.add_cell(self.current_state, new_x // self.cell_size, new_y // self.cell_size)

            logging.log(logging.INFO, f"Drawing at with state {self.current_state}" +
                        f"{(new_x // self.cell_size, new_y // self.cell_size)}")

            # Update Everything
            self.scroll_area.update()
            self.label.update()
            self.update()

        elif self.mode == "selecting":
            x: int = event.pos().x()
            y: int = event.pos().y()

            # Mapping coordinates based on ScrollArea
            x_offset: int = self.scroll_area.horizontalScrollBar().value()
            y_offset: int = self.scroll_area.verticalScrollBar().value()

            # Align to Cells
            self.rubber_band.setGeometry(QRect(self.offset_origin,
                                               QPoint(x - x % self.cell_size - (x_offset + 1) % self.cell_size,
                                                      y - y % self.cell_size - (y_offset + 1) % self.cell_size)))

    def mousePressEvent(self, event: QMouseEvent) -> None:
        if self.mode == "selecting":
            x: int = event.pos().x()
            y: int = event.pos().y()

            # Mapping coordinates based on ScrollArea
            x_offset: int = self.scroll_area.horizontalScrollBar().value()
            y_offset: int = self.scroll_area.verticalScrollBar().value()

            # Align to Cells
            self.origin = QPoint(x - x % self.cell_size,
                                 y - y % self.cell_size)
            self.offset_origin = QPoint(x - x % self.cell_size - (x_offset + 1) % self.cell_size,
                                        y - y % self.cell_size - (y_offset + 1) % self.cell_size)
            self.rubber_band.setGeometry(QRect(self.offset_origin, QSize()))
            self.rubber_band.show()

            logging.log(logging.INFO, f"Start selecting at {(x // self.cell_size, y // self.cell_size)}")

    def mouseReleaseEvent(self, event: QMouseEvent) -> None:
        if self.mode == "selecting":
            x: int = event.pos().x()
            y: int = event.pos().y()

            self.selection_release = QPoint(x - x % self.cell_size,
                                            y - y % self.cell_size)
            logging.log(logging.INFO, f"Stopped selecting at {(x // self.cell_size, y // self.cell_size)}")
