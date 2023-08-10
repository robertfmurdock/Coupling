package com.zegreatrob.coupling.server.graphql.stripe

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.external.stripe.stripe
import js.core.jso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise

val stripe by lazy { stripe(Config.stripeSecretKey) }

val addCreditCardSecretResolver: (Json, Json, CouplingContext, Json) -> Promise<dynamic> = { _, _, context, _ ->
    MainScope().promise {
        val commandDispatcher = context.commandDispatcher
        val user = commandDispatcher.currentUser

        val stripeCustomerId = user.stripeCustomerId ?: commandDispatcher.stripeCustomerId(user)

        stripe.setupIntents.create(
            jso {
                customer = stripeCustomerId
                paymentMethodTypes = arrayOf("card")
            },
        )
            .then { it.clientSecret }
            .await()
    }
}

private suspend fun CommandDispatcher.stripeCustomerId(user: UserDetails) = user.findOrCreateStripeCustomer().id
    .also { userRepository.save(user.copy(stripeCustomerId = it)) }

private suspend fun UserDetails.findOrCreateStripeCustomer() =
    stripe.customers.list(jso { email = this@findOrCreateStripeCustomer.email })
        .await()
        .data
        .firstOrNull()
        ?: stripe.customers.create(jso { email = this@findOrCreateStripeCustomer.email })
            .await()
