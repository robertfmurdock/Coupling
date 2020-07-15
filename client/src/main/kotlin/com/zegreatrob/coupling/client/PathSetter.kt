package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.pairassignments.spin.encodeURIComponent
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

typealias PathSetter = (String) -> Unit

fun PathSetter.tribeList() = this("/tribes/")

fun PathSetter.pinList(tribeId: TribeId) = this(tribeId.pinListPath())

private fun TribeId.pinListPath() = "/$value/pins"

fun PathSetter.playerConfig(tribeId: TribeId, player: Player) = this("/${tribeId.value}/player/${player.id}/")

fun PathSetter.currentPairs(tribeId: TribeId) = this(tribeId.currentPairsPage())
private fun TribeId.currentPairsPage() = "/$value/pairAssignments/current/"

fun PathSetter.tribeConfig(tribe: Tribe) = this("/${tribe.id.value}/edit/")

fun PathSetter.newPairAssignments(
    tribe: Tribe,
    playerSelections: List<Pair<Player, Boolean>>,
    pinSelections: List<Pin>
) = this(
    "/${tribe.id.value}/pairAssignments/new?${buildQueryString(playerSelections, pinSelections)}"
)

private fun buildQueryString(playerSelections: List<Pair<Player, Boolean>>, pinSelections: List<Pin>) =
    (playerSelections.buildQueryParameters() + pinSelections.buildQueryParameters())
        .toQueryString()

private fun List<Pair<Player, Boolean>>.buildQueryParameters() = filter { (_, isSelected) -> isSelected }
    .map { it.first.id }.toProperty("player")

private fun List<Pin>.buildQueryParameters() = map { it._id }.toProperty("pin")
private fun List<Pair<String, String?>>.toQueryString() = toList().joinToString("&") { (propName, id) ->
    "$propName=${encodeURIComponent(id)}"
}

private fun List<String?>.toProperty(propName: String) = map { propName to it }
