package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.jso
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.useState
import web.cssom.ClassName
import web.cssom.Padding
import web.cssom.px

@ReactFunc
val SettingsButton by nfc<PartyButtonProps> { props ->
    val (showDropDown, setShowDropDown) = useState(false)

    div {
        onMouseLeave = { setShowDropDown(false) }
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = black
            onClick = { setShowDropDown(true) }
            buttonProps = jso {
                onMouseEnter = { setShowDropDown(true) }
            }
            css {
                fontSize = 24.px
                padding = Padding(1.px, 4.px, 2.px)
                "i" {
                    margin = 0.px
                }
            }
            i { css(ClassName("fa fa-cog")) {} }
        }
        if (showDropDown) {
            CouplingDropDown {
                CouplingDropDownLink(to = "/${props.partyId.value}/edit") { +"Party Settings" }
                CouplingDropDownLink(to = "/${props.partyId.value}/secrets") { +"Secrets" }
            }
        }
    }
}
