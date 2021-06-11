package application.model.rules.hrot

import application.model.rules.hrot.HROTGenerations
import application.model.Coordinate
import application.model.simulation.Simulator
import kotlin.Throws
import java.io.IOException
import application.model.SymmetryGenerator
import application.model.rules.ruleloader.RuleLoader
import application.model.rules.ruleloader.RuleDirective
import application.model.simulation.Grid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.FileWriter
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern

class HROTGenerationsTest {
    fun getStream(resourcePath: String): InputStream {
        return javaClass.getResourceAsStream(resourcePath)!!
    }

    @Test
    fun testFromRulestring() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT Generations/parsingTest1.txt"))

        // Run through them
        var rulestring = ""
        var birth = HashSet<Int>()
        var survival = HashSet<Int>()
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                // Loading rulestring
                line.startsWith("#R") -> rulestring = line.substring(3)

                // Loading birth conditions
                line.startsWith("#B") -> {
                    var tokens = arrayOf<String>()
                    if (line.length > 2) {
                        val withoutHeader = line.substring(3)
                        tokens = withoutHeader.split(",").toTypedArray()
                    }

                    birth = HashSet()
                    tokens.forEach { birth.add(it.toInt()) }
                }

                // Loading survival conditions
                line.startsWith("#S") -> {
                    var tokens = arrayOf<String>()
                    if (line.length > 2) {
                        val withoutHeader = line.substring(3)
                        tokens = withoutHeader.split(",").toTypedArray()
                    }

                    survival = HashSet()
                    tokens.forEach { survival.add(it.toInt()) }
                }

                // Running the testcase
                !line.startsWith("#") -> {
                    val hrot = HROTGenerations(rulestring)
                    Assertions.assertEquals(birth, hrot.birth)
                    Assertions.assertEquals(survival, hrot.survival)
                }
            }
        }
    }

    @Test
    fun testCanonise() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT Generations/parsingTest1.txt"))

        // Run through them
        var rulestring = ""
        var canonisedRulestring = ""
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                // Loading rulestring
                line.startsWith("#R") -> rulestring = line.substring(3)

                // Loading canonised rulestring
                line.startsWith("#C") -> canonisedRulestring = line.substring(3)

                // Running the testcase
                !line.startsWith("#") -> {
                    val hrot = HROTGenerations(rulestring)
                    Assertions.assertEquals(canonisedRulestring, hrot.rulestring)
                }
            }
        }
    }

    @Test
    fun testGenerateComments() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT Generations/parsingTest2.txt"))
        var weights = IntArray(0)
        var stateWeights: IntArray? = IntArray(0)
        var neighbourhood = arrayOfNulls<Coordinate>(0)
        var comments = ArrayList<String>()

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> comments.add(line)
                line.startsWith("#W") ->
                    weights = line.substring(3).split(",").map { it.toInt() }.toIntArray()
                line.startsWith("#S") -> {
                    stateWeights = if (line.substring(3) == "null") {
                        null
                    } else {
                        line.substring(3).split(",").map { it.toInt() }.toIntArray()
                    }
                }
                line.startsWith("#N") -> {
                    val regex = Regex("(-?[0-9]+),\\s?(-?[0-9]+)")
                    neighbourhood = regex.findAll(line).map {
                        Coordinate(it.groupValues[1].toInt(), it.groupValues[2].toInt())
                    }.toList().toTypedArray()
                }
                else -> {
                    val hrot = HROTGenerations("R2,C5,S2-3,B3,N@")
                    hrot.setWeights(weights)
                    hrot.stateWeights = stateWeights
                    hrot.setNeighbourhood(neighbourhood)

                    val generated = hrot.generateComments()
                    val trimmed = generated.map { comment ->
                        comment.trim { it <= ' ' }
                    }.toTypedArray()

                    Assertions.assertArrayEquals(comments.toTypedArray(), trimmed)
                    comments = ArrayList() // Resetting for the next testcase
                }
            }
        }
    }

    @Test
    fun testLoadComments() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT Generations/parsingTest2.txt"))
        var weights = IntArray(0)
        var stateWeights: IntArray? = IntArray(0)
        var neighbourhood = arrayOf<Coordinate>()
        var comments = ArrayList<String>()

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> comments.add(line)
                line.startsWith("#W") ->
                    weights = line.substring(3).split(",").map { it.toInt() }.toIntArray()
                line.startsWith("#S") -> {
                    stateWeights = if (line.substring(3) == "null") {
                        null
                    } else {
                        line.substring(3).split(",").map { it.toInt() }.toIntArray()
                    }
                }
                line.startsWith("#N") -> {
                    val regex = Regex("(-?[0-9]+),\\s?(-?[0-9]+)")
                    neighbourhood = regex.findAll(line).map {
                        Coordinate(it.groupValues[1].toInt(), it.groupValues[2].toInt())
                    }.toList().toTypedArray()
                }
                else -> {
                    val hrot = HROTGenerations("R2,C5,S2-3,B3,N@")
                    hrot.loadComments(comments.toTypedArray())

                    Assertions.assertArrayEquals(weights, hrot.getWeights())
                    Assertions.assertArrayEquals(stateWeights, hrot.stateWeights)
                    Assertions.assertArrayEquals(neighbourhood, hrot.getNeighbourhood())
                    comments = ArrayList() // Resetting for the next testcase
                }
            }
        }
    }

    @Test
    fun testRuleRange() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT Generations/ruleRangeTest.txt"))
        var rule = HROTGenerations()
        var minRule: HROTGenerations? = null
        var maxRule: HROTGenerations? = null
        var inputPattern: Simulator? = null
        var targetPattern: String? = null

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> rule = HROTGenerations(line.substring(3))
                line.startsWith("#I") -> {
                    inputPattern = Simulator(rule)
                    inputPattern.fromRLE(line.substring(3), Coordinate(0, 0))
                }
                line.startsWith("#MIN") -> minRule = HROTGenerations(line.substring(5))
                line.startsWith("#MAX") -> maxRule = HROTGenerations(line.substring(5))
                line.startsWith("#T") -> targetPattern = line.substring(3)
                else -> {
                    val pattern = inputPattern!!.identify()
                    Assertions.assertEquals(targetPattern, pattern.toString())
                    Assertions.assertEquals(minRule!!.rulestring, pattern.minRule.rulestring)
                    Assertions.assertEquals(maxRule!!.rulestring, pattern.maxRule.rulestring)
                }
            }
        }
    }

    @Test
    fun testClone() {
        val hrot = HROTGenerations("R3,C3,S6-10,B3,N+")
        val hrotClone = hrot.clone() as HROTGenerations
        hrot.rulestring = "R2,C3,S2-3,B3,5,NW0010003330130310333000100,031"

        // Ensure they are different
        Assertions.assertNotEquals(hrotClone.birth, hrot.birth)
        Assertions.assertNotEquals(hrotClone.survival, hrot.survival)
        Assertions.assertNotEquals(hrotClone.getWeights(), hrot.getWeights())
        Assertions.assertNotEquals(hrotClone.stateWeights, hrot.stateWeights)
        Assertions.assertNotEquals(hrotClone.getNeighbourhood(), hrot.getNeighbourhood())
    }

    @Test
    @Throws(IOException::class)
    fun testGenerateApgtable() {
        val rules = arrayOf(
            "23/3/3", "R1,C4,S2-3,B3,N@891891", "R1,C4,S,B0,4,NN",
            "R2,C3,S9,B0-3,NN", "R2,C3,S6-11,B9-11,NW0010003330130310333000100"
        )

        for (rule in rules) {
            val hrotRule = HROTGenerations(rule)
            val simulator = Simulator(hrotRule)
            simulator.insertCells(
                SymmetryGenerator.generateC1(50, intArrayOf(1), 16, 16),
                Coordinate()
            )

            val ruleLoader = RuleLoader()
            for (ruleDirective in hrotRule.generateApgtable()) ruleLoader.addRuleDirective(ruleDirective)

            val file = FileWriter("rules/Temp.rule")
            file.write(ruleLoader.export())
            file.close()

            val simulator2 = Simulator(RuleLoader("Temp"))
            simulator2.insertCells(simulator, Coordinate())

            for (i in 0 until 5 * hrotRule.alternatingPeriod) {
                simulator.step()
                simulator2.step()
            }

            Assertions.assertEquals(
                simulator.toRLE().replace("o", "A").replace("b", "."),
                simulator2.toRLE().replace("o", "A").replace("b", ".")
            )
        }
    }

    @Test
    fun testSimulation() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT Generations/simulationTest.txt"))
        var generations = 0
        var hrot = HROTGenerations()
        var inputPattern: Simulator? = null
        var targetPattern: Grid? = null

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> hrot = HROTGenerations(line.substring(3))
                line.startsWith("#G") -> generations = line.substring(3).toInt()
                line.startsWith("#I") -> {
                    inputPattern = Simulator(hrot)
                    inputPattern.fromRLE(line.substring(3), Coordinate())
                }
                line.startsWith("#O") -> {
                    targetPattern = Grid()
                    targetPattern.fromRLE(line.substring(3), Coordinate())
                }
                else -> {
                    // Run N generations
                    for (i in 0 until generations) inputPattern!!.step()
                    Assertions.assertEquals(targetPattern!!.toRLE(), inputPattern!!.toRLE())
                }
            }
        }
    }
}