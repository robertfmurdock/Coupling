package com.zegreatrob.coupling.client.external.react

import org.w3c.dom.Node
import react.*
import kotlin.lazy
import kotlin.reflect.KClass

abstract class RComponent<P : RProps>(private val provider: PropsClassProvider<P>) : ComponentBuilder<P>,
    PropsClassProvider<P> by provider {

    operator fun invoke(
        props: P,
        key: String? = null,
        ref: RReadableRef<Node>? = null,
        handler: RHandler<P> = {}
    ) = buildElement { render.invoke(this)(props, key, ref, handler) }!!

    val component: ReactFunctionComponent<P> by lazy { build() }
    val render by lazy {
        { rBuilder: RBuilder -> RenderToBuilder(this, rBuilder) }
    }
}

operator fun RComponent<EmptyProps>.invoke(
    key: String? = null,
    ref: RReadableRef<Node>? = null,
    handler: RHandler<EmptyProps> = {}
) = invoke(EmptyProps, key, ref, handler)

interface PropsClassProvider<T : RProps> {
    val kClass: KClass<T>
}

inline fun <reified P : RProps, C : Function<P>> provider() = object : PropsClassProvider<P> {
    override val kClass: KClass<P> get() = P::class
}
