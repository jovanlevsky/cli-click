import Game
import java.util.Scanner
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.input.KeyType
import kotlinx.coroutines.*
import org.w3c.dom.Text


fun startGame(): Game {
    return Game()
}
fun main(args: Array<String>) {
    if (args.size < 1) {
        println("Usage: cli-click <options>")
        return
    } else if (args.size == 1 && args[0] == "start-game") {
        var game = startGame()
        val scanner = Scanner(System.`in`)
        println("Enter your name: ")
        val name = scanner.nextLine()
        var player = Player(name, game)
        var upgrades = player.init()
        var running = true
        println("Welcome, $name! Game starting in a second......")
        Thread.sleep(500)
        val terminal = DefaultTerminalFactory()
            .createTerminalEmulator()
        val screen = TerminalScreen(terminal)
        val uiprinter = UIPrinter()

        try {
            screen.startScreen()
            var selectedIndex = 0
            val actions = listOf(
                "Click",
                "Upgrade",
                "Inventory"
            )
            while (running) {
                uiprinter.printUI(screen, player, selectedIndex, actions)

                val key = screen.readInput()
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
                            2 -> player.openBuildingsMenu()
                        }
                    }

                    KeyType.Escape -> {
                        running = false
                    }

                    else -> {}
                }
                val keyStroke = screen.pollInput()
                if (keyStroke != null && (keyStroke.keyType == KeyType.Escape || keyStroke.character == 'q')) {
                    running = false
                }
                Thread.sleep(100)
            }
        } finally {
            screen.stopScreen()
        }
    }

}
