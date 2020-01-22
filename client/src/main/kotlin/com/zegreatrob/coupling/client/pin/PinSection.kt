package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pin.PinButton.pinButton
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import kotlinx.css.marginLeft
import kotlinx.css.px
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import styled.css
import styled.styledDiv

data class PinSectionProps(
    val pair: PinnedCouplingPair,
    val scale: PinButtonScale = PinButtonScale.Small,
    val className: String
) : RProps

external class PinSectionStyles {
    val className: String
}

object PinSection : FRComponent<PinSectionProps>(provider()) {

    fun RBuilder.pinSection(
        pair: PinnedCouplingPair,
        scale: PinButtonScale = PinButtonScale.Small,
        className: String = ""
    ) = child(PinSection(PinSectionProps(pair, scale, className)))

    override fun render(props: PinSectionProps) = reactElement {
        val styles = useStyles<PinSectionStyles>("pin/PinSection")

        styledDiv {
            attrs {
                classes += styles.className
                css { marginLeft = -(props.pair.pins.size * 12 * props.scale.factor).px }
            }
            props.pair.pins.map { pin -> pinButton(pin, props.scale) }
        }
    }

}