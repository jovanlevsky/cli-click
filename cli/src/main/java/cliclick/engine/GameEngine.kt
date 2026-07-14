package cliclick.engine

import cliclick.data.ContentRepository
import cliclick.model.Building
import cliclick.model.GameStats
import cliclick.model.Upgrade

/**
 * All game math in one place, behind the GameQueries/GameActions interfaces
 * (DIP: constructed from a ContentRepository abstraction; SRP: no I/O, no UI).
 * The formulas are copied unchanged from the original Player class.
 */
class GameEngine(repository: ContentRepository) : GameQueries, GameActions {

    override val stats = GameStats()
    override val buildings: List<Building> = repository.loadBuildings()

    private val allUpgrades: List<Upgrade> = repository.loadUpgrades()
    private var currentUpgradeIndex = -1

    override val nextUpgrade: Upgrade?
        get() = allUpgrades.getOrNull(currentUpgradeIndex + 1)

    override fun increment() {
        stats.blah += stats.blahPA
    }

    override fun upgrade() {
        val next = nextUpgrade ?: return
        if (stats.blah >= next.cost) {
            currentUpgradeIndex += 1
            if (next.type == "flat") {
                stats.baseBlahPA += next.amount
            }
            if (next.type == "mult") {
                stats.multiplier *= next.amount
            }
            stats.blahPA = stats.baseBlahPA * stats.multiplier
            stats.blahPS = stats.baseBlahPS * stats.multiplier
            stats.blah -= next.cost
        }
    }

    override fun buyBuilding(building: Building) {
        if (stats.blah >= building.cost) {
            stats.blah -= building.cost
            building.num += 1
            building.cost *= building.scaling
            stats.baseBlahPS += building.amount
            stats.blahPS = stats.baseBlahPS * stats.multiplier
        }
    }

    override fun tick(deltaSeconds: Double) {
        stats.blah += stats.blahPS * deltaSeconds
        stats.timePlayed += deltaSeconds
    }
}
