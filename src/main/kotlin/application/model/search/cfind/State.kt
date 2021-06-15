package application.model.search.cfind

import application.model.simulation.Grid

class State(val predecessor: State?, val cells: IntArray, val numStates: Int) {
    var depth = 0

    init {
        if (predecessor != null) {
            depth = predecessor.depth + 1
        }
    }

    fun getPredecessor(n: Int): State? {
        if (n == 0) return this
        if (n == 1) return predecessor
        return predecessor?.getPredecessor(n - 1)
    }

    fun getAllPredecessors(n: Int): List<State> {
        val list = mutableListOf(this)
        var predecessor: State? = this.predecessor

        while (predecessor != null) {
            if (depth - n == predecessor.depth) break

            list.add(State(null, predecessor.cells, numStates))
            predecessor = predecessor.predecessor
        }

        return list
    }

    fun completeShip(n: Int): Int {
        var predecessor = this
        for (i in 0 until n) {
            if (!predecessor.isEmpty()) return 0
            if (predecessor.predecessor == null) return 0
            predecessor = predecessor.predecessor!!
        }

        getAllPredecessors(-1).forEach {
            if (!it.isEmpty()) return 1
        }

        return 2
    }

    fun toRLE(period: Int, symmetry: Symmetry): String {
        val grid = Grid()
        var temp: State?
        var predecessor = this

        var counter = 0
        while (true) {
            predecessor.cells.forEachIndexed { index, state ->
                grid.setCell(index, -counter, state)
                if (symmetry == Symmetry.EVEN_SYMMETRIC) grid.setCell(2 * predecessor.cells.size - 1
                        - index, -counter, state)
                else if (symmetry == Symmetry.ODD_SYMMETRIC) grid.setCell(2 * predecessor.cells.size - 2
                        - index, -counter, state)
            }

            temp = predecessor.getPredecessor(period)
            if (temp == null) return grid.toRLE()

            predecessor = temp
            counter++
        }
    }

    fun isEmpty(): Boolean {
        for (cell in cells) {
            if (cell != 0) return false
        }

        return true
    }

    override fun toString(): String {
        return cells.joinToString(" ")
    }

    override fun hashCode(): Int {
        return cells.reduceIndexed { index, acc, state ->
            acc + state * pow(numStates, index)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as State

        if (!cells.contentEquals(other.cells)) return false
        return true
    }

    private fun pow(base: Int, exponent: Int): Int {
        if (exponent == 0) return 1
        val temp = pow(base, exponent / 2)
        return if (exponent % 2 == 0) temp * temp else base * temp * temp
    }
}
