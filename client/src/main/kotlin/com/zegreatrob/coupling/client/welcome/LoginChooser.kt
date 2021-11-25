package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.user.GoogleSignInCommandDispatcher
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import org.w3c.dom.get
import react.RBuilder
import react.Props
import react.dom.div

data class LoginChooseProps(val dispatchFunc: DispatchFunc<out GoogleSignInCommandDispatcher>) : Props

private val styles = useStyles("LoginChooser")

val LoginChooser = reactFunction { (_): LoginChooseProps ->
    val signInFunc = { window.location.pathname = "${window["basename"] ?:""}/auth0-login" }
    div(classes = styles.className) {
        div {
            couplingButton(supersize, white, styles["loginButton"], signInFunc) { +"Login" }
        }
    }
}

fun RBuilder.loginChooser(dispatchFunc: DispatchFunc<out GoogleSignInCommandDispatcher>) =
    child(LoginChooser, LoginChooseProps(dispatchFunc))
