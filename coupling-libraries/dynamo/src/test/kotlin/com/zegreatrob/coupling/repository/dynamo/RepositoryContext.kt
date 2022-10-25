package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.stubUser

interface RepositoryContext<T> {
    val repository: T
    val user: User
    val clock: MagicClock

    companion object {
        fun <T, R> buildRepository(
            setupContext: (RepositoryContext<R>) -> T,
            repoBuilder: suspend (User, MagicClock) -> R
        ): suspend () -> T = {
            val clock = MagicClock()
            val user = stubUser()
            val repository = repoBuilder(user, clock)
            setupContext(object : RepositoryContext<R> {
                override val repository = repository
                override val user = user
                override val clock = clock
            })
        }
    }
}
