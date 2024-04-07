/************************************************************************************
 *     Copyright 2024 Roland Spatzenegger
 *     This file is part of LSimCore.
 *
 *     LSimCore is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     LSimCore is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with LSimCore.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************************/
package net.npg.lsimcore.time

/**
 * Unit-less time interface, the base unit must be defined for the whole application
 */

// region Time Interfaces

/**
 * a fixed point in time
 */
interface Time : Comparable<Time> {
    val value: Long
    operator fun plus(addTime: TimeSpan): Time
    fun isZero(): Boolean
}

/**
 * defines the zero time and the max time a Time-object can contain
 */
data class TimeDefinition(val zeroTime: Time, val maxTime: Time)

/**
 * a span /duration of time
 */
interface TimeSpan {
    val timeSpan: Long
}

/**
 * the global clock, show the current time
 */
interface WallClock {
    fun advanceTo(nextTime: Time)
    val time: Time
}

// endregion

/**
 * Implementation of time for 1ms step sizes
 */

// region Milliseconds implementation

val msTIME = TimeDefinition(TimeImpl.ZERO, TimeImpl.MAX_VALUE)

// Factory Methods
fun fromMs(timeInMs: Long): Time = TimeImpl(timeInMs)
fun fromS(timeInS: Long): Time = TimeImpl(timeInS * 1000)

fun timeSpanFromMs(timeInMs: Long): TimeSpan = TimeSpanImpl(timeInMs)
fun timeSpanFromS(timeInS: Long): TimeSpan = TimeSpanImpl(timeInS * 1000)

/**
 * Implementation for time measured in ms
 */
@JvmInline
private value class TimeImpl(override val value: Long) : Time {

    companion object {
        val ZERO = TimeImpl(0)
        val MAX_VALUE = TimeImpl(Long.MAX_VALUE)
    }

    override operator fun compareTo(other: Time): Int {
        require(other is TimeImpl)
        return value.compareTo(other.value)
    }

    override operator fun plus(addTime: TimeSpan): Time {
        require(addTime is TimeSpanImpl)
        return TimeImpl(value + addTime.timeSpan)
    }

    override fun isZero(): Boolean {
        return value == 0L
    }

    override fun toString(): String {
        return "${value}ms"
    }
}

/**
 * implementation for a timespan in ms
 */
@JvmInline
private value class TimeSpanImpl(override val timeSpan: Long) : TimeSpan

// endregion

// region Implementation for a generic wall clock

fun createWallClock(time: Time): WallClock {
    require(time is TimeImpl)
    return WallClockImpl(time)
}

/**
 * Implementation for a Wall-Clock
 */
private class WallClockImpl(override var time: Time) : WallClock {
    override fun advanceTo(nextTime: Time) {
        require(nextTime >= this.time)
        this.time = nextTime
    }

    override fun toString(): String {
        return "WallClock=${time}"
    }
}

// endregion
