package com.zegreatrob.coupling.action.player.callsign

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.SimpleExecutableAction

data class FindCallSignOptionsAction(val players: List<Player>) :
    SimpleExecutableAction<FindCallSignOptionsAction.Dispatcher, CallSignOptions> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        fun perform(action: FindCallSignOptionsAction) = CallSignOptions(
            adjectives = defaultCallSignOptions.adjectives - action.players.adjectives().toSet(),
            nouns = defaultCallSignOptions.nouns - action.players.nouns().toSet(),
        )

        private fun List<Player>.adjectives() = mapNotNull { it.callSignAdjective }
        private fun List<Player>.nouns() = mapNotNull { it.callSignNoun }
    }
}

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
        "Mild",
    ),
    nouns = setOf(
        "Gazelle",
        "Goose",
        "Maverick",
        "Mongoose",
        "Wildebeest",
        "Varmint",
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
        "Buckeye",
    ),
)
