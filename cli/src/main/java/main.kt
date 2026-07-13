enum class Menu {
    MAIN,
    GAME,
    SHOP,
    SETTINGS,
    QUIT
}
fun main(args: Array<String>) {
    if (args.size < 1) {
        println("Usage: cli-click <options>")
        return
    } else if (args.size == 1 && args[0] == "start-game") {
        val instantiate = Instantiate()
        val player = instantiate.setupGame()
        val screen = instantiate.setupScreen()
        val uiprinter = UIPrinter()

        try {
            screen.startScreen()
            var currentMenu = Menu.GAME
            while (currentMenu != Menu.QUIT) {
                currentMenu = when (currentMenu) {
                    Menu.GAME -> uiprinter.runGameMenu(screen, player)
                    // Wire up additional menus (SHOP, SETTINGS, MAIN) here as they're built.
                    else -> Menu.QUIT
                }
            }
        } finally {
            screen.stopScreen()
        }
    }

}
