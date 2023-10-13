package com.zegreatrob.coupling.model.party

enum class PairingRule {
    LongestTime,
    PreferDifferentBadge,
    ;

    companion object {
        fun fromValue(value: Int?): PairingRule = when (value) {
            1 -> LongestTime
            2 -> PreferDifferentBadge
            else -> LongestTime
        }

        fun toValue(rule: PairingRule): Int = when (rule) {
            LongestTime -> 1
            PreferDifferentBadge -> 2
        }
    }
}
