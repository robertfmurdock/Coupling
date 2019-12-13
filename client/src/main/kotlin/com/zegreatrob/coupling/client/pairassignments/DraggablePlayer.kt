package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactdnd.useDrag
import com.zegreatrob.coupling.client.external.reactdnd.useDrop
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.div

object DraggablePlayer : RComponent<DraggablePlayerProps>(provider()), DraggablePlayerBuilder

val RBuilder.draggablePlayer get() = DraggablePlayer.render(this)

data class DraggablePlayerProps(
    val pinnedPlayer: PinnedPlayer,
    val tribe: Tribe,
    val pairAssignmentDocument: PairAssignmentDocument,
    val onPlayerDrop: (String) -> Unit
) : RProps

external interface DraggablePlayerStyles {
    val className: String
    val playerCard: String
    val hoverZoom: String
    val onDragHover: String
}

interface DraggablePlayerBuilder : StyledComponentRenderer<DraggablePlayerProps, DraggablePlayerStyles> {

    override val componentPath: String get() = "pairassignments/DraggablePlayer"

    override fun StyledRContext<DraggablePlayerProps, DraggablePlayerStyles>.render(): ReactElement {
        val (pinnedPlayer, tribe, pairAssignmentDocument, swapCallback) = props
        val draggablePlayerRef = useRef<Node>(null)
        val (_, drag) = useDrag(
            itemType = dragItemType,
            itemId = pinnedPlayer.player.id!!,
            collect = { }
        )
        val (isOver, drop) = useDrop(
            acceptItemType = dragItemType,
            drop = { item ->
                swapCallback(item["id"].unsafeCast<String>())
            },
            collect = { monitor ->
                monitor.isOver()
            }
        )
        drag(drop(draggablePlayerRef))

        return reactElement {
            div(classes = styles.className) {
                attrs { ref = draggablePlayerRef }
                playerCard(
                    PlayerCardProps(
                        tribeId = tribe.id,
                        player = pinnedPlayer.player,
                        pathSetter = {},
                        headerDisabled = false,
                        className = mapOf(
                            styles.hoverZoom to (pairAssignmentDocument.id == null),
                            styles.onDragHover to isOver
                        )
                            .filterValues { it }
                            .keys
                            .plus(styles.playerCard)
                            .joinToString(" ")
                    ),
                    key = pinnedPlayer.player.id
                )
            }
        }
    }
}

