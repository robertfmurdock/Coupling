package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.CommandFunc2
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.invoke
import com.zegreatrob.coupling.client.user.GoogleSignInCommand
import com.zegreatrob.coupling.client.user.GoogleSignInCommandDispatcher
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.div
import kotlin.browser.window


data class LoginChooserProps(val commandFunc: CommandFunc2<GoogleSignInCommandDispatcher>) : RProps

private val styles = useStyles("LoginChooser")

val LoginChooser = reactFunction<LoginChooserProps> { (commandFunc) ->
    val googleSignInFunc = commandFunc({ GoogleSignInCommand }) { window.location.pathname = "/" }
    div(classes = styles.className) {
        div {
            div(classes = "google-login super white button") {
                attrs { onClickFunction = { googleSignInFunc() } }
                +"Google"
            }
        }
        div {
            div(classes = "ms-login super blue button") {
                attrs { onClickFunction = { window.location.pathname = "/microsoft-login" } }
                +"Microsoft"
            }
        }
    }
}

fun RBuilder.loginChooser(commandFunc: CommandFunc2<GoogleSignInCommandDispatcher>) =
    LoginChooser.render(this)(LoginChooserProps(commandFunc))
