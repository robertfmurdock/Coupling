package com.zegreatrob.coupling.common.entity.player.callsign

import com.zegreatrob.coupling.core.entity.player.Player

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