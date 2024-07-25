package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.blue
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.span
import react.useState

external interface SponsorCouplingButtonProps : Props {
    var user: UserDetails
    var subscription: SubscriptionDetails?
    var stripeAdminCode: String
    var stripePurchaseCode: String
}

@ReactFunc
val SponsorCouplingButton by nfc<SponsorCouplingButtonProps> { props ->
    var showSubscriptionLink by useState(false)

    val subscription = props.subscription
    if (subscription?.isActive == true) {
        h3 { +"Subscription Live!" }
        val nextBillDate = subscription.currentPeriodEnd?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
        div { +"You will be billed next on $nextBillDate." }
        div {
            a {
                href = stripeAdminUrl(props.stripeAdminCode, props.user.email)
                +"Click here to administrate your subscription."
            }
        }
    } else if (showSubscriptionLink) {
        a {
            href = stripePurchaseUrl(props.stripePurchaseCode, props.user.email)
            +"Click for ongoing sponsorship via subscription."
        }
    } else {
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = blue
            onClick = { showSubscriptionLink = true }
            span { +"Sponsor Coupling!" }
        }
    }
}

private fun stripePurchaseUrl(purchaseCode: String, email: String) =
    "https://buy.stripe.com/$purchaseCode?prefilled_email=$email"

private fun stripeAdminUrl(adminCode: String, userEmail: String) =
    "https://billing.stripe.com/p/login/$adminCode?prefilled_email=$userEmail"
