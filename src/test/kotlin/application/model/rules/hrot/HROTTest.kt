package application.model.rules.hrot

import application.model.Coordinate
import application.model.SymmetryGenerator
import application.model.rules.ruleloader.RuleLoader
import application.model.simulation.Grid
import application.model.simulation.Simulator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

class HROTTest {
    private fun getStream(resourcePath: String): InputStream {
        return javaClass.getResourceAsStream(resourcePath)!!
    }

    @Test
    fun testFromRulestring() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT/parsingTest1.txt"))

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
                    var tokens = arrayOfNulls<String>(0)
                    if (line.length > 2) {
                        val withoutHeader = line.substring(3)
                        tokens = withoutHeader.split(",").toTypedArray()
                    }

                    birth = HashSet()
                    tokens.forEach { birth.add(it!!.toInt()) }
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
                    val hrot = HROT(rulestring)
                    Assertions.assertEquals(birth, hrot.getBirth())
                    Assertions.assertEquals(survival, hrot.getSurvival())
                }
            }
        }
    }

    @Test
    fun testCanonise() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT/parsingTest1.txt"))

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
                    val hrot = HROT(rulestring)
                    Assertions.assertEquals(canonisedRulestring, hrot.rulestring)
                }
            }
        }
    }

    @Test
    fun testGenerateComments() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT/parsingTest2.txt"))
        var weights = IntArray(0)
        var neighbourhood = arrayOf<Coordinate>()
        val comments = ArrayList<String>()

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> comments.add(line)
                line.startsWith("#W") ->
                    weights = line.substring(3).split(",").map { it.toInt() }.toIntArray()
                line.startsWith("#N") -> {
                    val regex = Regex("(-?[0-9]+),\\s?(-?[0-9]+)")
                    neighbourhood = regex.findAll(line).map {
                        Coordinate(it.groupValues[1].toInt(), it.groupValues[2].toInt())
                    }.toList().toTypedArray()
                }
                else -> {
                    val hrot = HROT("R2,C2,S2-3,B3,N@")
                    hrot.setWeights(weights)
                    hrot.setNeighbourhood(neighbourhood)
                    Assertions.assertArrayEquals(comments.toTypedArray(), hrot.generateComments())
                }
            }
        }
    }

    @Test
    fun testLoadComments() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT/parsingTest2.txt"))
        var weights = IntArray(0)
        var neighbourhood = arrayOfNulls<Coordinate>(0)
        val comments = ArrayList<String>()

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> comments.add(line)
                line.startsWith("#W") ->
                    weights = line.substring(3).split(",").map { it.toInt() }.toIntArray()
                line.startsWith("#N") -> {
                    val regex = Regex("(-?[0-9]+),\\s?(-?[0-9]+)")
                    neighbourhood = regex.findAll(line).map {
                        Coordinate(it.groupValues[1].toInt(), it.groupValues[2].toInt())
                    }.toList().toTypedArray()
                }
                else -> {
                    val hrot = HROT("R2,C2,S2-3,B3,N@")
                    hrot.loadComments(comments.toTypedArray())
                    Assertions.assertArrayEquals(weights, hrot.getWeights())
                    Assertions.assertArrayEquals(neighbourhood, hrot.getNeighbourhood())
                }
            }
        }
    }

    @Test
    fun testRuleRange() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT/ruleRangeTest.txt"))
        var rule = HROT()
        var minRule: HROT? = null
        var maxRule: HROT? = null
        var inputPattern: Simulator? = null
        var targetPattern: String? = null

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> rule = HROT(line.substring(3))
                line.startsWith("#I") -> {
                    inputPattern = Simulator(rule)
                    inputPattern.fromRLE(line.substring(3), Coordinate(0, 0))
                }
                line.startsWith("#MIN") -> minRule = HROT(line.substring(5))
                line.startsWith("#MAX") -> maxRule = HROT(line.substring(5))
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
        val hrot = HROT("B2/S")
        val hrotClone = hrot.clone() as HROT
        hrot.rulestring = "R2,C2,S2-3,B3,N@891891"

        // Ensure they are different
        Assertions.assertNotEquals(hrotClone.getBirth(), hrot.getBirth())
        Assertions.assertNotEquals(hrotClone.getSurvival(), hrot.getSurvival())
        Assertions.assertNotEquals(hrotClone.getNeighbourhood(), hrot.getNeighbourhood())
    }

    @Test
    @Throws(IOException::class)
    fun testGenerateApgtable() {
        val rules = arrayOf(
            "B3/S23", "R1,C2,S2-3,B3,N@891891", "R2,C2,S9,B0-3,NN",
            "R2,C2,S5-9,B7-8,NM", "R2,C2,S6-11,B9-11,NW0010003330130310333000100"
        )

        for (rule in rules) {
            val hrotRule = HROT(rule)
            val simulator = Simulator(hrotRule)
            simulator.insertCells(
                SymmetryGenerator.generateC1(50, intArrayOf(1), 16, 16),
                Coordinate()
            )

            val ruleLoader = RuleLoader()
            for (ruleDirective in hrotRule.generateApgtable()) ruleLoader.addRuleDirective(ruleDirective)

            File("rules/Temp.rule").writeText(ruleLoader.export())

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
        val scanner = Scanner(getStream("/HROT/simulationTest.txt"))
        var generations = 0
        var hrot = HROT()
        var inputPattern: Simulator? = null
        var targetPattern: Grid

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> hrot = HROT(line.substring(3))
                line.startsWith("#G") -> generations = line.substring(3).toInt()
                line.startsWith("#I") -> {
                    inputPattern = Simulator(hrot)
                    inputPattern.fromRLE(line.substring(3), Coordinate())
                }
                line.startsWith("#O") -> {
                    targetPattern = Grid()
                    targetPattern.fromRLE(line.substring(3), Coordinate())

                    // Run N generations
                    for (i in 0 until generations) inputPattern!!.step()

                    Assertions.assertEquals(
                        targetPattern.toRLE(),
                        inputPattern!!.toRLE().replace("o", "A").replace("b", ".")
                    )
                }
            }
        }
    }
}