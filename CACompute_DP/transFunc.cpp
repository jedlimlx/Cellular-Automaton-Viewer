#include "vector"
using namespace std;
int transition_func(vector<int> vec) {
    int sum = 0;
    for (int i = 0; i < vec.size() - 1; i++) {
        sum += vec.at(i);
    }

    if (vec.at(vec.size() - 1) == 0) {
        if (sum == 3) {
            return 1;
        }
    }
    else {
        if (sum == 3 || sum == 2) {
            return 1;
        }
    }
    return 0;
};
