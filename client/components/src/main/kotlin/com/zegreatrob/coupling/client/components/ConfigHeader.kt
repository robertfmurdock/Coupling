package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.pairassignments.viewHistoryButton
import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.components.party.PartySelectButton
import com.zegreatrob.coupling.client.components.pin.pinListButton
import com.zegreatrob.coupling.client.components.player.addPlayerButton
import com.zegreatrob.coupling.client.components.player.viewRetireesButton
import com.zegreatrob.coupling.client.components.stats.statisticsButton
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.span
import web.cssom.AlignItems
import web.cssom.AlignSelf
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.TextDecoration
import web.cssom.VerticalAlign
import web.cssom.WhiteSpace
import web.cssom.fr
import web.cssom.integer
import web.cssom.number
import web.cssom.pt
import web.cssom.px
import web.cssom.repeat
import web.cssom.rgb

external interface ConfigHeaderProps : PropsWithChildren {
    var party: PartyDetails
}

val ConfigHeader by nfc<ConfigHeaderProps> { props ->
    val party = props.party
    div {
        css {
            textAlign = TextAlign.left
            margin = 5.px
        }
        div {
            css {
                display = Display.grid
                gridTemplateColumns = repeat(6, 1.fr)
                flexDirection = FlexDirection.row
                alignItems = AlignItems.center
                whiteSpace = WhiteSpace.nowrap
            }
            div {
                css {
                    gridColumn = integer(1)
                    gridRowStart = integer(1)
                    gridRowEnd = integer(3)
                }
                add(PartyCard(party, 75))
            }
            div {
                css {
                    gridColumnStart = integer(2)
                    gridColumnEnd = integer(7)
                    gridRow = integer(1)
                }
                h1 {
                    css {
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        alignItems = AlignItems.center
                        marginLeft = 15.px
                        textDecoration = TextDecoration.underline
                        flexGrow = number(2.0)
                    }
                    div {
                        css {
                            display = Display.inlineBlock
                            marginLeft = 15.px
                            textDecoration = TextDecoration.underline
                            flexGrow = number(2.0)
                            alignSelf = AlignSelf.stretch
                            "*" { verticalAlign = VerticalAlign.middle }
                        }
                        div {
                            css {
                                display = Display.flex
                                alignItems = AlignItems.baseline
                            }
                            span {
                                css { flexGrow = number(2.0) }
                                +props.children
                            }
                            span {
                                css {
                                    margin = Margin(0.px, 20.px)
                                    alignItems = AlignItems.baseline
                                    alignSelf = AlignSelf.stretch
                                }
                                PartySelectButton()
                                LogoutButton()
                                GqlButton()
                                NotificationButton()
                            }
                        }
                    }
                }
            }
            div {
                css {
                    gridColumnStart = integer(2)
                    gridColumnEnd = integer(6)
                    gridRow = integer(2)
                    display = Display.flex
                    alignItems = AlignItems.center
                    flexDirection = FlexDirection.column
                }
                div {
                    css {
                        display = Display.inlineFlex
                        alignItems = AlignItems.center
                        borderRadius = 20.px
                        padding = Padding(5.px, 5.px)
                        margin = Margin(2.px, 2.px)
                        fontSize = 0.pt
                        backgroundColor = Color("#00000014")
                        boxShadow = BoxShadow(1.px, 1.px, 3.px, rgb(0, 0, 0, 0.6))
                    }
                    settingsButton(party)
                    addPlayerButton { this.partyId = party.id }
                    viewHistoryButton(party)
                    pinListButton(party)
                    statisticsButton(party)
                    viewRetireesButton(party)
                }
            }
        }
    }
}
