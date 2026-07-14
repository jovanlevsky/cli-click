package cliclick.app

import cliclick.data.ResourceContentRepository
import cliclick.engine.GameEngine
import cliclick.terminal.SwingScreenFactory
import cliclick.ui.BuildingsMenu
import cliclick.ui.GameMenu
import cliclick.ui.Menu

/**
 * Composition root: the only place that knows the concrete classes and wires
 * them together (DIP). Everything else depends on interfaces.
 *
 * Manifest Main-Class: cliclick.app.MainKt (see cli/pom.xml).
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: cli-click <options>")
        return
    }
    if (args.size == 1 && args[0] == "start-game") {
        val playerName = ConsolePlayerPrompt().askName()
        val engine = GameEngine(ResourceContentRepository())
        val screen = SwingScreenFactory().create()
        val buildingsMenu = BuildingsMenu(screen, playerName, engine, engine)
        val gameMenu = GameMenu(screen, playerName, engine, engine, buildingsMenu)

        try {
            screen.startScreen()
            var currentMenu = Menu.GAME
            while (currentMenu != Menu.QUIT) {
                currentMenu = when (currentMenu) {
                    Menu.GAME -> gameMenu.run()
                    // Wire up additional menus (SHOP, SETTINGS, MAIN) here as they're built.
                    else -> Menu.QUIT
                }
            }
        } finally {
            screen.stopScreen()
        }
    }
}
