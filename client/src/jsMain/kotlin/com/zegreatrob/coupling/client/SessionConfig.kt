package com.zegreatrob.coupling.client

import kotlinx.browser.window

object SessionConfig {
    val animationsDisabled get() = window.sessionStorage.getItem("animationDisabled") == "true"
    val thirdPartyAvatarsDisabled get() = window.sessionStorage.getItem("thirdPartyAvatarsDisabled") == "true"
}
