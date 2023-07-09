package com.zegreatrob.coupling.client.components.external.reactdnd

import com.zegreatrob.coupling.client.components.external.reactdndhtml5backend.DnDBackend
import react.PropsWithChildren

external interface DnDProvideProps : PropsWithChildren {
    var backend: DnDBackend
}

external interface DragOptions {
    fun collect(monitor: DragSourceMonitor)
}

external interface DragSourceMonitor {
    fun isDragging(): Boolean
    fun isOver(): Boolean
}
