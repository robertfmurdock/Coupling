@file:JsModule("@stripe/react-stripe-js")

package com.zegreatrob.coupling.client.components.external.stripe

import react.Props
import react.ReactElement

external val Elements: ReactElement<ElementsProps>

external interface ElementsProps : Props {
    var stripe: Stripe
    var options: ElementsOptions
}

external interface ElementsOptions {
    val clientSecret: String
}
