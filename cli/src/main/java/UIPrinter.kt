import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.SimpleTheme
import com.googlecode.lanterna.gui2.ActionListBox
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
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.WindowListenerAdapter
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen
import java.math.RoundingMode
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * UI layer built on Lanterna's GUI2 widget framework (MultiWindowTextGUI + Panels +
 * layout managers) instead of manual putString()/pollInput() rendering.
 *
 * Architecture choice (vs. the old one-loop-per-menu model):
 *   The Menu enum dispatch in main.kt is preserved -- runGameMenu(screen, player) still
 *   blocks until the player quits and returns the next Menu. Internally, though, a single
 *   MultiWindowTextGUI owns the event loop; the buildings menu is a modal window stacked
 *   on top of the game window (opened with addWindowAndWait from the Buildings button,
 *   the same pattern Lanterna's built-in dialogs use). This is cleaner in GUI2's model
 *   than tearing the GUI down between menus, and means runGameMenu only ever returns
 *   Menu.QUIT -- which matches the old behavior, since the old runGameMenu also invoked
 *   the buildings menu inline and only exited on quit.
 *
 * Passive income:
 *   A daemon ScheduledExecutorService fires every 100ms for the lifetime of runGameMenu
 *   (i.e. while the GUI is showing) and hands all work to gui.guiThread.invokeLater --
 *   GUI2's TextGUIThread is not safe to touch from other threads. The tick mutates
 *   game stats (blah += blahPS * 0.1, timePlayed += 0.1) and runs every registered
 *   refresh hook (game window always; buildings window adds/removes its own hook while
 *   it is open). Because both the tick and all widget callbacks run on the GUI thread,
 *   no additional synchronization is needed.
 *
 * Centering & wrapping:
 *   Windows carry Window.Hint.CENTERED so the window manager centers them on screen,
 *   and panels use LinearLayout with Alignment.Center layout data -- no coordinate math.
 *   Long descriptions wrap natively via Label.setLabelWidth(), replacing the old
 *   hand-rolled drawBox()/wrapLine() helpers.
 */
class UIPrinter {

    companion object {
        private const val TICK_MILLIS = 100L
        private const val TICK_SECONDS = TICK_MILLIS / 1000.0
        // Width (in columns) of the wrapping detail labels and the buildings list.
        private const val CONTENT_WIDTH = 46
    }

    private fun fmt(value: Double): String =
        value.toBigDecimal().setScale(3, RoundingMode.HALF_UP).toPlainString()

    /** White-on-black flat theme; focused/selected items are highlighted in blue,
     *  echoing the old manual blue "|" selection bar. */
    private fun buildTheme(): SimpleTheme = SimpleTheme.makeTheme(
        true,
        TextColor.ANSI.WHITE, TextColor.ANSI.BLACK,   // base
        TextColor.ANSI.WHITE, TextColor.ANSI.BLACK,   // editable
        TextColor.ANSI.WHITE, TextColor.ANSI.BLUE,    // selected/focused
        TextColor.ANSI.BLACK                          // GUI background
    )

    fun runGameMenu(screen: Screen, player: Player): Menu {
        val stats = player.game.stats
        val gui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLACK))
        gui.theme = buildTheme()

        // Refresh hooks run on the GUI thread once per tick, only for windows currently
        // showing (each window registers on open and deregisters on close).
        val tickHooks = mutableListOf<() -> Unit>()

        val window = BasicWindow("${player.name}'s game")
        window.setHints(listOf(Window.Hint.CENTERED))

        // --- Stats box (centered) ---
        val blahLabel = Label("")
        val bpsLabel = Label("")
        val bpcLabel = Label("")
        val multLabel = Label("")
        val statsPanel = Panel(LinearLayout(Direction.VERTICAL))
        listOf(blahLabel, bpsLabel, bpcLabel, multLabel).forEach {
            statsPanel.addComponent(it, LinearLayout.createLayoutData(LinearLayout.Alignment.Center))
        }

        // --- Details panel (content depends on which action is focused) ---
        val detailsLabel = Label("")
        detailsLabel.setLabelWidth(CONTENT_WIDTH)   // enables native word-wrapping

        // --- Action row: Click / Upgrade / Buildings, Left/Right to move focus ---
        // (Button handles ArrowLeft/ArrowRight as focus movement out of the box.)
        lateinit var refreshGame: () -> Unit
        val clickButton = Button("Click") {
            player.increment()
            refreshGame()
        }
        val upgradeButton = Button("Upgrade") {
            player.upgrade()
            refreshGame()
        }
        val buildingsButton = Button("Buildings") {
            showBuildingsWindow(gui, screen, player, tickHooks)
        }
        val actionsPanel = Panel(LinearLayout(Direction.HORIZONTAL))
        actionsPanel.addComponent(clickButton)
        actionsPanel.addComponent(upgradeButton)
        actionsPanel.addComponent(buildingsButton)

        fun detailsFor(focused: Interactable?): String = when (focused) {
            clickButton -> "Press Enter to Increment Blahs"
            upgradeButton -> nextUpgradeText(player)
            buildingsButton -> "Press Enter to open the buildings menu"
            else -> ""
        }

        refreshGame = {
            blahLabel.text = "Blah: ${fmt(stats.blah)}"
            bpsLabel.text = "BPS: ${fmt(stats.blahPS)}"
            bpcLabel.text = "Blah per click: ${stats.blahPA}"
            multLabel.text = "Multiplier: ${stats.multiplier}"
            detailsLabel.text = detailsFor(window.focusedInteractable)
        }

        // --- Root layout: everything vertically stacked and center-aligned ---
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
        tickHooks.add(refreshGame)

        // --- Passive income ticker (~10x/sec), pushed onto the GUI thread ---
        val ticker = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "cli-click-ticker").apply { isDaemon = true }
        }
        ticker.scheduleAtFixedRate({
            gui.guiThread.invokeLater {
                stats.blah += stats.blahPS * TICK_SECONDS
                stats.timePlayed += TICK_SECONDS
                tickHooks.toList().forEach { it() }
            }
        }, TICK_MILLIS, TICK_MILLIS, TimeUnit.MILLISECONDS)

        try {
            gui.addWindowAndWait(window)   // blocks until the game window is closed
        } finally {
            ticker.shutdownNow()
            tickHooks.remove(refreshGame)
        }
        return Menu.QUIT
    }

    private fun nextUpgradeText(player: Player): String {
        val nextIndex = player.currUpgrade + 1
        if (nextIndex > player.allUpgrades.lastIndex) {
            return "No more upgrades available"
        }
        val upgrade = player.allUpgrades[nextIndex]
        return "${upgrade.name}\n" +
            "Cost: ${upgrade.cost}\n" +
            "Increase: ${upgrade.amount}\n" +
            "Description: ${upgrade.description}\n" +
            "Tier: ${upgrade.tier}\n" +
            "Type: ${upgrade.type}"
    }

    /**
     * Buildings menu as a modal window on top of the game window.
     *
     * The old UI drew one bordered box per building and scrolled the whole page; here an
     * ActionListBox provides Up/Down navigation, focus highlighting, and native scrolling
     * when the list outgrows its viewport, while a Details panel below shows the full
     * info (name+count, cost, BPS, description, tier) for the selected entry. List rows
     * are objects whose toString() reads live building state, so counts and costs
     * redraw fresh on every tick without rebuilding the list.
     */
    private fun showBuildingsWindow(
        gui: WindowBasedTextGUI,
        screen: Screen,
        player: Player,
        tickHooks: MutableList<() -> Unit>
    ) {
        val window = BasicWindow("${player.name}'s buildings")
        window.setHints(listOf(Window.Hint.CENTERED))

        val headerLabel = Label("")
        val detailsLabel = Label("")
        detailsLabel.setLabelWidth(CONTENT_WIDTH)

        class BuildingItem(val building: Game.Building) : Runnable {
            override fun run() {
                player.buyBuilding(building)
            }
            override fun toString() =
                "${building.name} (x${building.num})  Cost: ${fmt(building.cost)}"
        }

        // Viewport: fill the terminal minus room for header/details/borders; the list
        // scrolls automatically when the buildings don't fit.
        val visibleRows = (screen.terminalSize.rows - 14)
            .coerceIn(3, player.allBuildings.size + 1)
        val listBox = ActionListBox(TerminalSize(CONTENT_WIDTH, visibleRows))
        player.allBuildings.forEach { listBox.addItem(BuildingItem(it)) }
        listBox.addItem(object : Runnable {
            override fun run() {
                window.close()
            }
            override fun toString() = "Return"
        })

        val refreshBuildings = {
            headerLabel.text = "Current blah: ${player.game.stats.blah.toInt()}"
            val selected = listBox.selectedItem
            detailsLabel.text = if (selected is BuildingItem) {
                val b = selected.building
                "${b.name} (x${b.num})\n" +
                    "Cost: ${fmt(b.cost)}\n" +
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
        tickHooks.add(refreshBuildings)
        try {
            gui.addWindowAndWait(window)   // modal: blocks until closed, game ticker keeps running
        } finally {
            tickHooks.remove(refreshBuildings)
        }
    }

    fun clearScreen() {
        print("[H[2J")
        System.out.flush()
    }
}
