package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.CouplingPopUp
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
}

@ReactFunc
val ContributionOverviewContent by nfc<ContributionOverviewContentProps> { (_, contributions, contributors) ->
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
            val (referenceElement, setReferenceElement) = useState<ReferenceElement?>(null)
            val popperInstance = usePopper(referenceElement, popperRef.current, popperOptions(arrowRef))
            CouplingPopUp(
                hide = referenceElement == null,
                popperRef = popperRef,
                arrowRef = arrowRef,
                popperInstance = popperInstance,
            ) {
                +"Menu Goes here"
            }
            contributions.forEach { contribution ->
                ContributionCard(
                    contribution = contribution,
                    contributors = contributors,
                    key = contribution.id,
                    onPlayerClick = { _, element ->
                        setReferenceElement(ReferenceElement(element))
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
