package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.dom.blue
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.user.GoogleSignInCommand
import com.zegreatrob.coupling.client.user.GoogleSignInCommandDispatcher
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.RProps
import react.dom.div
import kotlinx.browser.window

data class LoginChooserProps(val dispatchFunc: DispatchFunc<out GoogleSignInCommandDispatcher>) : RProps

private val styles = useStyles("LoginChooser")

val LoginChooser = reactFunction { (commandFunc): LoginChooserProps ->
    val googleSignInFunc = commandFunc({ GoogleSignInCommand }) { window.location.pathname = "/" }
    val msSignInFunc = { window.location.pathname = "/microsoft-login" }
    div(classes = styles.className) {
        div {
            couplingButton(supersize, white, styles["googleLoginButton"], googleSignInFunc) {
                +"Google"
            }
        }
        div {
            couplingButton(supersize, blue, styles["microsoftLoginButton"], msSignInFunc) { +"Microsoft" }
        }
    }
}

fun RBuilder.loginChooser(dispatchFunc: DispatchFunc<out GoogleSignInCommandDispatcher>) =
    child(LoginChooser, LoginChooserProps(dispatchFunc))
