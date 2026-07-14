package cliclick.ui

import java.math.RoundingMode

/** Shared number formatting for stat displays. */
object Numbers {
    /** 2 decimal places, half-up, never scientific notation. */
    fun fmt(value: Double): String =
        value.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toPlainString()
}
