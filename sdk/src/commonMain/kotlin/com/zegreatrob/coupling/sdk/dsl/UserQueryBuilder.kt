package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.GqlUser

class UserQueryBuilder : QueryBuilder<GqlUser> {
    override var output: GqlUser =
        GqlUser(boost = null, details = null, "", subscription = null)

    fun details() = also { output = output.copy(details = GqlReference.user) }
    fun boost() = also { output = output.copy(boost = GqlReference.boost) }
    fun subscription() = also { output = output.copy(subscription = GqlReference.subscription) }
}
