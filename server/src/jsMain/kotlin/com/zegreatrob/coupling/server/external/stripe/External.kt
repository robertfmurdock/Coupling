package com.zegreatrob.coupling.server.external.stripe

import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsModule("stripe")
external fun stripe(secretKey: String): Stripe

external interface Stripe {
    val customers: StripeCustomersApi
    val subscriptions: StripeSubscriptionsApi
}

external interface StripeCustomersApi {
    fun create(options: StripeCustomersCreateOptions): Promise<StripeCustomer>
    fun list(options: StripeCustomersListOptions): Promise<StripeListResult<StripeCustomer>>
}

@JsPlainObject
sealed external interface StripeListResult<T> {
    val data: Array<T>
}

external interface StripeSubscriptionsApi {
    fun list(options: StripeSubscriptionsListOptions): Promise<StripeListResult<StripeSubscription>>
}

@JsPlainObject
sealed external interface StripeCustomersCreateOptions {
    val email: String
}

@JsPlainObject
sealed external interface StripeCustomersListOptions {
    val email: String
}

@JsPlainObject
sealed external interface StripeSubscriptionsListOptions {
    val customer: String
}

@JsPlainObject
sealed external interface StripeCustomer {
    val id: String
}

@JsPlainObject
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

@JsPlainObject
sealed external interface StripeSetupIntentCreateOptions {
    val customer: String

    @JsName("payment_method_types")
    val paymentMethodTypes: Array<String>
}

@JsPlainObject
sealed external interface StripeSetupIntent {
    @JsName("client_secret")
    val clientSecret: String?
}
