package com.zegreatrob.coupling.server.graphql.stripe

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.stripe.stripe

val stripe by lazy { stripe(Config.stripeSecretKey) }
