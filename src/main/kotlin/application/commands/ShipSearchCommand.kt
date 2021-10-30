package application.commands

import application.model.Utils
import application.model.rules.hrot.HROT
import application.model.search.cfind.ShipSearch
import application.model.search.cfind.ShipSearchParameters
import application.model.search.cfind.Symmetry
import application.model.search.rulesrc.RuleSearch
import application.model.search.rulesrc.RuleSearchParameters
import application.model.simulation.Simulator
import javafx.application.Application
import picocli.CommandLine
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.pow

@CommandLine.Command(name = "cfind", description = ["Uses a gfind-like algorithm to find spaceships"])
class ShipSearchCommand : Runnable {
    @CommandLine.Option(
        names = ["-p", "--period"],
        description = ["Period of the spaceship"],
        required = true
    )
    private var period = 2

    @CommandLine.Option(
        names = ["-k"],
        description = ["Displacement of the spaceship"],
        required = true
    )
    private var dy = 1

    @CommandLine.Option(
        names = ["-w", "--width"],
        description = ["Width of the spaceship"],
        required = true
    )
    private var width = 5

    @CommandLine.Option(
        names = ["-r", "--rule"],
        description = ["The rule in which to search for the spaceship"],
        required = true
    )
    private var rulestring = ""

    @CommandLine.Option(
        names = ["-s", "--symmetry"],
        description = ["Symmetry of the spaceship (0 - asymmetric, 1 - even-symmetric, 2 - odd-symmetric)"],
        required = true
    )
    private var symmetryId = 0

    @CommandLine.Option(
        names = ["-n", "--num"],
        description = ["Number of ships to find before terminating"],
        defaultValue = "100"
    )
    private var num = 0

    @CommandLine.Option(
        names = ["-m", "--min"],
        description = ["The minimum deepening increment"],
        defaultValue = "0"
    )
    private var minDeepeningIncrement = 0

    @CommandLine.Option(
        names = ["-q", "--queue"],
        description = ["The maximum size of the BFS queue (2^Q, default: 2^20)"],
        defaultValue = "20"
    )
    private var maxQueueSize = 0

    @CommandLine.Option(
        names = ["-l", "--lookup"],
        description = ["The width of the lookup table (default: 5, " +
                "larger values results in faster search but consume more memory)"],
        defaultValue = "4"
    )
    private var lookupTableSize = 0

    @CommandLine.Option(
        names = ["-dfs", "--dfs"],
        description = ["Use only DFS instead of a hybrid BFS-DFS approach"]
    )
    private var dfs = false

    @CommandLine.Option(
        names = ["-stdin", "--stdin"],
        description = ["Outputs all partials"]
    )
    private var stdin = false

    @CommandLine.Option(
        names = ["-no_partial", "--no_partial"],
        description = ["Suppress output of partials"]
    )
    private var partial = false

    @CommandLine.Option(
        names = ["-rng", "--random"],
        description = ["Applies a randomised search order. Are you feeling lucky?"]
    )
    private var rng = false

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private var help = false

    override fun run() {
        val symmetry = when (symmetryId) {
            0 -> Symmetry.ASYMMETRIC
            1 -> Symmetry.EVEN_SYMMETRIC
            else -> Symmetry.ODD_SYMMETRIC
        }

        val shipSearchParameters = ShipSearchParameters(Utils.fromRulestring(rulestring), width, dy, period,
            symmetry, 2.0.pow(maxQueueSize).toLong(),
            if (minDeepeningIncrement == 0) period else minDeepeningIncrement, lookupTableSize,
            true, stdin, partial, dfs, rng)

        val shipSearch = ShipSearch(shipSearchParameters)
        shipSearch.search(num)
    }
}