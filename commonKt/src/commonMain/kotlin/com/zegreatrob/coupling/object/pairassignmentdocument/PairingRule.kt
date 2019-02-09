enum class PairingRule {
    LongestTime, PreferDifferentBadge;

    companion object {
        fun fromValue(value: Int?): PairingRule = when (value) {
            1 -> LongestTime
            2 -> PreferDifferentBadge
            else -> LongestTime
        }
    }
}