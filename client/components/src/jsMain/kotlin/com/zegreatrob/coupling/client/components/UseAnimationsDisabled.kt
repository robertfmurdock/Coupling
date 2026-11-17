package com.zegreatrob.coupling.client.components

import kotlinx.browser.window
import kotlinx.coroutines.awaitCancellation
import org.w3c.dom.events.EventListener
import react.use
import react.useEffectOnce
import react.useMemo
import react.useState

fun useAnimationsDisabled(): Boolean {
    val explicitlyDisabled = use(animationsDisabledContext)
    val preference = useMemo { window.matchMedia("(prefers-reduced-motion: reduce)") }
    var reducedMotion by useState(preference.matches)
    useEffectOnce {
        val listener = EventListener { reducedMotion = preference.matches }
        preference.addEventListener("change", listener)
        reducedMotion = preference.matches
        try {
            awaitCancellation()
        } finally {
            preference.removeEventListener("change", listener)
        }
    }
    return explicitlyDisabled || reducedMotion
}
