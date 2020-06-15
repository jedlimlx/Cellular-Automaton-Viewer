def correct_coor(coordinate, bound_type, x_bound, y_bound):
    if bound_type == b"#":
        return coordinate
    elif bound_type == b"T":  # Toroidal
        if y_bound == 0:
            return coordinate[0], coordinate[1] % x_bound
        elif x_bound == 0:
            return coordinate[0] % y_bound, coordinate[1]
        else:
            return coordinate[0] % y_bound, coordinate[1] % x_bound
    elif bound_type == b"K":  # Klein Bottle
        on_edge_x = coordinate[1] == x_bound or coordinate[1] == -1
        return y_bound - coordinate[0] % y_bound if on_edge_x else coordinate[0] % y_bound, coordinate[1] % x_bound
    elif bound_type == b"C":  # Cross Surface
        on_edge_y = coordinate[0] == y_bound or coordinate[0] == -1
        on_edge_x = coordinate[1] == x_bound or coordinate[1] == -1
        return y_bound - coordinate[0] % y_bound if on_edge_x else coordinate[0] % y_bound, \
               x_bound - coordinate[1] % x_bound if on_edge_y else coordinate[1] % x_bound
    elif bound_type == b"S":  # Spherical by Bubblegum
        on_edge_y = coordinate[0] == y_bound or coordinate[0] == -1
        on_edge_x = coordinate[1] == x_bound or coordinate[1] == -1

        remapped_x = x_bound * int(not 0 <= coordinate[0] < x_bound) + \
                     (int(0 <= coordinate[0] < x_bound) - int(not 0 <= coordinate[0] < x_bound)) * (
                             coordinate[0] % x_bound)
        remapped_y = y_bound * int(not 0 <= coordinate[1] < y_bound) + \
                     (int(0 <= coordinate[1] < y_bound) - int(not 0 <= coordinate[1] < y_bound)) * (
                             coordinate[1] % y_bound)

        return remapped_x if on_edge_x else coordinate[0], remapped_y if on_edge_y else coordinate[1]

    elif bound_type == b"P":  # Bounded Grid
        return 1000 if coordinate[0] > y_bound else coordinate[0], 1000 if coordinate[1] > x_bound else coordinate[1]
