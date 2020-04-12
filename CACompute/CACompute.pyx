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
from libcpp.map cimport map
from libcpp.unordered_map cimport unordered_map
from libcpp.unordered_set cimport unordered_set
from transFunc import transition_func, depend_on_neighbours, alternating_period

cdef extern from "compute.cpp":
    pass

cdef unordered_map[pair[int, int], int] depends_cache
cdef map[pair[vector[int], int], int] transition_func_cache
cdef int alternating_period2 = alternating_period
cpdef compute(vector[pair[int, int]] neighbourhood,
              unordered_set[pair[int, int]] cells_changed,
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

        if copy_grid.find(coordinates) == copy_grid.end():
            if depends_cache.find(pair[int, int] (0, generations % alternating_period2)) == \
                    depends_cache.end():
                ans = depend_on_neighbours(0, generations % alternating_period2)
                depends_cache[pair[int, int] (0, generations % alternating_period2)] = ans
            else:
                ans = depends_cache[pair[int, int] (0, generations % alternating_period2)]
        else:
            if depends_cache.find(pair[int, int] (copy_grid[coordinates],
                                                  generations % alternating_period2)) == \
                    depends_cache.end():
                ans = depend_on_neighbours(copy_grid[coordinates], generations % alternating_period2)
                depends_cache[pair[int, int] (copy_grid[coordinates], generations % alternating_period2)] = ans
            else:
                ans = depends_cache[pair[int, int] (copy_grid[coordinates], generations % alternating_period2)]

        if ans == -1:
            for neighbour in neighbourhood:
                coordinates2 = pair[int, int] (coordinates.first + neighbour.first,
                                               coordinates.second + neighbour.second)
                if copy_grid.find(coordinates2) != copy_grid.end():
                    neighbours.push_back(copy_grid[coordinates2])
                else:
                    neighbours.push_back(0)

        if copy_grid.find(coordinates) != copy_grid.end():
            neighbours.push_back(copy_grid[coordinates])
            if transition_func_cache.find(
                    pair[vector[int], int] (neighbours, generations % alternating_period2)) == \
                    transition_func_cache.end():
                if ans == -1:
                    ans = transition_func(neighbours, generations % alternating_period2)
                    transition_func_cache[
                        pair[vector[int], int] (neighbours, generations % alternating_period2)] = ans
            else:
                if ans == -1: ans = transition_func_cache[
                    pair[vector[int], int] (neighbours, generations % alternating_period2)]

            if ans == 0:
                dict_grid.erase(coordinates)
                cells_changed.insert(coordinates)
            elif ans != copy_grid[coordinates]:
                dict_grid[coordinates] = ans
                cells_changed.insert(coordinates)
        else:
            neighbours.push_back(0)
            if transition_func_cache.find(
                    pair[vector[int], int] (neighbours, generations % alternating_period2)) == \
                    transition_func_cache.end():
                if ans == -1:
                    ans = transition_func(neighbours, generations % alternating_period2)
                    transition_func_cache[
                        pair[vector[int], int] (neighbours, generations % alternating_period2)] = ans
            else:
                if ans == -1: ans = transition_func_cache[
                    pair[vector[int], int] (neighbours, generations % alternating_period2)]

            if ans != 0:
                dict_grid.insert(pair[pair[int, int], int] (coordinates, ans))
                cells_changed.insert(coordinates)

    return cells_changed, dict_grid
