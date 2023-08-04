package com.zegreatrob.coupling.model.user

interface UserIdProvider {
    val userId: String
}

interface CurrentUserProvider {
    val currentUser: UserDetails
}

interface CurrentUserIdProvider : CurrentUserProvider, UserIdProvider {
    override val userId get() = currentUser.email
}

interface AuthorizedPartyIdsProvider : CurrentUserProvider {
    fun authorizedPartyIds() = currentUser.authorizedPartyIds
}
