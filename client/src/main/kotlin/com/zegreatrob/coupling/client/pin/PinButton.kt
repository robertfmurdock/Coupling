package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.PropertiesBuilder
import csstype.px
import emotion.react.css
import react.dom.html.ReactHTML.div
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
) : DataPropsBind<PinButton>(pinButton)

private val styles = useStyles("pin/PinButton")

val pinButton = tmFC<PinButton> { (pin, scale, className, showTooltip, onClick) ->
    div {
        css(ClassName(className), styles.className) {
            scaledStyles(scale)
        }
        this.onClick = { onClick() }

        if (showTooltip) {
            span {
                this.className = styles["tooltip"]
                +pin.name
            }
        }
        i { this.className = ClassName("${scale.faTag} ${targetIcon(pin)}") }
    }
}

private fun PropertiesBuilder.scaledStyles(scale: PinButtonScale) {
    padding = ((3.2 * scale.factor).px)
    borderWidth = (2 * scale.factor).px
    borderRadius = (12 * scale.factor).px
    lineHeight = (4.6 * scale.factor).px
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
