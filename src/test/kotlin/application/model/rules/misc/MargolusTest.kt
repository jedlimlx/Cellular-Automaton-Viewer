package application.model.rules.misc

import application.model.Coordinate
import application.model.rules.hrot.HROT
import application.model.simulation.Grid
import application.model.simulation.Simulator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.util.*

class MargolusTest {
    private fun getStream(resourcePath: String): InputStream {
        return javaClass.getResourceAsStream(resourcePath)!!
    }

    @Test
    fun testIdentification() {
        // Loading the testcases
        val scanner = Scanner(getStream("/Margolus/identificationTest.txt"))
        var rule = Margolus()
        var inputPattern: Simulator? = null
        var targetPattern: String? = null

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> rule = Margolus(line.substring(3))
                line.startsWith("#I") -> {
                    inputPattern = Simulator(rule)
                    inputPattern.fromRLE(line.substring(3), Coordinate())
                }
                line.startsWith("#T") -> targetPattern = line.substring(3)
                else -> {
                    val pattern = inputPattern!!.identify()
                    Assertions.assertEquals(targetPattern, pattern.toString())
                }
            }
        }
    }

    @Test
    fun testSimulation() {
        // Loading the testcases
        val scanner = Scanner(getStream("/Margolus/simulationTest.txt"))
        var generations = 0
        var margolusRule = Margolus()
        var inputPattern: Simulator? = null
        var targetPattern: Grid

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> margolusRule = Margolus(line.substring(3))
                line.startsWith("#G") -> generations = line.substring(3).toInt()
                line.startsWith("#I") -> {
                    inputPattern = Simulator(margolusRule)
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