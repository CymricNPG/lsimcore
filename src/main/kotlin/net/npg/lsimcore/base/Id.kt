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
package net.npg.lsimcore.base

import java.util.concurrent.atomic.AtomicInteger

/**
 * a local unique id generator
 */
@JvmInline
value class Id private constructor(private val value: Int) : Comparable<Id> {
    companion object {
        private val count = AtomicInteger(0)
        fun create(): Id {
            return Id(count.incrementAndGet())
        }
    }

    override fun compareTo(other: Id): Int {
        return value.compareTo(other.value)
    }
}