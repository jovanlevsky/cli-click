import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.util.Scanner

class Instantiate {
    // ---- Font configuration for the Swing terminal-emulator window ----
    // Tweak these three to change the on-screen font. The first family from
    // PREFERRED_FONTS that is actually installed wins; if none are found we fall
    // back to the JVM's built-in logical monospaced font so it always renders.
    private val preferredFonts = listOf(
        "Consolas",          // Windows default
        "Cascadia Mono",     // Windows Terminal font
        "Menlo",             // macOS
        "DejaVu Sans Mono",  // common on Linux
        "Monospaced"         // logical fallback
    )
    private val fontSize = 20
    private val fontStyle = Font.PLAIN

    private fun pickFont(): Font {
        val installed = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .availableFontFamilyNames.toHashSet()
        val family = preferredFonts.firstOrNull { it in installed } ?: Font.MONOSPACED
        return Font(family, fontStyle, fontSize)
    }
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
        // The font configuration is immutable once the terminal exists, so it must be
        // supplied to the factory *before* createTerminalEmulator() is called.
        // NOTE: this only affects the Swing terminal-emulator window path
        // (createTerminalEmulator()); it has no effect when running in a real
        // system terminal.
        val fontConfig = SwingTerminalFontConfiguration.newInstance(pickFont())
        val terminal = DefaultTerminalFactory()
            .setTerminalEmulatorFontConfiguration(fontConfig)
            .createTerminalEmulator()
        return TerminalScreen(terminal)
    }
}
