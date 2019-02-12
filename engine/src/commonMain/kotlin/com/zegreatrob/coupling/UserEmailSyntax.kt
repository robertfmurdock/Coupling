package com.zegreatrob.coupling

import com.zegreatrob.coupling.common.entity.user.User

interface UserEmailSyntax {
    val userEmail: String
}

interface AuthenticatedUserEmailSyntax : UserEmailSyntax {
    val user: User
    override val userEmail get() = user.email
}

interface UserAuthorizedTribeIdsSyntax {
    val user: User
    fun userAuthorizedTribeIds() = user.authorizedTribeIds
}