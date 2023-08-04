package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.user.UserGet
import com.zegreatrob.coupling.repository.user.UserGetByEmail
import com.zegreatrob.coupling.repository.user.UserRepository

class CompoundUserRepository(private val repository1: UserRepository, private val repository2: UserRepository) :
    UserRepository,
    UserGet by repository1,
    UserGetByEmail by repository1 {

    override suspend fun save(user: UserDetails) = arrayOf(repository1, repository2).forEach { it.save(user) }
}
