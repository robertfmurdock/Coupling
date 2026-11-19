// Standalone: WebdriverIO serializes this function into each new document.
export function installPageObservers() {
    const supported = PerformanceObserver.supportedEntryTypes ?? [];
    const state = { supported, shifts: [], longTasks: [], lcpMs: null, truncated: false };
    performance.setResourceTimingBufferSize(2000);
    const observers = [];
    const retain = (list, value) => {
        if (list.length < 2000) list.push(value);
        else state.truncated = true;
    };
    const observe = (type, accept) => {
        if (!supported.includes(type)) return;
        const consume = entries => entries.forEach(accept);
        const observer = new PerformanceObserver(list => consume(list.getEntries()));
        observer.observe({ type, buffered: true });
        observers.push(() => consume(observer.takeRecords()));
    };
    observe('largest-contentful-paint', entry => { state.lcpMs = entry.startTime; });
    observe('layout-shift', entry => retain(state.shifts, {
        startTime: entry.startTime, value: entry.value, hadRecentInput: entry.hadRecentInput,
    }));
    observe('longtask', entry => retain(state.longTasks, entry.duration));
    performance.addEventListener('resourcetimingbufferfull', () => { state.truncated = true; });
    window.__couplingPerformanceSnapshot = () => {
        observers.forEach(flush => flush());
        return {
            ...state,
            observedUntilMs: performance.now(),
            resources: performance.getEntriesByType('resource').map(entry => ({
                transferSize: entry.transferSize,
                encodedBodySize: entry.encodedBodySize,
                decodedBodySize: entry.decodedBodySize,
            })),
            navigation: performance.getEntriesByType('navigation').map(entry => ({
                ttfbMs: entry.responseStart,
                domContentLoadedMs: entry.domContentLoadedEventEnd,
                loadMs: entry.loadEventEnd,
            }))[0] ?? null,
            viewport: { width: innerWidth, height: innerHeight, devicePixelRatio },
            visibilityState: document.visibilityState,
        };
    };
}

export function summarize(snapshot) {
    const { supported = [], resources = [], shifts = [], longTasks = [] } = snapshot;
    let cls = 0, windowScore = 0, windowStart = -Infinity, previous = -Infinity;
    for (const shift of shifts) {
        if (shift.hadRecentInput) continue;
        if (shift.startTime - previous < 1000 && shift.startTime - windowStart < 5000) {
            windowScore += shift.value;
        } else {
            windowStart = shift.startTime;
            windowScore = shift.value;
        }
        previous = shift.startTime;
        cls = Math.max(cls, windowScore);
    }
    return {
        lcpMs: supported.includes('largest-contentful-paint') ? snapshot.lcpMs ?? null : null,
        cls: supported.includes('layout-shift') ? cls : null,
        longTaskDurationMs: supported.includes('longtask') ? longTasks.reduce((sum, duration) => sum + duration, 0) : null,
        longTaskCount: supported.includes('longtask') ? longTasks.length : null,
        supportedObserverTypes: supported.filter(type => ['largest-contentful-paint', 'layout-shift', 'longtask'].includes(type)),
        visibilityState: snapshot.visibilityState ?? null,
        resourceCount: resources.length,
        reportedTransferBytes: resources.reduce((sum, entry) => sum + (entry.transferSize ?? 0), 0),
        zeroSizeResourceCount: resources.filter(entry => !entry.transferSize && !entry.encodedBodySize && !entry.decodedBodySize).length,
        observedUntilMs: snapshot.observedUntilMs ?? null,
        truncated: snapshot.truncated ?? false,
        navigation: snapshot.navigation ?? null,
        viewport: snapshot.viewport ?? null,
    };
}
