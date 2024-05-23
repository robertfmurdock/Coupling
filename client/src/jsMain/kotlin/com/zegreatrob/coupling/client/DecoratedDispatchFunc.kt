@file:OptIn(ExperimentalCoroutinesApi::class)

package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.react.dataloader.DataLoaderTools
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.coroutines.ExperimentalCoroutinesApi

class DecoratedDispatchFunc<D>(
    val cannonFunc: () -> ActionCannon<D>,
    private val tools: DataLoaderTools,
) : DispatchFunc<D> {
    override fun invoke(block: suspend ActionCannon<D>.() -> Unit): () -> Unit = fun() {
        val cannon = cannonFunc()
        tools.performAsyncWork(
            work = { block(cannon) },
            errorResult = { handler: Throwable -> throw handler },
            onWorkComplete = {},
        )
    }
}
