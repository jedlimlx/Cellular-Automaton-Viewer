package application.model.search.cfind

import application.model.Coordinate
import application.model.LRUCache
import application.model.search.SearchProgram
import java.io.File
import kotlin.math.max
import kotlin.math.pow

class ShipSearch(val searchParameters: ShipSearchParameters): SearchProgram(searchParameters) {
    val parameters = (searchParameters as ShipSearchParameters)

    private var range = 0
    private var indexOfUnknown = 0
    private lateinit var centralCoordinate: Coordinate
    private lateinit var extraBoundaryConditions: IntArray
    private lateinit var effectiveNeighbourhood: List<Coordinate>

    // private lateinit var lookupTable: IntArray
    private lateinit var lookupTable2: LRUCache<Key, ArrayList<IntArray>>

    override fun search(num: Int) {
        // Construct the lookup table
        /*
        lookupTable = IntArray(pow(parameters.rule.numStates, parameters.rule.neighbourhood.size + 1)) {
            val string = it.toString(parameters.rule.numStates).padStart(
                parameters.rule.neighbourhood.size + 1, '0')
            val neighbours = IntArray(parameters.rule.neighbourhood.size) { i ->
                string[string.length - 1 - i].code - '0'.code
            }

            parameters.rule.transitionFunc(neighbours, string[0].code - '0'.code,
                0, Coordinate())
        }*/

        // Print out parameters
        println(
            "Beginning search for ${parameters.symmetry} width ${parameters.width} " +
                    "${parameters.dy}c/${parameters.period}o ship in ${parameters.rule}...\n"
        )

        val startTime = System.currentTimeMillis()

        // Obtain range of rule
        range = 0
        parameters.rule.neighbourhood.forEach {
            range = range.coerceAtLeast(max(it.x, it.y))
        }

        // Obtain the index of the unknown cell to be used later
        var coordinateOfUnknown = Coordinate()
        parameters.rule.neighbourhood.forEachIndexed { index, coordinate ->
            if (coordinate.y > coordinateOfUnknown.y) {
                coordinateOfUnknown = coordinate
                indexOfUnknown = index
            }
        }

        parameters.rule.neighbourhood.forEachIndexed { index, coordinate ->
            if (coordinate.y == coordinateOfUnknown.y && coordinate.x > coordinateOfUnknown.x) {
                coordinateOfUnknown = coordinate
                indexOfUnknown = index
            }
        }

        effectiveNeighbourhood = parameters.rule.neighbourhood.map {
            it.subtract(coordinateOfUnknown)
        }.toList()

        centralCoordinate = Coordinate().subtract(coordinateOfUnknown)

        // What extra boundary conditions are necessary?
        extraBoundaryConditions = IntArray(range) { -100 }
        effectiveNeighbourhood.forEach {
            if (it.x > 0 && it.y < 0)
                extraBoundaryConditions[it.x - 1] = max(extraBoundaryConditions[it.x - 1], it.y)
        }
        println(extraBoundaryConditions.joinToString(" "))

        // Creating lookup table for successor states
        lookupTable2 = LRUCache(2.0.pow(15).toInt())

        // Creating transposition table to detect equivalent states
        val transpositionTable: HashMap<Int, List<State>> = hashMapOf()

        // Creating initial 2 * range * period rows
        var prevState = State(null, IntArray(parameters.width) { 0 }, parameters.rule.numStates)
        val bfsQueue: ArrayDeque<State> = ArrayDeque(2 * range * parameters.period)
        for (i in 0..2 * range * parameters.period) {  // Initialise 2Rp empty rows
            prevState = State(prevState, IntArray(parameters.width) { 0 }, parameters.rule.numStates)
        }

        bfsQueue.add(prevState)

        // Main loop
        var count = 0
        var count2 = 0

        var hash: Int
        var state: State
        var statesToCheck: List<State>
        while (true) {
            if (!parameters.stdin && !parameters.dfs) println("Beginning breath-first search round...")
            else if (!parameters.stdin) println("Beginning depth-first search round...")

            // BFS
            while (bfsQueue.size < parameters.maxQueueSize) {
                if (bfsQueue.isEmpty() && !parameters.stdin) {
                    println(
                        "\nSearch complete! Took ${(System.currentTimeMillis() - startTime) / 1000} seconds, " +
                                "found $count ships."
                    )
                    return
                }

                state = if (parameters.dfs) bfsQueue.removeLast()
                else bfsQueue.removeFirst()

                if (++count2 % 15000 == 0 || parameters.stdin) {
                    println(
                        "\nx = 0, y = 0, rule = ${parameters.rule}\n" +
                                state.toRLE(parameters.period, parameters.symmetry)
                    )
                }

                // Ship is complete if last 2Rp rows are empty
                val output = state.completeShip(2 * range * parameters.period)
                if (output == 1 && !parameters.stdin) {
                    println("\nShip found!")
                    println(
                        "x = 0, y = 0, rule = ${parameters.rule}\n" +
                                state.toRLE(parameters.period, parameters.symmetry)
                    )
                    if (++count >= num) {
                        println(
                            "\nSearch complete! Took ${(System.currentTimeMillis() - startTime) / 1000} seconds, " +
                                    "found $count ships."
                        )
                        return
                    }
                } else if (output == 2 && state.depth > 2 * range * parameters.period + 2) continue

                // Check for equivalent states (last 2Rp rows are the same)
                statesToCheck = state.getAllPredecessors(2 * range * parameters.period)
                hash = statesToCheck.hashCode()

                if (hash !in transpositionTable || statesToCheck != transpositionTable[hash]) {
                    transpositionTable[hash] = statesToCheck
                    findSuccessors(state).forEach { bfsQueue.addLast(it) }
                }
            }

            if (!parameters.stdin) print("\nBeginning depth-first search round, queue size ${bfsQueue.size} ")

            var deleted = 0  // DFS on all nodes in the BFS queue
            for (index in 0 until bfsQueue.size) {
                // Limit depth of DFS
                val maxDepth = bfsQueue[index - deleted].depth + parameters.minDeepingIncrement

                // DFS is basically BFS but with a stack
                val dfsStack: ArrayList<State> = ArrayList()
                dfsStack.add(bfsQueue[index - deleted])

                do {
                    // The state leads to a dead end
                    if (dfsStack.isEmpty())
                        break

                    state = dfsStack.removeLast()

                    if (parameters.stdin) {
                        println(
                            "\nx = 0, y = 0, rule = ${parameters.rule}\n" +
                                    state.toRLE(parameters.period, parameters.symmetry)
                        )
                    }

                    // Ship is complete if last 2Rp rows are empty
                    val output = state.completeShip(2 * range * parameters.period)
                    if (output == 1 && !parameters.stdin) {
                        val rle = state.toRLE(parameters.period, parameters.symmetry)
                        val rleBuilder = StringBuilder()
                        for (i in 10..rle.length step 10) {
                            rleBuilder.append(rle.substring(i - 10, i) + "\n")
                        }

                        println("\nShip found!")
                        println("x = 0, y = 0, rule = ${parameters.rule}\n${rle}")
                        if (++count >= num) {
                            println(
                                "\nSearch complete! Took ${(System.currentTimeMillis() - startTime) / 1000} seconds, " +
                                        "found $count ships."
                            )
                            return
                        }
                    }

                    // Check for equivalent states (last 2Rp rows are the same)
                    statesToCheck = state.getAllPredecessors(2 * range * parameters.period)
                    hash = statesToCheck.hashCode()

                    if (hash !in transpositionTable || statesToCheck != transpositionTable[hash]) {
                        transpositionTable[hash] = statesToCheck
                        findSuccessors(state).forEach { bfsQueue.addLast(it) }
                    }
                } while (state.depth < maxDepth)

                bfsQueue.removeAt(index - deleted++)

                bfsQueue.addAll(dfsStack)
                deleted -= dfsStack.size
            }

            if (!parameters.stdin) println("-> ${bfsQueue.size}")
        }
    }

    override fun searchThreaded(num: Int, numThreads: Int) {
        TODO("Implement multi-threading")
    }

    override fun writeToFile(file: File?): Boolean {
        TODO("Write ships to file")
    }

    private fun findSuccessors(state: State): List<State> {
        val key = Key(Array(2 * range + 1) {  // Checking lookup table
            if (it == 2 * range) {
                if (-centralCoordinate.y * parameters.period - parameters.dy - 1 == -1)
                    IntArray(parameters.width) { -1 }
                else state.getPredecessor(-centralCoordinate.y * parameters.period - parameters.dy - 1)!!.cells
            } else state.getPredecessor((it + 1) * parameters.period - 1)!!.cells
        })

        val key2 = if (parameters.period != 1) {
            Key(Array(2 * range + 1) {
                if (centralCoordinate.y == -1) {
                    if (it == 2 * range) IntArray(parameters.width) { -1 }
                    else state.getPredecessor(it * parameters.period + parameters.dy - 1)!!.cells
                } else {
                    if (it == 2 * range) state.getPredecessor(
                        -(centralCoordinate.y + 1) * parameters.period -
                                parameters.dy - 1
                    )!!.cells
                    else if (it == 0) IntArray(parameters.width) { -1 }
                    else state.getPredecessor(it * parameters.period - 1)!!.cells
                }
            })
        } else key

        val successors: ArrayList<State> = ArrayList()
        if (lookupTable2.containsKey(key)) {
            lookupTable2[key]!!.forEach {
                val newState = State(state, it, parameters.rule.numStates)

                // Check if the state can be extended
                if (centralCoordinate.y < -1) key2.key[0] = newState.cells
                if (parameters.period == 1 || !parameters.lookahead || lookahead(key2, newState))
                    successors.add(newState)
            }

            return successors
        }

        val successors2: ArrayList<IntArray> = ArrayList()
        val dfsStack: ArrayList<Node> = ArrayList(30)
        dfsStack.add(Node(null, 0))

        var node: Node
        var neighbours: IntArray
        while (dfsStack.isNotEmpty()) {
            node = dfsStack.removeLast()
            if (node.depth == parameters.width) {
                // Check boundary conditions
                var valid = true
                for (i in parameters.width..parameters.width + range) {
                    neighbours = getNeighbours(key, node, i, parameters.symmetry, fillUnknown = true)

                    val index = when {
                        i + centralCoordinate.x < parameters.width -> i + centralCoordinate.x
                        parameters.symmetry == Symmetry.ODD_SYMMETRIC -> 2 * (parameters.width - 1) -
                                (i + centralCoordinate.x)
                        parameters.symmetry == Symmetry.EVEN_SYMMETRIC -> 2 * parameters.width - 1 -
                                (i + centralCoordinate.x)
                        else -> 0
                    }

                    var cellState: Int
                    var nextState: Int

                    if (i + centralCoordinate.x >= parameters.width && parameters.symmetry == Symmetry.ASYMMETRIC) {
                        cellState = 0
                        nextState = 0
                    } else {
                        cellState = key.key[-centralCoordinate.y - 1][index]
                        nextState = key.key[2 * range][index]

                        if (nextState == -1)
                            nextState = getNeighbour(Coordinate(centralCoordinate.x, 0), key, node,
                                i, parameters.symmetry)
                    }

                    if (nextState != parameters.rule.transitionFunc(neighbours, cellState, 0,
                            Coordinate())) {
                        valid = false
                        break  // State is invalid
                    }
                }

                if (!valid) continue // State is invalid

                // Add to possible successors
                var predecessor = node
                val cells = IntArray(parameters.width) { 0 }
                for (i in 0 until parameters.width) {
                    cells[parameters.width - i - 1] = predecessor.cellState
                    predecessor = predecessor.predecessor!!
                }

                val newState = State(state, cells, parameters.rule.numStates)

                // Check if the state can be extended
                successors2.add(newState.cells)
                if (centralCoordinate.y < -1) key2.key[0] = newState.cells
                if (parameters.period == 1 || !parameters.lookahead || lookahead(key2, newState))
                    successors.add(newState)
            } else {
                // Compute possible next nodes
                val cellState: Int
                var nextState: Int
                if (node.depth + centralCoordinate.x >= 0) {
                    cellState = key.key[-centralCoordinate.y - 1][node.depth + centralCoordinate.x]
                    nextState = key.key[2 * range][node.depth + centralCoordinate.x]
                    if (nextState == -1)
                        nextState = getNeighbour(
                            Coordinate(centralCoordinate.x, 0), key, node,
                            node.depth, parameters.symmetry
                        )
                } else {
                    cellState = 0
                    nextState = 0
                }

                val possibleNextState = parameters.rule.dependsOnNeighbours(cellState, 0, Coordinate())
                if (possibleNextState != -1) {
                    if (possibleNextState == nextState) {
                        // Creating new node & adding to stack
                        for (i in 0 until parameters.rule.numStates) {
                            if (node.depth == 0) {
                                var valid = true

                                // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                for (j in extraBoundaryConditions.indices) {
                                    if (extraBoundaryConditions[j] == 0) continue
                                    if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                            Node(node, i), parameters.symmetry)) {
                                        valid = false  // Boundary condition not satisfied
                                        break
                                    }
                                }

                                if (valid) dfsStack.add(Node(node, i))
                            } else dfsStack.add(Node(node, i))
                        }
                    }
                } else {
                    neighbours = getNeighbours(key, node, node.depth, parameters.symmetry)

                    val arr = parameters.rule.getSuccessor(neighbours, indexOfUnknown, cellState, nextState, 0)

                    // Creating new node & adding to stack
                    for (i in arr.indices) {
                        if (arr[i]) {
                            if (node.depth == 0) {
                                var valid = true

                                // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                for (j in extraBoundaryConditions.indices) {
                                    if (extraBoundaryConditions[j] == 0) continue
                                    if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                            Node(node, i), parameters.symmetry)) {
                                        valid = false  // Boundary condition not satisfied
                                        break
                                    }
                                }

                                if (valid) dfsStack.add(Node(node, i))
                            } else dfsStack.add(Node(node, i))
                        }
                    }
                }
            }
        }

        lookupTable2.put(key, successors2)
        return successors
    }

    private fun lookahead(key: Key, state: State): Boolean {
        val dfsStack: ArrayList<Node> = ArrayList(30)
        dfsStack.add(Node(null, 0))

        var node: Node
        var neighbours: IntArray
        while (dfsStack.isNotEmpty()) {
            node = dfsStack.removeLast()
            if (node.depth == parameters.width) {
                // Check boundary conditions
                var valid = true
                for (i in parameters.width..parameters.width + range) {
                    neighbours = getNeighbours(key, node, i, parameters.symmetry, fillUnknown = true)

                    val index = when {
                        i + centralCoordinate.x < parameters.width -> i + centralCoordinate.x
                        parameters.symmetry == Symmetry.ODD_SYMMETRIC -> 2 * (parameters.width - 1) -
                                (i + centralCoordinate.x)
                        parameters.symmetry == Symmetry.EVEN_SYMMETRIC -> 2 * parameters.width - 1 -
                                (i + centralCoordinate.x)
                        else -> 0
                    }

                    var cellState: Int
                    var nextState: Int

                    if (i + centralCoordinate.x >= parameters.width && parameters.symmetry == Symmetry.ASYMMETRIC) {
                        cellState = 0
                        nextState = 0
                    } else {
                        cellState = key.key[-centralCoordinate.y - 1][index]

                        nextState = if (key.key[2 * range][index] == -1) state.cells[index]
                        else key.key[2 * range][index]
                    }

                    if (nextState != parameters.rule.transitionFunc(neighbours, cellState, 0, Coordinate())) {
                        valid = false
                        break  // State is invalid
                    }
                }

                if (!valid) continue // State is invalid
                return true
            } else {
                // Compute possible next nodes
                val cellState: Int
                val nextState: Int
                if (node.depth + centralCoordinate.x >= 0) {
                    cellState = key.key[-centralCoordinate.y - 1][node.depth + centralCoordinate.x]

                    nextState = if (key.key[2 * range][node.depth + centralCoordinate.x] == -1)
                        state.cells[node.depth + centralCoordinate.x]
                    else key.key[2 * range][node.depth + centralCoordinate.x]
                } else {
                    cellState = 0
                    nextState = 0
                }

                val possibleNextState = parameters.rule.dependsOnNeighbours(cellState, 0, Coordinate())
                if (possibleNextState != -1) {
                    if (possibleNextState == nextState) {
                        // Creating new node & adding to stack
                        for (i in 0 until parameters.rule.numStates) {
                            if (node.depth == 0) {
                                var valid = true

                                // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                for (j in extraBoundaryConditions.indices) {
                                    if (extraBoundaryConditions[j] == 0) continue
                                    if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                            Node(node, i), parameters.symmetry)) {
                                        valid = false  // Boundary condition not satisfied
                                        break
                                    }
                                }

                                if (valid) dfsStack.add(Node(node, i))
                            } else dfsStack.add(Node(node, i))
                        }
                    }
                } else {
                    neighbours = getNeighbours(key, node, node.depth, parameters.symmetry)

                    val arr = parameters.rule.getSuccessor(neighbours, indexOfUnknown, cellState, nextState, 0)

                    // Creating new node & adding to stack
                    for (i in arr.indices) {
                        if (arr[i]) {
                            if (node.depth == 0) {
                                var valid = true

                                /// Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                for (j in extraBoundaryConditions.indices) {
                                    if (extraBoundaryConditions[j] == 0) continue
                                    if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                            Node(node, i), parameters.symmetry)) {
                                        valid = false  // Boundary condition not satisfied
                                        break
                                    }
                                }

                                if (valid) dfsStack.add(Node(node, i))
                            } else dfsStack.add(Node(node, i))
                        }
                    }
                }
            }
        }

        return false
    }

    private fun extraBoundaryCondition(dx: Int, dy: Int, key: Key, node: Node, symmetry: Symmetry): Boolean {
        val possibleNextState = parameters.rule.dependsOnNeighbours(0, 0, Coordinate())
        if (possibleNextState == 0) {
            return true
        } else {
            val neighbours = IntArray(effectiveNeighbourhood.size) {
                getNeighbour(
                    effectiveNeighbourhood[it].add(Coordinate(dx, dy)), key, node, node.depth,
                    symmetry, true
                )
            }

            if (0 == parameters.rule.transitionFunc(neighbours, 0, 0, Coordinate())) {
                return true
            }
        }

        return false
    }

    private fun getNeighbours(key: Key, node: Node, depth: Int, symmetry: Symmetry,
                              fillUnknown: Boolean = false): IntArray {
        return IntArray(effectiveNeighbourhood.size) {
            getNeighbour(effectiveNeighbourhood[it], key, node, depth, symmetry, fillUnknown)
        }
    }

    private fun getNeighbour(
        coordinate: Coordinate, key: Key, node: Node, depth: Int,
        symmetry: Symmetry, fillUnknown: Boolean = false
    ): Int {
        return when {
            depth + coordinate.x < 0 -> 0
            depth + coordinate.x > parameters.width - 1 -> {
                if (symmetry == Symmetry.ODD_SYMMETRIC) {
                    if (2 * (parameters.width - 1) < depth + coordinate.x) 0
                    else if (coordinate.y == 0) node.getPredecessor(
                        depth + coordinate.x - (2 * parameters.width - node.depth - 1)
                    )!!.cellState
                    else key.key[-coordinate.y - 1][2 * (parameters.width - 1) - (depth + coordinate.x)]
                } else if (symmetry == Symmetry.EVEN_SYMMETRIC) {
                    if (2 * parameters.width - 1 < depth + coordinate.x) 0
                    else if (coordinate.y == 0) node.getPredecessor(
                        depth + coordinate.x - (2 * parameters.width - node.depth)
                    )!!.cellState
                    else key.key[-coordinate.y - 1][2 * parameters.width - 1 - (depth + coordinate.x)]
                } else 0
            }
            coordinate.x == 0 && coordinate.y == 0 && !fillUnknown -> -1
            coordinate.y == 0 -> node.getPredecessor(-coordinate.x - 1 - (depth - node.depth))!!.cellState
            else -> key.key[-coordinate.y - 1][depth + coordinate.x]
        }
    }

    private fun pow(a: Int, p: Int): Int {
        var res = 1
        val i1 = 31 - Integer.numberOfLeadingZeros(p) // highest bit index
        for (i in i1 downTo 0) {
            res *= res
            if (p and (1 shl i) > 0) res *= a
        }
        return res
    }
}

class Node(val predecessor: Node?, val cellState: Int) {
    var depth = 0
        private set

    init {
        if (predecessor != null) depth = predecessor.depth + 1
    }

    fun getPredecessor(n: Int): Node? {
        if (n == 0) return this
        if (n == 1) return predecessor
        return predecessor?.getPredecessor(n - 1)
    }
}

class Key(val key: Array<IntArray>) {
    override fun hashCode(): Int {
        return key.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Key

        if (!key.contentDeepEquals(other.key)) return false

        return true
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder("[")
        key.forEach { stringBuilder.append("[" + it.joinToString(", ") + "] ") }
        stringBuilder.append("]")

        return stringBuilder.toString()
    }
}