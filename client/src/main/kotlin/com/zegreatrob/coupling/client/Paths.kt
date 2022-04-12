package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.PartyElement
import com.zegreatrob.coupling.model.tribe.PartyId

object Paths {
    fun welcome() = "/welcome"
    fun tribeList() = "/tribes/"
    fun PartyId.pinListPath() = "/$value/pins"
    fun PartyId.currentPairsPage() = "/$value/pairAssignments/current/"
    fun Party.tribeConfigPath() = "/${id.value}/edit/"
    fun Party.newPairAssignmentsPath() = "/${id.value}/pairAssignments/new"
    fun PartyElement<Player>.playerConfigPage() = "/${id.value}/player/${element.id}/"
}
