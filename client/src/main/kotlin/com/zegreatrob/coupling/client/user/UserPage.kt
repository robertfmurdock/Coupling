package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc

val UserPage by nfc<PageProps> {
    add(
        CouplingQuery(
            commander = it.commander,
            query = UserQuery(),
            toDataprops = { _, _, data -> UserConfig(data) },
        ),
    )
}
