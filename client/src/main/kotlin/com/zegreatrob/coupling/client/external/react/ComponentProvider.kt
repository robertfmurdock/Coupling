package com.zegreatrob.coupling.client.external.react

import react.RBuilder
import react.RProps
import kotlin.reflect.KClass

abstract class ComponentProvider<P : RProps>(private val provider: PropsClassProvider<P>) : ComponentBuilder<P>,
    PropsClassProvider<P> by provider {

    val component: ReactFunctionComponent<P> by lazy { build() }
    val captor by lazy {
        { rBuilder: RBuilder -> BuilderCaptor(this, rBuilder) }
    }
}

interface PropsClassProvider<T : RProps> {
    val kClass: KClass<T>
}

inline fun <reified P : RProps> provider() = object : PropsClassProvider<P> {
    override val kClass: KClass<P> get() = P::class
}
