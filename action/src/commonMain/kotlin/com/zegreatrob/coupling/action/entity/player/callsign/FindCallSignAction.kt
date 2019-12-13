package com.zegreatrob.coupling.action.entity.player.callsign

import com.zegreatrob.coupling.model.player.Player

data class FindCallSignAction(val players: List<Player>, val email: String)

interface FindCallSignActionDispatcher : GenerateCallSignActionDispatcher {

    fun FindCallSignAction.perform() = defaultCallSignOptions
        .let { (adjectives, nouns) ->
            generateCallSign(adjectives, nouns)
        }

    private fun FindCallSignAction.generateCallSign(adjectives: Set<String>, nouns: Set<String>) =
        GenerateCallSignAction(adjectives, nouns, email, players)
            .perform()

}