package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.domtoimage.domToImage
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.player.TinyPlayerList
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.Float
import csstype.VerticalAlign
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.DataTransfer
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.ref
import react.useRef
import kotlin.js.Json
import kotlin.js.json

data class PairSection(
    val party: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val allowSave: Boolean,
    val setPairs: (PairAssignmentDocument) -> Unit,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>,
) : DataPropsBind<PairSection>(pairSection)

private val pairSection = tmFC<PairSection> { (party, players, pairs, allowSave, setPairs, controls) ->
    val pairSectionNode = useRef<Node>(null)

    div {
        css { verticalAlign = VerticalAlign.top }
        add(PairSectionPanel(party, players, pairs, allowSave, setPairs, controls)) {
            ref = pairSectionNode
        }
        div {
            css { float = Float.right; width = 0.px }
            div { copyToClipboardButton(pairSectionNode) }
            add(TinyPlayerList(party, players))
        }
    }
}

private fun ChildrenBuilder.copyToClipboardButton(ref: MutableRefObject<Node>) {
    if (js("!!global.ClipboardItem").unsafeCast<Boolean>()) {
        add(
            CouplingButton(
                sizeRuleSet = large,
                colorRuleSet = white,
                onClick = ref.current?.copyToClipboardOnClick() ?: {},
                attrs = { tabIndex = -1 }
            )
        ) {
            i { className = ClassName("fa fa-clipboard") }
        }
    }
}

private fun Node.copyToClipboardOnClick(): () -> Unit = if (isReallyTrulySafari())
    writeImageToClipboardAsPromise()
else
    collectImageThenWriteToClipboard()

private fun Node.collectImageThenWriteToClipboard(): () -> Unit = {
    domToImage.toBlob(this).then { window.navigator.clipboard.write(dataTransfer(it)) }
}

private fun Node.writeImageToClipboardAsPromise(): () -> Unit = {
    window.navigator.clipboard.write(dataTransfer(domToImage.toBlob(this)))
}

private fun isReallyTrulySafari() = window.navigator.userAgent.indexOf("Safari") != -1 &&
    window.navigator.userAgent.indexOf("Chrome") == -1

private fun dataTransfer(it: Any) = arrayOf(ClipboardItem(json("image/png" to it))).unsafeCast<DataTransfer>()

external class ClipboardItem(params: Json)
