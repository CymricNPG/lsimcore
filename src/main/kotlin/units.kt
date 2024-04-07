@JvmInline
value class Time(val value: Double) : Comparable<Time> {
    override fun compareTo(other: Time): Int {
        return value.compareTo(other.value)
    }
}