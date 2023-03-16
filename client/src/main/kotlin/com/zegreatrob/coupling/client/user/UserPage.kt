package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.create
import react.FC

val UserPage = FC<PageProps> {
    +CouplingQuery(
        commander = it.commander,
        query = UserQuery(),
        toDataprops = { _, _, data -> UserConfig(data) },
    ).create()
}
