package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdPlayersSyntax : TribeIdPlayerRecordsListSyntax {
    suspend fun TribeId.getPlayerList() = getPlayerRecords().map { it.data.element }
}
