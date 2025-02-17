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

import net.npg.lsimcore.base.Id

/**
 * an interface for the lookahead based time management
 */
interface TimeManagement {
    /**
     * add a new time managed worker, must be added before the simulation started!
     * TODO register worker during simulation
     */
    fun registerWorker(worker: Worker)

    /**
     * the registered worker has completed its work and wants to advance to the next time.
     * The call is async and returns immediately
     */
    fun requestAdvance(worker: Worker, nextTime: Time)

    /**
     * removes a worker, a current execution will be finished
     */
    fun removeWorker(worker: Worker)

    /**
     * returns the time at the wallclock
     */
    fun getWallClock(): Time

}

interface BlockingTimeManagement {
    fun requestAdvance(nextTime: Time)
    fun defaultAdvance(): Time
}

interface AdvanceGrantedListener {
    /**
     * callback method when a requestAdvance is granted, the nextTime requested is the granted time.
     * The method is called in a thread depending on the used RequestExecutor,
     * if an error happens the worker will be removed from the time management
     */
    fun advanceGranted(time: Time)
}

/**
 * An interface to a worker
 */
interface Worker : AdvanceGrantedListener {

    /**
     * the lookahead which should be initially used
     */
    val lookahead: TimeSpan

    /**
     * the unique id of the worker, used for all further identification
     */
    val id: Id
}