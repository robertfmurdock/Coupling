package com.zegreatrob.coupling.common.entity.callsign

interface RandomWordPicker {

    fun List<String>.pickForGiven(givenString: String) = givenString.map { it.toInt() }
            .sum()
            .let { sumOfCharacterValues -> sumOfCharacterValues % size }
            .let { index -> this[index] }

}