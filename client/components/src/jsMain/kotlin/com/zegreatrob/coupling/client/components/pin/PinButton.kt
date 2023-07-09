package com.zegreatrob.coupling.client.components.pin

import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.react.css
import react.Props
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.useCallback
import web.cssom.BackgroundRepeat
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Display
import web.cssom.FontSize
import web.cssom.Length
import web.cssom.LineStyle
import web.cssom.NamedColor
import web.cssom.Padding
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.Visibility
import web.cssom.WhiteSpace
import web.cssom.integer
import web.cssom.pct
import web.cssom.px
import web.cssom.string
import web.cssom.translatex
import web.cssom.url

enum class PinButtonScale(val faTag: String, val factor: Double) {
    Normal("fa-3x", 3.0), Large("fa-10x", 10.0), Small("fa-1x", 1.0), ExtraSmall("fa-xs", 0.75);

    fun diameterInPixels() = 14 * factor
}

external interface PinButtonProps : Props {
    var pin: Pin
    var scale: PinButtonScale?
    var className: String?
    var showTooltip: Boolean?
    var onClick: (() -> Unit)?
}

@ReactFunc
val PinButton by nfc<PinButtonProps> { props ->
    val onClickCallback: (MouseEvent<*, *>) -> Unit = useCallback { props.onClick?.invoke() }
    val scale = props.scale ?: PinButtonScale.Normal
    val showTooltip = props.showTooltip ?: true
    div {
        val pin = props.pin
        asDynamic()["data-pin-button"] = "${pin.id}"
        css(ClassName(props.className ?: "")) {
            pinButtonStyles()
            scaledStyles(scale)
        }
        onClick = onClickCallback

        if (showTooltip) {
            span {
                this.className = tooltipStyles
                +pin.name
            }
        }
        i { this.className = ClassName("${scale.faTag} ${targetIcon(pin)}") }
    }
}

private val tooltipStyles = emotion.css.ClassName {
    visibility = Visibility.hidden
    position = Position.absolute
    padding = Padding(5.px, 8.px)
    borderRadius = 15.px
    zIndex = integer(1)
    bottom = 125.pct
    left = 50.pct
    transform = translatex((-50).pct)
    backgroundRepeat = BackgroundRepeat.repeatX
    backgroundImage = url(pngPath("overlay"))
    backgroundColor = Color("#222222")
    color = Color("#fff")
    display = Display.inlineBlock
    lineHeight = Length.normal
    fontSize = FontSize.larger
    whiteSpace = WhiteSpace.nowrap
    after {
        content = string("''")
        position = Position.absolute
        top = 96.pct
        left = 50.pct
        marginLeft = (-5).px
        borderWidth = 5.px
        borderStyle = LineStyle.solid
        borderTopColor = Color("#222222")
        borderRightColor = NamedColor.transparent
        borderLeftColor = NamedColor.transparent
        borderBottomColor = NamedColor.transparent
    }
}

private fun PropertiesBuilder.pinButtonStyles() {
    display = Display.inlineBlock
    backgroundColor = NamedColor.white
    borderStyle = LineStyle.double
    borderColor = NamedColor.black
    textAlign = TextAlign.center
    position = Position.relative
    hover {
        tooltipStyles {
            visibility = Visibility.visible
            position = Position.absolute
            zIndex = integer(1)
        }
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
