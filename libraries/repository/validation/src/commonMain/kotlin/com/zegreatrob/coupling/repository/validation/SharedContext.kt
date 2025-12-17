package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.user.UserDetails

interface SharedContext<R> {
    val repository: R
    val clock: MagicClock
    val user: UserDetails
}

data class SharedContextData<R>(
    override val repository: R,
    override val clock: MagicClock,
    override val user: UserDetails,
) : SharedContext<R>

fun <C : ContextMint<R>, R : Any> C.bind(): suspend (SharedContext<R>) -> C = { parent: SharedContext<R> ->
    also {
        repository = parent.repository
        clock = parent.clock
        user = parent.user
    }
}

abstract class ContextMint<R : Any> : SharedContext<R> {
    override lateinit var repository: R
    override lateinit var clock: MagicClock
    override lateinit var user: UserDetails
}
