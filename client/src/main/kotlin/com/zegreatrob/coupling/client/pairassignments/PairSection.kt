package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.external.domtoimage.domToImage
import com.zegreatrob.coupling.client.player.TinyPlayerList
import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.large
import com.zegreatrob.coupling.components.white
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.Display
import csstype.Float
import csstype.VerticalAlign
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.DataTransfer
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.useRef
import web.html.HTMLElement
import kotlin.js.Json
import kotlin.js.json

data class PairSection(
    val party: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val allowSave: Boolean,
    val setPairs: (PairAssignmentDocument) -> Unit,
    val controls: Controls<DeletePairAssignmentsCommand.Dispatcher>,
) : DataPropsBind<PairSection>(pairSection)

private val pairSection = tmFC<PairSection> { (party, players, pairs, allowSave, setPairs, controls) ->
    val pairSectionNode = useRef<HTMLElement>(null)

    div {
        css { verticalAlign = VerticalAlign.top }
        div {
            ref = pairSectionNode
            css { display = Display.inlineBlock }
            add(PairSectionPanel(party, players, pairs, allowSave, setPairs, controls))
        }
        div {
            css { float = Float.right; width = 0.px }
            div { copyToClipboardButton(pairSectionNode) }
            add(TinyPlayerList(party, players))
        }
    }
}

private fun ChildrenBuilder.copyToClipboardButton(ref: MutableRefObject<HTMLElement>) {
    if (js("!!global.ClipboardItem").unsafeCast<Boolean>()) {
        add(
            CouplingButton(
                sizeRuleSet = large,
                colorRuleSet = white,
                onClick = { ref.current?.copyToClipboardOnClick()?.invoke() },
                attrs = { tabIndex = -1 },
            ),
        ) {
            i { className = ClassName("fa fa-clipboard") }
        }
    }
}

private fun HTMLElement.copyToClipboardOnClick(): () -> Unit = if (isReallyTrulySafari()) {
    writeImageToClipboardAsPromise()
} else {
    collectImageThenWriteToClipboard()
}

private fun HTMLElement.collectImageThenWriteToClipboard(): () -> Unit = {
    domToImage.toBlob(this).then { window.navigator.clipboard.write(dataTransfer(it)) }
}

private fun HTMLElement.writeImageToClipboardAsPromise(): () -> Unit = {
    window.navigator.clipboard.write(dataTransfer(domToImage.toBlob(this)))
}

private fun isReallyTrulySafari() = window.navigator.userAgent.indexOf("Safari") != -1 &&
    window.navigator.userAgent.indexOf("Chrome") == -1

private fun dataTransfer(it: Any) = arrayOf(ClipboardItem(json("image/png" to it))).unsafeCast<DataTransfer>()

external class ClipboardItem(params: Json)
