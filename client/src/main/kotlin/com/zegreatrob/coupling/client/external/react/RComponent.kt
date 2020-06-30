package com.zegreatrob.coupling.client.external.react

import org.w3c.dom.Node
import react.*
import kotlin.lazy
import kotlin.reflect.KClass

abstract class RComponent<P : RProps>(private val provider: PropsClassProvider<P>) : ComponentBuilder<P>,
    PropsClassProvider<P> by provider {
    val component: ReactFunctionComponent<P> by lazy { build() }
}

class BuilderAdapter<P : RProps>(private val builder: RBuilder, private val component: RClass<P>) {
    operator fun invoke(props: P, key: String? = null, ref: RReadableRef<Node>? = null, handler: RHandler<P> = {}) =
        builder.child(component, props, key, ref, handler)
}

operator fun BuilderAdapter<EmptyProps>.invoke(
    key: String? = null,
    ref: RReadableRef<Node>? = null,
    handler: RHandler<EmptyProps> = {}
) = invoke(EmptyProps, key, ref, handler)

fun <P : RProps> RClass<P>.render(builder: RBuilder) = BuilderAdapter(builder, this)

interface PropsClassProvider<T : RProps> {
    val kClass: KClass<T>
}

inline fun <reified P : RProps> provider() = object : PropsClassProvider<P> {
    override val kClass: KClass<P> get() = P::class
}
