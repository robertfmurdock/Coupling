package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pngPath
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import csstype.BackgroundRepeat
import csstype.ClassName
import csstype.Color
import csstype.Display
import csstype.FontSize
import csstype.Length
import csstype.LineStyle
import csstype.NamedColor
import csstype.Padding
import csstype.Position
import csstype.PropertiesBuilder
import csstype.TextAlign
import csstype.Visibility
import csstype.WhiteSpace
import csstype.integer
import csstype.pct
import csstype.px
import csstype.string
import csstype.translatex
import csstype.url
import emotion.react.css
import org.w3c.dom.HTMLDivElement
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.useCallback

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

val pinButton = tmFC<PinButton> { (pin, scale, className, showTooltip, onClickFunc) ->
    val onClickCallback: (MouseEvent<HTMLDivElement, *>) -> Unit = useCallback { onClickFunc() }
    div {
        css(ClassName(className), styles.className) {
            pinButtonStyles()
            scaledStyles(scale)
        }
        onClick = onClickCallback

        if (showTooltip) {
            span {
                css(styles["tooltip"]) {
                    tooltipStyles()
                }
                +pin.name
            }
        }
        i { this.className = ClassName("${scale.faTag} ${targetIcon(pin)}") }
    }
}

private fun PropertiesBuilder.tooltipStyles() {
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
        styles["tooltip"] {
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
