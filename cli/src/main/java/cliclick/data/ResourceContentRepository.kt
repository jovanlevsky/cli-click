package cliclick.data

import cliclick.model.Building
import cliclick.model.Upgrade

class ResourceContentRepository : ContentRepository {

    private fun readLines(path: String): List<String> {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalStateException("Missing resource: $path")
        return stream.bufferedReader().readLines()
    }

    override fun loadUpgrades(): List<Upgrade> {
        val costs = readLines("upgradeutils/cost.txt")
        val amounts = readLines("upgradeutils/amount.txt")
        val names = readLines("upgradeutils/names.txt")
        val tiers = readLines("upgradeutils/tier.txt")
        val types = readLines("upgradeutils/type.txt")
        val descriptions = readLines("upgradeutils/descriptions.txt")
        return costs.indices.map { i ->
            Upgrade(
                name = names[i],
                type = types[i],
                amount = amounts[i].toDouble(),
                tier = tiers[i].toInt(),
                cost = costs[i].toDouble(),
                description = descriptions[i]
            )
        }
    }

    override fun loadBuildings(): List<Building> {
        val costs = readLines("buildingutils/cost.txt")
        val amounts = readLines("buildingutils/amount.txt")
        val names = readLines("buildingutils/names.txt")
        val tiers = readLines("buildingutils/tier.txt")
        val descriptions = readLines("buildingutils/descriptions.txt")
        val scalings = readLines("buildingutils/scaling.txt")
        return costs.indices.map { i ->
            Building(
                name = names[i],
                amount = amounts[i].toDouble(),
                tier = tiers[i].toInt(),
                cost = costs[i].toDouble(),
                description = descriptions[i],
                scaling = scalings[i].toDouble(),
                num = 0
            )
        }
    }
}
