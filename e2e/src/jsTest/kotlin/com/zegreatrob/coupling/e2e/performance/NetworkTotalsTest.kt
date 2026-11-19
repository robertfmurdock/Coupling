package com.zegreatrob.coupling.e2e.performance

import com.zegreatrob.coupling.e2e.diagnostics.Path
import com.zegreatrob.coupling.e2e.diagnostics.importModule
import com.zegreatrob.coupling.e2e.diagnostics.nativeJson
import com.zegreatrob.coupling.e2e.diagnostics.nodeProcess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import kotlin.test.Test

class NetworkTotalsTest {
    @Test
    fun aggregatesModuleResponsesAndAbortsWithoutRetainingSecrets() = asyncSetup(object {
        lateinit var totals: NetworkTotals
    }) {
        totals = importModule<NetworkModule>(Path.join(nodeProcess.env.repositoryRoot, "e2e/performance/network-totals.mjs"))
            .await().createNetworkTotals()
        totals.request(nativeJson("""{"request":{"request":"one","url":"https://esm.sh/package?token=secret","headers":[{"value":"secret"}]}}"""))
        totals.request(nativeJson("""{"request":{"request":"aborted","url":"https://esm.sh/other?token=secret"}}"""))
    } exercise {
        totals.response(nativeJson("""{"request":{"request":"one"},"response":{"bytesReceived":120,"status":200,"fromCache":false,"headers":[{"value":"secret"}],"body":"secret"}}"""))
        totals.failure(nativeJson("""{"request":{"request":"aborted"},"errorText":"net::ERR_ABORTED secret"}"""))
        totals.snapshot()
    } verify { result ->
        result.responses.assertIsEqualTo(1)
        result.failedRequests.assertIsEqualTo(1)
        result.abortedRequests.assertIsEqualTo(1)
        result.reportedBytesReceived.assertIsEqualTo(120)
        result.groups.modules.responses.assertIsEqualTo(1)
        JSON.stringify(result).contains("secret").assertIsEqualTo(false)
        JSON.stringify(result).contains("esm.sh").assertIsEqualTo(false)
    }

    @Test
    fun resetExcludesResponsesStartedInPreviousSample() = asyncSetup(object {
        lateinit var totals: NetworkTotals
    }) {
        totals = importModule<NetworkModule>(Path.join(nodeProcess.env.repositoryRoot, "e2e/performance/network-totals.mjs"))
            .await().createNetworkTotals()
        totals.request(nativeJson("""{"request":{"request":"old","url":"https://localhost/"}}"""))
    } exercise {
        totals.reset()
        totals.response(nativeJson("""{"request":{"request":"old"},"response":{"bytesReceived":100,"status":200}}"""))
        totals.snapshot()
    } verify { result ->
        result.responses.assertIsEqualTo(0)
        result.reportedBytesReceived.assertIsEqualTo(0)
    }

    @Test
    fun cachedZeroBytesRemainDistinctFromUnavailableByteCounts() = asyncSetup(object {
        lateinit var totals: NetworkTotals
    }) {
        totals = importModule<NetworkModule>(Path.join(nodeProcess.env.repositoryRoot, "e2e/performance/network-totals.mjs"))
            .await().createNetworkTotals()
        listOf("cached", "unknown").forEach { id ->
            totals.request(nativeJson("""{"request":{"request":"$id","url":"https://static.localhost/script.js"}}"""))
        }
    } exercise {
        totals.response(nativeJson("""{"request":{"request":"cached"},"response":{"bytesReceived":0,"fromCache":true,"status":200}}"""))
        totals.response(nativeJson("""{"request":{"request":"unknown"},"response":{"bytesReceived":null,"fromCache":false,"status":200}}"""))
        totals.snapshot()
    } verify { result ->
        result.responses.assertIsEqualTo(2)
        result.cachedResponses.assertIsEqualTo(1)
        result.unknownByteResponses.assertIsEqualTo(1)
    }
}

private external interface NetworkModule {
    fun createNetworkTotals(): NetworkTotals
}
private external interface NetworkTotals {
    fun request(event: dynamic)
    fun response(event: dynamic)
    fun failure(event: dynamic)
    fun reset()
    fun snapshot(): NetworkSummary
}
private external interface NetworkSummary {
    val responses: Int
    val reportedBytesReceived: Int
    val cachedResponses: Int
    val failedRequests: Int
    val abortedRequests: Int
    val unknownByteResponses: Int
    val groups: NetworkGroups
}
private external interface NetworkGroups {
    val modules: NetworkSummary
}
