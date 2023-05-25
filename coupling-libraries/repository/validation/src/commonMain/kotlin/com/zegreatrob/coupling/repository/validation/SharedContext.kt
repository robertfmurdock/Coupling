package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.user.User

interface SharedContext<R> {
    val repository: R
    val clock: MagicClock
    val user: User
}

data class SharedContextData<R>(
    override val repository: R,
    override val clock: MagicClock,
    override val user: User,
) : SharedContext<R>

fun <C : ContextMint<R>, R> C.bind(): suspend (SharedContext<R>) -> C =
    { parent: SharedContext<R> ->
        also {
            repository = parent.repository
            clock = parent.clock
            user = parent.user
        }
    }

abstract class ContextMint<R : Any> : SharedContext<R> {
    override lateinit var repository: R
    override lateinit var clock: MagicClock
    override lateinit var user: User
}
