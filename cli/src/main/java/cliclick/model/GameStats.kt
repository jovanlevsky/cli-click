package cliclick.model

/**
 * Mutable bag of live game numbers. Pure state: no behavior, no I/O.
 * (SRP: holding the numbers is this class's only job; the math that changes
 * them lives in cliclick.engine.GameEngine.)
 */
data class GameStats(
    var blah: Double = 0.0,
    var blahPS: Double = 0.0,
    var baseBlahPS: Double = 0.0,
    var blahPA: Double = 1.0,
    var baseBlahPA: Double = 1.0,
    var highestTier: Int = 1,
    var multiplier: Double = 1.0,
    var timePlayed: Double = 0.0
)
