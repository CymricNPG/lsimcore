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

import java.util.*
import java.util.concurrent.ConcurrentSkipListSet
import java.util.stream.Stream

// region Interface
/**
 * SortedQueue implements a sorted queue that allows adding and removing elements while maintaining order. It's reduced to the functions which are actually needed in time-management
 */
interface SortedQueue<E : Comparable<E>> {
    /**
     * Removes all elements from the queue that match the given filter.
     * @param filter the filter function to remove elements
     */
    fun removeIf(filter: (e: E) -> Boolean)

    /**
     * Checks if the queue is empty.
     * @return true if the queue is empty, false otherwise
     */
    fun isEmpty(): Boolean

    /**
     * Returns a stream of elements from the queue.
     * @return a stream of elements
     */
    fun stream(): Stream<E>

    /**
     * Returns the first element in the queue without removing it.
     * @return the first element or null if the queue is empty
     */
    fun peek(): E?

    /**
     * Returns true if the queue is not empty, false otherwise
     * @return true if the queue is not empty, false otherwise
     */
    fun isNotEmpty(): Boolean

    /**
     * Adds an element to the end of the queue.
     * @param e the element to be added
     * @return true if the element was added successfully, false otherwise
     */
    fun add(e: E): Boolean

    /**
     * Removes and returns the first element in the queue, or null if the queue is empty.
     * @return the first element, or exception if not first element exists
     */
    fun poll(): E
}

// endregion

// region Implementation

/**
 * a wrapper around an existing collection implementation
 */
class SortedQueueWrapper<E : Comparable<E>>(private val queue: NavigableSet<E>) : SortedQueue<E> {
    companion object {
        /**
         * returns a default implementation, which is currently ConcurrentSkipListSet, which should have the best
         * performance for the time management
         */
        fun <E : Comparable<E>> createDefaultQueue(): SortedQueue<E> {
            return SortedQueueWrapper(ConcurrentSkipListSet())
        }
    }

    override fun removeIf(filter: (e: E) -> Boolean) {
        queue.removeIf(filter)
    }

    override fun isEmpty(): Boolean {
        return queue.isEmpty()
    }

    override fun stream(): Stream<E> {
        return queue.stream()
    }

    override fun peek(): E? {
        if (queue.isEmpty()) {
            return null
        }
        return queue.first()
    }

    override fun isNotEmpty(): Boolean {
        return queue.isNotEmpty()
    }

    override fun poll(): E {
        return queue.pollFirst()!!
    }

    override fun add(e: E): Boolean {
        return queue.add(e)
    }
}
// endregion