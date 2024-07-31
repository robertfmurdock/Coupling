package com.zegreatrob.coupling.model.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.PartyElement

data class Player(
    val id: String,
    val badge: Int,
    val name: String,
    val email: String,
    val callSignAdjective: String,
    val callSignNoun: String,
    val imageURL: String?,
    val avatarType: AvatarType?,
    val additionalEmails: Set<String>,
)

val Player.displayName get() = name.ifBlank { "Unknown" }

fun Player.matches(email: String) = emails.map(String::lowercase).contains(email.lowercase())

val Player.emails get() = listOf(email) + additionalEmails

val defaultPlayer = Player(
    id = "DEFAULT",
    badge = Badge.Default.value,
    name = "",
    email = "",
    callSignAdjective = "",
    callSignNoun = "",
    imageURL = null,
    avatarType = null,
    additionalEmails = emptySet(),
)

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
