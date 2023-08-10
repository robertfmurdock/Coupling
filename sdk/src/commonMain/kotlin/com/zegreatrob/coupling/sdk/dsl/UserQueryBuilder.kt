package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.JsonUser

class UserQueryBuilder : QueryBuilder<JsonUser> {
    override var output: JsonUser = JsonUser("", null, null)

    fun details() = also { output = output.copy(details = GqlReference.user) }
    fun boost() = also { output = output.copy(boost = GqlReference.boost) }
    fun subscription() = also { output = output.copy(subscription = GqlReference.subscription) }
}
