package cliclick.engine

import cliclick.model.Building
import cliclick.model.GameStats
import cliclick.model.Upgrade

/**
 * Read-side of the game (ISP: views that only display state depend on this,
 * not on the mutation API).
 */
interface GameQueries {
    val stats: GameStats
    val buildings: List<Building>
    /** The next upgrade the player can buy, or null when all are bought. */
    val nextUpgrade: Upgrade?
}
