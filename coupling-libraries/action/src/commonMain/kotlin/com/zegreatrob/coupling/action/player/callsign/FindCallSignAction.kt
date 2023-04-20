package com.zegreatrob.coupling.action.player.callsign

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import com.zegreatrob.testmints.action.SimpleExecutableAction

data class FindCallSignAction(val players: List<Player>, val email: String) :
    SimpleExecutableAction<FindCallSignAction.Dispatcher, CallSign> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher :
        GenerateCallSignAction.Dispatcher,
        ExecutableActionExecuteSyntax {

        fun perform(action: FindCallSignAction) = with(defaultCallSignOptions) {
            action.generateCallSign(adjectives, nouns)
        }

        private fun FindCallSignAction.generateCallSign(adjectives: Set<String>, nouns: Set<String>) =
            execute(GenerateCallSignAction(adjectives, nouns, email, players))
    }
}
