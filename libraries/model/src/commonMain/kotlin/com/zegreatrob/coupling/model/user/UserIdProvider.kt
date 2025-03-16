package com.zegreatrob.coupling.model.user

import kotools.types.text.NotBlankString

interface UserIdProvider {
    val userId: NotBlankString
}

interface CurrentUserProvider {
    val currentUser: UserDetails
}

interface CurrentUserIdProvider :
    CurrentUserProvider,
    UserIdProvider {
    override val userId get() = currentUser.email
}

interface AuthorizedPartyIdsProvider : CurrentUserProvider {
    fun authorizedPartyIds() = currentUser.authorizedPartyIds
}
