package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.dom.attrs
import react.dom.i
import react.dom.span
import styled.css
import styled.styledDiv

enum class PinButtonScale(val faTag: String, val factor: Double) {
    Normal("fa-3x", 3.0), Large("fa-10x", 10.0), Small("fa-1x", 1.0), ExtraSmall("fa-xs", 0.75);

    fun diameterInPixels() = 14 * factor

}

data class PinButton(
    val pin: Pin,
    val scale: PinButtonScale = PinButtonScale.Normal,
    val className: String = "",
    val showTooltip: Boolean = true,
    val onClick: () -> Unit = {}
) : DataProps<PinButton> {
    override val component: TMFC<PinButton> get() = pinButton
}

private val styles = useStyles("pin/PinButton")

val pinButton = reactFunction<PinButton> { (pin, scale, className, showTooltip, onClick) ->
    styledDiv {
        attrs {
            classes = classes + listOf(className, styles.className)
            css { scaledStyles(scale) }
            onClickFunction = { onClick() }
        }

        if (showTooltip) {
            span(classes = styles["tooltip"]) { +pin.name }
        }
        i(scale.faTag) { attrs { classes = classes + targetIcon(pin) } }
    }
}

private fun CssBuilder.scaledStyles(scale: PinButtonScale) {
    padding((3.2 * scale.factor).px)
    borderWidth = (2 * scale.factor).px
    borderRadius = (12 * scale.factor).px
    lineHeight = LineHeight((4.6 * scale.factor).px.value)
    height = scale.diameterInPixels().px
    width = scale.diameterInPixels().px
}

private fun targetIcon(pin: Pin): String {
    var targetIcon = pin.icon.ifEmpty { "fa-skull" }
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