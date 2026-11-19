package com.zegreatrob.coupling.e2e.performance

import com.zegreatrob.coupling.e2e.diagnostics.Path
import com.zegreatrob.coupling.e2e.diagnostics.importModule
import com.zegreatrob.coupling.e2e.diagnostics.nativeJson
import com.zegreatrob.coupling.e2e.diagnostics.nodeProcess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import kotlin.test.Test

class PageMetricsTest {
    @Test
    fun clsUsesLargestSessionWindowAndExcludesRecentInput() = asyncSetup(object {
        lateinit var module: MetricsModule
    }) {
        module = importModule<MetricsModule>(Path.join(nodeProcess.env.repositoryRoot, "e2e/performance/page-metrics.mjs")).await()
    } exercise {
        module.summarize(nativeJson("""{"shifts":[{"startTime":0,"value":0.1},{"startTime":500,"value":0.2},{"startTime":600,"value":9,"hadRecentInput":true},{"startTime":2000,"value":0.4},{"startTime":3000,"value":0.1}],"supported":["layout-shift"]}"""))
    } verify { result ->
        result.cls.assertIsEqualTo(0.4)
    }

    @Test
    fun zeroResourceSizesAreReportedSeparatelyFromKnownTransfer() = asyncSetup(object {
        lateinit var module: MetricsModule
    }) {
        module = importModule<MetricsModule>(Path.join(nodeProcess.env.repositoryRoot, "e2e/performance/page-metrics.mjs")).await()
    } exercise {
        module.summarize(nativeJson("""{"resources":[{"transferSize":120,"encodedBodySize":100,"decodedBodySize":200},{"transferSize":0,"encodedBodySize":0,"decodedBodySize":0},{"transferSize":0,"encodedBodySize":100,"decodedBodySize":200}]}"""))
    } verify { result ->
        result.resourceCount.assertIsEqualTo(3)
        result.reportedTransferBytes.assertIsEqualTo(120)
        result.zeroSizeResourceCount.assertIsEqualTo(1)
    }

    @Test
    fun unsupportedObserversRemainUnknownWhileSupportedEmptyClsIsZero() = asyncSetup(object {
        lateinit var module: MetricsModule
    }) {
        module = importModule<MetricsModule>(Path.join(nodeProcess.env.repositoryRoot, "e2e/performance/page-metrics.mjs")).await()
    } exercise {
        module.summarize(nativeJson("""{"supported":["layout-shift"]}"""))
    } verify { result ->
        result.cls.assertIsEqualTo(0.0)
        result.lcpMs.assertIsEqualTo(null)
        result.longTaskDurationMs.assertIsEqualTo(null)
    }
}

private external interface MetricsModule {
    fun summarize(snapshot: dynamic): MetricsSummary
}
private external interface MetricsSummary {
    val cls: Double?
    val lcpMs: Double?
    val longTaskDurationMs: Double?
    val resourceCount: Int
    val reportedTransferBytes: Int
    val zeroSizeResourceCount: Int
}
