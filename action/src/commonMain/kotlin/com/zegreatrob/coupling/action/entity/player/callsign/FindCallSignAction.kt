package com.zegreatrob.coupling.action.entity.player.callsign

import com.zegreatrob.testmints.action.GeneralExecutableActionDispatcherSyntax
import com.zegreatrob.testmints.action.SimpleExecutableAction
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign

data class FindCallSignAction(val players: List<Player>, val email: String) :
    SimpleExecutableAction<FindCallSignActionDispatcher, CallSign> {
    override val performFunc = link(FindCallSignActionDispatcher::perform)
}

interface FindCallSignActionDispatcher : GenerateCallSignActionDispatcher, GeneralExecutableActionDispatcherSyntax {

    fun perform(action: FindCallSignAction) = with(defaultCallSignOptions) {
        action.generateCallSign(adjectives, nouns)
    }

    private fun FindCallSignAction.generateCallSign(adjectives: Set<String>, nouns: Set<String>) =
        execute(GenerateCallSignAction(adjectives, nouns, email, players))

}
