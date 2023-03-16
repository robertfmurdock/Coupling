package com.zegreatrob.coupling.components

@JsModule("react")
private external val React: dynamic

fun <T> useStateWithSetterFunction(default: T): StateValueContentWithSetterFunction<T> {
    val stateArray = React.useState(default)
    return StateValueContentWithSetterFunction(
        value = stateArray[0].unsafeCast<T>(),
        setter = stateArray[1].unsafeCast<((T) -> T) -> Unit>(),
    )
}

data class StateValueContentWithSetterFunction<T>(val value: T, val setter: ((T) -> T) -> Unit)
