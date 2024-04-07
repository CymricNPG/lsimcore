package net.npg.lsimcore

import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors


class CoroutineTest {
    val list = mutableListOf<CompletableFuture<Boolean>>()
    var executorService = Executors.newWorkStealingPool(6)

    @Test
    fun test() {
        val two = Two()
        two.x(this)

        System.err.println("Waiting Message")
        CompletableFuture.allOf(*list.toTypedArray()).join()
        System.err.println("Finished Message")
    }


    fun doMessage() {

        val completableFuture = CompletableFuture.supplyAsync {
            Thread.sleep(1000)
            System.err.println("Hello Message" + Thread.currentThread().name)
            true
        }

        list.add(completableFuture)
    }

}

class Two {

    fun x(x: CoroutineTest) {
        for (i in 0..100) {
            x.doMessage()
        }
    }

}