package cliclick.ui

import com.googlecode.lanterna.gui2.TextGUI
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Drives the game clock while the GUI is showing (SRP: scheduling only).
 *
 * A daemon ScheduledExecutorService fires every 100ms and hands all work to
 * gui.guiThread.invokeLater, because GUI2's TextGUIThread must not be touched
 * from other threads. Each tick first runs onTick (the engine's passive-income
 * update) and then every registered refresh hook. Windows register a hook while
 * they are showing and remove it when they close, so labels refresh live and
 * only for visible windows. Since hooks and widget callbacks all run on the
 * GUI thread, no extra synchronization is needed.
 */
class GuiTicker(
    private val gui: TextGUI,
    private val onTick: (deltaSeconds: Double) -> Unit
) {
    companion object {
        private const val PERIOD_MILLIS = 100L
        private const val PERIOD_SECONDS = PERIOD_MILLIS / 1000.0
    }

    private val refreshHooks = mutableListOf<() -> Unit>()
    private var executor: ScheduledExecutorService? = null

    fun addRefreshHook(hook: () -> Unit) {
        refreshHooks.add(hook)
    }

    fun removeRefreshHook(hook: () -> Unit) {
        refreshHooks.remove(hook)
    }

    fun start() {
        check(executor == null) { "Ticker is already running" }
        executor = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "cli-click-ticker").apply { isDaemon = true }
        }.also {
            it.scheduleAtFixedRate({
                gui.guiThread.invokeLater {
                    onTick(PERIOD_SECONDS)
                    refreshHooks.toList().forEach { hook -> hook() }
                }
            }, PERIOD_MILLIS, PERIOD_MILLIS, TimeUnit.MILLISECONDS)
        }
    }

    fun stop() {
        executor?.shutdownNow()
        executor = null
    }
}
