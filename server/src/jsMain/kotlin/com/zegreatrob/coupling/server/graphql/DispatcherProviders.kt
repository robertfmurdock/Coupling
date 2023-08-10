package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.repository.dynamo.DynamoBoostRepository
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.ICommandDispatcher
import com.zegreatrob.coupling.server.PrereleaseDispatcher
import com.zegreatrob.coupling.server.action.subscription.SubscriptionRepository
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.stripe.stripe
import js.core.jso
import kotlinx.coroutines.await
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonNull

object DispatcherProviders {

    fun <E, I> command(): GraphQLDispatcherProvider<E, I, CommandDispatcher> = { context, _, _ ->
        context.commandDispatcher
    }

    val partyCommand: (CouplingContext, JsonParty, JsonNull) -> CommandDispatcher = { context, _, _ ->
        context.commandDispatcher
    }

    suspend fun authorizedPartyDispatcher(
        context: CouplingContext,
        partyId: String,
    ) = context.commandDispatcher.authorizedPartyIdDispatcher(partyId).let { if (it.isAuthorized()) it else null }

    fun <E, I> prereleaseCommand(): GraphQLDispatcherProvider<E, I, PrereleaseDispatcher> = { request, entity, args ->
        val dispatcher = command<E, I>()(request, entity, args)

        if (dispatcher == null || !Config.prereleaseMode) {
            null
        } else {
            val boostRepo = DynamoBoostRepository(dispatcher.currentUser.id, Clock.System)
            object : PrereleaseDispatcher, ICommandDispatcher by dispatcher {
                override val boostRepository = boostRepo
                override val subscriptionRepository by lazy { StripeSubscriptionRepository() }
                override val userId = dispatcher.currentUser.id

                override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message) =
                    dispatcher.sendMessageAndReturnIdWhenFail(connectionId, message)
            }
        }
    }
}

class StripeSubscriptionRepository : SubscriptionRepository {
    override suspend fun findSubscriptionDetails(email: String): SubscriptionDetails? {
        val customers = stripe.customers.list(jso { this.email = email }).await()
        return customers.data.firstOrNull()?.let {
            SubscriptionDetails(it.id)
        }
    }
}
