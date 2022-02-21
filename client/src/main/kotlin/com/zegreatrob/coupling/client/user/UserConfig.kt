package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div

private val styles = useStyles("user/UserConfig")

data class UserConfig(val user: User) : DataProps<UserConfig> {
    override val component = userConfig
}

private val userConfig = tmFC<UserConfig> { (user) ->
    div {
        className = styles.className
        div { +"User ID: ${user.id}" }
        div { +"User Email: ${user.email}" }
        div { +"This user owns these tribes:" }
        user.authorizedTribeIds
            .map { it.value }
            .forEach { id ->
                div { +"Tribe ID: $id" }
            }
    }
}
