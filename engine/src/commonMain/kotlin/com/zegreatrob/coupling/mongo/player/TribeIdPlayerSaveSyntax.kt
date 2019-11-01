package com.zegreatrob.coupling.mongo.player

import com.zegreatrob.coupling.core.entity.player.TribeIdPlayer

interface TribeIdPlayerSaveSyntax {

    val playerRepository: PlayerSaver

    suspend fun TribeIdPlayer.save() = playerRepository.save(this)

}