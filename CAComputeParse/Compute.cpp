#include "tuple"
#include "unordered_map"
#include "unordered_set"
#include "transFunc.cpp"

#define ii pair<int, int>

namespace std {
  template <>
  struct hash<pair<int, int> >
  {
    std::size_t operator()(const pair<int, int>& p) const
    {
      using std::size_t;
      using std::hash;

      return (hash<int>()(p.first) << 4) ^ (hash<int>()(p.second) >> 3);
    }
  };
}

using namespace std;
struct compute_return {
    int lower_x;
    int lower_y;
    int upper_x;
    int upper_y;
    unordered_set<ii> cells_changed;
    unordered_map<ii, int> dict_grid;
} return_val;

compute_return compute(vector<pair<int, int> > neighbourhood, bool first, unordered_set<ii> cells_changed,
           int lower_x, int upper_x, int lower_y, int upper_y, unordered_map<ii, int> copy_grid,
           unordered_map<ii, int> dict_grid) {

    vector<int> neighbours;
    neighbours.reserve(neighbourhood.size() + 1);

    int change = 1;
    unordered_set<ii> cells_to_check;

    for (const auto& cell: cells_changed) {
        for (int i = 0; i < neighbourhood.size(); i++) {
            cells_to_check.insert(make_pair(cell.first + neighbourhood.at(i).first,
                                            cell.second + neighbourhood.at(i).second));
        }
        cells_to_check.insert(cell);
    }

    pair<int, int> coordinates;
    if (first) {
        for (int i = lower_y - 1; i < upper_y + 1; i++) {
            for (int j = lower_x - 1; j < upper_x + 1; j++) {
                neighbours.clear();
                for (pair<int, int> neighbour : neighbourhood) {
                    coordinates = make_pair(i + neighbour.first, j + neighbour.second);
                    if (copy_grid.find(coordinates) != copy_grid.end()) {
                        neighbours.push_back(1);
                    }
                    else {
                        neighbours.push_back(0);
                    }
                }

                coordinates = make_pair(i, j);
                if (copy_grid.find(coordinates) != copy_grid.end()) {
                    neighbours.push_back(1);
                    if (transition_func(neighbours) == 0) {
                        dict_grid.erase(coordinates);
                        cells_changed.insert(coordinates);
                    }
                }
                else {
                    neighbours.push_back(0);
                    if (transition_func(neighbours) == 1) {
                        dict_grid.insert(make_pair(coordinates, 1));
                        cells_changed.insert(coordinates);
                        if (j < lower_x)
                            lower_x -= change;
                        else if (j > upper_x)
                            upper_x += change;

                        if (i < lower_y)
                            lower_y -= change;
                        else if (i > upper_y)
                            upper_y += change;
                    }
                }
            }
        }
    }
    else {
        for (pair<int, int> cell: cells_to_check) {
            neighbours.clear();
            for (pair<int, int> neighbour : neighbourhood) {
                coordinates = make_pair(cell.first + neighbour.first, cell.second + neighbour.second);
                if (copy_grid.find(coordinates) != copy_grid.end()) {
                    neighbours.push_back(1);
                }
                else {
                    neighbours.push_back(0);
                }
            }

            coordinates = cell;
            if (copy_grid.find(coordinates) != copy_grid.end()) {
                neighbours.push_back(1);
                if (transition_func(neighbours) == 0) {
                    dict_grid.erase(coordinates);
                    cells_changed.insert(coordinates);
                }
            }
            else {
                neighbours.push_back(0);
                if (transition_func(neighbours) == 1) {
                    dict_grid.insert(make_pair(coordinates, 1));
                    cells_changed.insert(coordinates);
                    if (cell.second < lower_x)
                        lower_x -= change;
                    else if (cell.second > upper_x)
                        upper_x += change;

                    if (cell.first < lower_y)
                        lower_y -= change;
                    else if (cell.first > upper_y)
                        upper_y += change;
                }
            }
        }
    }

    return_val.lower_x = lower_x;
    return_val.lower_y = lower_y;
    return_val.upper_x = upper_x;
    return_val.upper_y = upper_y;
    return_val.cells_changed = cells_changed;
    return_val.dict_grid = dict_grid;
    return return_val;

}
