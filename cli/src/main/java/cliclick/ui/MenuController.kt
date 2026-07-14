package cliclick.ui

/**
 * A top-level menu that runs until finished and says where to go next
 * (OCP: new menus plug into the dispatch loop by implementing this).
 */
interface MenuController {
    fun run(): Menu
}
