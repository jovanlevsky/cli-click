package cliclick.model

/**
 * Mutable bag of live game numbers. Pure state: no behavior, no I/O.
 * (SRP: holding the numbers is this class's only job; the math that changes
 * them lives in cliclick.engine.GameEngine.)
 */
data class GameStats(
    var inputs: Double = 0.0,
    var inputsPS: Double = 0.0,
    var baseInputsPS: Double = 0.0,
    var inputsPA: Double = 1.0,
    var baseInputsPA: Double = 1.0,
    var highestTier: Int = 1,
    var multiplier: Double = 1.0,
    var timePlayed: Double = 0.0
)
