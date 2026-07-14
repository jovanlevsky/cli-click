package cliclick.engine

import cliclick.model.Building

/**
 * Write-side of the game (ISP counterpart of GameQueries). Method names and
 * math match the original Player class one-to-one.
 */
interface GameActions {
    /** Manual click: inputs += inputsPA. */
    fun increment()
    /** Buy the next upgrade if affordable (flat -> baseInputsPA, mult -> multiplier). */
    fun upgrade()
    /** Buy one building if affordable; scales its cost and raises baseInputsPS. */
    fun buyBuilding(building: Building)
    /** Passive income: inputs += inputsPS * deltaSeconds; timePlayed += deltaSeconds. */
    fun tick(deltaSeconds: Double)
}
