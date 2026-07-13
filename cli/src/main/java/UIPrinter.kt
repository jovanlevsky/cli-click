import com.googlecode.lanterna.TextColor
import Game
import java.util.Scanner
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.graphics.TextGraphics
import kotlinx.coroutines.*
import org.w3c.dom.Text
import java.math.RoundingMode

class UIPrinter {
    // Word-wraps a single line so no wrapped piece exceeds maxWidth columns. Words longer
    // than maxWidth on their own get hard-broken so they can't blow past the terminal edge.
    private fun wrapLine(text: String, maxWidth: Int): List<String> {
        if (maxWidth <= 0 || text.length <= maxWidth) return listOf(text)
        val words = text.split(" ")
        val wrapped = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidateLength = if (current.isEmpty()) word.length else current.length + 1 + word.length
            if (candidateLength <= maxWidth) {
                if (current.isNotEmpty()) current.append(" ")
                current.append(word)
            } else {
                if (current.isNotEmpty()) wrapped.add(current.toString())
                current = StringBuilder(word)
                while (current.length > maxWidth) {
                    wrapped.add(current.substring(0, maxWidth))
                    current = StringBuilder(current.substring(maxWidth))
                }
            }
        }
        if (current.isNotEmpty()) wrapped.add(current.toString())
        return wrapped
    }

    // Draws a box around contentLines, sized to fit the longest line (wrapped to fit within
    // maxContentWidth if given), bordered with "|" on the sides and "+--+" on the top/bottom.
    // Returns the total height (rows) and width (columns) consumed.
    private fun drawBox(
        graphics: TextGraphics,
        x: Int,
        y: Int,
        contentLines: List<String>,
        maxContentWidth: Int = Int.MAX_VALUE
    ): Pair<Int, Int> {
        val wrapped = contentLines.flatMap { wrapLine(it, maxContentWidth) }
        val contentWidth = if (wrapped.isEmpty()) 0 else wrapped.maxOf { it.length }
        val border = "+" + "-".repeat(contentWidth + 2) + "+"
        graphics.putString(x, y, border)
        wrapped.forEachIndexed { i, line ->
            graphics.putString(x, y + 1 + i, "| " + line.padEnd(contentWidth) + " |")
        }
        graphics.putString(x, y + 1 + wrapped.size, border)
        return Pair(wrapped.size + 2, contentWidth + 4)
    }

    fun printUI(screen: Screen, player: Player, selectedIndex: Int, actions: List<String>) {
        val stats = player.game.stats
        screen.clear()
        val graphics = screen.newTextGraphics()
        graphics.foregroundColor = TextColor.ANSI.WHITE
        graphics.putString(0, 0, "${player.name}'s game")

        val maxContentWidth = screen.terminalSize.columns - 4
        val statLines = listOf(
            "Blah: ${stats.blah.toBigDecimal().setScale(3, RoundingMode.HALF_UP)}",
            "BPS: ${stats.blahPS.toBigDecimal().setScale(3, RoundingMode.HALF_UP)}",
            "Blah per click: ${stats.blahPA}",
            "Multiplier: ${stats.multiplier}"
        )
        var y = 1
        val (statsHeight, _) = drawBox(graphics, 0, y, statLines, maxContentWidth)
        y += statsHeight

        var x = 0
        actions.forEachIndexed { index, action ->
            val text = if (index == selectedIndex) {
                "[ $action ]"
            } else " $action "
            graphics.putString(x, y, text)
            x += text.length + 1
        }
        y += 1

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
        if (infoText.isNotEmpty()) {
            drawBox(graphics, 0, y, infoText.split("\n"), maxContentWidth)
        }
        screen.refresh()
    }
    fun printBuildingsUI(screen: Screen, player: Player, selectedIndex: Int, actions: List<String>) {
        screen.clear()
        val graphics = screen.newTextGraphics()
        graphics.foregroundColor = TextColor.ANSI.WHITE
        graphics.putString(0, 0, "${player.name}'s buildings, and current blah is: ${player.game.stats.blah.toInt()}")

        val buildings = player.allBuildings
        val headerRows = 1
        val linesPerBuilding = 5 + 2

        val terminalRows = screen.terminalSize.rows
        val visibleCount = maxOf(1, (terminalRows - headerRows) / linesPerBuilding)
        val maxContentWidth = screen.terminalSize.columns - 4

        var scrollOffset = 0
        if (selectedIndex >= scrollOffset + visibleCount) scrollOffset = selectedIndex - visibleCount + 1
        if (selectedIndex < scrollOffset) scrollOffset = selectedIndex
        scrollOffset = scrollOffset.coerceIn(0, maxOf(0, buildings.size - visibleCount))

        var y = headerRows
        val endIndex = minOf(buildings.size, scrollOffset + visibleCount)
        for (i in scrollOffset until endIndex) {
            val building = buildings.get(i)
            val contentLines = listOf(
                "${building.name} (x${building.num})",
                "Cost: ${building.cost.toBigDecimal().setScale(3, RoundingMode.HALF_UP)}",
                "BPS: ${building.amount}",
                building.description,
                "Tier: ${building.tier}"
            )
            graphics.foregroundColor = TextColor.ANSI.WHITE
            val (height, boxWidth) = drawBox(graphics, 0, y, contentLines, maxContentWidth)
            if (i == selectedIndex) {
                graphics.foregroundColor = TextColor.ANSI.BLUE
                for (row in 0 until height) {
                    graphics.putString(boxWidth + 1, y + row, "|")
                }
                graphics.foregroundColor = TextColor.ANSI.WHITE
            }
            y += height
        }
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
