package com.zegreatrob.coupling.server.external.stripe

import kotlin.js.Promise

@JsModule("stripe")
external fun stripe(secretKey: String): Stripe

external interface Stripe {
    val customers: StripeCustomersApi
    val subscriptions: StripeSubscriptionsApi
    val setupIntents: StripeSetupIntentsApi
}

external interface StripeCustomersApi {
    fun create(options: StripeCustomersCreateOptions): Promise<StripeCustomer>
    fun list(options: StripeCustomersListOptions): Promise<StripeListResult<StripeCustomer>>
}

external interface StripeListResult<T> {
    val data: Array<T>
}

external interface StripeSubscriptionsApi {
    fun list(options: StripeSubscriptionsListOptions): Promise<StripeListResult<StripeSubscription>>
}

external interface StripeCustomersCreateOptions {
    var email: String
}

external interface StripeCustomersListOptions {
    var email: String
}

external interface StripeSubscriptionsListOptions {
    var customer: String
}

external interface StripeCustomer {
    val id: String
}

external interface StripeSubscription {
    val id: String
    val status: String
}

external interface StripeSetupIntentsApi {
    fun create(options: StripeSetupIntentCreateOptions): Promise<StripeSetupIntent>
}

external interface StripeSetupIntentCreateOptions {
    var customer: String

    @JsName("payment_method_types")
    var paymentMethodTypes: Array<String>
}

external interface StripeSetupIntent {
    @JsName("client_secret")
    var clientSecret: String?
}
