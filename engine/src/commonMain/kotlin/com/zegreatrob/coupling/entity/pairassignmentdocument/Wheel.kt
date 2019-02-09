package com.zegreatrob.coupling.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.player.Player
import kotlin.random.Random

interface Wheel {

    val random: Random get() = Random.Default

    fun Array<Player>.spin(): Player = random(random)

}
