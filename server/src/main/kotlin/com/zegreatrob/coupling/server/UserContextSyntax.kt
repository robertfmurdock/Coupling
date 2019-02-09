package com.zegreatrob.coupling.server

interface UserContextSyntax {
    val userContext: UserContext
    fun username() = userContext.username
}