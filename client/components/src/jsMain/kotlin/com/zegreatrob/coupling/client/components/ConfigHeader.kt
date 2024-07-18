package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.components.party.PartySelectButton
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.span
import web.cssom.AlignItems
import web.cssom.AlignSelf
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.Margin
import web.cssom.TextAlign
import web.cssom.TextDecoration
import web.cssom.VerticalAlign
import web.cssom.WhiteSpace
import web.cssom.fr
import web.cssom.integer
import web.cssom.number
import web.cssom.px
import web.cssom.repeat

external interface ConfigHeaderProps : PropsWithChildren {
    var party: PartyDetails
    var boost: Boost?
}

@ReactFunc
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
                PartyCard(party = party, size = 75, boost = props.boost)
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
            PartyNavigation(party)
        }
    }
}
