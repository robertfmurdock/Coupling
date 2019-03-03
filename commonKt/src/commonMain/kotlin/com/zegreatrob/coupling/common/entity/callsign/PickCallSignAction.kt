package com.zegreatrob.coupling.common.entity.callsign

import kotlin.random.Random

data class PickCallSignAction(val adjectives: List<String>, val nouns: List<String>, val email: String)

interface PickCallSignActionDispatcher : PredictableWordPicker {

    fun PickCallSignAction.pick() = CallSign(
            pickAdjective(),
            pickNoun()
    )

    private fun PickCallSignAction.pickAdjective() = shuffledAdjectives().pickForGiven(email)
    private fun PickCallSignAction.shuffledAdjectives() = adjectives.shuffled(adjectiveRandom)
    private val adjectiveRandom get() = Random(0)
    private fun PickCallSignAction.pickNoun() = shuffledNouns().pickForGiven(email)
    private fun PickCallSignAction.shuffledNouns() = nouns.shuffled(nounRandom)
    private val nounRandom get() = Random(1)

}
