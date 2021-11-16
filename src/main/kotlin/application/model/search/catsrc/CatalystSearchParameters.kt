package application.model.search.catsrc

import application.model.Coordinate
import application.model.search.catsrc.CatalystSearchParameters
import application.model.search.SearchProgram
import application.model.patterns.Catalyst
import application.model.rules.Rule
import application.model.simulation.Simulator
import java.util.HashSet
import application.model.search.catsrc.PlacedCatalyst
import java.io.FileWriter
import java.io.IOException
import java.util.logging.LogManager
import application.model.search.SearchParameters
import application.model.simulation.Grid

class CatalystSearchParameters(
    val maxRepeatTime: Int, val numCatalysts: Int, val bruteForce: Boolean,
    val rotateCatalyst: Boolean, val flipCatalyst: Boolean, val catalysts: List<Grid>, val target: Grid,
    val coordinateList: List<Coordinate>, val rule: Rule
) : SearchParameters()