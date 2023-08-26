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

sealed external interface StripeListResult<T> {
    val data: Array<T>
}

external interface StripeSubscriptionsApi {
    fun list(options: StripeSubscriptionsListOptions): Promise<StripeListResult<StripeSubscription>>
}

sealed external interface StripeCustomersCreateOptions {
    var email: String
}

sealed external interface StripeCustomersListOptions {
    var email: String
}

sealed external interface StripeSubscriptionsListOptions {
    var customer: String
}

sealed external interface StripeCustomer {
    val id: String
}

sealed external interface StripeSubscription {
    val id: String
    val status: String

    @JsName("current_period_end")
    val currentPeriodEnd: Int

    @JsName("current_period_start")
    val currentPeriodStart: Int
}

external interface StripeSetupIntentsApi {
    fun create(options: StripeSetupIntentCreateOptions): Promise<StripeSetupIntent>
}

sealed external interface StripeSetupIntentCreateOptions {
    var customer: String

    @JsName("payment_method_types")
    var paymentMethodTypes: Array<String>
}

sealed external interface StripeSetupIntent {
    @JsName("client_secret")
    var clientSecret: String?
}
