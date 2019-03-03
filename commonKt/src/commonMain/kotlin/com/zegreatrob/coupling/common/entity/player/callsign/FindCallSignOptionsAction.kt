package com.zegreatrob.coupling.common.entity.player.callsign

import com.zegreatrob.coupling.common.entity.player.Player

data class FindCallSignOptionsAction(val players: List<Player>)

data class CallSignOptions(val adjectives: Set<String>, val nouns: Set<String>)

val defaultCallSignOptions = CallSignOptions(
        adjectives = setOf(
                "Swift",
                "Angry",
                "Hyper",
                "Fierce",
                "Crazy",
                "Intense",
                "Secure",
                "Relaxed",
                "Modest",
                "Mild"
        ),
        nouns = setOf(
                "Gazelle",
                "Goose",
                "Maverick",
                "Mongoose",
                "Wildebeast",
                "Varmit",
                "Mosquito",
                "Muskrat",
                "Mouse",
                "Squirrel",
                "Tiger",
                "Lion",
                "Wolf",
                "Shrew",
                "Bat",
                "Duck",
                "Wolverine",
                "Buckeye"
        )
)

interface FindCallSignOptionsActionDispatcher {
    fun FindCallSignOptionsAction.perform() = CallSignOptions(
            adjectives = defaultCallSignOptions.adjectives - players.adjectives(),
            nouns = defaultCallSignOptions.nouns - players.nouns()
    )

    private fun List<Player>.adjectives() = mapNotNull { it.callSignAdjective }
    private fun List<Player>.nouns() = mapNotNull { it.callSignNoun }
}