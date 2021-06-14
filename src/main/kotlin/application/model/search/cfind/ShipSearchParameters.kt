package application.model.search.cfind

import application.model.rules.Rule
import application.model.search.SearchParameters
import kotlin.math.pow

data class ShipSearchParameters(val rule: Rule, val width: Int, val dy: Int, val period: Int,
                                val symmetry: Symmetry = Symmetry.ASYMMETRIC,
                                val maxQueueSize: Long = 2.0.pow(20).toLong(),
                                val minDeepingIncrement: Int = period,
                                val lookahead: Boolean = true, val stdin: Boolean = false,
                                val dfs: Boolean = false): SearchParameters()