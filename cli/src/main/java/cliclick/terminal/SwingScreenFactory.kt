package cliclick.terminal

import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration

/**
 * Creates the terminal screen (SRP: terminal/font setup only).
 *
 * The font configuration is immutable once the terminal exists, so it must be
 * supplied to the factory *before* createTerminalEmulator() is called.
 * NOTE: the font only affects the Swing terminal-emulator window path
 * (createTerminalEmulator()); it has no effect in a real system terminal.
 */
class SwingScreenFactory(
    private val fontPicker: MonospaceFontPicker = MonospaceFontPicker()
) {
    fun create(): TerminalScreen {
        val fontConfig = SwingTerminalFontConfiguration.newInstance(fontPicker.pick())
        val terminal = DefaultTerminalFactory()
            .setTerminalEmulatorFontConfiguration(fontConfig)
            .createTerminalEmulator()
        return TerminalScreen(terminal)
    }
}
