@file:JsModule("@stripe/stripe-js")

package com.zegreatrob.coupling.client.components.external.stripe

import kotlin.js.Promise

external fun loadStripe(publicKey: String): Promise<Stripe>

external interface Stripe
