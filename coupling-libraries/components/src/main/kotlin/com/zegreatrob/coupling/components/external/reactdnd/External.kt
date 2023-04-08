package com.zegreatrob.coupling.components.external.reactdnd

import com.zegreatrob.coupling.components.external.reactdndhtml5backend.DnDBackend
import react.Props

external interface DnDProvideProps : Props {
    var backend: DnDBackend
}

external interface DragOptions {
    fun collect(monitor: DragSourceMonitor)
}

external interface DragSourceMonitor {
    fun isDragging(): Boolean
    fun isOver(): Boolean
}
