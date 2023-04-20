package com.zegreatrob.coupling.action.player.callsign

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.SimpleExecutableAction

data class FindCallSignOptionsAction(val players: List<Player>) :
    SimpleExecutableAction<FindCallSignOptionsActionDispatcher, CallSignOptions> {
    override val performFunc = link(FindCallSignOptionsActionDispatcher::perform)
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

interface FindCallSignOptionsActionDispatcher {
    fun perform(action: FindCallSignOptionsAction) = CallSignOptions(
        adjectives = defaultCallSignOptions.adjectives - action.players.adjectives(),
        nouns = defaultCallSignOptions.nouns - action.players.nouns(),
    )

    private fun List<Player>.adjectives() = mapNotNull { it.callSignAdjective }
    private fun List<Player>.nouns() = mapNotNull { it.callSignNoun }
}
