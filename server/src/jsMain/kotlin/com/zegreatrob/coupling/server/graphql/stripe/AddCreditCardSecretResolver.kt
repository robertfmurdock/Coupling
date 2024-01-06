package com.zegreatrob.coupling.server.graphql.stripe

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.stripe.stripe
import js.objects.jso
import kotlinx.coroutines.await

val stripe by lazy { stripe(Config.stripeSecretKey) }

private suspend fun CommandDispatcher.stripeCustomerId(user: UserDetails) = user.findOrCreateStripeCustomer().id
    .also { userRepository.save(user.copy(stripeCustomerId = it)) }

private suspend fun UserDetails.findOrCreateStripeCustomer() =
    stripe.customers.list(jso { email = this@findOrCreateStripeCustomer.email })
        .await()
        .data
        .firstOrNull()
        ?: stripe.customers.create(jso { email = this@findOrCreateStripeCustomer.email })
            .await()
