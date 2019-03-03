package com.zegreatrob.coupling.common.entity.callsign

import kotlin.random.Random


interface CallSignPicker : PredictableWordPicker {

    fun AvailableComponents.pick(email: String) = CallSign(
            shuffledAdjectives().pickForGiven(email),
            shuffledNouns().pickForGiven(email)
    )

    private val adjectiveRandom get() = Random(0)
    private val nounRandom get() = Random(1)

    private fun AvailableComponents.shuffledAdjectives() = adjectives.shuffled(adjectiveRandom)
    private fun AvailableComponents.shuffledNouns() = nouns.shuffled(nounRandom)
}

data class AvailableComponents(val adjectives: List<String>, val nouns: List<String>)

data class CallSign(val adjective: String, val noun: String)