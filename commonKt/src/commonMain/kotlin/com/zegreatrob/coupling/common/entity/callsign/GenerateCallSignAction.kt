package com.zegreatrob.coupling.common.entity.callsign

import kotlin.random.Random

data class GenerateCallSignAction(val adjectives: List<String>, val nouns: List<String>, val email: String)

interface PickCallSignActionDispatcher : PredictableWordPicker {

    fun GenerateCallSignAction.pick() = CallSign(
            pickAdjective(),
            pickNoun()
    )

    private fun GenerateCallSignAction.pickAdjective() = shuffledAdjectives().pickForGiven(email)
    private fun GenerateCallSignAction.shuffledAdjectives() = adjectives.shuffled(adjectiveRandom)
    private val adjectiveRandom get() = Random(0)
    private fun GenerateCallSignAction.pickNoun() = shuffledNouns().pickForGiven(email)
    private fun GenerateCallSignAction.shuffledNouns() = nouns.shuffled(nounRandom)
    private val nounRandom get() = Random(1)

}
