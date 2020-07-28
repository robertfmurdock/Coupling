package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.dom.blue
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.user.GoogleSignInCommand
import com.zegreatrob.coupling.client.user.GoogleSignInCommandDispatcher
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.div
import kotlin.browser.window

data class LoginChooserProps(val dispatchFunc: DispatchFunc<out GoogleSignInCommandDispatcher>) : RProps

private val styles = useStyles("LoginChooser")

val LoginChooser = reactFunction { (commandFunc): LoginChooserProps ->
    val googleSignInFunc = commandFunc({ GoogleSignInCommand }) { window.location.pathname = "/" }
    div(classes = styles.className) {
        div {
            couplingButton(supersize, white, "google-login") {
                attrs {
                    onClickFunction = { googleSignInFunc() }
                }
                +"Google"
            }
        }
        div {
            couplingButton(supersize, blue, "ms-login") {
                attrs {
                    onClickFunction = { window.location.pathname = "/microsoft-login" }
                }
                +"Microsoft"
            }
        }
    }
}

fun RBuilder.loginChooser(dispatchFunc: DispatchFunc<out GoogleSignInCommandDispatcher>) =
    child(LoginChooser, LoginChooserProps(dispatchFunc))
