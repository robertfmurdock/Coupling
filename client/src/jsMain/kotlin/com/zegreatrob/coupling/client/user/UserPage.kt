package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc

val UserPage by nfc<PageProps> {
    add(
        CouplingQuery(
            commander = it.commander,
            query = graphQuery { user() },
            toDataprops = { _, _, result -> UserConfig(result.user) },
        ),
    )
}
