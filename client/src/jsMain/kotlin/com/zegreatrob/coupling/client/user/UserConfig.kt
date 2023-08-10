package com.zegreatrob.coupling.client.user

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DemoButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.GqlButton
import com.zegreatrob.coupling.client.components.LogoutButton
import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.blue
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.party.GeneralControlBar
import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.party.AboutButton
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.sdk.gql.GraphQuery
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.span
import react.useState
import web.cssom.Color
import web.cssom.Display
import web.html.InputType

external interface UserConfigProps : Props {
    var user: UserDetails?
    var subscription: SubscriptionDetails?
    var partyList: List<PartyDetails>
    var dispatcher: DispatchFunc<out GraphQuery.Dispatcher>
}

@ReactFunc
val UserConfig by nfc<UserConfigProps> { props ->
    val (user, subscription, partyList) = props
    PageFrame(
        borderColor = Color("rgb(94, 84, 102)"),
        backgroundColor = Color("floralwhite"),
    ) {
        GeneralControlBar {
            title = "User Config"
            AboutButton()
            DemoButton()
            LogoutButton()
            GqlButton()
            NotificationButton()
        }
        if (user == null) {
            div { +"User not found." }
        } else {
            div {
                css { display = Display.flex }

                Editor {
                    li {
                        val inputId = uuid4().toString()
                        label {
                            +"User Id"
                            htmlFor = inputId
                        }
                        input {
                            name = "id"
                            id = inputId
                            type = InputType.text
                            disabled = true
                            value = user.id
                            autoFocus = true
                        }
                    }
                    li {
                        val inputId = uuid4().toString()
                        label {
                            +"User Email"
                            htmlFor = inputId
                        }
                        input {
                            name = "email"
                            id = inputId
                            type = InputType.text
                            disabled = true
                            value = user.email
                        }
                    }
                }
                PlayerCard(
                    Player(
                        id = "",
                        name = user.email,
                        email = user.email,
                        avatarType = null,
                    ),
                )
            }
            div {
                div { +"You can see these parties:" }
                partyList.forEach { party ->
                    PartyCard(party = party, size = 50)
                }
            }
            div {
                SponsorCouplingButton(user, subscription)
            }
        }
    }
}

external interface SponsorCouplingButtonProps : Props {
    var user: UserDetails
    var subscription: SubscriptionDetails?
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
                href = "https://billing.stripe.com/p/login/test_4gw9BcbgqaeYaRybII?prefilled_email=${props.user.email}"
                +"Click here to administrate your subscription."
            }
        }
    } else if (showSubscriptionLink) {
        a {
            href = "https://buy.stripe.com/test_fZe5kta5OcHOfkI7ss?prefilled_email=${props.user.email}"
            +"Click for ongoing sponsorship via subscription."
        }
    } else {
        CouplingButton(large, blue, onClick = { showSubscriptionLink = true }) {
            span { +"Sponsor Coupling!" }
        }
    }
}
