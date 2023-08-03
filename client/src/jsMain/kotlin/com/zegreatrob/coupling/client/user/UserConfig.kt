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
import com.zegreatrob.coupling.client.components.external.stripe.Elements
import com.zegreatrob.coupling.client.components.external.stripe.loadStripe
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.party.GeneralControlBar
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.party.AboutButton
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.sdk.gql.GraphQuery
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.span
import react.useState
import web.cssom.Color
import web.cssom.Display
import web.html.InputType

external interface UserConfigProps : Props {
    var user: User?
    var dispatcher: DispatchFunc<out GraphQuery.Dispatcher>
}

@ReactFunc
val UserConfig by nfc<UserConfigProps> { props ->
    val (user) = props
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
                    div { +"You are authorized for these parties:" }
                    user.authorizedPartyIds
                        .map { it.value }
                        .forEach { id ->
                            div { +"Party ID: $id" }
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
                SponsorCouplingButton(props.dispatcher)
            }
        }
    }
}

external interface SponsorCouplingButtonProps : Props {
    var dispatchFunc: DispatchFunc<out GraphQuery.Dispatcher>
}

@ReactFunc
val SponsorCouplingButton by nfc<SponsorCouplingButtonProps> { props ->
    val (stripePk, setStripePk) = useState<String?>(null)
    var addSecret by useState<String?>(null)

    val getSecretFunc = props.dispatchFunc {
        val config = fire(
            graphQuery {
                config {
                    stripePublishableKey()
                    addCreditCardSecret()
                }
            },
        )?.config
        setStripePk(config?.stripePublishableKey)
        addSecret = config?.addCreditCardSecret
    }

    if (addSecret != null && stripePk != null) {
        +"Enter credit card information to sponsor."

        Elements {
            stripe = loadStripe(stripePk)
        }

        +addSecret
    } else {
        CouplingButton(large, blue, onClick = getSecretFunc) {
            span { +"Sponsor Coupling!" }
        }
    }
}
