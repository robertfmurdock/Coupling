package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.PartyNavigation
import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.components.party.PartySelectButton
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML
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
    ReactHTML.div {
        css {
            textAlign = TextAlign.Companion.left
            margin = 5.px
        }
        ReactHTML.div {
            css {
                display = Display.Companion.grid
                gridTemplateColumns = repeat(6, 1.fr)
                flexDirection = FlexDirection.Companion.row
                alignItems = AlignItems.Companion.center
                whiteSpace = WhiteSpace.Companion.nowrap
            }
            ReactHTML.div {
                css {
                    gridColumn = integer(1)
                    gridRowStart = integer(1)
                    gridRowEnd = integer(3)
                }
                PartyCard(party = party, size = 75, boost = props.boost)
            }
            ReactHTML.div {
                css {
                    gridColumnStart = integer(2)
                    gridColumnEnd = integer(7)
                    gridRow = integer(1)
                }
                ReactHTML.h1 {
                    css {
                        display = Display.Companion.flex
                        flexDirection = FlexDirection.Companion.column
                        alignItems = AlignItems.Companion.center
                        marginLeft = 15.px
                        textDecoration = TextDecoration.Companion.underline
                        flexGrow = number(2.0)
                    }
                    ReactHTML.div {
                        css {
                            display = Display.Companion.inlineBlock
                            marginLeft = 15.px
                            textDecoration = TextDecoration.Companion.underline
                            flexGrow = number(2.0)
                            alignSelf = AlignSelf.Companion.stretch
                            "*" { verticalAlign = VerticalAlign.Companion.middle }
                        }
                        ReactHTML.div {
                            css {
                                display = Display.Companion.flex
                                alignItems = AlignItems.Companion.baseline
                            }
                            ReactHTML.span {
                                css { flexGrow = number(2.0) }
                                +props.children
                            }
                            ReactHTML.span {
                                css {
                                    margin = Margin(0.px, 20.px)
                                    alignItems = AlignItems.Companion.baseline
                                    alignSelf = AlignSelf.Companion.stretch
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
