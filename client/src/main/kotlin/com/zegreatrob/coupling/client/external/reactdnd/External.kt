@file:JsModule("react-dnd")
@file:Suppress("unused")

package com.zegreatrob.coupling.client.external.reactdnd

import com.zegreatrob.coupling.client.external.reactdndhtml5backend.DnDBackend
import react.ElementType
import react.Props
import kotlin.js.Json

@JsName("useDrag")
external fun useDragFunc(options: Json): dynamic

@JsName("useDrop")
external fun useDropFunc(options: Json): dynamic

@JsName("DndProvider")
external val DndProvider: ElementType<DnDProvideProps>

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
