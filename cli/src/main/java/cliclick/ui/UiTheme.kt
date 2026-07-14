package cliclick.ui

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.SimpleTheme

/** Central theme definition so every window looks the same (SRP). */
object UiTheme {
    /** White-on-black flat theme; focused/selected items are highlighted in
     *  blue, echoing the old manual blue "|" selection bar. */
    fun standard(): SimpleTheme = SimpleTheme.makeTheme(
        true,
        TextColor.ANSI.WHITE, TextColor.ANSI.BLACK,   // base
        TextColor.ANSI.WHITE, TextColor.ANSI.BLACK,   // editable
        TextColor.ANSI.WHITE, TextColor.ANSI.BLUE,    // selected/focused
        TextColor.ANSI.BLACK                          // GUI background
    )
}
