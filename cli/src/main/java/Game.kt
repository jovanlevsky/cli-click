

class Game {
    data class GameStats(
        var blah: Double = 0.0,
        var blahPS: Double = 0.0,
        var baseBlahPS: Double = 0.0,
        var blahPA: Double = 1.0,
        var baseBlahPA: Double = 1.0,
        var upgrades: ArrayList<Upgrade> = arrayListOf(),
        var highestTier: Int = 1,
        var multiplier: Double = 1.0,
        var timePlayed: Double = 0.0
    )
    data class Upgrade(
        val name: String,
        val type: String,
        val amount: Double,
        val tier: Int,
        val cost: Double,
        val description: String,
    )
    data class Building(
        val name: String,
        val amount: Double,
        val tier: Int,
        var cost: Double,
        val description: String,
        val scaling: Double,
        var num: Int
    )

    val stats = GameStats()
}
