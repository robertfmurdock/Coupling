package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.pairassignments.spin.encodeURIComponent
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

object Paths {
    fun welcome() = "/welcome"
    fun tribeList() = "/tribes/"
    fun TribeId.pinListPath() = "/$value/pins"
    fun TribeId.currentPairsPage() = "/$value/pairAssignments/current/"
    fun Tribe.tribeConfigPath() = "/${id.value}/edit/"
    fun newPairAssignmentsPath(
        tribe: Tribe,
        playerSelections: List<Pair<Player, Boolean>>,
        pinSelections: List<Pin>
    ) = "/${tribe.id.value}/pairAssignments/new?${buildQueryString(playerSelections, pinSelections)}"

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
}