package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.unsafeJso
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.useId
import react.useState
import web.cssom.ClassName
import web.cssom.Padding
import web.cssom.px
import web.dom.Node
import web.dom.document
import web.html.HTMLButtonElement

@ReactFunc
val SettingsButton by nfc<PartyButtonProps> { props ->
    val (showDropDown, setShowDropDown) = useState(false)
    val dropdownId = useId()

    div {
        onMouseLeave = { event ->
            if (!event.currentTarget.contains(document.activeElement)) setShowDropDown(false)
        }
        onBlur = { event ->
            if (!event.currentTarget.contains(event.relatedTarget?.unsafeCast<Node>())) setShowDropDown(false)
        }
        onKeyDown = { event ->
            if (event.key == "Escape" && showDropDown) {
                event.preventDefault()
                event.stopPropagation()
                setShowDropDown(false)
                event.currentTarget.querySelector("button")?.unsafeCast<HTMLButtonElement>()?.focus()
            }
        }
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = black
            onClick = { setShowDropDown(true) }
            buttonProps = unsafeJso {
                ariaLabel = "Settings"
                ariaExpanded = showDropDown
                ariaControls = dropdownId
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
        div {
            id = dropdownId
            if (showDropDown) {
                CouplingDropDown {
                    CouplingDropDownLink(to = "/${props.partyId.value}/edit") { +"Party Settings" }
                    CouplingDropDownLink(to = "/${props.partyId.value}/secrets") { +"Secrets" }
                    CouplingDropDownLink(to = "/${props.partyId.value}/contributions") { +"Contributions" }
                    CouplingDropDownLink(to = "/${props.partyId.value}/developer-settings/") { +"Developer settings" }
                }
            }
        }
    }
}
