@file:JsModule("@stripe/react-stripe-js")

package com.zegreatrob.coupling.client.components.external.stripe

import react.FC
import react.Props
import kotlin.js.Promise

external val Elements: FC<ElementsProps>

external interface ElementsProps : Props {
    var stripe: Promise<Stripe>
    var options: ElementsOptions
}

sealed external interface ElementsOptions {
    var clientSecret: String
}
