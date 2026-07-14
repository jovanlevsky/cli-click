package cliclick.ui

import cliclick.engine.GameActions
import cliclick.engine.GameQueries
import cliclick.model.Building
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.ActionListBox
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.WindowListenerAdapter
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Buildings menu, shown as a modal window on top of the game window.
 *
 * An ActionListBox provides Up/Down navigation, focus highlighting, and native
 * scrolling when the list outgrows its viewport; a Details panel below shows
 * the full info (name+count, cost, BPS, description, tier) for the selected
 * entry. List rows are objects whose toString() reads live building state, so
 * counts and costs redraw fresh on every tick without rebuilding the list.
 * Enter buys the selected building, "Return" or Escape closes the window.
 */
class BuildingsMenu(
    private val screen: Screen,
    private val playerName: String,
    private val game: GameQueries,
    private val actions: GameActions
) {
    private inner class BuildingItem(val building: Building) : Runnable {
        override fun run() {
            actions.buyBuilding(building)
        }
        override fun toString() =
            "${building.name} (x${building.num})  Cost: ${Numbers.fmt(building.cost)}"
    }

    fun show(gui: WindowBasedTextGUI, ticker: GuiTicker) {
        val window = BasicWindow("$playerName's buildings")
        window.setHints(listOf(Window.Hint.CENTERED))

        val headerLabel = Label("")
        val detailsLabel = Label("")
        detailsLabel.setLabelWidth(UiConstants.CONTENT_WIDTH)

        // Viewport: fill the terminal minus room for header/details/borders; the
        // list scrolls automatically when the buildings don't fit.
        val visibleRows = (screen.terminalSize.rows - 14)
            .coerceIn(3, game.buildings.size + 1)
        val listBox = ActionListBox(TerminalSize(UiConstants.CONTENT_WIDTH, visibleRows))
        game.buildings.forEach { listBox.addItem(BuildingItem(it)) }
        listBox.addItem(object : Runnable {
            override fun run() {
                window.close()
            }
            override fun toString() = "Return"
        })

        val refreshBuildings = {
            headerLabel.text = "Current blah: ${game.stats.blah.toInt()}"
            val selected = listBox.selectedItem
            detailsLabel.text = if (selected is BuildingItem) {
                val b = selected.building
                "${b.name} (x${b.num})\n" +
                    "Cost: ${Numbers.fmt(b.cost)}\n" +
                    "BPS: ${b.amount}\n" +
                    b.description + "\n" +
                    "Tier: ${b.tier}"
            } else {
                "Press Enter to return to the game menu"
            }
        }

        val root = Panel(LinearLayout(Direction.VERTICAL))
        root.addComponent(headerLabel, LinearLayout.createLayoutData(LinearLayout.Alignment.Center))
        root.addComponent(
            listBox.withBorder(Borders.singleLine("Buildings")),
            LinearLayout.createLayoutData(LinearLayout.Alignment.Center)
        )
        root.addComponent(
            Panel(LinearLayout(Direction.VERTICAL)).apply { addComponent(detailsLabel) }
                .withBorder(Borders.singleLine("Details")),
            LinearLayout.createLayoutData(LinearLayout.Alignment.Center)
        )
        window.component = root

        // Escape returns to the game menu (the list consumes arrows/Enter, not Escape).
        window.addWindowListener(object : WindowListenerAdapter() {
            override fun onUnhandledInput(basePane: Window, keyStroke: KeyStroke, hasBeenHandled: AtomicBoolean) {
                if (keyStroke.keyType == KeyType.Escape) {
                    basePane.close()
                    hasBeenHandled.set(true)
                }
            }
        })

        refreshBuildings()
        ticker.addRefreshHook(refreshBuildings)
        try {
            gui.addWindowAndWait(window)   // modal: blocks until closed, ticker keeps running
        } finally {
            ticker.removeRefreshHook(refreshBuildings)
        }
    }
}
