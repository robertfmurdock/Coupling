package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.player.TribeIdPlayer

interface TribeIdPlayerSaveSyntax {
    val playerRepository: PlayerSave
    suspend fun TribeIdPlayer.save() = playerRepository.save(this)
}