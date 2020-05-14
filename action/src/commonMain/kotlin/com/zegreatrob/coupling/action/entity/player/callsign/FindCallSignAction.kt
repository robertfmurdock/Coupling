package com.zegreatrob.coupling.action.entity.player.callsign

import com.zegreatrob.coupling.action.DispatchSyntax
import com.zegreatrob.coupling.action.SimpleSuccessfulExecutableAction
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign

data class FindCallSignAction(val players: List<Player>, val email: String) :
    SimpleSuccessfulExecutableAction<FindCallSignActionDispatcher, CallSign> {
    override val performFunc = link(FindCallSignActionDispatcher::perform)
}

interface FindCallSignActionDispatcher : GenerateCallSignActionDispatcher, DispatchSyntax {

    fun perform(action: FindCallSignAction) = defaultCallSignOptions.let { (adjectives, nouns) ->
        action.generateCallSign(adjectives, nouns)
    }

    private fun FindCallSignAction.generateCallSign(adjectives: Set<String>, nouns: Set<String>) =
        execute(GenerateCallSignAction(adjectives, nouns, email, players))

}