package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

object Paths {
    fun welcome() = "/welcome"
    fun partyList() = "/parties/"
    fun PartyId.pinListPath() = "/$value/pins"

    @OptIn(ExperimentalKotoolsTypesApi::class)
    fun PartyId.newPlayerConfigPath() = playerConfigUrl(this, NotBlankString.create("new"))
    fun PartyId.currentPairsPath() = "/$value/pairAssignments/current/"
    fun PartyDetails.newPairAssignmentsPath() = "/${id.value}/pairAssignments/new"
    fun PartyElement<Player>.playerConfigPath() = playerConfigUrl(partyId, element.id.value)

    private fun playerConfigUrl(partyId: PartyId, playerIdValue: NotBlankString): String = "/${partyId.value}/player/$playerIdValue/"
}
