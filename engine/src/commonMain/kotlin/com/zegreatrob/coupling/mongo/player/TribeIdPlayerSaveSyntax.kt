package com.zegreatrob.coupling.mongo.player

import com.zegreatrob.coupling.model.player.PlayerSaver
import com.zegreatrob.coupling.model.player.TribeIdPlayer

interface TribeIdPlayerSaveSyntax {

    val playerRepository: PlayerSaver

    suspend fun TribeIdPlayer.save() = playerRepository.save(this)

}