package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User

abstract class TribeContextMint<R : Any> : TribeContext<R> {
    override lateinit var repository: R
    override lateinit var clock: MagicClock
    override lateinit var user: User
    override var tribeId: TribeId = TribeId("NO INIT")
}

interface TribeContext<R> : SharedContext<R> {
    val tribeId: TribeId
}

data class TribeContextData<R>(
    override val repository: R,
    override val tribeId: TribeId,
    override val clock: MagicClock,
    override val user: User
) : TribeContext<R>

fun <C : TribeContextMint<R>, R> C.bind(): suspend (TribeContext<R>) -> C = { parent: TribeContext<R> ->
    also {
        repository = parent.repository
        clock = parent.clock
        user = parent.user
        tribeId = parent.tribeId
    }
}