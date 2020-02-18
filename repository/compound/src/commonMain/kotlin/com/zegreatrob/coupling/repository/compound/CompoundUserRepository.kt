package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserGet
import com.zegreatrob.coupling.repository.user.UserRepository

class CompoundUserRepository(val repository1: UserRepository, val repository2: UserRepository) : UserRepository,
    UserGet by repository1 {

    override suspend fun save(user: User) = arrayOf(repository1, repository2).forEach { it.save(user) }

}