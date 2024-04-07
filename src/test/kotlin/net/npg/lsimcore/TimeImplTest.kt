package net.npg.lsimcore

import net.npg.lsimcore.time.fromMs
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class TimeImplTest {

    @Test
    fun compareTo() {
        Assertions.assertThat(fromMs(100)).isGreaterThan(fromMs(50))
        Assertions.assertThat(fromMs(50)).isLessThan(fromMs(100))
    }
}