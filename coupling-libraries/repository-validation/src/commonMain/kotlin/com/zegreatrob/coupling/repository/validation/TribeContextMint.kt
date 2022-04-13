package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User

abstract class TribeContextMint<R : Any> : TribeContext<R> {
    override lateinit var repository: R
    override lateinit var clock: MagicClock
    override lateinit var user: User
    override var tribeId: PartyId = PartyId("NO INIT")
}

interface TribeContext<R> : SharedContext<R> {
    val tribeId: PartyId
}

data class TribeContextData<R>(
    override val repository: R,
    override val tribeId: PartyId,
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