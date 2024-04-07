package net.npg.lsimcore

import net.npg.lsimcore.base.Id
import net.npg.lsimcore.time.fromMs
import org.junit.jupiter.api.Test
import kotlin.random.Random

@Deprecated(message = "to remove just a playground")
class MultipleQueues {
    @Test
    fun testQueue() {
        val random = Random(1243234)
        val size = 100
        val values = ArrayList<MutableWorkTaskImpl>(size)
        for (i in 0 until size) {
            values.add(
                MutableWorkTaskImpl(
                    fromMs(i + 100 + random.nextLong() % 50),
                    Id.create(),
                    random.nextLong() % 50 > 25
                )
            )
        }
    }
}