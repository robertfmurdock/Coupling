package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span

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

val pinButton = tmFC<PinButton> { (pin, scale, className, showTooltip, onClick) ->
    +cssDiv(
        attrs = {
            classes = classes + listOf(className, styles.className)
            onClickFunction = { onClick() }
        },
        css = { scaledStyles(scale) }
    ) {
        if (showTooltip) {
            span {
                this.className = styles["tooltip"]
                +pin.name
            }
        }
        i { this.className = "${scale.faTag} ${targetIcon(pin)}" }
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