package com.zegreatrob.coupling.client.components

import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise

fun <T> stubDispatchFunc(cannon: ActionCannon<T>) =
    DispatchFunc { block ->
        fun() {
            MainScope().promise { block(cannon) }
        }
    }
