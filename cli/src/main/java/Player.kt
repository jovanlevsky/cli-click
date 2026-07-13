class Player(val name: String, val game: Game) {
    private fun readResourceLines(path: String): List<String> {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalStateException("Missing resource: $path")
        return stream.bufferedReader().readLines()
    }
    public var currUpgrade: Int = -1
    fun increment() {
        game.stats.blah += game.stats.blahPA
    }
    fun openBuildingsMenu() {

    }
    fun upgrade() {
        if (game.stats.blah >= allUpgrades.get(currUpgrade + 1).cost) {
            currUpgrade += 1
            val upg = allUpgrades.get(currUpgrade)
            if (upg.type.equals("flat")) {
                game.stats.baseBlahPA += upg.amount
            }
            if (upg.type.equals("mult")) {
                game.stats.multiplier *= upg.amount
            }
            game.stats.blahPA = game.stats.baseBlahPA * game.stats.multiplier
            game.stats.blahPS = game.stats.baseBlahPS * game.stats.multiplier
            game.stats.blah -= upg.cost
        }
    }
    fun buyBuilding(building: Game.Building) {
        if (game.stats.blah >= building.cost) {
            game.stats.blah -= building.cost
            building.num += 1
            building.cost *= building.scaling
            game.stats.baseBlahPS += building.amount
            game.stats.blahPS = game.stats.baseBlahPS * game.stats.multiplier
        } else { return }
    }
    public var allUpgrades = ArrayList<Game.Upgrade>()
    fun init(): ArrayList<Game.Upgrade> {
        val costs = readResourceLines("upgradeutils/cost.txt")
        val amounts = readResourceLines("upgradeutils/amount.txt")
        val names = readResourceLines("upgradeutils/names.txt")
        val tiers = readResourceLines("upgradeutils/tier.txt")
        val types = readResourceLines("upgradeutils/type.txt")
        val descs = readResourceLines("upgradeutils/descriptions.txt")
        val upgradeAmt = costs.size
        for (i in 0 until upgradeAmt) {
            allUpgrades.add(
                Game.Upgrade(
                    names.elementAt(i),
                    types.elementAt(i),
                    amounts.elementAt(i).toDouble(),
                    tiers.elementAt(i).toInt(),
                    costs.elementAt(i).toDouble(),
                    descs.elementAt(i)
                ))
        }
        return allUpgrades
    }
    public val allBuildings = ArrayList<Game.Building>()
    fun init2(): ArrayList<Game.Building> {
        val bcosts = readResourceLines("buildingutils/cost.txt")
        val bamounts = readResourceLines("buildingutils/amount.txt")
        val bnames = readResourceLines("buildingutils/names.txt")
        val btiers = readResourceLines("buildingutils/tier.txt")
        val bdescs = readResourceLines("buildingutils/descriptions.txt")
        val bscaling = readResourceLines("buildingutils/scaling.txt")
        val buildingAmt = bcosts.size
        for (i in 0 until buildingAmt) {
            allBuildings.add(
                Game.Building(
                    bnames.elementAt(i),
                    bamounts.elementAt(i).toDouble(),
                    btiers.elementAt(i).toInt(),
                    bcosts.elementAt(i).toDouble(),
                    bdescs.elementAt(i),
                    bscaling.elementAt(i).toDouble(),
                    0
                ))
        }

        return allBuildings
    }
}
