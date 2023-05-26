package com.zegreatrob.coupling.client.external.react.ga

@JsModule("react-ga")
external val ReactGA: ReactGoogleAnalytics

external interface ReactGoogleAnalytics {
    fun initialize(id: String)

    @Suppress("SpellCheckingInspection")
    fun pageview(pageUrl: String)
}
