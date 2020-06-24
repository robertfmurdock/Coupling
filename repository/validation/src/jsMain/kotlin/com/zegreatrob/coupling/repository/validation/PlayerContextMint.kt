package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User

abstract class PlayerContextMint<R : Any> : TribeSharedContext<R> {
    override lateinit var repository: R
    override lateinit var clock: MagicClock
    override lateinit var user: User
    override var tribeId: TribeId = TribeId("NO INIT")
}

interface TribeSharedContext<R> : SharedContext<R> {
    val tribeId: TribeId
}

data class TribeSharedContextData<R>(
    override val repository: R,
    override val tribeId: TribeId,
    override val clock: MagicClock,
    override val user: User
) : TribeSharedContext<R>

fun <C : PlayerContextMint<R>, R> C.bind(): suspend (TribeSharedContext<R>) -> C =
    { parent: TribeSharedContext<R> ->
        also {
            repository = parent.repository
            clock = parent.clock
            user = parent.user
            tribeId = parent.tribeId
        }
    }