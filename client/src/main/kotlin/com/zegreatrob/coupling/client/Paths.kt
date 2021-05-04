package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId

object Paths {
    fun welcome() = "/welcome"
    fun tribeList() = "/tribes/"
    fun TribeId.pinListPath() = "/$value/pins"
    fun TribeId.currentPairsPage() = "/$value/pairAssignments/current/"
    fun Tribe.tribeConfigPath() = "/${id.value}/edit/"
    fun newPairAssignmentsPath(tribe: Tribe) = "/${tribe.id.value}/pairAssignments/new"

    fun TribeElement<Player>.playerConfigPage() = "/${id.value}/player/${element.id}/"
}
