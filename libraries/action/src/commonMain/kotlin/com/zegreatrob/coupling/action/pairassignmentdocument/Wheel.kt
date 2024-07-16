package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.player.Player
import kotlin.random.Random

interface Wheel {

    val random: Random get() = Random.Default

    fun Array<Player>.spin(): Player = random(random)
}
