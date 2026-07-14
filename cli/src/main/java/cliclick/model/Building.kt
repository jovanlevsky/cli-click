package cliclick.model

/**
 * A purchasable building. cost/num are mutable because buying a building
 * raises its price (cost *= scaling) and its owned count.
 */
data class Building(
    val name: String,
    val amount: Double,
    val tier: Int,
    var cost: Double,
    val description: String,
    val scaling: Double,
    var num: Int
)
