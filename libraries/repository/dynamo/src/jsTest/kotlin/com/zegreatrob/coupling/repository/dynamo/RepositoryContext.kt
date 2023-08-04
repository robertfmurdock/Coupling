package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.stubUser

interface RepositoryContext<T> {
    val repository: T
    val user: UserDetails
    val clock: MagicClock

    companion object {
        fun <T, R> buildRepository(
            setupContext: (RepositoryContext<R>) -> T,
            repoBuilder: suspend (UserDetails, MagicClock) -> R,
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
