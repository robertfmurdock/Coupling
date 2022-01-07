package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.child
import react.FC
import react.Props
import react.dom.html.ReactHTML.div


private val styles = useStyles("LoginChooser")

val LoginChooser = FC<Props> {
    val (_, _, _, _, loginWithRedirect, _) = useAuth0Data()
    val signInFunc = { loginWithRedirect() }
    div {
        className = styles.className
        div {
            child(CouplingButton(supersize, white, styles["loginButton"], signInFunc)) {
                +"Login"
            }
        }
    }
}
