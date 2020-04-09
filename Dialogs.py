from typing import Tuple, List, Any

from PyQt5.Qt import QIcon
from PyQt5.QtCore import Qt
from PyQt5.QtWidgets import QLabel, QGridLayout, QSlider, QDialog, QDialogButtonBox, QComboBox, QCheckBox


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
    def __init__(self, speed: int, use_DP: bool):
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

        # Checkbox for using DP
        self.checkbox = QCheckBox()
        self.checkbox.setText("Use DP")
        self.checkbox.setChecked(use_DP)
        grid.addWidget(self.checkbox)

        # Okay and Cancel Button
        btns = QDialogButtonBox.Ok | QDialogButtonBox.Cancel

        button_box = QDialogButtonBox(btns)
        button_box.accepted.connect(self.accept)
        button_box.rejected.connect(self.reject)

        grid.addWidget(button_box)

    def change_label(self):
        self.label.setText(f"Max Speed (gen/s): {self.slider.value()}")

    def get_results(self) -> Tuple[int, Any]:
        if self.exec_() == QDialog.Accepted:
            return self.slider.value(), self.checkbox.isChecked()
        else:
            return -1, None
