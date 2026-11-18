@file:OptIn(DelicateCoroutinesApi::class)

package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.external.html2canvas.html2canvas
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.player.TinyPlayerList
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.globals.globalThis
import js.objects.unsafeJso
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import org.w3c.dom.DataTransfer
import react.ChildrenBuilder
import react.PropsWithChildren
import react.RefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.useRef
import web.cssom.AlignItems
import web.cssom.ClassName
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.Position
import web.cssom.VerticalAlign
import web.cssom.px
import web.html.HTMLElement
import web.html.toBlob
import kotlin.js.Json
import kotlin.js.json

external interface PairSectionProps : PropsWithChildren {
    var party: PartyDetails
    var players: List<Player>
    var pairAssignments: PairingSet?
    var allowSave: Boolean
    var setPairs: (PairingSet) -> Unit
    var controls: Controls<DeletePairAssignmentsCommand.Dispatcher>
}

@ReactFunc
val PairSection by nfc<PairSectionProps> { props ->
    val (party, players, pairs, allowSave, setPairs, controls) = props
    val pairSectionNode = useRef<HTMLElement>(null)

    div {
        css {
            verticalAlign = VerticalAlign.top
            position = Position.relative
        }
        div {
            ref = pairSectionNode
            css { display = Display.inlineBlock }
            PairSectionPanel(party, players, pairs, allowSave, setPairs, controls)
        }
        div {
            css {
                display = Display.flex
                justifyContent = JustifyContent.center
                alignItems = AlignItems.center
                gap = 10.px
            }
            +props.children
            copyToClipboardButton(pairSectionNode)
        }
        div {
            css {
                position = Position.absolute
                right = 0.px
                top = 0.px
                width = 0.px
            }
            TinyPlayerList(party, players)
        }
    }
}

private fun ChildrenBuilder.copyToClipboardButton(ref: RefObject<HTMLElement>) {
    if (globalThis["ClipboardItem"] != null) {
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = white
            onClick = { ref.current?.copyToClipboardOnClick()?.invoke() }
            buttonProps = unsafeJso {
                ariaLabel = "Copy pair image to clipboard"
            }
            i { className = ClassName("fa fa-clipboard") }
        }
    }
}

private fun HTMLElement.copyToClipboardOnClick(): () -> Unit = writeImageToClipboardAsPromise()

private fun HTMLElement.writeImageToClipboardAsPromise(): () -> Unit = {
    window.navigator.clipboard.write(
        dataTransfer(
            html2canvas(this, json("useCORS" to true, "imageTimeout" to 500))
                .then { hTMLCanvasElement ->
                    kotlinx.coroutines.GlobalScope.launch {
                        hTMLCanvasElement.toBlob("image/svg")
                    }
                },
        ),
    )
}

private fun dataTransfer(it: Any) = arrayOf(ClipboardItem(json("image/png" to it))).unsafeCast<DataTransfer>()

external class ClipboardItem(@Suppress("unused") params: Json)
