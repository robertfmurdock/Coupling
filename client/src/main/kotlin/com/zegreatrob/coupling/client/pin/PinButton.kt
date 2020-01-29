package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.model.pin.Pin
import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.dom.i
import react.dom.span
import styled.css
import styled.styledDiv

enum class PinButtonScale(val faTag: String, val factor: Double) {
    Normal("fa-3x", 3.0), Large("fa-10x", 10.0), Small("fa-1x", 1.0), ExtraSmall("fa-xs", 0.75)
}

data class PinButtonProps(
    val pin: Pin,
    val scale: PinButtonScale = PinButtonScale.Normal,
    val className: String = "",
    val showTooltip: Boolean = true,
    val onClick: () -> Unit = {}
) : RProps

object PinButton : FRComponent<PinButtonProps>(provider()) {

    fun RBuilder.pinButton(
        pin: Pin,
        scale: PinButtonScale = PinButtonScale.Small,
        className: String = "",
        onClick: () -> Unit = {},
        key: String? = null,
        showTooltip: Boolean = true
    ) = child(
        PinButton(PinButtonProps(pin, scale, className, showTooltip, onClick), key = key)
    )

    override fun render(props: PinButtonProps) = reactElement {
        val (pin, scale) = props
        val styles = useStyles("pin/PinButton")

        styledDiv {
            attrs {
                classes += listOf(props.className, styles.className)
                css { scaledStyles(scale) }
                onClickFunction = { props.onClick() }
            }

            if (props.showTooltip) {
                span(classes = styles["tooltip"]) { +(pin.name ?: "") }
            }
            i(scale.faTag) { attrs { classes += targetIcon(pin) } }
        }
    }

    private fun CSSBuilder.scaledStyles(scale: PinButtonScale) {
        padding((3.2 * scale.factor).px)
        borderWidth = (2 * scale.factor).px
        borderRadius = (12 * scale.factor).px
        lineHeight = LineHeight((4.6 * scale.factor).px.value)
        height = (14 * scale.factor).px
        width = (14 * scale.factor).px
    }

    private fun targetIcon(pin: Pin): String {
        var targetIcon = if (pin.icon.isNullOrEmpty()) "fa-skull" else pin.icon!!
        if (!targetIcon.startsWith("fa")) {
            targetIcon = "fa-$targetIcon"
        }
        var fontAwesomeStyle = "fa"
        val split = targetIcon.split(" ")
        if (split.size > 1) {
            fontAwesomeStyle = ""
        }

        targetIcon = "$fontAwesomeStyle $targetIcon"
        return targetIcon
    }
}