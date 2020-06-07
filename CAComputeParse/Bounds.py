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
        if coordinate[0] == y_bound - 1 or coordinate[0] == 0:
            return y_bound - coordinate[0] % y_bound, coordinate[1] % x_bound
        return coordinate[0] % y_bound, coordinate[1] % x_bound
    elif bound_type == b"C":  # Cross Surface
        if (coordinate[0] == y_bound or coordinate[0] == -1) and \
                (coordinate[1] == x_bound or coordinate[1] == -1):
            return y_bound - coordinate[0] % y_bound, x_bound - coordinate[1] % x_bound
        elif coordinate[0] == y_bound or coordinate[0] == -1:
            return y_bound - coordinate[0] % y_bound, coordinate[1] % x_bound
        elif coordinate[1] == x_bound or coordinate[1] == -1:
            return coordinate
        return coordinate[0] % y_bound, coordinate[1] % x_bound
    elif bound_type == b"P":  # Bounded Grid
        return 1000 if coordinate[0] > y_bound else coordinate[0], 1000 if coordinate[1] > x_bound else coordinate[1]
