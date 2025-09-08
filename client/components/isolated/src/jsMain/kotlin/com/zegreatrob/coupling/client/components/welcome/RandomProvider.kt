package com.zegreatrob.coupling.client.components.welcome

import kotlin.random.Random

interface RandomProvider {

    fun nextRandomInt(until: Int) = Random.Default.nextInt(until)

    fun <T> List<T>.random() = nextRandomInt(this.size).let(::get)

    companion object : RandomProvider
}
