# distutils: language=c++
"""
cdef extern from "Compute.cpp":
    cdef struct compute_return:
        int lower_x, lower_y, upper_x, upper_y
        unordered_map[pair[int, int], int] dict_grid
        unordered_set[pair[int, int]] cells_changed

    cdef compute_return compute(vector[pair[int, int]] neighbourhood, bool first,
                                unordered_set[pair[int, int]] cells_changed, int lower_x, int upper_x, int lower_y,
                                int upper_y, unordered_map[pair[int, int], int] copy_grid,
                                unordered_map[pair[int, int], int] dict_grid)


cpdef pycompute(vector[pair[int, int]] neighbourhood, bool first,
              unordered_set[pair[int, int]] cells_changed, int lower_x, int upper_x, int lower_y,
              int upper_y, unordered_map[pair[int, int], int] copy_grid,
              unordered_map[pair[int, int], int] dict_grid):

    cdef compute_return ans = compute(neighbourhood, first, cells_changed, lower_x, upper_x, lower_y, upper_y,
                                      copy_grid, dict_grid)

    return ans.lower_x, ans.lower_y, ans.upper_x, ans.upper_y, ans.cells_changed, ans.dict_grid


"""

from libcpp.vector cimport vector
from libcpp.pair cimport pair
from libcpp.unordered_map cimport unordered_map
from libcpp.unordered_set cimport unordered_set
from transFunc import transition_func, depend_on_neighbours, alternating_period

cdef extern from "compute.cpp":
    pass

cdef int alternating_period2 = alternating_period
cpdef compute(vector[pair[int, int]] neighbourhood,
              unordered_set[pair[int, int]] cells_changed,
              int lower_x, int upper_x, int lower_y, int upper_y,
              unordered_map[pair[int, int], int] copy_grid, unordered_map[pair[int, int], int] dict_grid,
              int generations):

    cdef vector[int] neighbours
    neighbours.reserve(neighbourhood.size() + 1)

    cdef unordered_set[pair[int, int]] cells_to_check

    cdef int i, j
    cdef int ans
    cdef pair[int, int] coordinates, coordinates2
    cdef pair[int, int] neighbour

    for coor in cells_changed:
        for neighbour in neighbourhood:
            coordinates.first = coor.first + neighbour.first
            coordinates.second = coor.second + neighbour.second
            cells_to_check.insert(coordinates)

        cells_to_check.insert(coor)

    if alternating_period2 > 1:
        if generations % (alternating_period2 - 1) == 0:
            cells_changed.clear()
    else:
        cells_changed.clear()

    for coordinates in cells_to_check:
        neighbours.clear()
        ans = -1

        if copy_grid.find(coordinates) == copy_grid.end(): ans = depend_on_neighbours(0, generations)
        else: ans = depend_on_neighbours(copy_grid[coordinates], generations)

        if ans == -1:
            for k in neighbourhood:
                coordinates2 = pair[int, int] (coordinates.first + k.first,
                                               coordinates.second + k.second)
                if copy_grid.find(coordinates2) != copy_grid.end():
                    neighbours.push_back(copy_grid[coordinates2])
                else:
                    neighbours.push_back(0)

        if copy_grid.find(coordinates) != copy_grid.end():
            neighbours.push_back(copy_grid[coordinates])
            if ans == -1: ans = transition_func(neighbours, generations)
            if ans == 0:
                dict_grid.erase(coordinates)
                cells_changed.insert(coordinates)
            elif ans != copy_grid[coordinates]:
                dict_grid[coordinates] = ans
                cells_changed.insert(coordinates)
        else:
            neighbours.push_back(0)
            if ans == -1: ans = transition_func(neighbours, generations)
            if ans != 0:
                dict_grid.insert(pair[pair[int, int], int] (coordinates, ans))
                cells_changed.insert(coordinates)
                if coordinates.second < lower_x:
                    lower_x = coordinates.second
                elif coordinates.second > upper_x:
                    upper_x = coordinates.second

                if coordinates.first < lower_y:
                    lower_y = coordinates.first
                elif coordinates.first > upper_y:
                    upper_y = coordinates.first


    return lower_x, upper_x, lower_y, upper_y, cells_changed, dict_grid
