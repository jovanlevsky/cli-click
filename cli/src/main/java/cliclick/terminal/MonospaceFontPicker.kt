package cliclick.terminal

import java.awt.Font
import java.awt.GraphicsEnvironment

/**
 * Picks the terminal-emulator font. Tweak the constructor defaults to change
 * the on-screen font; the first installed family from preferredFamilies wins,
 * falling back to the JVM's logical monospaced font so it always renders.
 */
class MonospaceFontPicker(
    private val preferredFamilies: List<String> = DEFAULT_FAMILIES,
    private val size: Int = 20,
    private val style: Int = Font.PLAIN
) {
    companion object {
        val DEFAULT_FAMILIES = listOf(
            "Consolas",          // Windows default
            "Cascadia Mono",     // Windows Terminal font
            "Menlo",             // macOS
            "DejaVu Sans Mono",  // common on Linux
            "Monospaced"         // logical fallback
        )
    }

    fun pick(): Font {
        val installed = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .availableFontFamilyNames.toHashSet()
        val family = preferredFamilies.firstOrNull { it in installed } ?: Font.MONOSPACED
        return Font(family, style, size)
    }
}
