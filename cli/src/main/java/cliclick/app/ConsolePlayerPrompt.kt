package cliclick.app

import java.util.Scanner

/** Asks for the player's name on stdin before the terminal GUI starts (SRP). */
class ConsolePlayerPrompt {
    fun askName(): String {
        val scanner = Scanner(System.`in`)
        println("Enter your name: ")
        val name = scanner.nextLine()
        println("Welcome, $name! Game starting in a second......")
        Thread.sleep(200)
        return name
    }
}
