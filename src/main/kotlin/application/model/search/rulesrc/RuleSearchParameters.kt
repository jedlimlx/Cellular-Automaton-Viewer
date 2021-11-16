package application.model.search.rulesrc

import application.model.search.rulesrc.RuleSearchParameters
import application.model.search.SearchProgram
import java.util.HashSet
import kotlin.Throws
import java.lang.IllegalArgumentException
import application.model.simulation.Simulator
import application.model.rules.MinMaxRuleable
import application.model.Coordinate
import application.model.patterns.Spaceship
import java.io.FileWriter
import application.model.rules.RuleFamily
import application.model.database.GliderDBEntry
import application.model.patterns.Oscillator
import java.io.IOException
import java.util.logging.LogManager
import application.model.search.SearchParameters
import application.model.simulation.Grid

class RuleSearchParameters(// Accessors
    val targetPattern: Grid, val minRule: RuleFamily, val maxRule: RuleFamily, val maxPeriod: Int,
    val minPop: Int, val maxPop: Int, val maxX: Int, val maxY: Int
) : SearchParameters()