import net.npg.lsimcore.base.SortedQueueWrapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SortedQueueWrapperTest {

    @Test
    fun `test ist leer zu Beginn`() {
        val queue = SortedQueueWrapper.createDefaultQueue<Int>()
        assertTrue(queue.isEmpty(), "Queue sollte am Anfang leer sein")
        assertFalse(queue.isNotEmpty(), "Queue sollte am Anfang nicht gefüllt sein")
        assertNull(queue.peek(), "Peek sollte bei leerer Queue null zurückgeben")
    }

    @Test
    fun `test Hinzufügen und Peek`() {
        val queue = SortedQueueWrapper.createDefaultQueue<Int>()
        queue.add(5)
        queue.add(10)
        assertEquals(5, queue.peek(), "Das erste Element sollte 5 sein")
        assertFalse(queue.isEmpty(), "Queue darf nicht mehr leer sein")
        assertTrue(queue.isNotEmpty(), "Queue sollte gefüllt sein")
    }

    @Test
    fun `test Poll entfernt erstes Element`() {
        val queue = SortedQueueWrapper.createDefaultQueue<Int>()
        queue.add(7)
        queue.add(3)
        queue.add(9)
        // Da es sich um eine sortierte Queue handelt, sollte 3 an erster Stelle stehen
        assertEquals(3, queue.poll(), "Poll sollte das kleinste Element zurückgeben")
        assertEquals(7, queue.poll(), "Als nächstes sollte 7 entfernt werden")
        assertEquals(9, queue.poll(), "Dann 9")
        assertTrue(queue.isEmpty(), "Nachdem alle Elemente entfernt wurden, sollte die Queue leer sein")
    }

    @Test
    fun `test RemoveIf filtert Elemente`() {
        val queue = SortedQueueWrapper.createDefaultQueue<Int>()
        queue.add(2)
        queue.add(4)
        queue.add(6)
        queue.add(8)
        // Entfernt alle geraden Zahlen größer als 5
        queue.removeIf { it > 5 }
        assertEquals(2, queue.poll(), "Erstes übrig gebliebenes Element sollte 2 sein")
        assertEquals(4, queue.poll(), "Als nächstes sollte 4 übrig bleiben")
        assertTrue(queue.isEmpty(), "Restliche Elemente sollten entfernt worden sein")
    }

    @Test
    fun `test Stream bietet Zugriff auf alle Elemente`() {
        val queue = SortedQueueWrapper.createDefaultQueue<Int>()
        queue.add(3)
        queue.add(1)
        queue.add(2)
        val gesamtSumme = queue.stream().mapToInt { it }.sum()
        assertEquals(6, gesamtSumme, "Summe aus 1, 2 und 3 sollte 6 sein")
    }
}