package application

import kotlin.Throws
import java.io.IOException
import kotlin.jvm.JvmStatic
import java.io.FileWriter
import application.model.simulation.Simulator
import application.model.rules.hrot.HROT
import application.model.Coordinate
import application.model.Utils
import application.model.rules.RuleFamily
import application.model.simulation.bounds.BoundedGrid
import application.model.rules.misc.naive.ReadingOrder
import application.model.simulation.bounds.Torus
import java.io.File

object Regex {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        var writer = FileWriter(File("regex.txt"))
        for (family in Utils.ruleFamilies) {
            for (string in family.regex) {
                writer.write("$string\n")
            }
        }
        writer.close()

        writer = FileWriter(File("regex2.txt"))
        for (grid in Utils.boundedGrids) {
            for (string in grid.regex) {
                writer.write("$string\n")
            }
        }
        writer.close()

        writer = FileWriter(File("regex3.txt"))
        for (readingOrder in Utils.readingOrders) {
            for (string in readingOrder.regex) {
                writer.write("$string\n")
            }
        }
        writer.close()
    }
}