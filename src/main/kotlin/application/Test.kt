package application

import application.model.rules.hrot.HROT
import application.model.rules.isotropic.rules.INTGenerations
import application.model.search.cfind.ShipSearch
import application.model.search.cfind.ShipSearchParameters
import application.model.search.cfind.Symmetry

object Test {
    @JvmStatic
    fun main(args: Array<String>) {
        //val shipSearchParameters = ShipSearchParameters(INTGenerations("23/3/3"), 9, 1, 3,
        //    symmetry = Symmetry.EVEN_SYMMETRIC)
        //val shipSearchParameters = ShipSearchParameters(HROT("B267/S035"), 8, 1, 3,
        //    symmetry = Symmetry.EVEN_SYMMETRIC)
        //val shipSearchParameters = ShipSearchParameters(INTGenerations("0/2/3"), 8, 1, 2)
        //val shipSearchParameters = ShipSearchParameters(INTGenerations("237/3/3"), 8, 1, 3)
        //val shipSearchParameters = ShipSearchParameters(HROT("B3567/S3568"), 6, 1, 4,
        //    symmetry = Symmetry.ODD_SYMMETRIC)
        //val shipSearchParameters = ShipSearchParameters(INTGenerations("12/34/3"), 5, 1, 5)
        val shipSearchParameters = ShipSearchParameters(HROT("R2,C2,S5-9,B7-8,NM"), 8, 1, 2,
            symmetry = Symmetry.EVEN_SYMMETRIC)

        val shipSearch = ShipSearch(shipSearchParameters)
        shipSearch.search(1)
    }
}