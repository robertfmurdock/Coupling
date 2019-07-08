package com.zegreatrob.coupling.client

//val meh = ::playerCardHeader.unsafeCast<RClass<PlayerCardHeaderProps>>()
//
//
//meh {
//    attrs {
//        this.player = player
//        this.size = size
//        this.tribeId = tribeId
//        this.disabled = disabled
//        this.size = size
//        this.pathSetter = pathSetter
//    }
//}


//fun playerCardHeader(props: PlayerCardHeaderProps) = buildElement {
//    val player = props.player
//    val size = props.size
//
//    val cardHeaderRef: RReadableRef<Node> = React.useRef(null).unsafeCast<RReadableRef<Node>>()
//
//    React.useLayoutEffect {
//        fitPlayerName(size, cardHeaderRef.current)
//        return@useLayoutEffect undefined
//    }
//
//    styledDiv {
//        attrs {
//            classes = setOf("player-card-header", styles.header)
//            ref = cardHeaderRef
//            onClickFunction = { event ->
//                if (!props.disabled) {
//                    event.stopPropagation()
//
//                    props.pathSetter("/${props.tribeId}/player/${player.id}")
//                }
//            }
//        }
//        css {
//            margin(top = (size * 0.02).px)
//        }
//        div {
//            +(if (player.id == null) "NEW:" else "")
//            +(player.name ?: "Unknown")
//        }
//    }
//}