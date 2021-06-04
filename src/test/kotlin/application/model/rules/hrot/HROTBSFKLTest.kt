package application.model.rules.hrot

import application.model.rules.hrot.HROTBSFKL
import application.model.simulation.Simulator
import application.model.Coordinate
import application.model.simulation.Grid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.util.*

class HROTBSFKLTest {
    fun getStream(resourcePath: String): InputStream {
        return javaClass.getResourceAsStream(resourcePath)!!
    }

    @Test
    fun testCanonise() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT BSFKL/parsingTest.txt"))

        // Run through them
        var rulestring = ""
        var canonisedRulestring = ""
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            if (line.startsWith("#R")) {
                // Loading rulestring
                rulestring = line.substring(3)
            } else if (line.startsWith("#C")) {
                // Loading canonised rulestring
                canonisedRulestring = line.substring(3)
            } else if (!line.startsWith("#")) {
                // Running the testcase
                val hrot = HROTBSFKL(rulestring)
                Assertions.assertEquals(canonisedRulestring, hrot.rulestring)
            }
        }
    }

    @Test
    fun testClone() {
        val hrot = HROTBSFKL("R1,B1,S2,F3,K4,L5,NM")
        val hrotClone = hrot.clone() as HROTBSFKL
        hrot.rulestring = "R2,B2,S3,F4,K5,L6,N@891891"

        // Ensure they are different
        Assertions.assertNotEquals(hrotClone.birth, hrot.birth)
        Assertions.assertNotEquals(hrotClone.survival, hrot.survival)
        Assertions.assertNotEquals(hrotClone.forcing, hrot.forcing)
        Assertions.assertNotEquals(hrotClone.killing, hrot.killing)
        Assertions.assertNotEquals(hrotClone.living, hrot.living)
        Assertions.assertNotEquals(hrotClone.getNeighbourhood(), hrot.getNeighbourhood())
    }

    @Test
    fun testSimulation() {
        // Loading the testcases
        val scanner = Scanner(getStream("/HROT BSFKL/simulationTest.txt"))
        var generations = 0
        var hrot = HROTBSFKL()
        var inputPattern: Simulator? = null
        var targetPattern: Grid? = null
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            when {
                line.startsWith("#R") -> hrot = HROTBSFKL(line.substring(3))
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
                    // Run N generations
                    for (i in 0 until generations)
                        inputPattern!!.step()

                    Assertions.assertEquals(
                        targetPattern!!.toRLE(),
                        inputPattern!!.toRLE().replace("o", "A").replace("b", ".")
                    )
                }
            }
        }
    }
}