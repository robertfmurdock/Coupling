package com.zegreatrob.coupling.model.player.callsign


fun List<String>.pickForGiven(givenString: String) = givenString.map { it.code }
    .sum()
    .let { sumOfCharacterValues -> sumOfCharacterValues % size }
    .let { index -> this[index] }
