package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User

abstract class PartyContextMint<R : Any> : PartyContext<R> {
    override lateinit var repository: R
    override lateinit var clock: MagicClock
    override lateinit var user: User
    override var partyId: PartyId = PartyId("NO INIT")
}

interface PartyContext<R> : SharedContext<R> {
    val partyId: PartyId
}

data class PartyContextData<R>(
    override val repository: R,
    override val partyId: PartyId,
    override val clock: MagicClock,
    override val user: User
) : PartyContext<R>

fun <C : PartyContextMint<R>, R> C.bind(): suspend (PartyContext<R>) -> C = { parent: PartyContext<R> ->
    also {
        repository = parent.repository
        clock = parent.clock
        user = parent.user
        partyId = parent.partyId
    }
}