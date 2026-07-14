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
        stats.inputs += stats.inputsPA
    }

    override fun upgrade() {
        val next = nextUpgrade ?: return
        if (stats.inputs >= next.cost) {
            currentUpgradeIndex += 1
            if (next.type == "flat") {
                stats.baseInputsPA += next.amount
            }
            if (next.type == "mult") {
                stats.multiplier *= next.amount
            }
            stats.inputsPA = stats.baseInputsPA * stats.multiplier
            stats.inputsPS = stats.baseInputsPS * stats.multiplier
            stats.inputs -= next.cost
        }
    }

    override fun buyBuilding(building: Building) {
        if (stats.inputs >= building.cost) {
            stats.inputs -= building.cost
            building.num += 1
            building.cost *= building.scaling
            stats.baseInputsPS += building.amount
            stats.inputsPS = stats.baseInputsPS * stats.multiplier
        }
    }

    override fun tick(deltaSeconds: Double) {
        stats.inputs += stats.inputsPS * deltaSeconds
        stats.timePlayed += deltaSeconds
    }
}
