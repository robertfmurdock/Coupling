package com.zegreatrob.coupling.client.components

import com.zegreatrob.minassert.assertIsNotEqualTo

fun <T> T?.assertNotNull(callback: (T) -> Unit = {}) {
    this.assertIsNotEqualTo(null)
    callback(this!!)
}
