package application.model.search.cfind

enum class Symmetry(val index: Int) {
    ASYMMETRIC(0),
    EVEN_SYMMETRIC(1),
    ODD_SYMMETRIC(2),
    GLIDE_SYMMETRIC(3);

    override fun toString(): String {
        return when (index) {
            0 -> "asymmetric"
            1 -> "even-symmetric"
            2 -> "odd-symmetric"
            else -> "glide-symmetric"
        }
    }
}