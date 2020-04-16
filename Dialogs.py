import random, traceback
from functools import partial
from typing import Tuple, List

from PyQt5.Qt import QIcon, pyqtSignal
from PyQt5.QtCore import Qt
from PyQt5.QtWidgets import QLabel, QGridLayout, QSlider, QDialog, QDialogButtonBox, \
    QComboBox, QCheckBox, QWidget, QPushButton, QLineEdit


class Table(QWidget):
    def __init__(self, width: int, height: int, text: str, row_names: List, column_names: List,
                 button_width=-1, button_height=-1):
        super().__init__()

        grid = QGridLayout()
        self.setLayout(grid)

        label = QLabel(text)  # Label to Display Description
        label.setAlignment(Qt.AlignCenter)
        grid.addWidget(label)

        table_btns = QWidget()
        table_grid = QGridLayout()
        table_btns.setLayout(table_grid)

        btns = []  # List to Store Buttons
        self.num = [["0" for x in range(width)] for y in range(height)]  # List to Store Values
        for i in range(width + 1):
            for j in range(height + 1):
                if i == 0:  # Row and Column Labels
                    if j == 0: continue
                    label = QLabel(column_names[j - 1])
                    label.setAlignment(Qt.AlignCenter)
                    table_grid.addWidget(label, j, i)
                    continue
                if j == 0:
                    label = QLabel(row_names[i - 1])
                    label.setAlignment(Qt.AlignCenter)
                    table_grid.addWidget(label, j, i)
                    continue
                btn = QPushButton("{i} {j}".format(i=i - 1, j=j - 1), self)
                if button_width != -1 and button_height != -1:
                    btn.setFixedWidth(button_width)
                    btn.setFixedHeight(button_height)
                btn.clicked.connect(partial(self.func, j - 1, i - 1))
                btn.setText("0")
                table_grid.addWidget(btn, j, i)

                btns.append(btn)

        grid.addWidget(table_btns)

    def func(self, i, j):
        current_text: str = self.sender().text()
        new_text = str((int(current_text) + 11) % 21 - 10)
        self.num[i][j] = new_text
        self.sender().setText(new_text)


class SoupSettings(QDialog):
    def __init__(self, density: int, symmetry: str):
        super().__init__()

        grid = QGridLayout()
        self.setLayout(grid)
        self.setWindowTitle("Random Soup Settings")
        self.setWindowIcon(QIcon("Icons/RandomSoupIcon.png"))

        # Density Label
        self.density_label = QLabel(f"Density: {density}%")
        grid.addWidget(self.density_label)

        # Slider for adjusting density
        self.slider = QSlider(Qt.Horizontal)
        self.slider.setMaximum(100)
        self.slider.setMinimum(0)
        self.slider.setTickInterval(5)
        self.slider.setValue(density)
        self.slider.valueChanged.connect(self.change_label)
        grid.addWidget(self.slider)

        # Symmetry Label
        symmetry_label = QLabel("Select Soup Symmetry: ")
        grid.addWidget(symmetry_label)

        # Combo Box for Selecting Symmetries
        self.symmetries: List[str] = ["C1", "C2_1", "C2_2", "C2_4",
                                      "D2_+1", "D2_+2", "D4_+1", "D4_+2", "D4_+4"]

        self.combo = QComboBox()
        self.combo.addItems(self.symmetries)
        self.combo.setCurrentIndex(self.symmetries.index(symmetry))
        grid.addWidget(self.combo)

        # Okay and Cancel Button
        btns = QDialogButtonBox.Ok | QDialogButtonBox.Cancel

        button_box = QDialogButtonBox(btns)
        button_box.accepted.connect(self.accept)
        button_box.rejected.connect(self.reject)

        grid.addWidget(button_box)

    def change_label(self):
        self.density_label.setText(f"Density: {self.slider.value()}%")

    def get_results(self) -> Tuple[int, str]:
        if self.exec_() == QDialog.Accepted:
            return self.slider.value(), self.symmetries[self.combo.currentIndex()]
        else:
            return -1, "#"


class SettingZoom(QDialog):
    def __init__(self, zoom: int):
        super().__init__()

        grid = QGridLayout()
        self.setLayout(grid)
        self.setWindowTitle("Set Zoom")
        self.setWindowIcon(QIcon("Icons/ZoomIn.png"))

        self.label = QLabel(f"Zoom: {zoom}")
        grid.addWidget(self.label)

        # Slider for adjusting zoom
        self.slider = QSlider(Qt.Horizontal)
        self.slider.setMaximum(25)
        self.slider.setMinimum(1)
        self.slider.setTickInterval(5)
        self.slider.setValue(zoom)
        self.slider.valueChanged.connect(self.change_label)
        grid.addWidget(self.slider)

        # Okay and Cancel Button
        btns = QDialogButtonBox.Ok | QDialogButtonBox.Cancel

        button_box = QDialogButtonBox(btns)
        button_box.accepted.connect(self.accept)
        button_box.rejected.connect(self.reject)

        grid.addWidget(button_box)

    def change_label(self):
        self.label.setText(f"Zoom: {self.slider.value()}")

    def get_results(self) -> int:
        if self.exec_() == QDialog.Accepted:
            return self.slider.value()
        else:
            return -1


class SimulationSettings(QDialog):
    def __init__(self, speed: int):
        super().__init__()

        grid = QGridLayout()
        self.setLayout(grid)
        self.setWindowTitle("Simulation Settings")
        self.setWindowIcon(QIcon("Icons/PulsarIcon.png"))

        self.label = QLabel(f"Max Speed (gen/s): {speed}")
        grid.addWidget(self.label)

        # Slider for adjusting zoom
        self.slider = QSlider(Qt.Horizontal)
        self.slider.setMaximum(500)
        self.slider.setMinimum(0)
        self.slider.setTickInterval(5)
        self.slider.setValue(speed)
        self.slider.valueChanged.connect(self.change_label)
        grid.addWidget(self.slider)

        # Okay and Cancel Button
        btns = QDialogButtonBox.Ok | QDialogButtonBox.Cancel

        button_box = QDialogButtonBox(btns)
        button_box.accepted.connect(self.accept)
        button_box.rejected.connect(self.reject)

        grid.addWidget(button_box)

    def change_label(self):
        self.label.setText(f"Max Speed (gen/s): {self.slider.value()}")

    def get_results(self) -> int:
        if self.exec_() == QDialog.Accepted:
            return self.slider.value()
        else:
            return -1


class RandomRuleDialog(QDialog):
    reset = pyqtSignal()

    def __init__(self):
        super().__init__()

        self.grid = QGridLayout()
        self.setLayout(self.grid)
        self.setWindowTitle("New Rule")
        self.setWindowIcon(QIcon("Icons/PulsarIcon.png"))

        label_rulename = QLabel("Rule Name:")
        self.grid.addWidget(label_rulename, 0, 0)

        self.rulename = QLineEdit()
        self.grid.addWidget(self.rulename, 1, 0)

        above_widget = QWidget()
        above_grid = QGridLayout()

        above_widget.setLayout(above_grid)

        label_rulespace = QLabel("Rulespace:")
        above_grid.addWidget(label_rulespace, 0, 0)

        self.rulespaces = ["Outer Totalistic", "Extended Generations", "BSFKL"]
        self.combo_box_rulespace = QComboBox()  # Choose Rulespace
        self.combo_box_rulespace.addItems(self.rulespaces)
        self.combo_box_rulespace.currentTextChanged.connect(self.change_rulespace)
        above_grid.addWidget(self.combo_box_rulespace, 0, 1)

        self.isotropic_check_box = QCheckBox(text="Isotropic")  # Is the rule isotropic?
        above_grid.addWidget(self.isotropic_check_box, 1, 0)

        self.neighbourhood_table = Table(5, 5, "Neighbourhood Weights",  # Select Neighbourhood Weights
                                         [str(x) for x in range(-2, 3)], [str(x) for x in range(-2, 3)])
        above_grid.addWidget(self.neighbourhood_table, 1, 1)

        self.grid.addWidget(above_widget, 2, 0)

        self.n_states: int = 2
        self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                   [str(x) for x in range(self.n_states)], ["Weights"])
        self.grid.addWidget(self.state_weights, 3, 0)
        self.state_weights.hide()

        self.state_btns = QWidget()
        grid_state_btns = QGridLayout()
        self.state_btns.setLayout(grid_state_btns)

        self.add_state_btn = QPushButton("Add State")
        self.add_state_btn.clicked.connect(self.add_state)
        grid_state_btns.addWidget(self.add_state_btn, 0, 1)

        self.remove_state_btn = QPushButton("Remove State")
        self.remove_state_btn.clicked.connect(self.remove_state)
        grid_state_btns.addWidget(self.remove_state_btn, 0, 2)

        self.grid.addWidget(self.state_btns, 4, 0)
        self.state_btns.hide()

        label_rulestring = QLabel("Rulestring:")
        self.grid.addWidget(label_rulestring, 5, 0)

        self.rulestring = QLineEdit()
        self.grid.addWidget(self.rulestring, 6, 0)

        btn_write = QPushButton("Create Rule")
        btn_write.clicked.connect(self.write_to_rule)
        self.grid.addWidget(btn_write, 7, 0)

    def add_state(self):
        self.state_weights.setParent(None)
        self.state_weights.destroy()

        self.n_states += 1
        if self.n_states > 5:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"],
                                       button_height=30, button_width=40)
        else:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"])
        self.grid.addWidget(self.state_weights, 3, 0)

    def remove_state(self):
        self.state_weights.setParent(None)
        self.state_weights.destroy()

        self.n_states -= 1
        if self.n_states > 5:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"],
                                       button_height=30, button_width=40)
        else:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"])
        self.grid.addWidget(self.state_weights, 3, 0)

    def change_rulespace(self):
        if self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Extended Generations":
            self.state_weights.show()
            self.state_btns.show()  # Show State Weights
        else:
            self.state_weights.hide()  # Hide State Weights
            self.state_btns.hide()

    def write_to_rule(self):
        if self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Outer Totalistic":
            file = open("rule.ca_rule", "w")
            file.write(f"Name: {self.rulename.text()}\n\n")
            file.write("Neighbourhood Range: 2\n\n")
            file.write("Neighbourhood:\n")
            for i in range(len(self.neighbourhood_table.num)):
                string = ""
                for j in range(len(self.neighbourhood_table.num)):
                    if j != len(self.neighbourhood_table.num) - 1:
                        string += str(self.neighbourhood_table.num[i][j]) + ","
                    else:
                        string += str(self.neighbourhood_table.num[i][j])

                file.write(string + "\n")

            file.write("\nState Weights: 0,1\n\n")
            file.write("Rulespace: Outer Totalistic\n\n")
            file.write(f"Rulestring: {self.rulestring.text()}\n\n")
            file.write("Colour Palette:\nNone\n")
            file.close()

        elif self.rulespaces[self.combo_box_rulespace.currentIndex()] == "BSFKL":
            file = open("rule.ca_rule", "w")
            file.write(f"Name: {self.rulename.text()}\n\n")
            file.write("Neighbourhood Range: 2\n\n")
            file.write("Neighbourhood:\n")
            for i in range(len(self.neighbourhood_table.num)):
                string = ""
                for j in range(len(self.neighbourhood_table.num)):
                    if j != len(self.neighbourhood_table.num) - 1:
                        string += str(self.neighbourhood_table.num[i][j]) + ","
                    else:
                        string += str(self.neighbourhood_table.num[i][j])

                file.write(string + "\n")

            file.write("\nState Weights: 0,1,1\n\n")
            file.write("Rulespace: BSFKL\n\n")
            file.write(f"Rulestring: {self.rulestring.text()}\n\n")
            file.write("Colour Palette:\nNone\n")
            file.close()

            self.reset.emit()
            self.close()

        elif self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Extended Generations":
            file = open("rule.ca_rule", "w")
            file.write(f"Name: {self.rulename.text()}\n\n")
            file.write("Neighbourhood Range: 2\n\n")
            file.write("Neighbourhood:\n")
            for i in range(len(self.neighbourhood_table.num[0])):
                string = ""
                for j in range(len(self.neighbourhood_table.num)):
                    if j != len(self.neighbourhood_table.num) - 1:
                        string += str(self.neighbourhood_table.num[i][j]) + ","
                    else:
                        string += str(self.neighbourhood_table.num[i][j])

                file.write(string + "\n")

            state_weights = ""  # Appending State Weights to String
            for i in range(len(self.state_weights.num[0])):
                if i != len(self.state_weights.num[0]) - 1:
                    state_weights += str(self.state_weights.num[0][i]) + ","
                else:
                    state_weights += str(self.state_weights.num[0][i])

            file.write(f"\nState Weights: {state_weights}\n\n")
            file.write("Rulespace: Extended Generations\n\n")
            file.write(f"Rulestring: {self.rulestring.text()}\n\n")
            file.write("Colour Palette:\nNone\n")
            file.close()

        self.reset.emit()
        self.close()
