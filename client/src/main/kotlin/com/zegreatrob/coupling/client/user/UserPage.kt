package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import react.FC

private val LoadedUserConfig by lazy { couplingDataLoader<UserConfig>() }

val UserPage = FC<PageProps> {
    +dataLoadProps(
        component = LoadedUserConfig,
        commander = it.commander,
        query = UserQuery(),
        toProps = { _, _, data -> UserConfig(data) }
    ).create()
}
