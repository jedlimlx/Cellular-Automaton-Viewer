package application

import application.model.Coordinate
import application.model.SymmetryGenerator
import application.model.rules.hrot.HROT
import application.model.simulation.Simulator
import java.io.File
import java.io.FileWriter

object Geneascopy {
    @JvmStatic
    fun main(args: Array<String>) {
        val rule = HROT("B3/S23")

        val numSoups = 10000
        val numGenerations = 5000

        val popList = IntArray(numGenerations) { 0 }
        val dPopList = IntArray(numGenerations) { 0 }

        for (i in 1..numSoups) {
            val simulator = Simulator(rule)
            simulator.insertCells(
                SymmetryGenerator.generateC1(30, intArrayOf(1), 16, 16),
                Coordinate())

            var temp = 0
            for (generation in 0 until numGenerations) {
                dPopList[generation] += simulator.population - temp
                popList[generation] += simulator.population

                temp = simulator.population
                simulator.step()
            }

            println("Completed run $i / $numSoups!")
        }

        val file = FileWriter(File("data.csv"))
        for (i in popList.indices) {
            file.write("${popList[i]},${dPopList[i]}\n")
        }

        file.close()
    }
}