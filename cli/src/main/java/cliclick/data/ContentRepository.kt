package cliclick.data

import cliclick.model.Building
import cliclick.model.Upgrade

/**
 * Source of game content (DIP: the engine depends on this abstraction, not on
 * where the data comes from; OCP: add JSON/network/save-file sources by adding
 * implementations, without touching the engine).
 */
interface ContentRepository {
    fun loadUpgrades(): List<Upgrade>
    fun loadBuildings(): List<Building>
}
