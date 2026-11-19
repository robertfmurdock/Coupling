package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.components.party.PartySelectButton
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.ChildrenBuilder
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.span
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.FontWeight
import web.cssom.Globals
import web.cssom.Length
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.TextDecoration
import web.cssom.VerticalAlign
import web.cssom.WhiteSpace
import web.cssom.em
import web.cssom.fr
import web.cssom.integer
import web.cssom.px
import web.cssom.repeat

external interface ConfigHeaderProps : PropsWithChildren {
    var party: PartyDetails
    var boost: Boost?
    var partyNavigable: Boolean?
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
                PartyCard(party = party, size = 75, boost = props.boost, navigable = props.partyNavigable)
            }
            div {
                css {
                    gridColumnStart = integer(2)
                    gridColumnEnd = integer(7)
                    gridRow = integer(1)
                }
                partyHeader(props, party)
            }
        }
    }
}

private fun ChildrenBuilder.partyHeader(props: ConfigHeaderProps, party: PartyDetails) {
    div {
        css {
            position = Position.relative
            minWidth = Length.maxContent
            display = Display.grid
            gridTemplateColumns = repeat(2, 1.fr)
            alignItems = AlignItems.start
            marginTop = 21.px
            marginBottom = 21.px
            marginLeft = 15.px
            marginRight = 20.px
            fontSize = 2.em
            fontWeight = FontWeight.bold
            "*" { verticalAlign = VerticalAlign.middle }
        }
        h1 {
            css {
                gridColumn = integer(1)
                gridRow = integer(1)
                fontSize = Globals.inherit
                margin = 0.px
                textDecoration = TextDecoration.underline
            }
            +props.children
        }
        div {
            css {
                gridColumnStart = integer(1)
                gridColumnEnd = integer(3)
                gridRow = integer(2)
            }
            PartyNavigation(party)
        }
        div {
            css {
                gridColumn = integer(2)
                gridRow = integer(1)
                textAlign = TextAlign.right
                marginRight = 20.px
            }
            PartySelectButton()
            LogoutButton()
            GqlButton()
        }
        span {
            css {
                position = Position.absolute
                right = (-60).px
                top = 0.px
            }
            NotificationButton()
        }
    }
}
