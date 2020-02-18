package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdRetiredPlayerRecordsSyntax {
    val playerRepository: PlayerListGetDeleted
    suspend fun TribeId.loadRetiredPlayerRecords() = playerRepository.getDeleted(this)
}