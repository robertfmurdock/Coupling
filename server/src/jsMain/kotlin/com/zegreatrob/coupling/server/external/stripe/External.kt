package com.zegreatrob.coupling.server.external.stripe

import kotlin.js.Promise

@JsModule("stripe")
external fun stripe(secretKey: String): Stripe

external interface Stripe {
    val setupIntents: StripeSetupIntents
}

external interface StripeSetupIntents {
    fun create(options: StripeSetupIntentCreateOptions): Promise<StripeSetupIntent>
}

external interface StripeSetupIntentCreateOptions {
    @JsName("payment_method_types")
    var paymentMethodTypes: Array<String>
}

external interface StripeSetupIntent {
    @JsName("client_secret")
    var clientSecret: String?
}
