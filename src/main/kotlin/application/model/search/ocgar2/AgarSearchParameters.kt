package application.model.search.ocgar2

import application.model.search.ocgar2.AgarSearchParameters
import application.model.search.SearchProgram
import java.util.HashSet
import application.model.simulation.Simulator
import application.model.SymmetryGenerator
import application.model.patterns.Spaceship
import application.model.patterns.Oscillator
import application.model.rules.Rule
import application.model.search.SearchParameters

class AgarSearchParameters(val rule: Rule, val maxPeriod: Int) : SearchParameters()