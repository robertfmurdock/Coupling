package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.child
import kotlinx.browser.window
import org.w3c.dom.get
import react.FC
import react.Props
import react.dom.html.ReactHTML.div


private val styles = useStyles("LoginChooser")

val LoginChooser = FC<Props> {
    val signInFunc = { window.location.pathname = "${window["basename"] ?: ""}/auth0-login" }
    div {
        className = styles.className
        div {
            child(CouplingButton(supersize, white, styles["loginButton"], signInFunc) { +"Login" })
        }
    }
}
