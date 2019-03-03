package com.zegreatrob.coupling.common.entity.callsign

interface RandomWordPicker {

    fun List<String>.pickForGiven(givenString: String): String {

        val sumOfCharacterValues = givenString.map { it.toInt() }.sum()

        val index = sumOfCharacterValues % size
        return this[index]
    }

}