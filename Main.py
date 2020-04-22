import logging
import sys

from PyQt5.Qt import QIcon, QAction
from PyQt5.QtWidgets import QApplication, QWidget, QGridLayout, QMenuBar

import CACanvas as cacanvas
from CACanvas import CACanvas
from Dialogs import SoupSettings, SettingZoom, SimulationSettings, RandomRuleDialog

logging.basicConfig(filename='log.log', level=logging.INFO)
logging.log(logging.INFO, "=" * 10 + "APPLICATION STARTING" + "=" * 10)


def change_zoom(new_cell_size: int, restore_pattern: bool = True, overwrite=None) -> None:
    global canvas, cell_size

    # Disconnect Actions
    open_pattern_action.triggered.disconnect(canvas.open_pattern)
    save_pattern_action.triggered.disconnect(canvas.save_pattern)
    open_rule_action.triggered.disconnect(canvas.load_new_rule)
    copy_action.triggered.disconnect(canvas.copy_selection)
    cut_action.triggered.disconnect(canvas.cut_selection)
    delete_action.triggered.disconnect(canvas.delete_selection)
    paste_action.triggered.disconnect(canvas.paste_clipboard)
    start_simulation_action.triggered.disconnect(canvas.toggle_simulation)
    forward_one_action.triggered.disconnect(canvas.update_cells)
    simulation_settings_action.triggered.disconnect(simulation_settings)
    random_soup_settings_action.triggered.disconnect(random_soup_settings)

    # Get Soup Settings
    density: float = canvas.density
    symmetry: str = canvas.symmetry
    max_speed: int = canvas.max_speed

    if restore_pattern:
        # Destroy Canvas
        x_scroll = canvas.scroll_area.horizontalScrollBar().value()
        y_scroll = canvas.scroll_area.verticalScrollBar().value()
        generations = canvas.generations
        dictionary = canvas.dict_grid
        cells_changed = canvas.cells_changed

        grid.removeWidget(canvas)
        canvas.setParent(None)
        canvas.destroy()
        del canvas

        # Make a new one
        canvas = CACanvas(new_cell_size)

        # Setting Values to what they were previously
        canvas.scroll_area.horizontalScrollBar().setValue(x_scroll)
        canvas.scroll_area.verticalScrollBar().setValue(y_scroll)
        canvas.generations = generations
        canvas.cells_changed = cells_changed
        canvas.density = density
        canvas.symmetry = symmetry
        canvas.max_speed = max_speed

        canvas.zoom_in.connect(zoom_in)
        canvas.zoom_out.connect(zoom_out)
        canvas.reset.connect(lambda: change_zoom(cell_size, restore_pattern=False))
        canvas.reset_and_load.connect(lambda grid: change_zoom(cell_size, overwrite=grid))
        canvas.change_title.connect(set_title)

        if overwrite is None:
            canvas.load_from_dict(dictionary)
        else:
            canvas.load_from_dict(overwrite, offset_x=100, offset_y=100)
    else:
        grid.removeWidget(canvas)
        canvas.setParent(None)
        canvas.destroy()
        del canvas

        # Make a new one
        canvas = CACanvas(new_cell_size)

        canvas.density = density
        canvas.symmetry = symmetry
        canvas.max_speed = max_speed

        canvas.zoom_in.connect(zoom_in)
        canvas.zoom_out.connect(zoom_out)
        canvas.reset.connect(lambda: change_zoom(cell_size, restore_pattern=False))
        canvas.reset_and_load.connect(lambda grid: change_zoom(cell_size, overwrite=grid))
        canvas.change_title.connect(set_title)
        if overwrite is not None: canvas.load_from_dict(overwrite, offset_x=100, offset_y=100)

    # Reconnect Canvas Actions to Menu Bar
    open_pattern_action.triggered.connect(canvas.open_pattern)
    save_pattern_action.triggered.connect(canvas.save_pattern)
    open_rule_action.triggered.connect(canvas.load_new_rule)
    copy_action.triggered.connect(canvas.copy_selection)
    cut_action.triggered.connect(canvas.cut_selection)
    delete_action.triggered.connect(canvas.delete_selection)
    paste_action.triggered.connect(canvas.paste_clipboard)
    start_simulation_action.triggered.connect(canvas.toggle_simulation)
    forward_one_action.triggered.connect(canvas.update_cells)
    simulation_settings_action.triggered.connect(simulation_settings)
    random_soup_settings_action.triggered.connect(random_soup_settings)

    grid.addWidget(canvas)


def zoom_in() -> None:
    global cell_size
    cell_size += 1
    change_zoom(cell_size)


def zoom_out() -> None:
    global cell_size
    cell_size -= 1
    change_zoom(cell_size)


def random_soup_settings() -> None:
    settings = SoupSettings(int(canvas.density * 100), canvas.symmetry,
                            cacanvas.num_states, [False] * cacanvas.num_states)

    new_density, symmetry, checkbox_states = settings.get_results()

    include_states = []
    for index, val in enumerate(checkbox_states):
        if val: include_states.append(index)

    if new_density != -1: canvas.density = new_density / 100  # Map from % to float
    if symmetry != "#": canvas.symmetry = symmetry
    if len(include_states) != 0: canvas.include_states = include_states[:]


def simulation_settings() -> None:
    settings = SimulationSettings(canvas.max_speed)
    max_speed = settings.get_results()

    if max_speed != -1: canvas.max_speed = max_speed


def set_zoom() -> None:
    global cell_size
    settings = SettingZoom(cell_size)

    result: int = settings.get_results()
    cell_size = result  # Change Cell Size to New Zoom

    if result != -1: change_zoom(result)  # Changing Zoom to Result


def new_random_rule() -> None:
    rule_dialog = RandomRuleDialog()
    rule_dialog.reset.connect(reload_rule)
    rule_dialog.exec_()


def reload_rule() -> None:
    canvas.reload_rule()
    change_zoom(cell_size, restore_pattern=False)


def set_title(title: str) -> None:
    window.setWindowTitle(title)


app = QApplication(sys.argv)
app.setStyle("fusion")

# Main Grid for Widgets
grid = QGridLayout()
grid.setContentsMargins(0, 0, 0, 0)
grid.setSpacing(1)

# Main Window
window = QWidget()
window.setLayout(grid)
window.setWindowTitle("Cellular Automaton Viewer")
window.setWindowIcon(QIcon("Icons/PulsarIcon.png"))

# Canvas to Simulate the CA
cell_size = 5

canvas = CACanvas(cell_size)
canvas.zoom_in.connect(zoom_in)
canvas.zoom_out.connect(zoom_out)
canvas.change_title.connect(set_title)
canvas.reset.connect(lambda: change_zoom(cell_size, restore_pattern=False))
canvas.reset_and_load.connect(lambda grid: change_zoom(cell_size, overwrite=grid))
grid.addWidget(canvas, 1, 0)

# Menu Bar
menu = QMenuBar()
file_menu = menu.addMenu("File")
new_pattern_action = QAction("New Pattern")
new_pattern_action.setShortcut("Ctrl+N")
new_pattern_action.triggered.connect(lambda: change_zoom(cell_size, restore_pattern=False))
file_menu.addAction(new_pattern_action)

open_pattern_action = QAction("Open Pattern")
open_pattern_action.setShortcut("Ctrl+O")
open_pattern_action.triggered.connect(canvas.open_pattern)
file_menu.addAction(open_pattern_action)

save_pattern_action = QAction("Save Pattern")
save_pattern_action.setShortcut("Ctrl+S")
save_pattern_action.triggered.connect(canvas.save_pattern)
file_menu.addAction(save_pattern_action)

file_menu.addSeparator()

new_rule_action = QAction("New Rule")
new_rule_action.triggered.connect(new_random_rule)
file_menu.addAction(new_rule_action)

open_rule_action = QAction("Open Rule")
open_rule_action.triggered.connect(canvas.load_new_rule)
file_menu.addAction(open_rule_action)

edit_menu = menu.addMenu("Edit")

copy_action = QAction("Copy Selected")
copy_action.setShortcut("Ctrl+C")
copy_action.triggered.connect(canvas.copy_selection)
edit_menu.addAction(copy_action)

cut_action = QAction("Cut Selected")
cut_action.setShortcut("Ctrl+X")
cut_action.triggered.connect(canvas.cut_selection)
edit_menu.addAction(cut_action)

delete_action = QAction("Delete Selected")
delete_action.setShortcut("Del")
delete_action.triggered.connect(canvas.delete_selection)
edit_menu.addAction(delete_action)

edit_menu.addSeparator()

paste_action = QAction("Paste Clipboard")
paste_action.setShortcut("Ctrl+V")
paste_action.triggered.connect(canvas.paste_clipboard)
edit_menu.addAction(paste_action)

control_menu = menu.addMenu("Control")
start_simulation_action = QAction("Start Simulation")
start_simulation_action.setShortcut("Return")
start_simulation_action.triggered.connect(canvas.toggle_simulation)
control_menu.addAction(start_simulation_action)

forward_one_action = QAction("Step Forward 1 Generation")
forward_one_action.setShortcut("Space")
forward_one_action.triggered.connect(canvas.update_cells)
control_menu.addAction(forward_one_action)

simulation_settings_action = QAction("Simulation Settings")
simulation_settings_action.triggered.connect(simulation_settings)
control_menu.addAction(simulation_settings_action)

control_menu.addSeparator()

random_soup_settings_action = QAction("Random Soup Settings")
random_soup_settings_action.triggered.connect(random_soup_settings)
control_menu.addAction(random_soup_settings_action)

view_menu = menu.addMenu("View")

zoom_in_action = QAction("Zoom In")
zoom_in_action.triggered.connect(zoom_in)
view_menu.addAction(zoom_in_action)

zoom_out_action = QAction("Zoom Out")
zoom_out_action.triggered.connect(zoom_out)
view_menu.addAction(zoom_out_action)

view_menu.addSeparator()

set_zoom_action = QAction("Set Zoom")
set_zoom_action.triggered.connect(set_zoom)
view_menu.addAction(set_zoom_action)

grid.addWidget(menu, 0, 0)

window.show()
sys.exit(app.exec_())
