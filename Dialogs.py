import re
import random
import traceback
import copy
import json
from functools import partial
from typing import List, Dict

from PyQt5.Qt import QIcon, pyqtSignal
from PyQt5.QtCore import Qt
from PyQt5.QtWidgets import QLabel, QGridLayout, QSlider, QDialog, QDialogButtonBox, \
    QComboBox, QCheckBox, QWidget, QPushButton, QLineEdit, QTabWidget, QMessageBox


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

        self.btns: List[QPushButton] = []  # List to Store Buttons
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

                self.btns.append(btn)

        grid.addWidget(table_btns)

    def func(self, i, j):
        current_text: str = self.sender().text()
        if current_text == "0":
            new_text = "?"
        elif current_text == "?":
            new_text = "1"
        else:
            new_text = str((int(current_text) + 11) % 21 - 10)
        self.num[i][j] = new_text
        self.sender().setText(new_text)

    def load_values(self, num):
        self.num = num[:]
        for i in range(len(self.num)):
            for j in range(len(self.num[i])):
                self.btns[i * len(self.num) + j].setText(self.num[i][j])


class SoupSettings(QDialog):
    def __init__(self, density: int, symmetry: str, num_states: int, checkbox_states: List[bool]):
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

        # Checkboxes to Select included States
        checkbox_widget = QWidget()
        grid_checkbox = QGridLayout()
        checkbox_widget.setLayout(grid_checkbox)

        try:
            self.checkbox_states: List[QCheckBox] = []
            for i in range(num_states):
                checkbox = QCheckBox(text=str(i))
                checkbox.setChecked(checkbox_states[i])
                grid_checkbox.addWidget(checkbox, 0, i)

                self.checkbox_states.append(checkbox)

            grid.addWidget(checkbox_widget)

        except Exception as e:
            print(traceback.format_exc())

        # Okay and Cancel Button
        btns = QDialogButtonBox.Ok | QDialogButtonBox.Cancel

        button_box = QDialogButtonBox(btns)
        button_box.accepted.connect(self.accept)
        button_box.rejected.connect(self.reject)

        grid.addWidget(button_box)

    def change_label(self):
        self.density_label.setText(f"Density: {self.slider.value()}%")

    def get_results(self):
        if self.exec_() == QDialog.Accepted:
            return self.slider.value(), self.symmetries[self.combo.currentIndex()], \
                   [x.isChecked() for x in self.checkbox_states]
        else:
            return -1, "#", []


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


class IdentityDialog(QDialog):
    def __init__(self):
        super().__init__()

        grid = QGridLayout()
        self.setLayout(grid)
        self.setWindowTitle("Identity")
        self.setWindowIcon(QIcon("Icons/PulsarIcon.png"))

        # Symmetry Label
        symmetry_label = QLabel("Select Pattern Type: ")
        grid.addWidget(symmetry_label)

        # Combo Box for Selecting Pattern Types
        self.pattern_types: List[str] = ["Still Life / Oscillator / Spaceship",
                                         "Gun", "Replicator", "Could be anything"]

        self.combo = QComboBox()
        self.combo.addItems(self.pattern_types)
        grid.addWidget(self.combo)

        # Okay and Cancel Button
        btns = QDialogButtonBox.Ok | QDialogButtonBox.Cancel

        button_box = QDialogButtonBox(btns)
        button_box.accepted.connect(self.accept)
        button_box.rejected.connect(self.reject)

        grid.addWidget(button_box)

    def get_results(self):
        if self.exec_() == QDialog.Accepted:
            return self.pattern_types[self.combo.currentIndex()]
        else:
            return ""


class RandomRuleDialog(QDialog):
    reset = pyqtSignal()

    def __init__(self):
        super().__init__()

        layout = QGridLayout()
        self.setLayout(layout)
        self.setWindowTitle("New Rule")
        self.setWindowIcon(QIcon("Icons/PulsarIcon.png"))

        tabs = QTabWidget()
        layout.addWidget(tabs)

        main_tab = QWidget()  # Tab for Rule Settings
        random_tab = QWidget()  # Tab for Random Settings

        tabs.addTab(main_tab, "Rule Settings")
        tabs.addTab(random_tab, "Random Settings")

        self.grid = QGridLayout()
        main_tab.setLayout(self.grid)

        label_rulename = QLabel("Rule Name:")
        self.grid.addWidget(label_rulename, 0, 0)

        # Get Rule Name from Settings.json
        try: rule_name = json.load(open("settings.json", "r"))["Rule Name"]
        except KeyError: rule_name = ""

        self.rulename = QLineEdit()
        self.rulename.setText(rule_name)
        self.grid.addWidget(self.rulename, 1, 0)

        above_widget = QWidget()
        above_grid = QGridLayout()

        above_widget.setLayout(above_grid)

        label_rulespace = QLabel("Rulespace:")
        above_grid.addWidget(label_rulespace, 0, 0)

        try: rulespace = json.load(open("settings.json", "r"))["Rule Space"]  # Get Previous Selected Rulespace
        except KeyError: rulespace = None

        self.rulespaces = ["BSFKL", "Single State", "Extended Generations", "Regenerating Generations"]
        self.combo_box_rulespace = QComboBox()  # Choose Rulespace
        self.combo_box_rulespace.addItems(self.rulespaces)
        self.combo_box_rulespace.currentTextChanged.connect(self.change_rulespace)
        above_grid.addWidget(self.combo_box_rulespace, 0, 1)

        label_bsconditions = QLabel("B/S Condition:")
        above_grid.addWidget(label_bsconditions, 1, 0)

        try: bscondition = json.load(open("settings.json", "r"))["B/S Conditions"]  # Get B/S Conditions
        except KeyError: bscondition = None

        self.bsconditions = ["Outer Totalistic", "Double Totalistic", "Range 1 Moore Semi Totalistic"]
        self.combo_box_bsconditions = QComboBox()  # Choose B/S Conditions
        self.combo_box_bsconditions.addItems(self.bsconditions)
        self.combo_box_bsconditions.currentTextChanged.connect(self.change_bsconditions)
        above_grid.addWidget(self.combo_box_bsconditions, 1, 1)

        self.isotropic_check_box = QCheckBox(text="Isotropic")  # Is the Rule Isotropic?

        # Get Rule Name from Settings.json
        try: isotropic = json.load(open("settings.json", "r"))["Isotropic"]
        except KeyError: isotropic = True

        self.isotropic_check_box.setChecked(isotropic)
        above_grid.addWidget(self.isotropic_check_box, 2, 0)

        self.neighbourhood_table = Table(5, 5, "Neighbourhood Weights",  # Select Neighbourhood Weights
                                         [str(x) for x in range(-2, 3)], [str(x) for x in range(-2, 3)])
        try: weights = json.load(open("settings.json", "r"))["Neighbourhood Weights"]  # Get Previous Selected Weights
        except KeyError: weights = None
        if weights is not None: self.neighbourhood_table.load_values(weights)

        above_grid.addWidget(self.neighbourhood_table, 2, 1)

        self.grid.addWidget(above_widget, 2, 0)

        try: weights = json.load(open("settings.json", "r"))["State Weights"]  # Get Previous Selected Weights
        except KeyError: weights = None

        self.n_states: int = 2
        if weights is not None: self.n_states = len(weights[0])

        if self.n_states <= 5:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"])
        else:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"],
                                       button_width=40, button_height=30)
        if weights is not None: self.state_weights.load_values(weights)

        self.grid.addWidget(self.state_weights, 3, 0)
        if rulespace != "Extended Generations" and rulespace != "Regenerating Generations":
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
        if rulespace != "Extended Generations" and rulespace != "Regenerating Generations":
            self.state_btns.hide()

        label_rulestring = QLabel("Rulestring:")
        self.grid.addWidget(label_rulestring, 5, 0)

        # Get Rule Name from Settings.json
        try: rulestring = json.load(open("settings.json", "r"))["Rule String"]
        except KeyError: rulestring = ""

        self.rulestring = QLineEdit()
        self.rulestring.setText(rulestring)
        self.grid.addWidget(self.rulestring, 6, 0)

        btn_write = QPushButton("Create Rule")
        btn_write.clicked.connect(self.write_to_rule)
        self.grid.addWidget(btn_write, 7, 0)

        self.random_range_line_edits: Dict[str, QLineEdit] = {}
        label_text = ["Rulestring Upper Bound", "Rulestring Lower Bound",
                      "State Weights Upper Bound", "State Weights Lower Bound",
                      "Neighbourhood Weights Upper Bound", "Neighbourhood Weights Lower Bound"]

        random_grid = QGridLayout()
        random_tab.setLayout(random_grid)

        # Get Rule Name from Settings.json
        try: text = json.load(open("settings.json", "r"))["Random Bounds"]
        except KeyError: text = None

        for index, val in enumerate(label_text):  # Adding Random Bounds Entries
            label = QLabel(val + ":")
            random_grid.addWidget(label, index, 0)

            self.random_range_line_edits[val] = QLineEdit()
            if text is not None: self.random_range_line_edits[val].setText(text[val])  # Reload Previous Values
            random_grid.addWidget(self.random_range_line_edits[val], index, 1)

        # Load Prev Rulespace
        if rulespace is not None:
            self.combo_box_rulespace.setCurrentIndex(self.rulespaces.index(rulespace))

        # Load Prev B/S Condition
        if bscondition is not None:
            self.combo_box_bsconditions.setCurrentIndex(self.bsconditions.index(bscondition))

    def add_state(self):
        num = self.state_weights.num[:]  # Make a Deepcopy
        num[0].append("0")

        self.state_weights.setParent(None)
        self.state_weights.destroy()

        self.n_states += 1
        if self.n_states > 5:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"],
                                       button_height=30, button_width=40)
            self.state_weights.load_values(num)  # Reload Values
        else:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"])
            self.state_weights.load_values(num)
        self.grid.addWidget(self.state_weights, 3, 0)

    def remove_state(self):
        num = self.state_weights.num[:]  # Make a Deepcopy
        num[0].pop()

        self.state_weights.setParent(None)
        self.state_weights.destroy()

        self.n_states -= 1
        if self.n_states > 5:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"],
                                       button_height=30, button_width=40)  # Reload Values
            self.state_weights.load_values(num)
        else:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"])
            self.state_weights.load_values(num)
        self.grid.addWidget(self.state_weights, 3, 0)

    def change_rulespace(self):
        if self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Extended Generations" or \
                self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Regenerating Generations":
            self.state_weights.show()
            self.state_btns.show()  # Show State Weights
        else:
            self.state_weights.hide()  # Hide State Weights
            self.state_btns.hide()

    def change_bsconditions(self):
        if self.bsconditions[self.combo_box_bsconditions.currentIndex()] == "Range 1 Moore Semi Totalistic":
            self.neighbourhood_table.hide()
            self.isotropic_check_box.hide()
        else:
            self.neighbourhood_table.show()
            self.isotropic_check_box.show()

    def write_to_rule(self):
        prev_rule = open("rule.ca_rule", "r").read()
        settings = json.load(open("settings.json", "r"))

        file = open("rule.ca_rule", "w")  # Write to Rule File
        file.write(f"Name: {self.rulename.text()}\n\n")
        file.write("Neighbourhood Range: 2\n\n")
        file.write("Neighbourhood:\n")

        if self.bsconditions[self.combo_box_bsconditions.currentIndex()] == "Range 1 Moore Semi Totalistic":
            self.neighbourhood_table.num = [["0", "0", "0", "0", "0"],
                                            ["0", "1", "1", "1", "0"],
                                            ["0", "1", "0", "1", "0"],
                                            ["0", "1", "1", "1", "0"],
                                            ["0", "0", "0", "0", "0"]]

        try:
            neighbourhood = copy.deepcopy(self.neighbourhood_table.num)
            for i in range(len(self.neighbourhood_table.num)):
                string = ""
                for j in range(len(self.neighbourhood_table.num[i])):
                    if j != len(self.neighbourhood_table.num) - 1:
                        if neighbourhood[i][j] == "?":  # Should RNG be used?
                            random_weight = str(random.randint(
                                int(self.random_range_line_edits["Neighbourhood Weights Lower Bound"].text()),
                                int(self.random_range_line_edits["Neighbourhood Weights Upper Bound"].text())))
                            neighbourhood[i][j] = random_weight
                            if self.isotropic_check_box.isChecked():
                                neighbourhood[i][-j - 1] = random_weight
                                neighbourhood[-i - 1][j] = random_weight
                                neighbourhood[-i - 1][-j - 1] = random_weight
                            string += str(neighbourhood[i][j]) + ","

                        else:
                            string += str(neighbourhood[i][j]) + ","
                    else:
                        if neighbourhood[i][j] == "?":  # Should RNG be used?
                            random_weight = str(random.randint(
                                int(self.random_range_line_edits["Neighbourhood Weights Lower Bound"].text()),
                                int(self.random_range_line_edits["Neighbourhood Weights Upper Bound"].text())))
                            neighbourhood[i][j] = random_weight
                            if self.isotropic_check_box.isChecked():
                                neighbourhood[i][-j - 1] = random_weight
                                neighbourhood[-i - 1][j] = random_weight
                                neighbourhood[-i - 1][-j - 1] = random_weight
                            string += str(neighbourhood[i][j])
                        else:
                            string += str(neighbourhood[i][j])

                file.write(string + "\n")
        except ValueError as e:  # Error Handling, Inform User of Error
            if "empty range for randrange()" in str(e):
                QMessageBox.warning(self, "Random State Weights Error",
                                    "The lower bound is larger than the upper bound!",
                                    QMessageBox.Ok, QMessageBox.Ok)

                file.close()

                open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                self.reset.emit()
                self.close()
                return -1
            else:
                QMessageBox.warning(self, "Random State Weights Error",
                                    "The bounds are not valid integers!",
                                    QMessageBox.Ok, QMessageBox.Ok)
                file.close()

                open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                self.reset.emit()
                self.close()
                return -1

        if self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Extended Generations" or \
            self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Regenerating Generations":
            state_weights = ""  # Appending State Weights to String
            try:
                settings["State Weights"] = self.state_weights.num
                for i in range(len(self.state_weights.num[0])):
                    if i != len(self.state_weights.num[0]) - 1:
                        if self.state_weights.num[0][i] == "?":  # Should RNG be used?
                            state_weights += str(random.randint(
                                int(self.random_range_line_edits["State Weights Lower Bound"].text()),
                                int(self.random_range_line_edits["State Weights Upper Bound"].text()))) + ","
                        else:
                            state_weights += str(self.state_weights.num[0][i]) + ","
                    else:
                        if self.state_weights.num[0][i] == "?":  # Should RNG be used?
                            state_weights += str(random.randint(
                                int(self.random_range_line_edits["State Weights Lower Bound"].text()),
                                int(self.random_range_line_edits["State Weights Upper Bound"].text())))
                        else:
                            state_weights += str(self.state_weights.num[0][i])

            except ValueError as e:  # Error Handling, Inform User of Error
                if "empty range for randrange()" in str(e):
                    QMessageBox.warning(self, "Random State Weights Error",
                                        "The lower bound is larger than the upper bound!",
                                        QMessageBox.Ok, QMessageBox.Ok)

                    file.close()

                    open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                    self.reset.emit()
                    self.close()
                    return -1
                else:
                    QMessageBox.warning(self, "Random State Weights Error",
                                        "The bounds are not valid integers!",
                                        QMessageBox.Ok, QMessageBox.Ok)
                    file.close()

                    open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                    self.reset.emit()
                    self.close()
                    return -1

            file.write(f"\nState Weights: {state_weights}\n\n")

        elif self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Single State":
            file.write("\nState Weights: 0,1\n\n")

        elif self.rulespaces[self.combo_box_rulespace.currentIndex()] == "BSFKL":
            file.write("\nState Weights: 0,1,1\n\n")

        file.write(f"Rulespace: {self.rulespaces[self.combo_box_rulespace.currentIndex()]}\n\n")

        # Adding Random States to Rulestring
        lst = [x for x in self.rulestring.text()]
        rulestring = ""

        try:
            for i in range(len(lst)):
                if lst[i] == "?":
                    rulestring += str(random.randint(
                        int(self.random_range_line_edits["Rulestring Lower Bound"].text()),
                        int(self.random_range_line_edits["Rulestring Upper Bound"].text())))
                else:
                    rulestring += lst[i]

        except ValueError as e:  # Error Handling, Inform User of Error
            if "empty range for randrange()" in str(e):
                QMessageBox.warning(self, "Random State Weights Error",
                                    "The lower bound is larger than the upper bound!",
                                    QMessageBox.Ok, QMessageBox.Ok)

                file.close()

                open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                self.reset.emit()
                self.close()
                return -1
            else:
                QMessageBox.warning(self, "Random State Weights Error",
                                    "The bounds are not valid integers!",
                                    QMessageBox.Ok, QMessageBox.Ok)
                file.close()

                open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                self.reset.emit()
                self.close()
                return -1

        file.write(f"B/S Conditions: {self.bsconditions[self.combo_box_bsconditions.currentIndex()]}\n\n")
        file.write(f"Rulestring: {rulestring}\n\n")
        file.write("Colour Palette:\nNone\n")
        file.close()

        settings["Isotropic"] = self.isotropic_check_box.isChecked()
        settings["Rule Space"] = self.rulespaces[self.combo_box_rulespace.currentIndex()]
        settings["B/S Conditions"] = self.bsconditions[self.combo_box_bsconditions.currentIndex()]
        settings["Rule String"] = self.rulestring.text()
        settings["Neighbourhood Weights"] = self.neighbourhood_table.num
        settings["Rule Name"] = self.rulename.text()

        text: Dict[str, str] = {}
        for key in self.random_range_line_edits:
            text[key] = self.random_range_line_edits[key].text()

        settings["Random Bounds"] = text

        json.dump(settings, open("settings.json", "w"), indent=4)
        self.reset.emit()
        self.close()


class ParamMapDialog(QDialog):
    run_param = pyqtSignal()

    def __init__(self):
        super().__init__()

        layout = QGridLayout()
        self.setLayout(layout)
        self.setWindowTitle("New Rule")
        self.setWindowIcon(QIcon("Icons/PulsarIcon.png"))

        tabs = QTabWidget()
        layout.addWidget(tabs)

        main_tab = QWidget()  # Tab for Rule Settings
        random_tab = QWidget()  # Tab for Random Settings

        tabs.addTab(main_tab, "Rule Settings")
        tabs.addTab(random_tab, "Random Settings")

        self.grid = QGridLayout()
        main_tab.setLayout(self.grid)

        label_rulename = QLabel("Rule Name:")
        self.grid.addWidget(label_rulename, 0, 0)

        # Get Rule Name from Settings.json
        try: rule_name = json.load(open("settings.json", "r"))["Rule Name PMap"]
        except KeyError: rule_name = ""

        self.rulename = QLineEdit()
        self.rulename.setText(rule_name)
        self.grid.addWidget(self.rulename, 1, 0)

        above_widget = QWidget()
        above_grid = QGridLayout()

        above_widget.setLayout(above_grid)

        label_rulespace = QLabel("Rulespace:")
        above_grid.addWidget(label_rulespace, 0, 0)

        try: rulespace = json.load(open("settings.json", "r"))["Rule Space PMap"]  # Get Previous Selected Rulespace
        except KeyError: rulespace = None

        self.rulespaces = ["BSFKL", "Single State", "Extended Generations", "Regenerating Generations"]
        self.combo_box_rulespace = QComboBox()  # Choose Rulespace
        self.combo_box_rulespace.addItems(self.rulespaces)
        self.combo_box_rulespace.currentTextChanged.connect(self.change_rulespace)
        above_grid.addWidget(self.combo_box_rulespace, 0, 1)

        label_bsconditions = QLabel("B/S Condition:")
        above_grid.addWidget(label_bsconditions, 1, 0)

        try: bscondition = json.load(open("settings.json", "r"))["B/S Conditions PMap"]  # Get B/S Conditions
        except KeyError: bscondition = None

        self.bsconditions = ["Outer Totalistic", "Double Totalistic"]
        self.combo_box_bsconditions = QComboBox()  # Choose B/S Conditions
        self.combo_box_bsconditions.addItems(self.bsconditions)
        above_grid.addWidget(self.combo_box_bsconditions, 1, 1)

        # Load Prev Rulespace
        if bscondition is not None: self.combo_box_bsconditions.setCurrentIndex(self.bsconditions.index(bscondition))

        self.isotropic_check_box = QCheckBox(text="Isotropic")  # Is the Rule Isotropic?

        # Get Rule Name from Settings.json
        try: isotropic = json.load(open("settings.json", "r"))["Isotropic PMap"]
        except KeyError: isotropic = True

        self.isotropic_check_box.setChecked(isotropic)
        above_grid.addWidget(self.isotropic_check_box, 2, 0)

        self.neighbourhood_table = Table(5, 5, "Neighbourhood Weights",  # Select Neighbourhood Weights
                                         [str(x) for x in range(-2, 3)], [str(x) for x in range(-2, 3)])

        # Get Previous Selected Weights
        try: weights = json.load(open("settings.json", "r"))["Neighbourhood Weights PMap"]
        except KeyError: weights = None
        if weights is not None: self.neighbourhood_table.load_values(weights)

        above_grid.addWidget(self.neighbourhood_table, 2, 1)

        self.grid.addWidget(above_widget, 2, 0)

        try: weights = json.load(open("settings.json", "r"))["State Weights PMap"]  # Get Previous Selected Weights
        except KeyError: weights = None

        self.n_states: int = 2
        if weights is not None: self.n_states = len(weights[0])

        if self.n_states <= 5:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"])
        else:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"],
                                       button_width=40, button_height=30)
        if weights is not None: self.state_weights.load_values(weights)

        self.grid.addWidget(self.state_weights, 3, 0)
        if rulespace != "Extended Generations" and rulespace != "Regenerating Generations":
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
        if rulespace != "Extended Generations" and rulespace != "Regenerating Generations":
            self.state_btns.hide()

        label_rulestring = QLabel("Rulestring:")
        self.grid.addWidget(label_rulestring, 5, 0)

        # Get Rule Name from Settings.json
        try: rulestring = json.load(open("settings.json", "r"))["Rule String PMap"]
        except KeyError: rulestring = ""

        self.rulestring = QLineEdit()
        self.rulestring.setText(rulestring)
        self.grid.addWidget(self.rulestring, 6, 0)

        btn_write = QPushButton("Generate Parameter Map")
        btn_write.clicked.connect(self.run_param_map)
        self.grid.addWidget(btn_write, 7, 0)

        self.random_range_line_edits: Dict[str, QLineEdit] = {}
        label_text = ["Rulestring Upper Bound", "Rulestring Lower Bound",
                      "State Weights Upper Bound", "State Weights Lower Bound",
                      "Neighbourhood Weights Upper Bound", "Neighbourhood Weights Lower Bound"]

        random_grid = QGridLayout()
        random_tab.setLayout(random_grid)

        # Get Rule Name from Settings.json
        try: text = json.load(open("settings.json", "r"))["Random Bounds PMap"]
        except KeyError: text = None

        for index, val in enumerate(label_text):  # Adding Random Bounds Entries
            label = QLabel(val + ":")
            random_grid.addWidget(label, index, 0)

            self.random_range_line_edits[val] = QLineEdit()
            if text is not None: self.random_range_line_edits[val].setText(text[val])  # Reload Previous Values
            random_grid.addWidget(self.random_range_line_edits[val], index, 1)

            # Load Prev Rulespace
            if rulespace is not None: self.combo_box_rulespace.setCurrentIndex(self.rulespaces.index(rulespace))

    def add_state(self):
        num = self.state_weights.num[:]  # Make a Deepcopy
        num[0].append("0")

        self.state_weights.setParent(None)
        self.state_weights.destroy()

        self.n_states += 1
        if self.n_states > 5:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"],
                                       button_height=30, button_width=40)
            self.state_weights.load_values(num)  # Reload Values
        else:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"])
            self.state_weights.load_values(num)
        self.grid.addWidget(self.state_weights, 3, 0)

    def remove_state(self):
        num = self.state_weights.num[:]  # Make a Deepcopy
        num[0].pop()

        self.state_weights.setParent(None)
        self.state_weights.destroy()

        self.n_states -= 1
        if self.n_states > 5:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"],
                                       button_height=30, button_width=40)  # Reload Values
            self.state_weights.load_values(num)
        else:
            self.state_weights = Table(self.n_states, 1, "State Weights",  # Select State Weights
                                       [str(x) for x in range(self.n_states)], ["Weights"])
            self.state_weights.load_values(num)
        self.grid.addWidget(self.state_weights, 3, 0)

    def change_rulespace(self):
        if self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Extended Generations" or \
                self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Regenerating Generations":
            self.state_weights.show()
            self.state_btns.show()  # Show State Weights
        else:
            self.state_weights.hide()  # Hide State Weights
            self.state_btns.hide()

    def run_param_map(self):
        try:
            for i in range(5):
                for j in range(5):
                    self.write_to_rule(i, j)

            settings = json.load(open("settings.json", "r"))  # Writing new settings to settings.json
            settings["Isotropic PMap"] = self.isotropic_check_box.isChecked()
            settings["Rule Space PMap"] = self.rulespaces[self.combo_box_rulespace.currentIndex()]
            settings["B/S Conditions PMap"] = self.bsconditions[self.combo_box_bsconditions.currentIndex()]
            settings["Rule String PMap"] = self.rulestring.text()
            settings["Neighbourhood Weights PMap"] = self.neighbourhood_table.num
            settings["State Weights PMap"] = self.state_weights.num
            settings["Rule Name PMap"] = self.rulename.text()

            text: Dict[str, str] = {}
            for key in self.random_range_line_edits:
                text[key] = self.random_range_line_edits[key].text()

            settings["Random Bounds PMap"] = text

            json.dump(settings, open("settings.json", "w"), indent=4)
            self.run_param.emit()

        except Exception:
            print(traceback.format_exc())

    def write_to_rule(self, x: int, y: int):
        prev_rule = open("rule.ca_rule", "r").read()

        file = open(f"PMap/rule_{x}_{y}.ca_rule", "w+")  # Write to Rule File
        file.write(f"Name: {self.rulename.text() + '_' + str(x) + '_' + str(y)}\n\n")
        file.write("Neighbourhood Range: 2\n\n")
        file.write("Neighbourhood:\n")
        try:
            neighbourhood = copy.deepcopy(self.neighbourhood_table.num)
            for i in range(len(self.neighbourhood_table.num)):
                string = ""
                for j in range(len(self.neighbourhood_table.num[i])):
                    if j != len(self.neighbourhood_table.num) - 1:
                        if neighbourhood[i][j] == "?":  # Should RNG be used?
                            random_weight = str(random.randint(
                                int(self.random_range_line_edits["Neighbourhood Weights Lower Bound"].text()),
                                int(self.random_range_line_edits["Neighbourhood Weights Upper Bound"].text())))
                            neighbourhood[i][j] = random_weight
                            if self.isotropic_check_box.isChecked():
                                neighbourhood[i][-j - 1] = random_weight
                                neighbourhood[-i - 1][j] = random_weight
                                neighbourhood[-i - 1][-j - 1] = random_weight
                            string += str(neighbourhood[i][j]) + ","

                        else:
                            string += str(neighbourhood[i][j]) + ","
                    else:
                        if neighbourhood[i][j] == "?":  # Should RNG be used?
                            random_weight = str(random.randint(
                                int(self.random_range_line_edits["Neighbourhood Weights Lower Bound"].text()),
                                int(self.random_range_line_edits["Neighbourhood Weights Upper Bound"].text())))
                            neighbourhood[i][j] = random_weight
                            if self.isotropic_check_box.isChecked():
                                neighbourhood[i][-j - 1] = random_weight
                                neighbourhood[-i - 1][j] = random_weight
                                neighbourhood[-i - 1][-j - 1] = random_weight
                            string += str(neighbourhood[i][j])
                        else:
                            string += str(neighbourhood[i][j])

                file.write(string + "\n")

        except ValueError as e:  # Error Handling, Inform User of Error
            if "empty range for randrange()" in str(e):
                QMessageBox.warning(self, "Random State Weights Error",
                                    "The lower bound is larger than the upper bound!",
                                    QMessageBox.Ok, QMessageBox.Ok)

                file.close()

                open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                self.reset.emit()
                self.close()
                return -1
            else:
                QMessageBox.warning(self, "Random State Weights Error",
                                    "The bounds are not valid integers!",
                                    QMessageBox.Ok, QMessageBox.Ok)
                file.close()

                open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                self.reset.emit()
                self.close()
                return -1

        if self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Extended Generations" or \
            self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Regenerating Generations":
            state_weights = ""  # Appending State Weights to String
            try:
                for i in range(len(self.state_weights.num[0])):
                    if i != len(self.state_weights.num[0]) - 1:
                        if self.state_weights.num[0][i] == "?":  # Should RNG be used?
                            state_weights += str(random.randint(
                                int(self.random_range_line_edits["State Weights Lower Bound"].text()),
                                int(self.random_range_line_edits["State Weights Upper Bound"].text()))) + ","
                        else:
                            state_weights += str(self.state_weights.num[0][i]) + ","
                    else:
                        if self.state_weights.num[0][i] == "?":  # Should RNG be used?
                            state_weights += str(random.randint(
                                int(self.random_range_line_edits["State Weights Lower Bound"].text()),
                                int(self.random_range_line_edits["State Weights Upper Bound"].text())))
                        else:
                            state_weights += str(self.state_weights.num[0][i])

            except ValueError as e:  # Error Handling, Inform User of Error
                if "empty range for randrange()" in str(e):
                    QMessageBox.warning(self, "Random State Weights Error",
                                        "The lower bound is larger than the upper bound!",
                                        QMessageBox.Ok, QMessageBox.Ok)

                    file.close()

                    open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                    self.reset.emit()
                    self.close()
                    return -1
                else:
                    QMessageBox.warning(self, "Random State Weights Error",
                                        "The bounds are not valid integers!",
                                        QMessageBox.Ok, QMessageBox.Ok)
                    file.close()

                    open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                    self.reset.emit()
                    self.close()
                    return -1

            file.write(f"\nState Weights: {state_weights}\n\n")

        elif self.rulespaces[self.combo_box_rulespace.currentIndex()] == "Single State":
            file.write("\nState Weights: 0,1\n\n")

        elif self.rulespaces[self.combo_box_rulespace.currentIndex()] == "BSFKL":
            file.write("\nState Weights: 0,1,1\n\n")

        file.write(f"Rulespace: {self.rulespaces[self.combo_box_rulespace.currentIndex()]}\n\n")

        # Adding Random States to Rulestring
        lst = [x for x in self.rulestring.text()]
        rulestring = ""

        try:
            for i in range(len(lst)):
                if lst[i] == "?":
                    rulestring += str(random.randint(
                        int(self.random_range_line_edits["Rulestring Lower Bound"].text()),
                        int(self.random_range_line_edits["Rulestring Upper Bound"].text())))
                else:
                    rulestring += lst[i]
        except ValueError as e:  # Error Handling, Inform User of Error
            if "empty range for randrange()" in str(e):
                QMessageBox.warning(self, "Random State Weights Error",
                                    "The lower bound is larger than the upper bound!",
                                    QMessageBox.Ok, QMessageBox.Ok)

                file.close()

                open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                self.reset.emit()
                self.close()
                return -1
            else:
                QMessageBox.warning(self, "Random State Weights Error",
                                    "The bounds are not valid integers!",
                                    QMessageBox.Ok, QMessageBox.Ok)
                file.close()

                open("rule.ca_rule", "w").write(prev_rule)  # Place Previous Rule Back in the File
                self.reset.emit()
                self.close()
                return -1

        # Handle Expressions in Rulestring
        expressions = re.findall("\"(.*?)\"", rulestring)
        for index, expression in enumerate(expressions):
            num = eval(expression.replace("x", str(x)).replace("y", str(y)))  # New to Replace Expression
            rulestring = re.sub("(\".*?\")", str(num), rulestring, 1)

        file.write(f"B/S Conditions: {self.bsconditions[self.combo_box_bsconditions.currentIndex()]}\n\n")
        file.write(f"Rulestring: {rulestring}\n\n")
        file.write("Colour Palette:\nNone\n")
        file.close()


class GeneascopyDialog(QDialog):
    def __init__(self):
        super().__init__()

        grid = QGridLayout()
        self.setLayout(grid)
        self.setWindowTitle("Geneascopy")
        self.setWindowIcon(QIcon("Icons/PulsarIcon.png"))

        # Soup Size Label
        self.num_soup_label = QLabel(f"Soup Num: 20")
        grid.addWidget(self.num_soup_label)

        # Slider for adjusting density
        self.num_soup_slider = QSlider(Qt.Horizontal)
        self.num_soup_slider.setMaximum(200)
        self.num_soup_slider.setMinimum(0)
        self.num_soup_slider.setValue(20)
        self.num_soup_slider.setTickInterval(5)
        self.num_soup_slider.valueChanged.connect(self.change_num_label)
        grid.addWidget(self.num_soup_slider)

        # Soup Size Label
        self.generations_label = QLabel(f"Generations: 750")
        grid.addWidget(self.generations_label)

        # Slider for adjusting density
        self.generations_slider = QSlider(Qt.Horizontal)
        self.generations_slider.setMaximum(10000)
        self.generations_slider.setMinimum(0)
        self.generations_slider.setValue(750)
        self.generations_slider.setTickInterval(5)
        self.generations_slider.valueChanged.connect(self.change_generations_label)
        grid.addWidget(self.generations_slider)

        # Okay and Cancel Button
        btns = QDialogButtonBox.Ok | QDialogButtonBox.Cancel

        button_box = QDialogButtonBox(btns)
        button_box.accepted.connect(self.accept)
        button_box.rejected.connect(self.reject)

        grid.addWidget(button_box)

    def change_num_label(self):
        self.num_soup_label.setText(f"Soup Num: {self.num_soup_slider.value()}")

    def change_generations_label(self):
        self.generations_label.setText(f"Generations: {self.generations_slider.value()}")

    def get_results(self):
        if self.exec_() == QDialog.Accepted:
            return self.generations_slider.value(), self.num_soup_slider.value()
        else:
            return None, None
