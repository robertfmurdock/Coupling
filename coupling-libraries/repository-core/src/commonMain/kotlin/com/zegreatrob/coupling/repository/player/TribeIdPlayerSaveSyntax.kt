package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player

interface TribeIdPlayerSaveSyntax {
    val playerRepository: PlayerSave
    suspend fun PartyElement<Player>.save() =
        playerRepository.save(this)
}