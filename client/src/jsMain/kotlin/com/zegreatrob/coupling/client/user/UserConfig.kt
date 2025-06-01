package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.action.ApplyBoostCommand
import com.zegreatrob.coupling.client.components.DemoButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.components.GqlButton
import com.zegreatrob.coupling.client.components.LogoutButton
import com.zegreatrob.coupling.client.components.NotificationButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.party.GeneralControlBar
import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.user.BoostConfiguration
import com.zegreatrob.coupling.client.party.AboutButton
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.sdk.gql.GraphQuery
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import web.cssom.Color
import web.cssom.Display
import web.cssom.number
import web.dom.ElementId
import web.html.InputType
import kotlin.uuid.Uuid

external interface UserConfigProps<D> : Props where D : GraphQuery.Dispatcher, D : ApplyBoostCommand.Dispatcher {
    var user: UserDetails?
    var subscription: SubscriptionDetails?
    var partyList: List<PartyDetails>
    var dispatcher: DispatchFunc<D>
    var stripeAdminCode: String
    var stripePurchaseCode: String
    var boost: Boost?
    var reload: () -> Unit
}

@ReactFunc
val UserConfig by nfc<UserConfigProps<*>> { props ->
    val (user, subscription, partyList, dispatcher) = props
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
                css {
                    display = Display.flex
                }

                div {
                    css {
                        flexGrow = number(1.0)
                    }
                    Editor {
                        li {
                            val inputId = Uuid.random().toString()
                            val elementId = ElementId(inputId)
                            label {
                                +"User Id"
                                htmlFor = elementId
                            }
                            input {
                                name = "id"
                                id = elementId
                                type = InputType.text
                                disabled = true
                                value = user.id.value.toString()
                                autoFocus = true
                            }
                        }
                        li {
                            val inputId = Uuid.random().toString()
                            val elementId = ElementId(inputId)
                            label {
                                +"User Email"
                                htmlFor = elementId
                            }
                            input {
                                name = "email"
                                id = elementId
                                type = InputType.text
                                disabled = true
                                value = user.email
                            }
                        }
                    }
                }
                PlayerCard(
                    defaultPlayer.copy(
                        id = PlayerId(user.id.value),
                        name = user.email.toString(),
                        email = user.email.toString(),
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
                SponsorCouplingButton(
                    user = user,
                    subscription = subscription,
                    stripeAdminCode = props.stripeAdminCode,
                    stripePurchaseCode = props.stripePurchaseCode,
                )
            }
            div {
                BoostConfiguration(
                    subscription = subscription,
                    boost = props.boost,
                    parties = props.partyList,
                    dispatchFunc = dispatcher,
                    reload = props.reload,
                )
            }
        }
    }
}
