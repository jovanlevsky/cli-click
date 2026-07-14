package cliclick.model

/** Immutable description of a purchasable upgrade. */
data class Upgrade(
    val name: String,
    val type: String,
    val amount: Double,
    val tier: Int,
    val cost: Double,
    val description: String
)
