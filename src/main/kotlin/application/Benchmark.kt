package application

import kotlin.Throws
import java.io.IOException
import kotlin.jvm.JvmStatic
import java.io.FileWriter
import application.model.simulation.Simulator
import application.model.rules.hrot.HROT
import application.model.Coordinate
import application.model.rules.RuleFamily
import application.model.simulation.bounds.BoundedGrid
import application.model.rules.misc.naive.ReadingOrder
import application.model.simulation.bounds.Torus
import java.io.File

object Benchmark {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("benchmark.txt")
        val fileWriter = FileWriter(file)
        fileWriter.write("Running R10 Checkerboard Ship...\n")

        for (j in 1..5) {
            fileWriter.write("Run $j...\n")

            val simulator = Simulator(HROT("R10,C2,S23-28,B72-133,NB"))
            simulator.fromRLE(
                "23bobo$22bobobobo$21bobobobobo$20bobobobobobo$19bobobobobobobo$18bobobobobobobobo$17bobobobobobobobobo$15bob4obobobobobobobo$20b2obobobobobobo$16bobobo5bobobobobo$15bobobobo5bobobobobo$16bobobobo5bobobobo$15bobobobo5bobobobobo$11bobo2bobobobobo3bobobobo$8bobobo4bobobobobobobobobo$7b3obobobo2bobobobob3obobo$6bobobobobobo2bobobobo2b2obo$5bobobobobobobo2bobobobobobo$4bobobobobobobobo2bobobo2b2o$3bobobob3obobobobo2bobobo$2bobobobob3obobobobo2bobo2bo$3bobobobobobobobobo$2bobobobo5bobobobobo\$bobobobo7bobobobo$2bobobobo5b3obobobo\$bobobobobo5b3obobo$2bobobobobo3bobobobo$3bobobobobobobobob3o$2bobobobobobobobobobo$3bobobobobobobobobo$4bobobobobobobobo$5bobobobobobobo$6bobobobobobo$7bobobobobo$10bobo!",
                Coordinate()
            )

            val startTime = System.currentTimeMillis()
            for (i in 0..99) {
                simulator.step()
            }

            fileWriter.write("Took ${(System.currentTimeMillis() - startTime) / 1000.0} s")
        }

        fileWriter.write("\nRunning B3/S23 Soup...\n")
        for (j in 1..5) {
            fileWriter.write("Run $j...\n")
            val simulator = Simulator(HROT("B3/S23"))
            simulator.fromRLE(
                "4b3o6b2o\$bo5bo3b2o2bo\$bo2bob3o3bo2bo$2o2b4ob2ob2obo$2b2o2bo2b2obobo$5o" +
                        "2b2obobo2bo$4bo4bo5bo\$b2o4bob4o\$b2ob4ob3obo\$b4ob5o2bo$3b2o4b2o2bobo\$o" +
                        "2bo3bob3o2bo$2o2b3ob2o2b2o\$obob3o2bo\$o5bo7bo$2bo3bob3ob2o!", Coordinate()
            )

            val startTime = System.currentTimeMillis()
            for (i in 0..4999) {
                simulator.step()
            }

            fileWriter.write("Took ${(System.currentTimeMillis() - startTime) / 1000.0} s")
        }
        fileWriter.close()
    }
}