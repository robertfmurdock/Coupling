package com.zegreatrob.coupling.server.graphql.stripe

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.stripe.Stripe

val stripe by lazy { Stripe(Config.stripeSecretKey) }
