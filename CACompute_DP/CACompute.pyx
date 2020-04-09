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
from libcpp.map cimport map
from libcpp.pair cimport pair
from libcpp.unordered_map cimport unordered_map
from libcpp.unordered_set cimport unordered_set
from libcpp.vector cimport vector

from transFunc import transition_func, depend_on_neighbours, alternating_period

cdef extern from "compute.cpp":
    pass

cdef map[vector[int], vector[int]] DP
cdef int alternating_period2 = alternating_period

cpdef compute(vector[pair[int, int]] neighbourhood, vector[pair[int, int]] DP_neighbourhood,
              unordered_set[pair[int, int]] cells_changed,
              int lower_x, int upper_x, int lower_y, int upper_y,
              unordered_map[pair[int, int], int] copy_grid, unordered_map[pair[int, int], int] dict_grid,
              int generations):

    cdef vector[int] neighbours
    cdef vector[int] DP_result
    cdef vector[int] DP_neighbours
    neighbours.reserve(neighbourhood.size() + 1)
    DP_result.reserve(neighbourhood.size() + 1)
    DP_neighbours.reserve(DP_neighbourhood.size() + 1)

    cdef vector[pair[int, int]] neighbourhood_with_centre = neighbourhood
    neighbourhood_with_centre.push_back(pair[int, int] (0, 0))

    cdef unordered_set[pair[int, int]] cells_to_check
    cdef unordered_set[pair[int, int]] cells_checked

    cdef int ans
    cdef pair[int, int] coordinates, coordinates2
    cdef pair[int, int] neighbour, cell

    for cell in cells_changed:
        cells_to_check.insert(cell)

    if alternating_period2 > 1:
        if generations % (alternating_period2 - 1) == 0:
            cells_changed.clear()
    else:
        cells_changed.clear()

    for cell in cells_to_check:
        if cells_checked.find(cell) != cells_checked.end():
            continue

        for k in DP_neighbourhood:
            coordinates2 = pair[int, int] (cell.first + k.first,
                                           cell.second + k.second)
            if copy_grid.find(coordinates2) != copy_grid.end():
                DP_neighbours.push_back(copy_grid[coordinates2])
            else:
                DP_neighbours.push_back(0)

        if DP.find(DP_neighbours) != DP.end():
            for i in range(DP[DP_neighbours].size()):
                ans = DP[DP_neighbours][i]
                coordinates = pair[int, int] (cell.first + neighbourhood_with_centre[i].first,
                                              cell.second + neighbourhood_with_centre[i].second)
                if cells_checked.find(coordinates) == cells_checked.end():
                    cells_checked.insert(coordinates)
                else:
                    continue

                if copy_grid.find(coordinates) != copy_grid.end():
                    if ans == 0 and dict_grid.find(coordinates) != dict_grid.end():
                        dict_grid.erase(coordinates)
                        cells_changed.insert(coordinates)
                    elif ans != copy_grid[coordinates]:
                        dict_grid[coordinates] = ans
                        cells_changed.insert(coordinates)
                else:
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
        else:
            DP_result.clear()
            for neighbour in neighbourhood_with_centre:
                neighbours.clear()

                ans = -1
                coordinates = pair[int, int] (cell.first + neighbour.first,
                                              cell.second + neighbour.second)

                if cells_checked.find(coordinates) == cells_checked.end():
                    cells_checked.insert(coordinates)

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
                        if dict_grid.find(coordinates) != dict_grid.end(): dict_grid.erase(coordinates)
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

                DP_result.push_back(ans)

            DP[DP_neighbours] = DP_result

    return lower_x, upper_x, lower_y, upper_y, cells_changed, dict_grid
