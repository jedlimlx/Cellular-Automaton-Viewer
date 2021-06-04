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

object Test {
    @JvmStatic
    fun main(args: Array<String>) {
        val torus = Torus("T3", Coordinate(1, 1))
        println(
            torus.map(Coordinate(0, 4)).toString() + " " + torus.map(Coordinate(1, 0)) + " " +
                    torus.map(Coordinate(-1, -1)) + " " + torus.map(Coordinate(5, 4))
        )
    }
}