package cliclick.ui

import cliclick.engine.GameActions
import cliclick.engine.GameQueries
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.DefaultWindowManager
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowListenerAdapter
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The main game menu (Lanterna GUI2): a centered window with a stats box, a
 * Click/Upgrade/Buildings action row (Left/Right moves focus, Enter triggers),
 * and an info panel that follows the focused action. Escape or 'q' quits.
 *
 * Owns the MultiWindowTextGUI and the GuiTicker for the whole GUI session; the
 * buildings menu is shown as a modal window on top (the same pattern Lanterna's
 * built-in dialogs use), so this controller only ever returns Menu.QUIT --
 * matching the original behavior, where the buildings menu was also invoked
 * inline and the game loop only exited on quit.
 *
 * DIP: depends on the GameQueries/GameActions interfaces, not on GameEngine.
 */
class GameMenu(
    private val screen: Screen,
    private val playerName: String,
    private val game: GameQueries,
    private val actions: GameActions,
    private val buildingsMenu: BuildingsMenu
) : MenuController {

    override fun run(): Menu {
        val stats = game.stats
        val gui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLACK))
        gui.theme = UiTheme.standard()
        val ticker = GuiTicker(gui) { deltaSeconds -> actions.tick(deltaSeconds) }

        val window = BasicWindow("$playerName's game")
        window.setHints(listOf(Window.Hint.CENTERED))

        // --- Stats box (centered) ---
        val inputsLabel = Label("")
        val ipsLabel = Label("")
        val ipcLabel = Label("")
        val multLabel = Label("")
        val statsPanel = Panel(LinearLayout(Direction.VERTICAL))
        listOf(inputsLabel, ipsLabel, ipcLabel, multLabel).forEach {
            statsPanel.addComponent(it, LinearLayout.createLayoutData(LinearLayout.Alignment.Center))
        }

        // --- Details panel (content depends on which action is focused) ---
        val detailsLabel = Label("")
        detailsLabel.setLabelWidth(UiConstants.CONTENT_WIDTH)   // native word-wrapping

        // --- Action row: Click / Upgrade / Buildings ---
        // (Button handles ArrowLeft/ArrowRight as focus movement out of the box.)
        lateinit var refreshGame: () -> Unit
        val clickButton = Button("Click") {
            actions.increment()
            refreshGame()
        }
        val upgradeButton = Button("Upgrade") {
            actions.upgrade()
            refreshGame()
        }
        val buildingsButton = Button("Buildings") {
            buildingsMenu.show(gui, ticker)
        }
        val actionsPanel = Panel(LinearLayout(Direction.HORIZONTAL))
        actionsPanel.addComponent(clickButton)
        actionsPanel.addComponent(upgradeButton)
        actionsPanel.addComponent(buildingsButton)

        fun detailsFor(focused: Interactable?): String = when (focused) {
            clickButton -> "Press Enter to Increment Inputs"
            upgradeButton -> nextUpgradeText()
            buildingsButton -> "Press Enter to open the buildings menu"
            else -> ""
        }

        refreshGame = {
            inputsLabel.text = "Inputs: ${Numbers.fmt(stats.inputs)}"
            ipsLabel.text = "IPS: ${Numbers.fmt(stats.inputsPS)}"
            ipcLabel.text = "Inputs per click: ${stats.inputsPA}"
            multLabel.text = "Multiplier: ${stats.multiplier}"
            detailsLabel.text = detailsFor(window.focusedInteractable)
        }

        // --- Root layout: vertically stacked, center-aligned ---
        val root = Panel(LinearLayout(Direction.VERTICAL))
        root.addComponent(
            statsPanel.withBorder(Borders.singleLine("Stats")),
            LinearLayout.createLayoutData(LinearLayout.Alignment.Center)
        )
        root.addComponent(actionsPanel, LinearLayout.createLayoutData(LinearLayout.Alignment.Center))
        root.addComponent(
            Panel(LinearLayout(Direction.VERTICAL)).apply { addComponent(detailsLabel) }
                .withBorder(Borders.singleLine("Info")),
            LinearLayout.createLayoutData(LinearLayout.Alignment.Center)
        )
        window.component = root

        // Escape or 'q' quits (buttons don't consume either, so they land here).
        window.addWindowListener(object : WindowListenerAdapter() {
            override fun onUnhandledInput(basePane: Window, keyStroke: KeyStroke, hasBeenHandled: AtomicBoolean) {
                if (keyStroke.keyType == KeyType.Escape ||
                    (keyStroke.keyType == KeyType.Character && keyStroke.character == 'q')
                ) {
                    basePane.close()
                    hasBeenHandled.set(true)
                }
            }
        })

        refreshGame()
        ticker.addRefreshHook(refreshGame)
        ticker.start()
        try {
            gui.addWindowAndWait(window)   // blocks until the game window is closed
        } finally {
            ticker.stop()
            ticker.removeRefreshHook(refreshGame)
        }
        return Menu.QUIT
    }

    private fun nextUpgradeText(): String {
        val upgrade = game.nextUpgrade ?: return "No more upgrades available"
        return "${upgrade.name}\n" +
            "Cost: ${upgrade.cost}\n" +
            "Increase: ${upgrade.amount}\n" +
            "Description: ${upgrade.description}\n" +
            "Tier: ${upgrade.tier}\n" +
            "Type: ${upgrade.type}"
    }
}
