package com.zegreatrob.coupling.model.user

interface UserIdProvider {
    val userId: UserId
}

interface CurrentUserProvider {
    val currentUser: UserDetails
}

interface CurrentUserIdProvider :
    CurrentUserProvider,
    UserIdProvider {
    override val userId get() = currentUser.id
}
