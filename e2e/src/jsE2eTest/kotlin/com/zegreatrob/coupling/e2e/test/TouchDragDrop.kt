package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.testlogging.withTestDiagnostics
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsName("browser")
private external object TouchBrowser {
    fun execute(script: (Boolean) -> TouchSwap, applyPolyfill: Boolean): Promise<TouchSwap>
    fun execute(script: () -> Boolean): Promise<Boolean>
    fun getWindowSize(): Promise<TouchViewport>
    fun setWindowSize(width: Int, height: Int): Promise<Unit>
    fun performActions(actions: Array<TouchPointer>): Promise<Unit>
    fun releaseActions(): Promise<Unit>
}

private external interface TouchViewport {
    val width: Int
    val height: Int
}

external interface TouchSwap {
    val sourceX: Int
    val sourceY: Int
    val targetX: Int
    val targetY: Int
    val pairs: Array<Array<String>>
}

@JsPlainObject
sealed external interface TouchPointer {
    val type: String
    val id: String
    val parameters: TouchParameters
    val actions: Array<TouchAction>
}

@JsPlainObject
sealed external interface TouchParameters {
    val pointerType: String
}

@JsPlainObject
sealed external interface TouchAction {
    val type: String
    val x: Int?
    val y: Int?
    val button: Int?
    val duration: Int?
}

private val touchSwapScript =
    js(
        """(function(applyPolyfill) {
        if (applyPolyfill) {
            window.MobileDragDrop.polyfill({forceApply: true});
            window.__touchEnded = false;
            document.addEventListener('dragend', () => { window.__touchEnded = true }, {once: true});

        }
        const pairs = Array.from(document.querySelectorAll('[data-assigned-pair]'));
        const source = pairs[0].querySelector('[data-player-id]');
        const target = pairs[1].querySelector('[data-player-id]');
        const first = source.getBoundingClientRect(), second = target.getBoundingClientRect();
        return {sourceX: Math.round(first.x + first.width / 2), sourceY: Math.round(first.y + first.height / 2),
            targetX: Math.round(second.x + second.width / 2), targetY: Math.round(second.y + second.height / 2),
            pairs: pairs.map(pair => Array.from(pair.querySelectorAll('[data-player-id]')).map(card => card.getAttribute('data-player-id')))};
    })""",
    ).unsafeCast<(Boolean) -> TouchSwap>()

suspend fun prepareTouchSwap(): TouchSwap = TouchBrowser.execute(touchSwapScript, true).await()

suspend fun performTouchSwap() {
    val viewport = TouchBrowser.getWindowSize().await()
    withTestDiagnostics({ TouchBrowser.setWindowSize(viewport.width, viewport.height).await() }) {
        TouchBrowser.setWindowSize(viewport.width, maxOf(1200, viewport.height)).await()
        val visible = TouchBrowser.execute(touchSwapScript, false).await()
        withTestDiagnostics({ TouchBrowser.releaseActions().await() }) {
            TouchBrowser.performActions(touchPointer(visible)).await()
            WebdriverBrowser.waitUntil({
                TouchBrowser.execute(js("(function() { return window.__touchEnded === true })").unsafeCast<() -> Boolean>()).await()
            }, 2000, "touch drag completed")
        }
    }
}

private fun touchPointer(swap: TouchSwap) = arrayOf(
    TouchPointer(
        type = "pointer",
        id = "touch-swap",
        parameters = TouchParameters(pointerType = "touch"),
        actions = arrayOf(
            TouchAction(type = "pointerMove", x = swap.sourceX, y = swap.sourceY, duration = 0),
            TouchAction(type = "pointerDown", button = 0),
            TouchAction(type = "pointerMove", x = swap.sourceX + 40, y = swap.sourceY, duration = 200),
            TouchAction(type = "pause", duration = 200),
            TouchAction(type = "pointerMove", x = swap.targetX, y = swap.targetY, duration = 300),
            TouchAction(type = "pause", duration = 200),
            TouchAction(type = "pointerUp", button = 0),
        ),
    ),
)
