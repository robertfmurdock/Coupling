const events = ['network.beforeRequestSent', 'network.responseCompleted', 'network.fetchError'];
const empty = () => ({ responses: 0, cachedResponses: 0, reportedBytesReceived: 0, unknownByteResponses: 0, httpErrors: 0, failedRequests: 0, abortedRequests: 0 });

function category(url) {
    try {
        const host = new URL(url).hostname;
        if (host === 'localhost') return 'app';
        if (host === 'static.localhost') return 'static';
        if (host === 'esm.sh') return 'modules';
        if (['fonts.googleapis.com', 'fonts.gstatic.com'].includes(host)) return 'fonts';
        if (['kit.fontawesome.com', 'ka-f.fontawesome.com'].includes(host)) return 'icons';
        if (['www.googletagmanager.com', 'www.google-analytics.com', 'region1.google-analytics.com'].includes(host)) return 'analytics';
        if (host === 'zegreatrob.us.auth0.com') return 'auth';
    } catch { /* Invalid/unknown destinations are never retained. */ }
    return 'other';
}

export function createNetworkTotals() {
    let pending = new Map(), groups = new Map(), droppedRequests = 0;
    return {
        request(event) {
            const id = event.request?.request;
            if (!id) return;
            if (pending.size >= 10000) { droppedRequests++; return; }
            pending.set(id, category(event.request.url));
        },
        response(event) {
            const id = event.request?.request;
            const group = pending.get(id);
            if (!group) return;
            pending.delete(id);
            const totals = groups.get(group) ?? empty();
            const response = event.response ?? {};
            totals.responses++;
            if (response.fromCache === true) totals.cachedResponses++;
            if (Number.isFinite(response.bytesReceived) && response.bytesReceived >= 0) totals.reportedBytesReceived += response.bytesReceived;
            else totals.unknownByteResponses++;
            if (response.status >= 400) totals.httpErrors++;
            groups.set(group, totals);
        },
        failure(event) {
            const id = event.request?.request;
            const group = pending.get(id);
            if (!group) return;
            pending.delete(id);
            const totals = groups.get(group) ?? empty();
            totals.failedRequests++;
            if (String(event.errorText ?? '').includes('ERR_ABORTED')) totals.abortedRequests++;
            groups.set(group, totals);
        },
        reset() { pending = new Map(); groups = new Map(); droppedRequests = 0; },
        snapshot() {
            const total = empty();
            for (const group of groups.values()) for (const key of Object.keys(total)) total[key] += group[key];
            return {
                ...total, pendingRequests: pending.size, droppedRequests,
                groups: Object.fromEntries([...groups].map(([key, value]) => [key, { ...value }])),
            };
        },
    };
}

export async function attachNetworkTotals(browser) {
    if (!browser.isBidi) throw new Error('Performance network measurement requires WebDriver BiDi');
    const totals = createNetworkTotals();
    const handlers = [totals.request, totals.response, totals.failure];
    const detach = () => events.forEach((event, index) => browser.off(event, handlers[index]));
    events.forEach((event, index) => browser.on(event, handlers[index]));
    try {
        await browser.sessionSubscribe({ events });
    } catch (error) {
        detach();
        throw error;
    }
    return { ...totals, detach };
}
