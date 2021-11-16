package application.model.search.csearch

import application.model.rules.Rule
import application.model.search.SearchParameters

class BruteForceSearchParameters(
    val rule: Rule, val maxPeriod: Int, val xBound: Int, val yBound: Int, val isRandom: Boolean,
    val symmetry: String, statesToInclude: List<Int>, val density: Int
) : SearchParameters() {
    val statesToInclude: IntArray

    init {
        require(!(!isRandom && symmetry != "C1")) { "Brute force search only supports C1 symmetry!" }
        this.statesToInclude = statesToInclude.toIntArray()
    }
}