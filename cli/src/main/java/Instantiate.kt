import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.util.Scanner

class Instantiate {
    fun setupGame(): Player {
        val game = Game()
        val scanner = Scanner(System.`in`)
        println("Enter your name: ")
        val name = scanner.nextLine()
        val player = Player(name, game)
        player.init()
        player.init2()
        println("Welcome, $name! Game starting in a second......")
        Thread.sleep(200)
        return player
    }

    fun setupScreen(): TerminalScreen {
        val terminal = DefaultTerminalFactory().createTerminalEmulator()
        return TerminalScreen(terminal)
    }
}
