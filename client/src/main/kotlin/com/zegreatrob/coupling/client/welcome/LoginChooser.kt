package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.EmptyProps
import com.zegreatrob.coupling.client.reactFunction
import kotlinx.browser.window
import org.w3c.dom.get
import react.RBuilder
import react.dom.div


private val styles = useStyles("LoginChooser")

val LoginChooser = reactFunction<EmptyProps> {
    val signInFunc = { window.location.pathname = "${window["basename"] ?: ""}/auth0-login" }
    div(classes = styles.className) {
        div {
            couplingButton(supersize, white, styles["loginButton"], signInFunc) { +"Login" }
        }
    }
}

fun RBuilder.loginChooser() = child(LoginChooser)
