package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.CouplingPopUp
import com.zegreatrob.coupling.client.components.contributor.ContributorMenu
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.jso
import popper.core.Placement
import popper.core.ReferenceElement
import popper.core.modifier
import popper.core.modifiers.Arrow
import popper.core.modifiers.Offset
import react.MutableRefObject
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.popper.UsePopperOptions
import react.popper.usePopper
import react.useRef
import react.useState
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.FlexDirection
import web.html.HTMLElement

external interface ContributionOverviewContentProps : Props {
    var party: PartyDetails
    var contributions: List<Contribution>
    var contributors: List<Player>
    var players: List<Player>
}

@ReactFunc
val ContributionOverviewContent by nfc<ContributionOverviewContentProps> { (party, contributions, contributors, players) ->
    div {
        div {
            css {
                display = Display.inlineFlex
                flexDirection = FlexDirection.column
                alignItems = AlignItems.center
            }
            h2 {
                +"Most Recent ${contributions.size} Contributions:"
            }
            val popperRef = useRef<HTMLElement>()
            val arrowRef = useRef<HTMLElement>()
            val (menuTarget, setMenuTarget) = useState<Pair<ReferenceElement, Player>?>(null)
            val popperInstance = usePopper(menuTarget?.first, popperRef.current, popperOptions(arrowRef))
            CouplingPopUp(
                hide = menuTarget == null,
                popperRef = popperRef,
                arrowRef = arrowRef,
                popperInstance = popperInstance,
            ) {
                if (menuTarget != null) {
                    ContributorMenu(menuTarget.second, players, party.id)
                }
            }
            contributions.forEach { contribution ->
                ContributionCard(
                    contribution = contribution,
                    contributors = contributors,
                    key = contribution.id,
                    onPlayerClick = { player, element ->
                        setMenuTarget(ReferenceElement(element)!! to player)
                        popperInstance.forceUpdate?.invoke()
                    },
                )
            }
        }
    }
}

private fun popperOptions(arrowRef: MutableRefObject<HTMLElement>): UsePopperOptions = jso {
    this.placement = Placement.right
    this.modifiers = arrayOf(
        Arrow.modifier {
            this.options = jso {
                this.element = arrowRef.current
            }
        },
        Offset.modifier {
            this.options = jso { offset = Offset(0.0, 10.0) }
        },
    )
}
