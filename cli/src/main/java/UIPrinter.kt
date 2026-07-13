import com.googlecode.lanterna.TextColor
import Game
import java.util.Scanner
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import kotlinx.coroutines.*
import org.w3c.dom.Text
class UIPrinter {
    fun printUI(screen: Screen, player: Player, selectedIndex: Int, actions: List<String>) {
        val stats = player.game.stats
        screen.clear()
        val graphics = screen.newTextGraphics()
//        graphics.backgroundColor = TextColor.RGB(0, 86, 150)
        graphics.foregroundColor = TextColor.ANSI.WHITE
        graphics.putString(0, 0, "${player.name}'s game")
        graphics.putString(0, 1, "+--------------------------------+")
        graphics.putString(0, 2, " Blah: ${stats.blah.toInt()} ")
        graphics.putString(0, 3, " BPS: ${stats.blahPS}              ")
        graphics.putString(0, 4, " Blah per click: ${stats.blahPA}     ")
        graphics.putString(0, 5, " Multiplier: ${stats.multiplier}   ")
        graphics.putString(0, 6, "+--------------------------------+")

        var x = 0

        actions.forEachIndexed { index, action ->

            val text = if (index == selectedIndex) {
                "[ $action ]"
            } else " $action "
            graphics.putString(x, 7, text)
            x += text.length + 1

        }
        var infoText = ""
        if (actions.get(selectedIndex).equals("Click")) {
            infoText = "Press Enter to Increment Blahs"
        } else if (actions.get(selectedIndex).equals("Upgrade")) {
            infoText += player.allUpgrades.get(player.currUpgrade + 1).name + "\n"
            infoText += "Cost: " + player.allUpgrades.get(player.currUpgrade + 1).cost.toString() + "\n"
            infoText += "Increase: " + player.allUpgrades.get(player.currUpgrade + 1).amount.toString() + "\n"
            infoText += "Description: " +  player.allUpgrades.get(player.currUpgrade + 1).description + "\n"
            infoText += "Tier: " + player.allUpgrades.get(player.currUpgrade + 1).tier.toString() + "\n"
            infoText += "Type: " + player.allUpgrades.get(player.currUpgrade + 1).type
        }
        else {infoText = ""}
        infoText.split("\n").forEachIndexed { i, line ->
            graphics.putString(0, 8 + i, line)
        }
        screen.refresh()
    }
    fun printBuildingsUI(screen: Screen, player: Player, selectedIndex: Int, actions: List<String>) {
        screen.clear()
        val graphics = screen.newTextGraphics()
        graphics.foregroundColor = TextColor.ANSI.WHITE
        graphics.putString(0, 0, "${player.name}'s buildings, and current blah is: ${player.game.stats.blah.toInt()}")

        val buildings = player.allBuildings
        val linesPerBuilding = 7
        val headerRows = 1

        // How many full building blocks fit on screen at once.
        val terminalRows = screen.terminalSize.rows
        val visibleCount = maxOf(1, (terminalRows - headerRows) / linesPerBuilding)

        // Keep the selected building scrolled into view.
        var scrollOffset = 0
        if (selectedIndex >= scrollOffset + visibleCount) scrollOffset = selectedIndex - visibleCount + 1
        if (selectedIndex < scrollOffset) scrollOffset = selectedIndex
        scrollOffset = scrollOffset.coerceIn(0, maxOf(0, buildings.size - visibleCount))

        var y = headerRows
        val endIndex = minOf(buildings.size, scrollOffset + visibleCount)
        for (i in scrollOffset until endIndex) {
            val building = buildings.get(i)
            val lines = listOf(
                "+------------------------+",
                "${building.name} (x${building.num})",
                "Cost: ${building.cost}",
                "BPS: ${building.amount}",
                building.description,
                "Tier: ${building.tier}",
                "+------------------------+"
            )
            val width = lines.maxOf { it.length }
            lines.forEachIndexed { e, line ->
                graphics.foregroundColor = TextColor.ANSI.WHITE
                graphics.putString(0, y + e, line)
                if (i == selectedIndex) {
                    graphics.foregroundColor = TextColor.ANSI.BLUE
                    graphics.putString(width + 1, y + e, "|")
                }
            }
            y += lines.size
        }
        graphics.foregroundColor = TextColor.ANSI.WHITE
        screen.refresh()
    }
    fun runBuildingsMenu(screen: Screen, player: Player): Menu {
        var selectedIndex = 0
        val actions = player.allBuildings.map { it.name } + "Return"
        var running = true
        var nextMenu = Menu.GAME
        while (running) {
            printBuildingsUI(screen, player, selectedIndex, actions)

            var key = screen.pollInput()
            while (key != null) {
                when (key.keyType) {
                    KeyType.ArrowDown -> {
                        selectedIndex++
                        if (selectedIndex > actions.lastIndex) {
                            selectedIndex = 0
                        }
                    }
                    KeyType.ArrowUp -> {
                        selectedIndex--
                        if (selectedIndex < 0) {
                            selectedIndex = actions.lastIndex
                        }
                    }
                    KeyType.Enter -> {
                        if (selectedIndex == actions.lastIndex) {
                            running = false
                            nextMenu = Menu.GAME
                        } else {
                            player.buyBuilding(player.allBuildings.get(selectedIndex))
                        }
                    }
                    KeyType.Escape -> {
                        running = false
                        nextMenu = Menu.GAME
                    }
                    else -> {}
                }
                key = screen.pollInput()
            }
            Thread.sleep(100)
            player.game.stats.blah += player.game.stats.blahPS * 0.1
            player.game.stats.timePlayed += 0.1
        }
        return nextMenu
    }
    fun runGameMenu(screen: Screen, player: Player): Menu {
        var selectedIndex = 0
        val actions = listOf(
            "Click",
            "Upgrade",
            "Buildings"
        )
        var running = true
        var nextMenu = Menu.QUIT

        while (running) {
            printUI(screen, player, selectedIndex, actions)

            var key = screen.pollInput()
            while (key != null) {
                when (key.keyType) {
                    KeyType.ArrowLeft -> {
                        selectedIndex--
                        if (selectedIndex < 0) {
                            selectedIndex = actions.lastIndex
                        }
                    }

                    KeyType.ArrowRight -> {
                        selectedIndex++
                        if (selectedIndex > actions.lastIndex) {
                            selectedIndex = 0
                        }
                    }

                    KeyType.Enter -> {
                        when (selectedIndex) {
                            0 -> player.increment()
                            1 -> player.upgrade()
                            2 -> runBuildingsMenu(screen, player)
                        }
                    }

                    KeyType.Escape -> {
                        running = false
                        nextMenu = Menu.QUIT
                    }

                    else -> {
                        if (key.character == 'q') {
                            running = false
                            nextMenu = Menu.QUIT
                        }
                    }
                }
                key = screen.pollInput()
            }
            Thread.sleep(100)
            player.game.stats.blah += player.game.stats.blahPS * 0.1
            player.game.stats.timePlayed += 0.1
        }
        return nextMenu
    }

    fun clearScreen() {
        print("[H[2J")
        System.out.flush()
    }

}
