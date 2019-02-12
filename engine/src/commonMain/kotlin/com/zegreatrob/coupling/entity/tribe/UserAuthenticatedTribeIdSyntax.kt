package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.UserContextSyntax
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId

interface UserAuthenticatedTribeIdSyntax : UserContextSyntax {

    fun List<TribeIdPlayer>.authenticatedFilter() = authenticatedTribeIds().authenticatedFilter()

    fun List<TribeIdPlayer>.authenticatedTribeIds() = map { it.tribeId } + userContext.tribeIds.map(::TribeId)

    private fun List<TribeId>.authenticatedFilter(): (KtTribe) -> Boolean = { contains(it.id) }
}