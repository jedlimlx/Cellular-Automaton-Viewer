package application.model.rules.hrot

import application.model.Coordinate
import application.model.simulation.Grid
import application.model.simulation.Simulator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.util.*

class MultistateCyclicHROTTest {
    fun getStream(resourcePath: String): InputStream {
        return javaClass.getResourceAsStream(resourcePath)!!
    }

    @Test
    fun testSimulation() {
        // Loading the testcases
        val scanner = Scanner(getStream("/Cyclic HROT/simulationTest.txt"))

        var generations = 0
        var hrot = MultistateCyclicHROT()
        var inputPattern: Simulator? = null
        var targetPattern: Grid? = null
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> hrot = MultistateCyclicHROT(line.substring(3))
                line.startsWith("#G") -> generations = line.substring(3).toInt()
                line.startsWith("#I") -> {
                    inputPattern = Simulator(hrot)
                    inputPattern.fromRLE(line.substring(3), Coordinate(0, 0))
                }
                line.startsWith("#O") -> {
                    targetPattern = Grid()
                    targetPattern.fromRLE(line.substring(3), Coordinate(0, 0))
                }
                else -> {
                    for (i in 0 until generations)
                        inputPattern!!.step()

                    Assertions.assertEquals(targetPattern!!.toRLE(), inputPattern!!.toRLE())
                }
            }
        }
    }
}