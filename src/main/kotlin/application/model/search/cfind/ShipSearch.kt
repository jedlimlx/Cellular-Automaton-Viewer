package application.model.search.cfind

import application.model.Coordinate
import application.model.LRUCache
import application.model.search.SearchProgram
import java.io.File
import java.io.PrintWriter
import kotlin.math.max

private val outputWriter = PrintWriter(System.out, true)

class ShipSearch(val searchParameters: ShipSearchParameters): SearchProgram(searchParameters) {
    val parameters = (searchParameters as ShipSearchParameters)

    private var range = 0
    private var indexOfUnknown = 0

    // Check if the neighbourhood has a single base cell (e.g. von neumann)
    private var singleBaseCell = false
    private var baseCellCount = 0

    private var additionalCells = 0
    private var numBeyondCentralCell = 0

    private lateinit var possibleSuccessor:Array<IntArray>

    private lateinit var centralCoordinate: Coordinate
    private lateinit var beyondCentralCell: IntArray
    private lateinit var extraBoundaryConditions: IntArray
    private lateinit var effectiveNeighbourhood: List<Coordinate>

    private lateinit var lookupTable: Array<IntArray>
    private lateinit var lookupTable2: Array<Array<Node>>
    private lateinit var lookupTable3: Array<Array<Node>>

    override fun search(num: Int) {
        // Print out parameters
        if (!parameters.stdin) outputWriter.println(
            "Beginning search for ${parameters.symmetry} width ${parameters.width} " +
                    "${parameters.dy}c/${parameters.period}o ship in ${parameters.rule}...\n"
        )

        // Disable lookahead for p1 searches
        if (parameters.period == 1) parameters.lookahead = false

        // Optimisation for generations rules
        possibleSuccessor = arrayOf(IntArray(parameters.rule.numStates) { it },
            IntArray(parameters.rule.numStates) { it })

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
        extraBoundaryConditions = IntArray(range + centralCoordinate.x) { -100 }
        effectiveNeighbourhood.forEach {
            if (it.x > 0 && it.y < 0)
                extraBoundaryConditions[it.x - 1] = max(extraBoundaryConditions[it.x - 1], it.y)
        }

        // How many cells extend beyond the central cell?
        beyondCentralCell = IntArray(2 * range + 1) { 0 }
        effectiveNeighbourhood.forEach {
            if (it.x > 0) {
                beyondCentralCell[2 * range + it.y]++
                numBeyondCentralCell++
            }
        }

        // Temporarily disable optimisation in cases where it does not work
        if (numBeyondCentralCell != 0) parameters.lookupTableWidth = 0
        else if (parameters.rule.alternatingPeriod != 1) parameters.lookupTableWidth = 0

        // Check if the neighbourhood has a single base cell (e.g. von neumann)
        baseCellCount = effectiveNeighbourhood.count { it.x == 0 }
        singleBaseCell = baseCellCount == 1

        additionalCells = effectiveNeighbourhood.count { it.y == -1 && it.x > -1 }

        // Creating transposition table to detect equivalent states
        val transpositionTable: LRUCache<Int, List<State>> = LRUCache(2000000)

        // Construct the lookup table for single-cell successor states
        lookupTable = Array(parameters.rule.alternatingPeriod) { generation ->
            IntArray(pow(parameters.rule.numStates, parameters.rule.neighbourhood.size + 1)) {
                val string = it.toString(parameters.rule.numStates).padStart(
                    parameters.rule.neighbourhood.size + 1, '0'
                )
                val neighbours = IntArray(parameters.rule.neighbourhood.size) { i ->
                    if (i != indexOfUnknown) string[string.length - 1 - (i -
                            if (i > indexOfUnknown) 1 else 0)].code - '0'.code
                    else -1
                }

                var count = 0

                parameters.rule.getSuccessor(
                    neighbours, indexOfUnknown, string[0].code - '0'.code,
                    string[1].code - '0'.code, generation
                ).forEachIndexed { index, i -> count += if (i) pow(2, index) else 0 }

                count
            }
        }

        // Constructing another lookup table
        outputWriter.println("Generating lookup table...")
        lookupTable2 = Array(pow(parameters.rule.numStates, (2 * range + 1) * parameters.lookupTableWidth -
                centralCoordinate.x + numBeyondCentralCell)) { h ->
            val generation = 0

            val finalNodes = ArrayList<Node>()
            val dfsStack = ArrayList<Node>()
            dfsStack.add(Node(null, 0, parameters.rule.numStates, baseCellCount))

            var index = 0
            val string = h.toString(parameters.rule.numStates).padStart(
                (2 * range + 1) * parameters.lookupTableWidth - centralCoordinate.x
                        + numBeyondCentralCell, '0'
            )
            val key = Key(Array(2 * range + 1) { i ->
                IntArray(parameters.lookupTableWidth + beyondCentralCell.maxOrNull()!!) { j ->
                    if (i < 2 * range && j < parameters.lookupTableWidth + beyondCentralCell[2 * range - i - 1])
                        string[string.length - 1 - index++].code - '0'.code
                    else if (i == 2 * range && j < parameters.lookupTableWidth + centralCoordinate.x)
                        string[string.length - 1 - index++].code - '0'.code
                    else 100
                }
            }, parameters, centralCoordinate, beyondCentralCell)

            val cache = Array(parameters.width + 1) {
                IntArray(pow(parameters.rule.numStates, baseCellCount - 1)) { -1 }
            }

            var node: Node
            var hash: Int
            while (dfsStack.isNotEmpty()) {
                node = dfsStack.removeLast()
                if (node.depth == parameters.lookupTableWidth) finalNodes.add(node)
                else {
                    // Checking for the special optimisation
                    if (cache[node.depth][node.actualHash] != -1) {
                        val output = cache[node.depth][node.actualHash]
                        for (i in 0 until parameters.rule.numStates) {
                            // Getting i th bit from the output
                            if (output shr i and 1 == 1) {
                                dfsStack.add(Node(
                                    node,
                                    parameters.rule.convertState(i, generation),
                                    parameters.rule.numStates,
                                    baseCellCount
                                ))
                            }
                        }

                        continue
                    }

                    // Compute possible next nodes
                    var cellState: Int
                    var nextState: Int
                    if (node.depth + centralCoordinate.x >= 0) {
                        cellState = key.key[-centralCoordinate.y - 1][node.depth + centralCoordinate.x]
                        nextState = key.key[2 * range][node.depth + centralCoordinate.x]
                        if (nextState == -1)
                            nextState = getNeighbour(Coordinate(centralCoordinate.x, 0), key, node,
                                node.depth, generation, parameters.symmetry)
                    } else {
                        cellState = 0
                        nextState = 0
                    }

                    // Convert based on the background
                    cellState = parameters.rule.convertState(cellState, generation)
                    nextState = parameters.rule.convertState(nextState, generation + 1)

                    // Obtain the next node
                    val possibleNextState = parameters.rule.dependsOnNeighbours(cellState, generation, Coordinate())
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
                                                Node(node, i, parameters.rule.numStates, baseCellCount), generation,
                                                parameters.symmetry)) {
                                            valid = false  // Boundary condition not satisfied
                                            break
                                        }
                                    }

                                    if (valid) dfsStack.add(Node(
                                        node,
                                        parameters.rule.convertState(i, generation),
                                        parameters.rule.numStates,
                                        baseCellCount
                                    ))
                                } else dfsStack.add(Node(
                                    node,
                                    parameters.rule.convertState(i, generation),
                                    parameters.rule.numStates,
                                    baseCellCount
                                ))
                            }
                        }
                    } else {
                        hash = getHash(key, node, node.depth, generation, parameters.symmetry)
                        hash += cellState * pow(parameters.rule.numStates, effectiveNeighbourhood.size)
                        if (nextState != -1)
                            hash += nextState * pow(parameters.rule.numStates, effectiveNeighbourhood.size - 1)

                        // Adding to special optimisation cache
                        val output = lookupTable[generation][hash]
                        cache[node.depth][node.actualHash] = output

                        // Creating new node & adding to stack
                        for (i in 0 until parameters.rule.numStates) {
                            // Getting i th bit from the output
                            if (output shr i and 1 == 1) {
                                if (node.depth == 0) {
                                    var valid = true

                                    // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                    for (j in extraBoundaryConditions.indices) {
                                        if (extraBoundaryConditions[j] == 0) continue
                                        if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                                Node(node, i, parameters.rule.numStates, baseCellCount), generation,
                                                parameters.symmetry)) {
                                            valid = false  // Boundary condition not satisfied
                                            cache[node.depth][node.actualHash] -= pow(2, i)
                                            break
                                        }
                                    }

                                    if (valid) dfsStack.add(Node(
                                        node,
                                        parameters.rule.convertState(i, generation),
                                        parameters.rule.numStates,
                                        baseCellCount
                                    ))
                                } else dfsStack.add(Node(
                                    node,
                                    parameters.rule.convertState(i, generation),
                                    parameters.rule.numStates,
                                    baseCellCount
                                ))
                            }
                        }
                    }
                }
            }

            //if (h % 100000 == 0) println(h)
            finalNodes.toTypedArray()
        }
        outputWriter.println("Completed successor lookup table!")

        lookupTable3 = Array(pow(parameters.rule.numStates, (2 * range + 1) * parameters.lookupTableWidth
                - centralCoordinate.x + numBeyondCentralCell)) { h ->
            val generation = 0
            var latestCell = 0

            val finalNodes = ArrayList<Node>()
            val dfsStack = ArrayList<Node>()
            dfsStack.add(Node(null, 0, parameters.rule.numStates, baseCellCount))

            var index = 0
            val string = h.toString(parameters.rule.numStates).padStart(
                (2 * range + 1) * parameters.lookupTableWidth - centralCoordinate.x
                        + numBeyondCentralCell, '0'
            )
            val key = Key(Array(2 * range + 1) { i ->
                IntArray(parameters.lookupTableWidth + beyondCentralCell.maxOrNull()!!) { j ->
                    if (i < 2 * range && j < parameters.lookupTableWidth + beyondCentralCell[2 * range - i - 1])
                        string[string.length - 1 - index++].code - '0'.code
                    else if (i == 2 * range && j < parameters.lookupTableWidth + centralCoordinate.x)
                        string[string.length - 1 - index++].code - '0'.code
                    else 100
                }
            }, parameters, centralCoordinate, beyondCentralCell)

            val cache = Array(parameters.width + 1) {
                BooleanArray(pow(parameters.rule.numStates, baseCellCount - 1)) { false }
            }

            var node: Node
            var hash: Int
            while (dfsStack.isNotEmpty()) {
                node = dfsStack.removeLast()
                if (cache[node.depth][node.actualHash]) continue
                else cache[node.depth][node.actualHash] = true

                if (node.depth == parameters.lookupTableWidth) finalNodes.add(node)
                else {
                    // Compute possible next nodes
                    var cellState: Int
                    var nextState: Int
                    if (node.depth + centralCoordinate.x >= 0) {
                        cellState = key.key[-centralCoordinate.y - 1][node.depth + centralCoordinate.x]
                        nextState = key.key[2 * range][node.depth + centralCoordinate.x]
                        if (nextState == -1)
                            nextState = getNeighbour(Coordinate(centralCoordinate.x, 0), key, node,
                                node.depth, generation, parameters.symmetry)
                    } else {
                        cellState = 0
                        nextState = 0
                    }

                    // Convert based on the background
                    cellState = parameters.rule.convertState(cellState, generation)
                    nextState = parameters.rule.convertState(nextState, generation + 1)

                    // Obtain the next node
                    val possibleNextState = parameters.rule.dependsOnNeighbours(cellState, generation, Coordinate())
                    if (possibleNextState != -1) {
                        if (possibleNextState == nextState) {
                            // Creating new node & adding to stack
                            var deadEnd = true
                            for (i in 0 until parameters.rule.numStates) {
                                if (node.depth == 0) {
                                    var valid = true

                                    // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                    for (j in extraBoundaryConditions.indices) {
                                        if (extraBoundaryConditions[j] == 0) continue
                                        if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                                Node(node, i, parameters.rule.numStates, baseCellCount), generation,
                                                parameters.symmetry)) {
                                            valid = false  // Boundary condition not satisfied
                                            break
                                        }
                                    }

                                    if (valid) {
                                        dfsStack.add(Node(
                                            node,
                                            parameters.rule.convertState(i, generation),
                                            parameters.rule.numStates,
                                            baseCellCount
                                        ))
                                        deadEnd = false
                                    }
                                } else  {
                                    dfsStack.add(Node(
                                        node,
                                        parameters.rule.convertState(i, generation),
                                        parameters.rule.numStates,
                                        baseCellCount
                                    ))
                                    deadEnd = false
                                }
                            }

                            if (deadEnd) latestCell = latestCell.coerceAtLeast(node.depth)
                        } else latestCell = latestCell.coerceAtLeast(node.depth)
                    } else {
                        hash = getHash(key, node, node.depth, generation, parameters.symmetry)
                        hash += cellState * pow(parameters.rule.numStates, effectiveNeighbourhood.size)
                        if (nextState != -1)
                            hash += nextState * pow(parameters.rule.numStates, effectiveNeighbourhood.size - 1)

                        val output = lookupTable[generation][hash]

                        // Creating new node & adding to stack
                        var deadEnd = true
                        for (i in 0 until parameters.rule.numStates) {
                            // Getting i th bit from the output
                            if (output shr i and 1 == 1) {
                                if (node.depth == 0) {
                                    var valid = true

                                    // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                    for (j in extraBoundaryConditions.indices) {
                                        if (extraBoundaryConditions[j] == 0) continue
                                        if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                                Node(node, i, parameters.rule.numStates, baseCellCount), generation,
                                                parameters.symmetry)) {
                                            valid = false  // Boundary condition not satisfied
                                            break
                                        }
                                    }

                                    if (valid) {
                                        dfsStack.add(Node(
                                            node,
                                            parameters.rule.convertState(i, generation),
                                            parameters.rule.numStates,
                                            baseCellCount
                                        ))
                                        deadEnd = false
                                    }
                                } else {
                                    dfsStack.add(Node(
                                        node,
                                        parameters.rule.convertState(i, generation),
                                        parameters.rule.numStates,
                                        baseCellCount
                                    ))
                                    deadEnd = false
                                }
                            }
                        }

                        if (deadEnd) latestCell = latestCell.coerceAtLeast(node.depth)
                    }
                }
            }

            finalNodes.add(Node(null, latestCell + 1, parameters.rule.numStates, baseCellCount))
            finalNodes.toTypedArray()
        }
        outputWriter.println("Completed lookahead lookup table!\n")

        // Creating initial 2 * range * period rows
        var prevState = State(null, IntArray(parameters.width) { 0 }, parameters.rule.numStates)
        var bfsQueue: ArrayDeque<State> = ArrayDeque(2 * range * parameters.period)
        for (i in 0..2 * range * parameters.period) {  // Initialise 2Rp empty rows
            prevState = State(prevState, IntArray(parameters.width) { 0 }, parameters.rule.numStates)
        }

        bfsQueue.add(prevState)

        // Note down the time when the search started
        val startTime = System.currentTimeMillis()

        // Main loop
        var count = 0
        var count2 = 0

        var hash: Int
        var generation: Int

        var state: State
        var statesToCheck: List<State>
        while (true) {
            if (!parameters.stdin && !parameters.dfs) outputWriter.println("Beginning breath-first search round...")
            else if (!parameters.stdin) outputWriter.println("Beginning depth-first search round...")

            // BFS
            while (bfsQueue.size < parameters.maxQueueSize) {
                if (bfsQueue.isEmpty() && !parameters.stdin) {
                    outputWriter.println("\nSearch complete! Took ${(System.currentTimeMillis() - startTime) / 1000} seconds, " +
                            "found $count ships.")
                    return
                } else if (bfsQueue.isEmpty()) return

                state = if (parameters.dfs) bfsQueue.removeLast()
                else bfsQueue.removeFirst()

                // Get the generation of the partial
                generation = (state.depth % parameters.period) % parameters.rule.alternatingPeriod

                // Printing out the partial
                if (!parameters.noPartials && generation == 0 && (++count2 % 15000 == 0 || parameters.stdin)) {
                    outputWriter.println("\nx = 0, y = 0, rule = ${parameters.rule}\n" +
                            state.toRLE(parameters.period, parameters.symmetry))
                }

                // Ship is complete if last 2Rp rows are empty
                val output = state.completeShip(2 * range * parameters.period)
                if (output == 1 && !parameters.stdin) {
                    outputWriter.println("\nShip found!\nx = 0, y = 0, rule = ${parameters.rule}\n" +
                            state.getPredecessor(generation + 1)!!.
                            toRLE(parameters.period, parameters.symmetry))

                    if (++count >= num) {
                        outputWriter.println("\nSearch complete! Took ${(System.currentTimeMillis() - startTime) / 1000}" +
                                " seconds, found $count ships.")
                        return
                    }
                } else if (output == 2 && state.depth > 2 * range * parameters.period + 2) continue

                // Check for equivalent states (last 2Rp rows are the same)
                statesToCheck = state.getAllPredecessors(2 * range * parameters.period)
                hash = statesToCheck.hashCode()

                if (!transpositionTable.containsKey(hash) || statesToCheck != transpositionTable.get(hash)) {
                    transpositionTable.put(hash, statesToCheck)
                    bfsQueue.addAll(findSuccessors(state))
                }
            }

            if (!parameters.stdin) outputWriter.print("\nBeginning depth-first search round, queue size ${bfsQueue.size} ")

            var pruned = false
            val newQueue = ArrayDeque<State>(bfsQueue.size / 50)
            val dfsStack = ArrayList<State>()
            for (index in 0 until bfsQueue.size) {
                // Limit depth of DFS
                val maxDepth = bfsQueue[index].prunedDepth + parameters.minDeepingIncrement

                // DFS is basically BFS but with a stack
                dfsStack.clear()
                dfsStack.add(bfsQueue[index])

                do {
                    // The state leads to a dead end
                    if (dfsStack.isEmpty()) {
                        pruned = true
                        break
                    }

                    state = dfsStack.removeLast()

                    // Get the generation of the partial
                    generation = (state.depth % parameters.period) % parameters.rule.alternatingPeriod

                    if (parameters.stdin && generation == 0) {
                        outputWriter.println("\nx = 0, y = 0, rule = ${parameters.rule}\n" +
                                state.toRLE(parameters.period, parameters.symmetry))
                    }

                    // Ship is complete if last 2Rp rows are empty
                    val output = state.completeShip(2 * range * parameters.period)
                    if (output == 1 && !parameters.stdin) {
                        outputWriter.println("\nShip found!\nx = 0, y = 0, rule = ${parameters.rule}\n" +
                                state.getPredecessor(generation + 1)!!.
                                toRLE(parameters.period, parameters.symmetry))

                        if (++count >= num) {
                            println("\nSearch complete! Took ${(System.currentTimeMillis() - startTime) / 1000}" +
                                    " seconds, found $count ships.")
                            return
                        }
                    }

                    dfsStack.addAll(findSuccessors(state))
                } while (state.depth < maxDepth)

                if (!pruned) {
                    bfsQueue[index].prunedDepth = maxDepth
                    newQueue.addAll(dfsStack)
                } else pruned = false
            }

            bfsQueue = newQueue
            if (!parameters.stdin) outputWriter.println("-> ${bfsQueue.size}")
        }
    }

    override fun searchThreaded(num: Int, numThreads: Int) {
        TODO("Implement multi-threading")
    }

    override fun writeToFile(file: File?): Boolean {
        TODO("Write ships to file")
    }

    private fun findSuccessors(state: State): List<State> {
        // Previous states that need to be checked
        val key = Key(Array(2 * range + 1) {  // Checking lookup table
            if (it == 2 * range) {
                if (-centralCoordinate.y * parameters.period - parameters.dy - 1 == -1)
                    IntArray(parameters.width) { -1 }
                else state.getPredecessor(-centralCoordinate.y * parameters.period - parameters.dy - 1)!!.cells
            } else state.getPredecessor((it + 1) * parameters.period - 1)!!.cells
        }, parameters, centralCoordinate, beyondCentralCell)
        val key2 = if (parameters.period != 1) {
            Key(Array(2 * range + 1) {
                if (centralCoordinate.y == -1) {
                    if (it == 2 * range) IntArray(parameters.width) { -1 }
                    else state.getPredecessor(it * parameters.period + parameters.dy - 1)!!.cells
                } else {
                    if (-(centralCoordinate.y + 1) * parameters.period - parameters.dy >= 0) {
                        if (it == 2 * range) {
                            if (-(centralCoordinate.y + 1) * parameters.period - parameters.dy == 0)
                                IntArray(parameters.width) { -1 }
                            else state.getPredecessor(-(centralCoordinate.y + 1) *
                                    parameters.period - parameters.dy - 1)!!.cells
                        }
                        else if (it == 0) IntArray(parameters.width) { -1 }
                        else state.getPredecessor(it * parameters.period - 1)!!.cells
                    } else {
                        if (it == 2 * range) IntArray(parameters.width) { -1 }
                        else state.getPredecessor(-(-centralCoordinate.y - it - 1) * parameters.period +
                                parameters.dy - 1)!!.cells
                    }
                }
            }, parameters, centralCoordinate, beyondCentralCell)
        } else key
        val prevState = state.getPredecessor(parameters.dy - 1)

        val fill0 = key2.key[0][0] == -1
        val fillSuccessor = key2.key[2 * range][0] == -1
        val generation = (state.depth % parameters.period) % parameters.rule.alternatingPeriod

        val successors: ArrayList<State> = ArrayList()
        val dfsStack: ArrayList<Node> = ArrayList(30)
        dfsStack.addAll(lookupTable2[key.lookupHash()])
        //dfsStack.add(Node(null, 0, parameters.rule.numStates, baseCellCount))

        // Cache optimisation
        val cache = Array(parameters.width + 1) {
            IntArray(pow(parameters.rule.numStates, baseCellCount - 1)) { -1 }
        }

        // Latest cell that must change for lookahead to give a different output
        var latestCell = -1
        var latestNode = Node(null, 0, 2, 1)

        var hash: Int
        var node: Node
        var neighbours: IntArray
        while (dfsStack.isNotEmpty()) {
            node = dfsStack.removeLast()
            if (latestCell != -1 && node.depth > latestCell) {
                if (node.depth > parameters.lookupTableWidth) continue
                else if (node.depth - latestCell < 0) continue
                else if (node.getPredecessor(node.depth - latestCell) == latestNode) continue
            }

            if (node.depth == parameters.width) {
                // Check boundary conditions
                var valid = true
                for (i in parameters.width until parameters.width + baseCellCount - 1) {
                    neighbours = getNeighbours(key, node, i, generation, parameters.symmetry, fillUnknown = true)

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
                                i, generation, parameters.symmetry)
                    }

                    // Convert based on the background
                    cellState = parameters.rule.convertState(cellState, generation)
                    nextState = parameters.rule.convertState(nextState, generation + 1)

                    if (nextState != parameters.rule.transitionFunc(neighbours, cellState, generation,
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
                if (fill0) key2.key[0] = newState.cells
                if (fillSuccessor) key2.key[2 * range] = newState.cells

                if (parameters.lookahead) {
                    latestCell = lookahead(key2, newState, fillSuccessor)

                    if (latestCell == -1) successors.add(newState)
                    else {
                        latestNode = if (node.depth - latestCell < 0)
                            Node(null, 0, parameters.rule.numStates, baseCellCount)
                        else node.getPredecessor(node.depth - latestCell)!!
                    }
                }
            } else {
                latestCell = -1

                // Checking for the special optimisation
                if (cache[node.depth][node.actualHash] != -1) {
                    val output = cache[node.depth][node.actualHash]
                    for (i in possibleSuccessor[prevState!!.cells[node.depth]]) {
                        // Getting i th bit from the output
                        if (output shr i and 1 == 1) {
                            dfsStack.add(Node(
                                node,
                                parameters.rule.convertState(i, generation),
                                parameters.rule.numStates,
                                baseCellCount
                            ))
                        }
                    }

                    continue
                }

                // Compute possible next nodes
                var cellState: Int
                var nextState: Int
                if (node.depth + centralCoordinate.x >= 0) {
                    cellState = key.key[-centralCoordinate.y - 1][node.depth + centralCoordinate.x]
                    nextState = key.key[2 * range][node.depth + centralCoordinate.x]
                    if (nextState == -1)
                        nextState = getNeighbour(Coordinate(centralCoordinate.x, 0), key, node,
                            node.depth, generation, parameters.symmetry)
                } else {
                    cellState = 0
                    nextState = 0
                }

                // Convert based on the background
                cellState = parameters.rule.convertState(cellState, generation)
                nextState = parameters.rule.convertState(nextState, generation + 1)

                // Obtain the next node
                val possibleNextState = parameters.rule.dependsOnNeighbours(cellState, generation, Coordinate())
                if (possibleNextState != -1) {
                    if (possibleNextState == nextState) {
                        // Creating new node & adding to stack
                        for (i in possibleSuccessor[prevState!!.cells[node.depth]]) {
                            if (node.depth == 0) {
                                var valid = true

                                // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                for (j in extraBoundaryConditions.indices) {
                                    if (extraBoundaryConditions[j] == 0) continue
                                    if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                            Node(node, i, parameters.rule.numStates, baseCellCount), generation,
                                            parameters.symmetry)) {
                                        valid = false  // Boundary condition not satisfied
                                        break
                                    }
                                }

                                if (valid) dfsStack.add(Node(
                                    node,
                                    parameters.rule.convertState(i, generation),
                                    parameters.rule.numStates,
                                    baseCellCount
                                ))
                            } else dfsStack.add(Node(
                                node,
                                parameters.rule.convertState(i, generation),
                                parameters.rule.numStates,
                                baseCellCount
                            ))
                        }
                    } else if (node.predecessor != null)
                        cache[node.depth - 1][node.predecessor!!.actualHash] -= pow(2, node.cellState)
                } else {
                    hash = getHash(key, node, node.depth, generation, parameters.symmetry)
                    hash += cellState * pow(parameters.rule.numStates, effectiveNeighbourhood.size)
                    if (nextState != -1)
                        hash += nextState * pow(parameters.rule.numStates, effectiveNeighbourhood.size - 1)

                    // Adding to special optimisation cache
                    val output = lookupTable[generation][hash]
                    cache[node.depth][node.actualHash] = output

                    // Creating new node & adding to stack
                    var deadEnd = true
                    for (i in possibleSuccessor[prevState!!.cells[node.depth]]) {
                        // Getting i th bit from the output
                        if (output shr i and 1 == 1) {
                            if (node.depth == 0) {
                                var valid = true

                                // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                for (j in extraBoundaryConditions.indices) {
                                    if (extraBoundaryConditions[j] == 0) continue
                                    if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                            Node(node, i, parameters.rule.numStates, baseCellCount), generation,
                                            parameters.symmetry)) {
                                        valid = false  // Boundary condition not satisfied
                                        cache[node.depth][node.actualHash] -= pow(2, i)  // Remove from cache
                                        break
                                    }
                                }

                                if (valid) {
                                    dfsStack.add(Node(
                                        node,
                                        parameters.rule.convertState(i, generation),
                                        parameters.rule.numStates,
                                        baseCellCount
                                    ))
                                    deadEnd = false
                                }
                            } else {
                                dfsStack.add(Node(
                                    node,
                                    parameters.rule.convertState(i, generation),
                                    parameters.rule.numStates,
                                    baseCellCount
                                ))
                                deadEnd = false
                            }
                        }
                    }

                    if (deadEnd) cache[node.depth - 1][node.predecessor!!.actualHash] -= pow(2, node.cellState)
                }
            }
        }

        return successors
    }

    private fun lookahead(key: Key, state: State, isSuccessor: Boolean): Int {
        val generation = (state.depth % parameters.period) % parameters.rule.alternatingPeriod

        // Initialise the DFS stack
        val dfsStack: ArrayList<Node> = ArrayList(30)
        dfsStack.addAll(lookupTable3[key.lookupHash()])
        //dfsStack.add(Node(null, 0, parameters.rule.numStates, baseCellCount))

        var node: Node

        // Cache optimisation
        val cache = Array(parameters.width + 1) {
            BooleanArray(pow(parameters.rule.numStates, baseCellCount - 1)) { false }
        }

        // Take note of the latest cell that must change for the outcome to be different
        var latestCell = dfsStack.removeLast().cellState
        val fromDepth = { x: Int ->
            if (isSuccessor) x
            else x + additionalCells
        }

        var hash: Int
        var neighbours: IntArray
        while (dfsStack.isNotEmpty()) {
            node = dfsStack.removeLast()
            if (cache[node.depth][node.actualHash]) continue
            else cache[node.depth][node.actualHash] = true

            if (node.depth == parameters.width) {
                // Check boundary conditions
                var valid = true
                for (i in parameters.width until parameters.width + baseCellCount - 1) {
                    neighbours = getNeighbours(key, node, i, generation, parameters.symmetry, fillUnknown = true)

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

                    // Convert based on the background
                    cellState = parameters.rule.convertState(cellState, generation)
                    nextState = parameters.rule.convertState(nextState, generation + 1)

                    if (nextState != parameters.rule.transitionFunc(neighbours, cellState, generation,
                            Coordinate())) {
                        valid = false
                        latestCell = latestCell.coerceAtLeast(fromDepth(i))
                        break  // State is invalid
                    }
                }

                if (!valid) continue // State is invalid
                return -1
            } else {
                // Compute possible next nodes
                var cellState: Int
                var nextState: Int
                if (node.depth + centralCoordinate.x >= 0) {
                    cellState = key.key[-centralCoordinate.y - 1][node.depth + centralCoordinate.x]
                    nextState = if (key.key[2 * range][node.depth + centralCoordinate.x] == -1)
                        state.cells[node.depth + centralCoordinate.x]
                    else key.key[2 * range][node.depth + centralCoordinate.x]
                } else {
                    cellState = 0
                    nextState = 0
                }

                // Convert based on the background
                cellState = parameters.rule.convertState(cellState, generation)
                nextState = parameters.rule.convertState(nextState, generation + 1)

                // Obtain the next node
                val possibleNextState = parameters.rule.dependsOnNeighbours(cellState, generation, Coordinate())
                if (possibleNextState != -1) {
                    if (possibleNextState == nextState) {
                        // Creating new node & adding to stack
                        var deadEnd = true
                        for (i in 0 until parameters.rule.numStates) {
                            if (node.depth == 0) {
                                var valid = true

                                // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                for (j in extraBoundaryConditions.indices) {
                                    if (extraBoundaryConditions[j] == 0) continue
                                    if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                            Node(node, i, parameters.rule.numStates, baseCellCount), generation,
                                            parameters.symmetry)) {
                                        valid = false  // Boundary condition not satisfied
                                        break
                                    }
                                }

                                if (valid) {
                                    dfsStack.add(Node(
                                        node,
                                        parameters.rule.convertState(i, generation),
                                        parameters.rule.numStates,
                                        baseCellCount
                                    ))
                                    deadEnd = false
                                }
                            } else {
                                dfsStack.add(Node(
                                    node,
                                    parameters.rule.convertState(i, generation),
                                    parameters.rule.numStates,
                                    baseCellCount
                                ))
                                deadEnd = false
                            }
                        }

                        if (deadEnd) latestCell = latestCell.coerceAtLeast(fromDepth(node.depth))
                    } else latestCell = latestCell.coerceAtLeast(fromDepth(node.depth))
                } else {
                    hash = getHash(key, node, node.depth, generation, parameters.symmetry)
                    hash += cellState * pow(parameters.rule.numStates, effectiveNeighbourhood.size)
                    if (nextState != -1)
                        hash += nextState * pow(parameters.rule.numStates, effectiveNeighbourhood.size - 1)

                    // Getting successor states
                    val output = lookupTable[generation][hash]
                    if (output == 0) latestCell = latestCell.coerceAtLeast(fromDepth(node.depth))

                    // Creating new node & adding to stack
                    var deadEnd = true
                    for (i in 0 until parameters.rule.numStates) {
                        // Getting i th bit from the output
                        if (output shr i and 1 == 1) {
                            if (node.depth == 0) {
                                var valid = true

                                // Enforcing some extra boundary conditions for neighbourhoods such as von neumann
                                for (j in extraBoundaryConditions.indices) {
                                    if (extraBoundaryConditions[j] == 0) continue
                                    if (!extraBoundaryCondition(-j - 2, -extraBoundaryConditions[j], key,
                                            Node(node, i, parameters.rule.numStates, baseCellCount), generation,
                                            parameters.symmetry)) {
                                        valid = false  // Boundary condition not satisfied
                                        break
                                    }
                                }

                                if (valid) {
                                    dfsStack.add(Node(
                                        node,
                                        parameters.rule.convertState(i, generation),
                                        parameters.rule.numStates,
                                        baseCellCount
                                    ))
                                    deadEnd = false
                                }
                            } else {
                                dfsStack.add(Node(
                                    node,
                                    parameters.rule.convertState(i, generation),
                                    parameters.rule.numStates,
                                    baseCellCount
                                ))
                                deadEnd = false
                            }
                        }
                    }

                    // Check for a dead end
                    if (deadEnd) latestCell.coerceAtLeast(fromDepth(node.depth))
                }
            }
        }

        return latestCell
    }

    private fun extraBoundaryCondition(dx: Int, dy: Int, key: Key, node: Node, generation: Int,
                                       symmetry: Symmetry): Boolean {
        val currentState = parameters.rule.convertState(0, generation)
        val nextState = parameters.rule.convertState(0,  generation + 1)

        val possibleNextState = parameters.rule.dependsOnNeighbours(currentState, generation, Coordinate())
        if (possibleNextState == nextState) {
            return true
        } else {
            val neighbours = IntArray(effectiveNeighbourhood.size) {
                getNeighbour(
                    effectiveNeighbourhood[it].add(Coordinate(dx, dy)), key, node, node.depth,
                    generation, symmetry, true
                )
            }

            if (nextState == parameters.rule.transitionFunc(neighbours, currentState, generation, Coordinate())) {
                return true
            }
        }

        return false
    }

    private fun getHash(key: Key, node: Node, depth: Int, generation: Int, symmetry: Symmetry): Int {
        var hash = 0
        var state: Int
        effectiveNeighbourhood.forEachIndexed { index, coordinate ->
            //println("$coordinate $depth")
            state = getNeighbour(coordinate, key, node, depth, generation, symmetry, false)
            if (state != -1)
                hash += state * pow(parameters.rule.numStates, index - if (index > indexOfUnknown) 1 else 0)
        }

        return hash
    }

    private fun getNeighbours(key: Key, node: Node, depth: Int, generation: Int,
                              symmetry: Symmetry, fillUnknown: Boolean = false): IntArray {
        return IntArray(effectiveNeighbourhood.size) {
            getNeighbour(effectiveNeighbourhood[it], key, node, depth, generation, symmetry, fillUnknown)
        }
    }

    private fun getNeighbour(coordinate: Coordinate, key: Key, node: Node, depth: Int,
                             generation: Int, symmetry: Symmetry, fillUnknown: Boolean = false): Int {
        return parameters.rule.convertState(when {
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
        }, generation)
    }
}

class Node(val predecessor: Node?, val cellState: Int, val numStates: Int, baseCellCount: Int) {
    var depth = 0
    val hash: Int
    var actualHash = 0
        private set

    init {
        if (predecessor != null) {
            depth = predecessor.depth + 1

            hash = predecessor.hash + cellState * pow(numStates, depth)
            var predecessor: Node = this
            for (i in 0 until baseCellCount - 1) {
                actualHash += predecessor.cellState * pow(numStates, i)
                if (predecessor.predecessor != null) {
                    predecessor = predecessor.predecessor!!
                } else break
            }
        } else {
            hash = 0
            actualHash = 0
        }
    }

    fun getPredecessor(n: Int): Node? {
        if (n == 0) return this
        if (n == 1) return predecessor
        return predecessor?.getPredecessor(n - 1)
    }

    override fun hashCode(): Int {
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (depth != other.depth) return false
        if (hash != other.hash) return false

        return true
    }
}

class Key(val key: Array<IntArray>, val parameters: ShipSearchParameters,
          val centralCoordinate: Coordinate, val beyondCentralCell: IntArray) {
    fun lookupHash(): Int {
        var hash = 0
        var index = 0
        for (i in key.indices) {
            for (j in 0 until parameters.lookupTableWidth + beyondCentralCell.maxOrNull()!!) {
                if (i < key.size - 1 && j < parameters.lookupTableWidth +
                    beyondCentralCell[beyondCentralCell.size - i - 2])
                    hash += key[i][j] * pow(parameters.rule.numStates, index++)
                else if (i == key.size - 1 && j < parameters.lookupTableWidth + centralCoordinate.x)
                    hash += key[i][j] * pow(parameters.rule.numStates, index++)
                else break
            }
        }

        return hash
    }

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

private fun pow(a: Int, p: Int): Int {
    var res = 1
    val i1 = 31 - Integer.numberOfLeadingZeros(p) // highest bit index
    for (i in i1 downTo 0) {
        res *= res
        if (p and (1 shl i) > 0) res *= a
    }
    return res
}