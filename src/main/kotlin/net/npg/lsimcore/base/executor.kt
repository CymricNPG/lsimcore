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

import java.util.concurrent.ExecutorService

/**
 * A functional interface for an executor that can be used to execute requests represented as lambdas.
 */
fun interface RequestExecutor {
    fun execute(request: () -> Unit)
}

/**
 * A concrete implementation of the RequestExecutor interface that uses the given {@code ExecutorService} for executing tasks.
 * Constructs a new instance of ThreadRequestExecutor with the given ExecutorService.
 * @param threadPool the ExecutorService to be used for executing tasks
 */
class ThreadRequestExecutor(private val threadPool: ExecutorService) : RequestExecutor {

    /**
     * Implements the execute method from RequestExecutor interface, which executes the given Runnable task using the provided ExecutorService.
     * @param request the lambda representation of a Runnable task to be executed
     */
    override fun execute(request: () -> Unit) {
        threadPool.execute(request)
    }
}

/**
 * requests are executed in the same thread, all workers are executed in serial, only useful for debugging because the stack will blow
 */

class SimpleRequestExecutor : RequestExecutor {
    override fun execute(request: () -> Unit) {
        request.invoke()
    }
}