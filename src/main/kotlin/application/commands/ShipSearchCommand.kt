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
        names = ["-stdin", "--stdin"],
        description = ["Outputs all partials"]
    )
    private var stdin = false

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private var help = false

    override fun run() {
        val symmetry = when (symmetryId) {
            0 -> Symmetry.ASYMMETRIC
            1 -> Symmetry.EVEN_SYMMETRIC
            else -> Symmetry.ODD_SYMMETRIC
        }

        val shipSearchParameters = ShipSearchParameters(Utils.fromRulestring(rulestring), width, dy, period,
            symmetry = symmetry, stdin = stdin)
        val shipSearch = ShipSearch(shipSearchParameters)
        shipSearch.search(num)
    }
}