package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import kotlinx.browser.window
import org.w3c.dom.get
import react.Props
import react.RBuilder
import react.dom.div
import react.fc


private val styles = useStyles("LoginChooser")

val LoginChooser = fc<Props> {
    val signInFunc = { window.location.pathname = "${window["basename"] ?: ""}/auth0-login" }
    div(classes = styles.className) {
        div {
            child(CouplingButton(supersize, white, styles["loginButton"], signInFunc, {}, fun RBuilder.() {
 +"Login"
}))
        }
    }
}

fun RBuilder.loginChooser() = child(LoginChooser)
