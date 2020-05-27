package com.zegreatrob.coupling.action.entity.player.callsign

import com.zegreatrob.coupling.actionFunc.GeneralExecutableActionDispatcherSyntax
import com.zegreatrob.coupling.actionFunc.SimpleExecutableAction
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.player.callsign.PredictableWordPicker
import kotlin.random.Random

data class GenerateCallSignAction(
    val adjectives: Set<String>,
    val nouns: Set<String>,
    val email: String,
    val players: List<Player>
) : SimpleExecutableAction<GenerateCallSignActionDispatcher, CallSign> {
    override val performFunc = link(GenerateCallSignActionDispatcher::perform)
}

interface GenerateCallSignActionDispatcher : PredictableWordPicker, GeneralExecutableActionDispatcherSyntax {

    fun perform(action: GenerateCallSignAction) = action.callSign()

    private fun GenerateCallSignAction.callSign() = CallSign(
        pickAdjective(),
        pickNoun()
    )

    private fun GenerateCallSignAction.pickAdjective() =
        rollForUnusedTerm(shuffledAdjectives(), players.adjectives(), email)

    private fun List<Player>.adjectives() = mapNotNull { it.callSignAdjective }

    private fun GenerateCallSignAction.shuffledAdjectives() = adjectives.shuffled(adjectiveRandom)

    private val adjectiveRandom get() = Random(0)

    private fun GenerateCallSignAction.pickNoun(): String {
        return rollForUnusedTerm(shuffledNouns(), players.nouns(), email)
    }

    private fun GenerateCallSignAction.shuffledNouns() = nouns.shuffled(nounRandom)
    private val nounRandom get() = Random(1)

    private fun rollForUnusedTerm(
        termList: List<String>,
        usedTerms: List<String>,
        email: String,
        offset: Int? = null
    ): String {
        if (usedTerms.containsAll(termList)) {
            return "Blank"
        }

        val candidate = termList.pickForGiven(email.with(offset))
        return when {
            usedTerms.contains(candidate) -> rollForUnusedTerm(termList, usedTerms, email, offset.next())
            else -> candidate
        }
    }

    private fun Int?.next() = if (this == null)
        1
    else
        this + 1

    private fun String.with(offset: Int? = null) = if (offset == null) {
        this
    } else {
        "${this}$offset"
    }

    private fun List<Player>.nouns() = mapNotNull { it.callSignNoun }
}
