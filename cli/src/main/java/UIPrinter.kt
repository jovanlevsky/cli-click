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
        graphics.foregroundColor = TextColor.ANSI.WHITE
        graphics.putString(0, 0, "${player.name}'s game")
        graphics.putString(0, 1, "+--------------------------------+")
        graphics.putString(0, 2, " Blah: ${stats.blah}               ")
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

    fun clearScreen() {
        print("[H[2J")
        System.out.flush()
    }

}
