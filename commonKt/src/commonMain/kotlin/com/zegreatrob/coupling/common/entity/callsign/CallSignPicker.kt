package com.zegreatrob.coupling.common.entity.callsign

import kotlin.random.Random


interface CallSignPicker : PredictableWordPicker {

    fun PickCallSignAction.pick() = CallSign(
            pickAdjective(),
            pickNoun()
    )

    private fun PickCallSignAction.pickNoun() = availableComponents.shuffledNouns().pickForGiven(email)
    private fun PickCallSignAction.pickAdjective() = availableComponents.shuffledAdjectives().pickForGiven(email)
    private fun AvailableComponents.shuffledAdjectives() = adjectives.shuffled(adjectiveRandom)
    private fun AvailableComponents.shuffledNouns() = nouns.shuffled(nounRandom)
    private val adjectiveRandom get() = Random(0)
    private val nounRandom get() = Random(1)

}

data class AvailableComponents(val adjectives: List<String>, val nouns: List<String>)

data class CallSign(val adjective: String, val noun: String)

data class PickCallSignAction(val availableComponents: AvailableComponents, val email: String)