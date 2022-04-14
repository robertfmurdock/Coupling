package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId

object Paths {
    fun welcome() = "/welcome"
    fun partyList() = "/tribes/"
    fun PartyId.pinListPath() = "/$value/pins"
    fun PartyId.currentPairsPage() = "/$value/pairAssignments/current/"
    fun Party.newPairAssignmentsPath() = "/${id.value}/pairAssignments/new"
    fun PartyElement<Player>.playerConfigPage() = "/${id.value}/player/${element.id}/"
}
