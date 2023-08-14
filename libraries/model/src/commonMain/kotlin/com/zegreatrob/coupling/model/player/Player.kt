package com.zegreatrob.coupling.model.player

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.PartyElement

data class Player(
    val id: String = "${uuid4()}",
    val badge: Int = Badge.Default.value,
    val name: String = "",
    val email: String = "",
    val callSignAdjective: String = "",
    val callSignNoun: String = "",
    val imageURL: String? = null,
    val avatarType: AvatarType?,
)

val defaultPlayer = Player(id = "DEFAULT", avatarType = null)

val PartyElement<Player>.player get() = element

fun List<PartyRecord<Player>>.pairCombinations() = mapIndexed { index, player ->
    slice(index + 1..lastIndex).pairsWith(player)
}.flatten()
    .plus(map { PlayerPair(listOf(it)) })

fun List<Player>.toPairCombinations() = mapIndexed { index, player ->
    slice(index + 1..lastIndex).toPairsWith(player)
}.flatten()

private fun List<Player>.toPairsWith(player: Player) = map { otherPlayer -> pairOf(player, otherPlayer) }

private fun List<PartyRecord<Player>>.pairsWith(player: PartyRecord<Player>) = map {
    PlayerPair(listOf(player, it))
}
