package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.GqlUser
import com.zegreatrob.coupling.model.user.UserId
import kotools.types.text.toNotBlankString

class UserQueryBuilder : QueryBuilder<GqlUser> {
    override var output: GqlUser = GqlUser(
        id = UserId("-".toNotBlankString().getOrThrow()),
        boost = null,
        details = null,
        subscription = null,
    )

    fun details() = also { output = output.copy(details = GqlReference.user) }
    fun boost() = also { output = output.copy(boost = GqlReference.boost) }
    fun subscription() = also { output = output.copy(subscription = GqlReference.subscription) }
}
