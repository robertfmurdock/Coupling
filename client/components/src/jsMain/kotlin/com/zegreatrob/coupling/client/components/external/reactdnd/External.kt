@file:JsModule("react-dnd")

package com.zegreatrob.coupling.client.components.external.reactdnd

import com.zegreatrob.coupling.client.components.external.reactdndhtml5backend.DnDBackend
import react.ElementType
import react.PropsWithChildren
import kotlin.js.Json

external fun useDrag(options: Json): dynamic
external fun useDrop(options: Json): dynamic

@JsName("DndProvider")
external val dndProvider: ElementType<DnDProvideProps>

external interface DnDProvideProps : PropsWithChildren {
    var backend: DnDBackend
}

sealed external interface DragOptions {
    fun collect(monitor: DragSourceMonitor)
}

external interface DragSourceMonitor {
    fun isDragging(): Boolean
    fun isOver(): Boolean
    fun getDropResult(): Json?
}
