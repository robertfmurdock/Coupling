package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.external.react.RFunction
import com.zegreatrob.coupling.client.external.react.ReactComponentRenderer
import com.zegreatrob.coupling.client.external.react.ReactScopeProvider
import com.zegreatrob.coupling.client.external.react.loadStyles
import com.zegreatrob.coupling.client.user.GoogleSignIn
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.sdk.SdkSingleton
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.buildElement
import react.dom.div
import kotlin.browser.window

interface LoginChooserCss {
    val className: String
}

interface LoginChooserRenderer {

    fun RBuilder.loginChooser() = element(loginChooser, object : RProps {})

    companion object : ReactComponentRenderer, GoogleSignIn, ReactScopeProvider, Sdk by SdkSingleton {
        private val styles = loadStyles<LoginChooserCss>("LoginChooser")

        private val loginChooser = {
            val scope = useScope("LoginChooser")
            buildElement {
                div(classes = styles.className) {
                    div {
                        div(classes = "google-login super white button") {
                            attrs { onClickFunction = { scope.launch { signIn() } } }
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
        }.unsafeCast<RFunction<RProps>>()
    }

}
