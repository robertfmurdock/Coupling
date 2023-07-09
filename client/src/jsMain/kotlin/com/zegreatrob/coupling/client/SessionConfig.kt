package com.zegreatrob.coupling.client

import kotlinx.browser.window

object SessionConfig {
    val animationsDisabled get() = window.sessionStorage.getItem("animationDisabled") == "true"
}
