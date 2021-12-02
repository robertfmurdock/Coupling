package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.Frame
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

sealed class DemoAnimationState {
    abstract fun next(): Pair<DemoAnimationState, Int>?

    companion object {
        fun generateSequence(): Sequence<Frame<DemoAnimationState>> =
            generateSequence(Frame(Start, 0)) { (state, time) ->
                state.next()
                    ?.let { (next, duration) -> Frame(next, time + duration) }
            }
    }
}

object Start : DemoAnimationState() {
    override fun next() = Pair(ShowIntro, 3000)
}

object ShowIntro : DemoAnimationState() {
    override fun next() = Pair(
        MakeTribe(
            Tribe(
                id = TribeId(""),
                name = "New Tribe",
                defaultBadgeName = "Default",
                alternateBadgeName = "Alternate"
            )
        ), 200
    )
}

data class MakeTribe(val tribe: Tribe) : DemoAnimationState() {
    override fun next() = Pair(AddPlayer1(tribe, Player()), 200)
}

data class AddPlayer1(val tribe: Tribe, val player: Player) : DemoAnimationState() {
    override fun next() = null
}
