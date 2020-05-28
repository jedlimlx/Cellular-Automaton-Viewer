def naive_gen(x, y, corner, direction, xy):
    if direction == b"o":
        if corner == 0:
            if xy == 0:
                for i in range(y):
                    for j in range(x):
                        yield i, j
            elif xy == 1:
                for i in range(x):
                    for j in range(y):
                        yield i, j
        elif corner == 1:
            if xy == 0:
                for i in range(y):
                    for j in range(x - 1, -1, -1):
                        yield i, j
            elif xy == 1:
                for i in range(x - 1, -1, -1):
                    for j in range(y):
                        yield i, j
        elif corner == 2:
            if xy == 0:
                for i in range(y - 1, -1, -1):
                    for j in range(x):
                        yield i, j
            elif xy == 1:
                for i in range(x):
                    for j in range(y - 1, -1, -1):
                        yield i, j
        elif corner == 3:
            if xy == 0:
                for i in range(y - 1, -1, -1):
                    for j in range(x - 1, -1, -1):
                        yield i, j
            elif xy == 1:
                for i in range(x - 1, -1, -1):
                    for j in range(y - 1, -1, -1):
                        yield i, j
    elif direction == b"d":
        if corner == 0:
            if xy == 0:
                for i in range(x * 2):
                    for j in range(i + 1):
                        if i - j <= x and j <= x:
                            yield i - j, j
            elif xy == 1:
                for i in range(x * 2):
                    for j in range(i + 1):
                        if i - j <= x and j <= x:
                            yield j, i - j
        elif corner == 1:
            if xy == 0:
                for i in range(x * 2):
                    for j in range(i + 1):
                        if j <= x and 0 <= j + x - i - 1 < x:
                            yield j, j + x - i - 1
            elif xy == 1:
                for i in range(x * 2):
                    for j in range(i - 1, -1, -1):
                        if j <= x and 0 <= j + x - i - 1 < x:
                            yield j, j + x - i - 1
        elif corner == 2:
            if xy == 0:
                for i in range(x * 2):
                    for j in range(i + 1):
                        if j <= x and 0 <= j + x - i - 1 < x:
                            yield j + x - i - 1, j
            elif xy == 1:
                for i in range(x * 2):
                    for j in range(i - 1, -1, -1):
                        if j <= x and 0 <= j + x - i - 1 < x:
                            yield j + x - i - 1, j
        elif corner == 3:
            if xy == 0:
                for i in range(x * 2 - 1, -1, -1):
                    for j in range(i + 1):
                        if i - j <= x and j <= x:
                            yield i - j, j
            elif xy == 1:
                for i in range(x * 2 - 1, -1, -1):
                    for j in range(i + 1):
                        if i - j <= x and j <= x:
                            yield j, i - j
